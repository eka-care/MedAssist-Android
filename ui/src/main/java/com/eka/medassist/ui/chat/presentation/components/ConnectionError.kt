package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchRed
import com.eka.medassist.ui.chat.theme.DarwinTouchRedBgLight
import com.eka.medassist.ui.chat.theme.DarwinTouchRedLight
import com.eka.medassist.ui.chat.theme.touchBodyBold
import com.eka.medassist.ui.chat.theme.touchCalloutRegular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular

/**
 * Connection / send error above the input: title and description, with the actions on their own
 * row underneath (Dismiss, Retry and/or e.g. "Start new chat") so long text never overlaps them.
 */
@Composable
fun ErrorContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    showRetry: Boolean,
    onRetry: () -> Unit,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val hasActions = onDismiss != null || showRetry || actionLabel != null
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(DarwinTouchRedBgLight)
            .border(1.dp, DarwinTouchRedLight.copy(alpha = 0.5f), shape)
            .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = if (hasActions) 0.dp else 10.dp),
    ) {
        Text(
            text = title,
            style = touchBodyBold,
            color = DarwinTouchRed,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 8.dp),
        )
        description?.let {
            Text(
                text = it,
                style = touchFootnoteRegular,
                color = DarwinTouchNeutral800,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp, end = 8.dp),
            )
        }
        if (hasActions) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                onDismiss?.let { TextButton(onClick = it) { Text("Dismiss", style = touchCalloutRegular, color = DarwinTouchNeutral600) } }
                if (showRetry) TextButton(onClick = onRetry) { Text("Retry", style = touchCalloutRegular, color = DarwinTouchPrimary) }
                actionLabel?.let { TextButton(onClick = onAction) { Text(it, style = touchCalloutRegular, color = DarwinTouchPrimary) } }
            }
        }
    }
}
