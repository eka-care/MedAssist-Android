package com.eka.medassist.ui.chat.presentation.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.eka.conversation.data.local.db.entities.MessageEntity
import com.eka.conversation.data.local.db.entities.models.MessageRole
import com.eka.medassist.ui.chat.common.models.CTA
import com.eka.medassist.ui.chat.presentation.models.ChatMessage
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.presentation.states.ActionType
import com.eka.medassist.ui.chat.presentation.states.SessionMessagesState
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import org.json.JSONObject

@Composable
fun EkaNewChatContent(
    viewModel: EkaChatViewModel,
    onSuggestionClick: (SuggestionModel) -> Unit,
    sessionId: String,
    onClick: (CTA) -> Unit
) {
    val sessionMessages by viewModel.sessionMessages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(sessionMessages) {
        if (sessionMessages.messageEntityResp.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    LaunchedEffect(Unit) {
//        if (sessionId.isNullOrEmpty()) {
//            viewModel.updateSessionId(session = viewModel.sessionId)
//            viewModel.getSessionMessages(sessionId = viewModel.sessionId)
//        } else {
//            viewModel.updateSessionId(session = sessionId)
//            viewModel.getSessionMessages(sessionId = sessionId)
//        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(color = DarwinTouchNeutral50)
    ) {
        if (sessionMessages.messageEntityResp.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, bottom = 16.dp, end = 8.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                itemsIndexed(
                    items = sessionMessages.messageEntityResp,
                    key = { _, chatMessage -> chatMessage.message.messageId }) { index, chatMessage ->

                    val message = chatMessage.message

//                    if (showLoader(
//                            sessionMessages.messageEntityResp,
//                            chatMessage
//                        ) && viewModel.isQueryResponseLoading
//                    ) {
//                        ChatBubbleProcessing()
//                    }

                    ChatMessageComponent(
                        chatMessage = chatMessage,
                        viewModel = viewModel,
                        sessionMessages = sessionMessages
                    )
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }
            }
        } else {
//            if (viewModel.getDefaultSuggestions(isPatientChat = false).isNotEmpty()) {
//                DefaultSuggestionsComponent(
//                    viewModel = viewModel,
//                    onSuggestionClicked = onSuggestionClick
//                )
//            } else {
//                EkaChatContent(
//                    modifier = Modifier,
//                    onClick = onClick
//                )
//            }
            // TODO Empty state
        }
    }
}

@Composable
fun ChatMessageComponent(
    chatMessage: ChatMessage,
    viewModel: EkaChatViewModel,
    sessionMessages: SessionMessagesState
) {
    val message = chatMessage.message
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    when (message.role) {
        MessageRole.USER -> {
//            ChatBubbleRight(
//                chatMessage = chatMessage,
//                onClick = {
//                }
//            )
        }

        MessageRole.AI -> {
            when (message.messageType) {
                else -> {
//                    ChatBubbleLeft(
//                        message = chatMessage,
//                        value = message.msgContent,
//                        showResponseButtons = shouldShowResponseButtons(
//                            viewModel = viewModel,
//                            messages = sessionMessages.messageEntityResp,
//                            message = chatMessage
//                        ),
//                        onClick = { cta ->
//                            handleMessageCTA(
//                                cta = cta,
//                                chatMessage = chatMessage,
//                                clipboardManager = clipboardManager,
//                                context = context,
//                                viewModel = viewModel
//                            )
//                        },
//                        isFirstMessage = shouldShowLeftIcon(
//                            sessionMessages.messageEntityResp,
//                            currentMessage = message
//                        )
//                    )
                }
            }

        }

        else -> {
        }
    }
}

//fun shouldShowResponseButtons(
//    viewModel: EkaChatViewModel,
//    messages: List<ChatMessage>,
//    message: ChatMessage
//): Boolean {
//    return viewModel.sendButtonEnabled || !isLastMessage(messages, message)
//}

//fun isLastMessage(messages: List<ChatMessage>, message: ChatMessage): Boolean {
//    return messages.first().message.messageId == message.message.messageId
//}

fun handleMessageCTA(
    context: Context,
    viewModel: EkaChatViewModel,
    chatMessage: ChatMessage,
    clipboardManager: ClipboardManager,
    cta: CTA
) {
    when (cta.action) {
        ActionType.ON_COPY_CLICKED.stringValue -> {
            val params = JSONObject()
            params.put("type", "copy")
            params.put("session_id", chatMessage.message.sessionId)
            params.put("text", chatMessage.message.messageContent)
            clipboardManager.setText(AnnotatedString(chatMessage.message.messageContent))
            viewModel.showToast("Copied to Clipboard.")
        }

        ActionType.ON_SHARE_CLICKED.stringValue -> {
            val params = JSONObject()
            params.put("type", "sharepdf")
            params.put("session_id", chatMessage.message.sessionId)
            params.put("text", chatMessage.message.messageContent)
            viewModel.generatePdf(
                data = chatMessage.message.messageContent,
                context = context
            )
        }

        ActionType.ON_POSITIVE_REVIEW_CLICKED.stringValue -> {
            val params = JSONObject()
            params.put("type", "good")
            params.put("session_id", chatMessage.message.sessionId)
            params.put("text", chatMessage.message.messageContent)
            viewModel.showToast("Review Submitted.")
        }

        ActionType.ON_NEGETIVE_REVIEW_CLICKED.stringValue -> {
            val params = JSONObject()
            params.put("type", "bad")
            params.put("session_id", chatMessage.message.sessionId)
            params.put("text", chatMessage.message.messageContent)
            viewModel.showToast("Review Submitted.")
        }
    }
}
fun shouldShowLeftIcon(messages: List<ChatMessage>, currentMessage: MessageEntity): Boolean {
    return true
}