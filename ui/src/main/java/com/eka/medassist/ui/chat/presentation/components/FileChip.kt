package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral100
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchCaption1Regular

/** Attached image / PDF; shows a remove button while it's still in the composer. */
@Composable
fun FileChip(file: MatrixFile, onRemove: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarwinTouchNeutral100)
            .padding(start = 8.dp, end = if (onRemove != null) 0.dp else 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(if (file.isImage) R.drawable.ic_image_regular else R.drawable.ic_file_pdf_solid),
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
