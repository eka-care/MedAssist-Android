package com.eka.medassist.ui.chat.presentation.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.common.models.CTA
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.presentation.models.Feedback
import com.eka.medassist.ui.chat.presentation.states.ActionType
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchCaption1Regular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import dev.jeziellago.compose.markdowntext.MarkdownText

/**
 * Bot reply: AI avatar + markdown.
 * [statusText] is a tool / progress line above the text, [userHint] a footnote below it.
 * Response buttons (like / dislike / share / copy) show on the latest reply only.
 */
@Composable
fun ChatBubbleLeft(
    message: String,
    onClick: (CTA) -> Unit = {},
    showResponseButtons: Boolean = true,
    isFirstMessage: Boolean = false,
    statusText: String? = null,
    userHint: String? = null,
    feedback: String = Feedback.NONE,
    onFeedback: ((String) -> Unit)? = null,
    /** The doctor cut this reply short. */
    stopped: Boolean = false,
) {
    val iconAlpha = if (isFirstMessage) 1f else 0f
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Column {
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable {
                    onClick(CTA(action = ActionType.ON_CHAT_MESSAGE_CLICKED.stringValue))
                }
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .alpha(iconAlpha)
                    .size(32.dp),
                painter = painterResource(id = R.drawable.ic_ai_chat_custom),
                tint = Color.Unspecified,
                contentDescription = ""
            )
            BorderCard(
                modifier = Modifier.weight(1f),
                border = BorderStroke(width = 0.dp, color = Color.Transparent),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                content = {
                    Column(modifier = Modifier.padding(end = 16.dp, bottom = if (showResponseButtons) 4.dp else 16.dp)) {
                        statusText?.let {
                            Text(
                                text = it,
                                style = touchCaption1Regular,
                                color = DarwinTouchNeutral600,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        if (message.isNotBlank()) {
                            MarkdownText(
                                markdown = message,
                                truncateOnTextOverflow = true,
                                enableSoftBreakAddsNewLine = true,
                                style = touchBodyRegular.copy(color = DarwinTouchNeutral1000)
                            )
                        }
                        if (stopped) {
                            Text(
                                text = "You stopped this reply",
                                style = touchCaption1Regular,
                                color = DarwinTouchNeutral600,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                background = Color.Transparent
            )
        }
        if (showResponseButtons && message.isNotBlank()) {
            Row(
                modifier = Modifier
                    .padding(start = 32.dp)
            ) {
                if (onFeedback != null && (feedback == Feedback.NONE || feedback == Feedback.LIKE)) {
                    IconButtonWrapper(
                        enabled = feedback == Feedback.NONE,
                        onClick = {
                            onClick(CTA(action = ActionType.ON_POSITIVE_REVIEW_CLICKED.stringValue))
                            onFeedback(Feedback.LIKE)
                        },
                        icon = R.drawable.ic_thumbs_up_regular,
                        contentDescription = "Thumbs up",
                        iconSize = 20.dp
                    )
                }
                if (onFeedback != null && (feedback == Feedback.NONE || feedback == Feedback.DISLIKE)) {
                    IconButtonWrapper(
                        enabled = feedback == Feedback.NONE,
                        onClick = {
                            onClick(CTA(action = ActionType.ON_NEGETIVE_REVIEW_CLICKED.stringValue))
                            onFeedback(Feedback.DISLIKE)
                        },
                        icon = R.drawable.ic_thumbs_down_regular,
                        contentDescription = "Thumbs down",
                        iconSize = 20.dp
                    )
                }
                IconButtonWrapper(
                    onClick = {
                        onClick(CTA(action = ActionType.ON_SHARE_CLICKED.stringValue))
                        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, message)
                        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    },
                    icon = R.drawable.ic_share_nodes_regular,
                    contentDescription = "Share",
                    iconSize = 20.dp
                )
                IconButtonWrapper(
                    onClick = {
                        onClick(CTA(action = ActionType.ON_COPY_CLICKED.stringValue))
                        clipboard.setText(AnnotatedString(message))
                    },
                    icon = R.drawable.ic_copy_regular,
                    contentDescription = "Copy",
                    iconSize = 20.dp
                )
            }
        }
        userHint?.let { hint ->
            Row(modifier = Modifier.padding(start = 40.dp, top = 6.dp, end = 16.dp)) {
                MarkdownText(markdown = hint, style = touchFootnoteRegular.copy(color = DarwinTouchNeutral800))
            }
        }
    }
}
