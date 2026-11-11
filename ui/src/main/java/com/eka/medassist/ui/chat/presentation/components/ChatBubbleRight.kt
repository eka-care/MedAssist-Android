package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.models.ChatUiMessage
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import dev.jeziellago.compose.markdowntext.MarkdownText

/** Doctor's message: white card on the right, with attached files and voice messages. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBubbleRight(
    message: ChatUiMessage,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .padding(top = 4.dp)
    ) {
        BorderCard(
            modifier = Modifier
                .weight(1f)
                .padding(start = 36.dp),
            border = BorderStroke(width = 0.dp, color = Color.Transparent),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 0.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            onClick = onClick,
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (message.files.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = if (message.content.isBlank()) 0.dp else 8.dp)
                        ) {
                            message.files.forEach { FileChip(it) }
                        }
                    }
                    if (message.isAudio && message.content.isBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_microphone_regular),
                                contentDescription = null,
                                tint = DarwinTouchPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Voice message", style = touchBodyRegular, color = DarwinTouchNeutral800)
                        }
                    }
                    if (message.content.isNotBlank()) {
                        MarkdownText(
                            markdown = message.content,
                            style = touchBodyRegular,
                            color = DarwinTouchNeutral800
                        )
                    }
                }
            },
            background = Color.White
        )
    }
}
