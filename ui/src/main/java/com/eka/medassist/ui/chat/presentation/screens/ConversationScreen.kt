@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.eka.medassist.ui.chat.presentation.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.eka.conversation.matrix.MatrixComponents
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.components.ChatBubbleLeft
import com.eka.medassist.ui.chat.presentation.components.ChatBubbleProcessing
import com.eka.medassist.ui.chat.presentation.components.ChatBubbleRight
import com.eka.medassist.ui.chat.presentation.components.ConversationHeader
import com.eka.medassist.ui.chat.presentation.components.ConversationInput
import com.eka.medassist.ui.chat.presentation.components.ErrorContent
import com.eka.medassist.ui.chat.presentation.components.ShimmerBubble
import com.eka.medassist.ui.chat.presentation.components.elicitation.AuthElicitationInput
import com.eka.medassist.ui.chat.presentation.components.elicitation.Chip
import com.eka.medassist.ui.chat.presentation.components.elicitation.ElicitationRenderer
import com.eka.medassist.ui.chat.presentation.components.elicitation.ExitFeedbackSheet
import com.eka.medassist.ui.chat.presentation.models.ConnectionStatus
import com.eka.medassist.ui.chat.presentation.models.MedAssistUiState
import com.eka.medassist.ui.chat.presentation.models.Role
import com.eka.medassist.ui.chat.presentation.models.SendOptions
import com.eka.medassist.ui.chat.presentation.states.rememberTypewriterText
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import com.eka.medassist.ui.chat.theme.touchHeadlineBold

/**
 * MedAssist chat on Matrix. The host provides file picking and the mic permission prompt;
 * everything else (history, feedback, elicitations, reconnects) is handled here.
 */
@Composable
fun ConversationScreen(
    viewModel: MedAssistViewModel,
    onBackClick : () -> Unit,
    onPickFiles: () -> Unit,
    askMicrophonePermission : (onGranted: () -> Unit) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    var showHistory by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.toasts.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) viewModel.onForeground() }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    if (showHistory) {
        ConversationHistoryScreen(
            sessions = state.pastSessions,
            loading = state.isLoadingHistory,
            currentSessionId = state.sessionId,
            onSessionClick = { showHistory = false; text = ""; viewModel.openPastSession(it) },
            onNewChatClick = { showHistory = false; text = ""; viewModel.startNewChat() },
            onBackClick = { showHistory = false },
        )
        return
    }

    val requestClose = { if (!viewModel.onCloseRequested()) onBackClick() }
    BackHandler { requestClose() }

    val cfg = state.agentConfig
    val startNewChat = { text = ""; viewModel.startNewChat() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarwinTouchNeutral50)
            .imePadding()
    ) {
        ConversationHeader(
            title = state.title,
            subTitle = getConnectionState(state),
            onBackClick = requestClose,
            onNewClick = startNewChat,
            onHistoryClick = if (cfg?.theme?.feature != false) {
                { showHistory = true; viewModel.loadPastSessions() }
            } else null,
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoadingSessionMessages -> ShimmerContent()
                state.isEmptyChat && cfg == null && state.connection == ConnectionStatus.CONNECTING -> ShimmerContent()
                state.isEmptyChat -> EmptyState(state) { viewModel.onInitialSuggestion(it) }
                else -> ConversationContent(state = state, viewModel = viewModel)
            }
        }

        val error = state.error
        when {
            error != null -> ErrorContent(
                title = error.title,
                description = error.description,
                showRetry = state.showRetry,
                onRetry = viewModel::retry,
                actionLabel = when {
                    state.authExpired -> "Close"
                    state.startNewConnection -> "Start new chat"
                    else -> null
                },
                onAction = { if (state.authExpired) onBackClick() else startNewChat() },
                onDismiss = viewModel::dismissError,
            )
            state.startNewConnection -> ErrorContent(
                title = "This chat has ended",
                showRetry = false,
                onRetry = {},
                actionLabel = "Start new chat",
                onAction = startNewChat,
            )
        }

        val active = state.activeElicitation
        if (active != null && active.second.component in MatrixComponents.AUTH) {
            AuthElicitationInput(
                entry = active.second,
                disabled = state.isComposerDisabled,
                onSubmit = { v -> viewModel.sendMessage(SendOptions(message = v, toolCalled = true, toolKey = active.first)) },
                onResend = { viewModel.sendMessage(SendOptions(toolCalled = true, resend = true, toolKey = active.first)) },
                onCancel = { viewModel.clearElicitation(active.first, "Cancel verification") },
            )
        } else {
            ConversationInput(
                text = text,
                onTextChange = { text = it },
                sendEnabled = !state.isComposerDisabled,
                recordingStatus = state.recordingStatus,
                allowAudio = cfg?.allows("audio") ?: true,
                allowFiles = cfg?.allows("file") ?: true,
                attachments = state.attachments,
                onSend = {
                    viewModel.sendComposer(text.trim())
                    text = ""
                },
                onAttach = onPickFiles,
                onRemoveAttachment = viewModel::removeAttachment,
                onMicrophoneClick = { askMicrophonePermission { viewModel.startRecording() } },
                onStopRecording = viewModel::stopRecording,
                onCancelRecording = viewModel::cancelRecording,
            )
        }
    }

    if (state.showExitFeedback) {
        state.agentConfig?.feedback?.let { fb ->
            ExitFeedbackSheet(
                config = fb,
                onSubmit = { f, n -> viewModel.submitSessionFeedback(f, n) { onBackClick() } },
                onSkip = { viewModel.dismissExitFeedback(); onBackClick() },
            )
        }
    }
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

/** Newest at the bottom (reversed list), streaming reply typed out, elicitations under their message. */
@Composable
private fun ConversationContent(
    state: MedAssistUiState,
    viewModel: MedAssistViewModel,
) {
    val listState = rememberLazyListState()
    val messages = state.messages
    val lastAssistantIndex = messages.indexOfLast { it.role == Role.ASSISTANT }
    val lastIsUser = messages.lastOrNull()?.role == Role.USER
    val isThinking = state.isWaitingForResponse || (state.isStreaming && lastIsUser) ||
        (state.progressMessage != null && lastIsUser)

    LaunchedEffect(messages.size, state.elicitations.size, isThinking) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(0)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
        reverseLayout = true
    ) {
        if (isThinking) {
            item(key = "thinking") {
                ChatBubbleProcessing(
                    progressMessage = state.progressMessage,
                    tips = state.tips.ifEmpty { state.agentConfig?.theme?.tips.orEmpty() },
                )
            }
        }
        items(count = messages.size, key = { r -> (messages.lastIndex - r).let { "${messages[it].id}-$it" } }) { r ->
            val index = messages.lastIndex - r
            val m = messages[index]
            if (m.role == Role.USER) {
                ChatBubbleRight(message = m)
                return@items
            }
            val isLast = index == messages.lastIndex
            val progress = if (isLast && !isThinking) state.progressMessage else null
            val shownText = rememberTypewriterText(
                key = "${m.id}-$index",
                text = m.content,
                streaming = isLast && state.isStreaming,
            )
            val hasBody = m.content.isNotBlank() || m.toolCallStatus != null || m.userHint != null || progress != null
            Column {
                if (hasBody) {
                    ChatBubbleLeft(
                        message = shownText,
                        isFirstMessage = index == 0 || messages[index - 1].role != Role.ASSISTANT,
                        showResponseButtons = index == lastAssistantIndex && !state.isStreaming &&
                            m.id != MedAssistViewModel.INITIAL_BOT_MESSAGE_ID,
                        statusText = m.toolCallStatus ?: progress,
                        userHint = m.userHint,
                        feedback = m.feedback,
                        onFeedback = { fb -> viewModel.sendMessageFeedback(m.id, fb) },
                    )
                }
                m.toolKeys.forEach { key ->
                    val entry = state.elicitations[key] ?: return@forEach
                    if (entry.component in MatrixComponents.AUTH) return@forEach
                    ElicitationRenderer(
                        messageId = m.id,
                        toolKey = key,
                        entry = entry,
                        disabled = entry.disabled || state.isComposerDisabled && entry.component != MatrixComponents.DOCTOR_CARD,
                        onSend = viewModel::sendMessage,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(state: MedAssistUiState, onSuggestion: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(R.drawable.ic_ai_chat_custom), null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(state.agentConfig?.theme?.title ?: state.title, style = touchHeadlineBold, color = DarwinTouchNeutral1000)
        state.agentConfig?.theme?.tagline?.let {
            Text(it, style = touchFootnoteRegular, color = DarwinTouchNeutral600, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        }
        state.initialMessage?.text?.let {
            Text(it, style = touchBodyRegular, color = DarwinTouchNeutral800, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
        }
        val suggestions = state.initialMessage?.suggestions.orEmpty()
        if (suggestions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp),
            ) {
                suggestions.forEach { s ->
                    val label = s.label ?: s.value ?: return@forEach
                    Chip(label, enabled = state.connection == ConnectionStatus.CONNECTED) { onSuggestion(s.value ?: label) }
                }
            }
        }
    }
}

/** Header subtitle, same wording as the old socket states. */
fun getConnectionState(state: MedAssistUiState): String = when {
    state.error != null && state.connection != ConnectionStatus.CONNECTED -> "Error : Connection Failed!"
    state.connection == ConnectionStatus.CONNECTING && state.sessionId == null -> "Starting Session..."
    state.connection == ConnectionStatus.CONNECTING -> "Connecting..."
    state.connection == ConnectionStatus.CONNECTED -> "Connected"
    else -> "Disconnected"
}
