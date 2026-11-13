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
import com.eka.medassist.ui.chat.presentation.models.VoiceStatus

/** Chat input: text, attachments, mic and send; swaps to the recording bar while the doctor speaks. */
@Composable
fun ConversationInput(
    text: String,
    onTextChange: (String) -> Unit,
    sendEnabled: Boolean,
    allowFiles: Boolean,
    attachments: List<MatrixFile>,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onRemoveAttachment: (MatrixFile) -> Unit,
    readingLabel: String? = null,
    voiceStatus: VoiceStatus = VoiceStatus.IDLE,
    voiceStartedAt: Long = 0L,
    /** null hides the mic. */
    onMicrophoneClick: (() -> Unit)? = null,
    onVoiceDone: () -> Unit = {},
    onVoiceCancel: () -> Unit = {},
    preparingAttachments: Boolean = false,
    onStop: (() -> Unit)? = null,
    voiceLevels: List<Float> = emptyList(),
) {
    val focusRequester = remember { FocusRequester() }
    AnimatedContent(
        // Once sent, the clip is a message ("Voice message" bubble), so the input comes back
        targetState = voiceStatus != VoiceStatus.LISTENING,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            enter togetherWith exit using SizeTransform(clip = false)
        },
        label = "conversation-input"
    ) { idle ->
        if (idle) {
            DefaultInputComponent(
                focusRequester = focusRequester,
                query = text,
                onQueryChange = onTextChange,
                onSend = onSend,
                sendEnabled = sendEnabled,
                onAttachClick = if (allowFiles) onAttach else null,
                attachments = attachments,
                onRemoveAttachment = onRemoveAttachment,
                readingLabel = readingLabel,
                onMicrophoneClick = onMicrophoneClick,
                preparingAttachments = preparingAttachments,
                onStop = onStop,
            )
        } else {
            AudioInputComponent(
                status = voiceStatus,
                startedAt = voiceStartedAt,
                levels = voiceLevels,
                onDone = onVoiceDone,
                onCancel = onVoiceCancel,
            )
        }
    }
}
