package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.conversation.client.models.Message
import com.eka.conversation.data.local.db.entities.models.MessageRole
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.presentation.screens.isLastMessage
import com.eka.medassist.ui.chat.presentation.states.SuggestionType
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchRed
import com.eka.medassist.ui.chat.theme.DarwinTouchRedBgLight
import kotlinx.coroutines.launch

@Composable
fun ErrorContent(
    modifier: Modifier = Modifier,
    error: Throwable,
    messages: List<Message>,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(error) {
        val result = snackbarHostState.showSnackbar(
            message = "Connection Failed!",
            actionLabel = "Retry",
            duration = SnackbarDuration.Indefinite
        )
        if (result == SnackbarResult.ActionPerformed) {
            onRetry()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize().weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
            reverseLayout = true
        ) {
            items(messages.reversed(), key = { item -> item.msgId }) { item ->
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
                                    showResponseButtons = isLastMessage(sendEnabled = false, messages = messages, message = item),
                                    onClick = {

                                    }
                                )
                            }
                        }
                    }

                    is Message.SingleSelect -> {
                        SuggestionsComponent(
                            showLeftIcon = true,
                            suggestionType = SuggestionType.SINGLE_SELECT,
                            suggestionList = item.choices.map { choice -> SuggestionModel(label = choice) },
                            enabled = false,
                            onSuggestionClicked = {

                            }
                        )
                        if(item.text.isNotBlank()) {
                            ChatBubbleLeft(
                                message = item.text,
                                isFirstMessage = true,
                                showResponseButtons = false,
                                onClick = {

                                }
                            )
                        }
                    }

                    is Message.MultiSelect -> {
                        SuggestionsComponent(
                            showLeftIcon = true,
                            suggestionList = item.choices.map { choice -> SuggestionModel(label = choice) },
                            suggestionType = SuggestionType.MULTI_SELECT,
                            enabled = false,
                            onSuggestionClicked = {

                            }
                        )
                        if(item.text.isNotBlank()) {
                            ChatBubbleLeft(
                                message = item.text,
                                isFirstMessage = true,
                                showResponseButtons = false,
                                onClick = {

                                }
                            )
                        }
                    }
                }
            }
        }
        ErrorSnackbar(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End)
                .systemBarsPadding()
                .padding(horizontal = 4.dp),
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun ErrorSnackbar(
    modifier: Modifier = Modifier,
    snackbarHostState : SnackbarHostState,
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Snackbar(
                containerColor = DarwinTouchRedBgLight,
                contentColor = DarwinTouchRed,
                actionContentColor = DarwinTouchPrimary,
                action = {
                    TextButton(onClick = { snackbarData.performAction() }) {
                        Text(text = snackbarData.visuals.actionLabel ?: "Retry")
                    }
                }
            ) {
                Text(text = snackbarData.visuals.message, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    )
}