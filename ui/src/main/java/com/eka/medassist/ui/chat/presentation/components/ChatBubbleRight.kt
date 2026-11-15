package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.models.ChatUiMessage
import com.eka.medassist.ui.chat.presentation.models.Delivery
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchRed
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchCaption1Regular
import dev.jeziellago.compose.markdowntext.MarkdownText

/**
 * Doctor's message: white card on the right with its photos / files, and under it the delivery
 * state ("Waiting to send", "Not sent · Tap to retry"). Long-press for Copy / Edit / Resend.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatBubbleRight(
    message: ChatUiMessage,
    onClick: () -> Unit = {},
    onEdit: ((ChatUiMessage) -> Unit)? = null,
    onResend: ((ChatUiMessage) -> Unit)? = null,
    onRetry: ((ChatUiMessage) -> Unit)? = null,
) {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var menu by remember { mutableStateOf(false) }
    val hasText = message.content.isNotBlank()

    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.End) {
        Box(modifier = Modifier.padding(start = 36.dp)) {
            BorderCard(
                modifier = Modifier
                    .alpha(if (message.delivery == Delivery.QUEUED) 0.7f else 1f)
                    .combinedClickable(
                        onClick = { if (message.delivery == Delivery.FAILED) onRetry?.invoke(message) else onClick() },
                        onLongClick = if (hasText || message.files.isNotEmpty()) {
                            {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                menu = true
                            }
                        } else null,
                    ),
                border = BorderStroke(width = 0.dp, color = Color.Transparent),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 0.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                content = {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (message.files.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = if (hasText) 8.dp else 0.dp)
                            ) {
                                message.files.forEach { FileChip(it) }
                            }
                        }
                        if (message.isAudio && !hasText) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_microphone_regular),
                                    contentDescription = null,
                                    tint = DarwinTouchPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Voice message · Transcribing...", style = touchBodyRegular, color = DarwinTouchNeutral800)
                            }
                        }
                        if (hasText) {
                            // A long prompt starts collapsed, with Show more / Show less
                            val long = remember(message.content) { isLongPrompt(message.content) }
                            var expanded by rememberSaveable(message.id) { mutableStateOf(false) }
                            MarkdownText(
                                markdown = if (long && !expanded) promptPreview(message.content) else message.content,
                                style = touchBodyRegular,
                                color = DarwinTouchNeutral800
                            )
                            if (long) {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable { expanded = !expanded },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = if (expanded) "Show less" else "Show more",
                                        style = touchBodyRegular,
                                        color = DarwinTouchPrimary,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_chevron_right_regular),
                                        contentDescription = null,
                                        tint = DarwinTouchPrimary,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .rotate(if (expanded) -90f else 90f),
                                    )
                                }
                            }
                        }
                    }
                },
                background = Color.White
            )
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                if (hasText) {
                    DropdownMenuItem(text = { Text("Copy") }, onClick = {
                        menu = false
                        clipboard.setText(AnnotatedString(message.content))
                    })
                }
                if (hasText && onEdit != null) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = {
                        menu = false
                        onEdit(message)
                    })
                }
                if (onResend != null && !message.isAudio) {
                    DropdownMenuItem(text = { Text("Resend") }, onClick = {
                        menu = false
                        onResend(message)
                    })
                }
            }
        }
        when (message.delivery) {
            Delivery.QUEUED -> Text(
                "Waiting for internet to send",
                style = touchCaption1Regular,
                color = DarwinTouchNeutral600,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
            )
            Delivery.FAILED -> Text(
                "Not sent · Tap to retry",
                style = touchCaption1Regular,
                color = DarwinTouchRed,
                modifier = Modifier
                    .padding(top = 4.dp, end = 4.dp)
                    .clickable { onRetry?.invoke(message) },
            )
            Delivery.SENT -> Unit
        }
    }
}

private const val PROMPT_PREVIEW_LINES = 6
private const val PROMPT_PREVIEW_CHARS = 320

/** More than 6 lines or ~320 characters starts collapsed. */
internal fun isLongPrompt(text: String): Boolean =
    text.length > PROMPT_PREVIEW_CHARS || text.lines().size > PROMPT_PREVIEW_LINES

/** First lines of a long prompt, cut at a word, with an ellipsis. */
internal fun promptPreview(text: String): String {
    val byLines = text.lines().take(PROMPT_PREVIEW_LINES).joinToString("\n")
    if (byLines.length <= PROMPT_PREVIEW_CHARS) return byLines.trimEnd() + "…"
    val cut = byLines.take(PROMPT_PREVIEW_CHARS)
    val space = cut.lastIndexOf(' ')
    return (if (space > PROMPT_PREVIEW_CHARS / 2) cut.take(space) else cut).trimEnd() + "…"
}

