package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.chat.presentation.models.ConversationInputState
import com.eka.medassist.ui.chat.presentation.models.RecordingStatus

/** Switches between the text input and the voice input while a recording is in progress. */
@Composable
fun ConversationInput(
    text: String,
    onTextChange: (String) -> Unit,
    sendEnabled: Boolean,
    recordingStatus: RecordingStatus,
    allowAudio: Boolean,
    allowFiles: Boolean,
    attachments: List<MatrixFile>,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onRemoveAttachment: (MatrixFile) -> Unit,
    onMicrophoneClick: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val state = if (recordingStatus == RecordingStatus.IDLE) ConversationInputState.Default else ConversationInputState.Audio

    AnimatedContent(
        targetState = state,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            enter togetherWith exit using SizeTransform(clip = false)
        },
        label = "conversation-input"
    ) {
        when (it) {
            is ConversationInputState.Default -> {
                DefaultInputComponent(
                    focusRequester = focusRequester,
                    query = text,
                    onQueryChange = onTextChange,
                    onSend = onSend,
                    onMicrophoneClick = onMicrophoneClick,
                    sendEnabled = sendEnabled,
                    showMicrophone = allowAudio,
                    onAttachClick = if (allowFiles) onAttach else null,
                    attachments = attachments,
                    onRemoveAttachment = onRemoveAttachment,
                )
            }

            is ConversationInputState.Audio -> {
                AudioInputComponent(
                    status = recordingStatus,
                    onSend = onStopRecording,
                    onCancel = onCancelRecording,
                )
            }
        }
    }
}
