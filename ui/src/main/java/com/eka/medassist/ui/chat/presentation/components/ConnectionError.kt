package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchRed
import com.eka.medassist.ui.chat.theme.DarwinTouchRedBgLight
import com.eka.medassist.ui.chat.theme.touchBodyBold
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular

/**
 * Connection / send error shown above the input (the old "Connection Failed! Retry" snackbar),
 * with Retry and/or a primary action such as "Start new chat".
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
    Snackbar(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        containerColor = DarwinTouchRedBgLight,
        contentColor = DarwinTouchRed,
        actionContentColor = DarwinTouchPrimary,
        action = {
            Row {
                onDismiss?.let { TextButton(onClick = it) { Text("Dismiss", color = DarwinTouchNeutral600) } }
                if (showRetry) TextButton(onClick = onRetry) { Text("Retry", color = DarwinTouchPrimary) }
                actionLabel?.let { TextButton(onClick = onAction) { Text(it, color = DarwinTouchPrimary) } }
            }
        }
    ) {
        Column {
            Text(text = title, style = touchBodyBold, color = DarwinTouchRed, maxLines = 2, overflow = TextOverflow.Ellipsis)
            description?.let {
                Text(text = it, style = touchFootnoteRegular, color = DarwinTouchNeutral800, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
