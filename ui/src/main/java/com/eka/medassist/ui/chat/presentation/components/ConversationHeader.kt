package com.eka.medassist.ui.chat.presentation.components

import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.presentation.common.organism.AppBar
import com.eka.medassist.ui.chat.theme.Blue50
import com.eka.medassist.ui.chat.theme.FuchsiaViolet100

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConversationHeader(
    title: String,
    subTitle: String? = null,
    /** Green dot on the mark when connected, grey otherwise. */
    online: Boolean = true,
    newChatEnabled : Boolean = true,
    onBackClick : () -> Unit,
    onNewClick : () -> Unit = {},
    /** Shows the chat-history action when set. */
    onHistoryClick: (() -> Unit)? = null,
) {
    AppBar(
        borderColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.4f to Blue50,
                    1f to FuchsiaViolet100
                )
            )
        ).drawBehind {
            // The header's own bottom fades into the chat background, so there is no edge
            val fade = 28.dp.toPx()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, DarwinTouchNeutral50),
                    startY = size.height - fade,
                    endY = size.height,
                ),
                topLeft = Offset(0f, size.height - fade),
                size = Size(size.width, fade),
            )
        },
        containerColor = Color.Transparent,
        title = title.replaceFirstChar { it.uppercaseChar() },
        subTitle = subTitle,
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButtonWrapper(
                    onClick = onBackClick,
                    icon = R.drawable.ic_arrow_left_regular,
                    iconSize = 16.dp
                )
                Image(
                    painter = painterResource(if (online) R.drawable.ic_ask_ai_mark_online else R.drawable.ic_ask_ai_mark_offline),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        actions = {
            if (onHistoryClick != null) {
                IconButtonWrapper(
                    icon = R.drawable.ic_chat_history,
                    iconSize = 20.dp,
                    onClick = onHistoryClick,
                    contentDescription = "Chat history"
                )
            }
            if(newChatEnabled) {
                IconButtonWrapper(
                    icon = R.drawable.ic_chat_new,
                    iconSize = 20.dp,
                    onClick = onNewClick,
                    contentDescription = "New Chat"
                )
            }
        }
    )
}
