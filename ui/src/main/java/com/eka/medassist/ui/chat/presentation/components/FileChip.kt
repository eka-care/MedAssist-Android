package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral100
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchCaption1Regular

/**
 * Attachment in the composer or a bubble: photos as a thumbnail (tap for full screen), PDFs as
 * a named chip. [onRemove] shows a ✕ while it's still in the composer.
 */
@Composable
fun FileChip(file: MatrixFile, onRemove: (() -> Unit)? = null) {
    var preview by remember { mutableStateOf(false) }
    if (preview) ImagePreviewDialog(file) { preview = false }

    if (file.isImage) {
        Box {
            AttachmentThumbnail(file = file, size = 56.dp) { preview = true }
            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_xmark_regular), contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(8.dp))
                }
            }
        }
        return
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarwinTouchNeutral100)
            .padding(start = 8.dp, end = if (onRemove != null) 0.dp else 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_file_pdf_solid),
            contentDescription = null,
            tint = DarwinTouchNeutral800,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(file.name.take(18), style = touchCaption1Regular, color = DarwinTouchNeutral800, maxLines = 1)
        if (onRemove != null) {
            IconButtonWrapper(
                modifier = Modifier.size(24.dp),
                onClick = onRemove,
                icon = R.drawable.ic_xmark_regular,
                iconSize = 10.dp,
                contentDescription = "Remove",
            )
        }
    }
}
