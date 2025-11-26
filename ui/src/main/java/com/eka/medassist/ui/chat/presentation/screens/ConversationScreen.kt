package com.eka.medassist.ui.chat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eka.conversation.client.models.Message
import com.eka.conversation.common.models.UserInfo
import com.eka.conversation.data.local.db.entities.models.MessageRole
import com.eka.conversation.data.remote.socket.states.SocketConnectionState
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.components.ChatBubbleLeft
import com.eka.medassist.ui.chat.presentation.components.ChatBubbleProcessing
import com.eka.medassist.ui.chat.presentation.components.ChatBubbleRight
import com.eka.medassist.ui.chat.presentation.components.ConversationHeader
import com.eka.medassist.ui.chat.presentation.components.ConversationInput
import com.eka.medassist.ui.chat.presentation.components.ErrorContent
import com.eka.medassist.ui.chat.presentation.components.ShimmerBubble
import com.eka.medassist.ui.chat.presentation.components.SuggestionsComponent
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.presentation.states.TypewriterState
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
    userInfo: UserInfo,
    viewModel: EkaChatViewModel
) {
    val responseStream by viewModel.responseStream.collectAsState()
    val messages = viewModel.messages.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.createNewSession(userInfo = userInfo)
    }
    val connectionState by viewModel.connectionState.collectAsState()
    val scope = rememberCoroutineScope()
    val typewriterState = remember { TypewriterState(charDelayMs = 20L, scope = scope) }
    val streamingMessage by typewriterState.currentMessage
    val isThinking = viewModel.isQueryResponseLoading
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarwinTouchNeutral50)
    ) {
        ConversationHeader(
            title = stringResource(id = R.string.new_chat),
            subTitle = getConnectionState(state = connectionState),
            onClick = {

            }
        )

        when(connectionState) {
            is SocketConnectionState.Error -> {
                ErrorContent(
                    error = (connectionState as? SocketConnectionState.Error)?.error ?: Throwable("Something went wrong"),
                ) {
                    viewModel.createNewSession(userInfo = userInfo)
                }
            }
            is SocketConnectionState.Disconnected, SocketConnectionState.Disconnecting, SocketConnectionState.Connected -> {
                ConnectedContent(
                    messages =messages,
                    responseStream = responseStream,
                    typewriterState = typewriterState,
                    streamingMessage = streamingMessage,
                    isThinking = isThinking,
                    viewModel = viewModel
                )
            }
            else -> {
                ShimmerContent(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColumnScope.ConnectedContent(
    messages: List<Message>,
    responseStream: Message?,
    typewriterState: TypewriterState,
    streamingMessage: Message.Text?,
    isThinking: Boolean,
    viewModel: EkaChatViewModel
) {
    ConversationContent(
        modifier = Modifier
            .weight(1f)
            .padding(start = 16.dp, end = 8.dp),
        messages = messages.reversed(),
        responseStreamMessage = responseStream,
        typewriterState = typewriterState,
        streamingMessage = streamingMessage,
        isThinking = isThinking
    )

    ConversationInput(
        viewModel = viewModel,
        sendEnabled = viewModel.sendButtonEnabled && streamingMessage == null
    )
}

@Composable
private fun ShimmerContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(6) {
            ShimmerBubble(isLeft = it % 2 == 0)
        }
    }
}

@Composable
private fun ConversationContent(
    modifier: Modifier = Modifier,
    messages: List<Message> = emptyList(),
    responseStreamMessage : Message?,
    typewriterState: TypewriterState,
    streamingMessage : Message.Text?,
    isThinking : Boolean
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val displayedText by typewriterState.displayedMessage.collectAsState()

    LaunchedEffect(responseStreamMessage) {
        if(responseStreamMessage == null) {
            typewriterState.complete()
        } else if(responseStreamMessage is Message.Text) {
            typewriterState.updateFullMessage(fullMessage = responseStreamMessage)
        }
    }

    val displayedMessages = remember(messages, streamingMessage) {
        messages.filterNot { it.msgId == streamingMessage?.msgId }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
        reverseLayout = true
    ) {
        streamingMessage?.let { streamMessage ->
            item(key = "streaming_${streamMessage.msgId}") {
                StreamingMessage(
                    displayedText = displayedText
                )
            }
        }
        if(isThinking) {
            item("thinking") {
                ChatBubbleProcessing()
            }
        }
        items(displayedMessages, key = { item -> item.msgId }) { item ->
            when (item) {
                is Message.Text -> {
                    when(item.role) {
                        MessageRole.USER -> {
                            ChatBubbleRight(
                                message = item
                            ) {

                            }
                        }
                        MessageRole.AI -> {
                            ChatBubbleLeft(
                                message = item.text,
                                isFirstMessage = true,
                                onClick = {

                                }
                            )
                        }
                    }
                }

                is Message.SingleSelect -> {
                    SuggestionsComponent(
                        showLeftIcon = true,
                        suggestionList = item.choices.map { choice -> SuggestionModel(label = choice) },
                        onSuggestionClicked = {
                            // TODO Suggestion click
                        }
                    )
                }

                is Message.MultiSelect -> {
                    SuggestionsComponent(
                        showLeftIcon = true,
                        suggestionList = item.choices.map { choice -> SuggestionModel(label = choice) },
                        onSuggestionClicked = {
                            // TODO Suggestion click
                        }
                    )
                }
            }
        }
    }
}

fun getConnectionState(state: SocketConnectionState): String {
    when (state) {
        is SocketConnectionState.Idle -> {
            return "Idle"
        }

        is SocketConnectionState.Starting -> {
            return "Starting Session..."
        }

        is SocketConnectionState.Connecting -> {
            return "Connecting..."
        }

        is SocketConnectionState.SocketConnected -> {
            return "Authenticating..."
        }

        is SocketConnectionState.Connected -> {
            return "Connected"
        }

        is SocketConnectionState.Disconnecting -> {
            return "Disconnecting..."
        }

        is SocketConnectionState.Disconnected -> {
            return "Disconnected"
        }

        is SocketConnectionState.Error -> {
            return "Error : ${state.error.message.toString()}"
        }
    }
}

@Composable
fun StreamingMessage(
    displayedText : String
) {
    ChatBubbleLeft(
        message = displayedText,
        isFirstMessage = true,
        onClick = {

        }
    )
}