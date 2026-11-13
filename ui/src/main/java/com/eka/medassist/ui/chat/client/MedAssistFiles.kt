package com.eka.medassist.ui.chat.client

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.exifinterface.media.ExifInterface
import com.eka.conversation.matrix.media.MatrixFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

/**
 * Turns picked files (gallery, camera, documents, the host's records) into chat attachments.
 * Matrix takes JPEG/PNG/PDF under 3 MB, so photos are upright-rotated, scaled down and
 * re-encoded as JPEG when needed; HEIC/WebP become JPEG too. PDFs pass through as they are.
 */
object MedAssistFiles {

    /** Leaves headroom under Matrix's 3 MB cap. */
    private const val TARGET_IMAGE_BYTES = 2_700_000
    private const val MAX_IMAGE_SIDE = 2048

    /** A content:// or file:// Uri; null when it can't be read or decoded. */
    suspend fun fromUri(context: Context, uri: Uri): MatrixFile? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            var name = uri.lastPathSegment?.substringAfterLast('/') ?: "file"
            runCatching {
                resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                    if (c.moveToFirst()) c.getString(0)?.let { name = it }
                }
            }
            val mime = resolver.getType(uri)
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substringAfterLast('.', "").lowercase())
                ?: "application/octet-stream"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            when {
                !mime.startsWith("image/") -> MatrixFile(name, mime, bytes, uri)
                // Already fine for Matrix: keep the original quality
                mime in setOf("image/jpeg", "image/jpg", "image/png") && bytes.size <= TARGET_IMAGE_BYTES &&
                    exifRotation(bytes) == 0 -> MatrixFile(name, mime, bytes, uri)
                else -> toJpeg(bytes)?.let { MatrixFile(name.substringBeforeLast('.') + ".jpg", "image/jpeg", it, uri) }
            }
        } catch (_: Exception) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }
    }

    /** A file on disk (e.g. a downloaded record page). */
    suspend fun fromFile(context: Context, file: File): MatrixFile? =
        if (file.exists()) fromUri(context, Uri.fromFile(file)) else null

    private fun toJpeg(bytes: ByteArray): ByteArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_IMAGE_SIDE) sample *= 2
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null

        val longest = max(bitmap.width, bitmap.height)
        val rotation = exifRotation(bytes)
        if (longest > MAX_IMAGE_SIDE || rotation != 0) {
            val scale = if (longest > MAX_IMAGE_SIDE) MAX_IMAGE_SIDE.toFloat() / longest else 1f
            val m = Matrix().apply {
                postScale(scale, scale)
                if (rotation != 0) postRotate(rotation.toFloat())
            }
            val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            if (transformed != bitmap) bitmap.recycle()
            bitmap = transformed
        }
        try {
            var quality = 85
            while (true) {
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                val jpeg = out.toByteArray()
                if (jpeg.size <= TARGET_IMAGE_BYTES || quality <= 40) {
                    return jpeg.takeIf { it.size < MatrixFile.MAX_FILE_SIZE }
                }
                quality -= 15
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun exifRotation(bytes: ByteArray): Int = runCatching {
        when (ExifInterface(bytes.inputStream()).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }.getOrDefault(0)
}
