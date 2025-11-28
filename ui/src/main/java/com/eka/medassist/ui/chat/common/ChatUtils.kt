package com.eka.medassist.ui.chat.common

import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.Color
import com.eka.medassist.ui.chat.data.local.models.ChatContext
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

internal object ChatUtils {
    fun getTimeStampString(utcTimestamp: Long): String {
        val now = Calendar.getInstance()
        val today = now.clone() as Calendar
        val yesterday = now.clone() as Calendar

        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        yesterday.timeInMillis = today.timeInMillis
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        val timestampDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        timestampDate.timeInMillis = utcTimestamp
        timestampDate.timeZone = TimeZone.getDefault()

        return when {
            timestampDate.after(today) -> {
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                timeFormat.format(timestampDate.time).uppercase(Locale.getDefault())
            }

            timestampDate.after(yesterday) -> {
                "Yesterday"
            }

            else -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.format(timestampDate.time)
            }
        }
    }

    fun generateRandomColor(name: String): Pair<Color, Color> {
        var lighterColor: Color
        var darkerColor: Color

        val hash = name.hashCode()

        val redVal = abs(hash and 0xFF0000 shr 16) % 200
        val greenVal = abs(hash and 0x00FF00 shr 8) % 200
        val blueVal = abs(hash and 0x0000FF) % 200

        val darkerShade = 10
        val redDark = (redVal + darkerShade).coerceAtMost(255)
        val greenDark = (greenVal + darkerShade).coerceAtMost(255)
        val blueDark = (blueVal + darkerShade).coerceAtMost(255)

        darkerColor = Color(red = redDark, green = greenDark, blue = blueDark, alpha = 200)
        lighterColor = darkerColor.copy(alpha = 0.2f)

        return Pair(lighterColor, darkerColor)
    }

    fun getChatContextFromString(chatContextString: String?): ChatContext? {
        if (chatContextString.isNullOrEmpty()) {
            return null
        }
        return Gson().fromJson(chatContextString, ChatContext::class.java)
    }

    fun getAudioFileDuration(filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            return 0L
        } finally {
            retriever.release()
        }
    }

    fun formatDuration(durationMs: Long): String {
        val minutes = (durationMs / 1000) / 60
        val seconds = (durationMs / 1000) % 60
        return String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds)
    }

//    fun getMonthStickyHeaderLabel(utc: Long?): String {
//        if (utc == null) {
//            return ""
//        }
//        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
//        return sdf.format(Date(utc))
//    }
}