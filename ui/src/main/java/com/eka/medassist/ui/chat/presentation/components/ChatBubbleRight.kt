package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.models.ChatMessage
import com.eka.conversation.ui.presentation.components.BorderCard
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ChatBubbleRight(
    chatMessage: ChatMessage,
    onClick: () -> Unit,
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
//                LazyRow(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(DarwinTouchNeutral0)
//                ) {
//                    items(chatMessage.files.size) { recordModelIdx ->
//                        val recordModel = chatMessage.files[recordModelIdx]
//                        AsyncImage(
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .size(80.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(color = DarwinTouchNeutral1000)
//                                .graphicsLayer(alpha = 0.4f),
//                            model = recordModel.thumbnail,
//                            contentDescription = "Thumbnail",
//                            contentScale = ContentScale.Crop,
//                        )
//                    }
//                }
                MarkdownText(
                    markdown = chatMessage.message.messageText.toString(),
                    modifier = Modifier.padding(16.dp),
                    style = touchBodyRegular,
                    color = DarwinTouchNeutral800
                )
            },
            background = Color.White
        )
    }
}
