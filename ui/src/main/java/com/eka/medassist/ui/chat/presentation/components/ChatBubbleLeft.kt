package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.common.models.CTA
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.presentation.models.ChatMessage
import com.eka.medassist.ui.chat.presentation.states.ActionType
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ChatBubbleLeft(
    message: ChatMessage,
    value: String,
    onClick: (CTA) -> Unit,
    showResponseButtons: Boolean = true,
    isFirstMessage: Boolean = false
) {
    val iconAlpha = if (isFirstMessage) 1f else 0f
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
                    Column {
                        MarkdownText(
                            modifier = Modifier
                                .padding(start = 0.dp, top = 0.dp, end = 16.dp, bottom = 16.dp),
                            markdown = value,
                            truncateOnTextOverflow = true,
                            enableSoftBreakAddsNewLine = true,
                            style = touchBodyRegular,
                            color = DarwinTouchNeutral1000
                        )
                    }
                },
                background = Color.Transparent
            )
        }
        if (message.message.msgType == com.eka.conversation.data.local.db.entities.models.MessageType.TEXT && showResponseButtons) {
            Row(
                modifier = Modifier
                    .padding(start = 24.dp, top = 4.dp)
            ) {
                IconButtonWrapper(
                    onClick = {
                        onClick(CTA(action = ActionType.ON_POSITIVE_REVIEW_CLICKED.stringValue))
                    },
                    icon = R.drawable.ic_thumbs_up_regular,
                    contentDescription = "Thumbs up",
                    iconSize = 20.dp
                )
                IconButtonWrapper(
                    onClick = {
                        onClick(CTA(action = ActionType.ON_NEGETIVE_REVIEW_CLICKED.stringValue))
                    },
                    icon = R.drawable.ic_thumbs_down_regular,
                    contentDescription = "Thumbs down",
                    iconSize = 20.dp
                )
                IconButtonWrapper(
                    onClick = {
                        onClick(CTA(action = ActionType.ON_SHARE_CLICKED.stringValue))
                    },
                    icon = R.drawable.ic_share_nodes_regular,
                    contentDescription = "Share",
                    iconSize = 20.dp
                )
                IconButtonWrapper(
                    onClick = {
                        onClick(CTA(action = ActionType.ON_COPY_CLICKED.stringValue))
                    },
                    icon = R.drawable.ic_copy_regular,
                    contentDescription = "Copy",
                    iconSize = 20.dp
                )
            }
        }
    }
}