package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import com.eka.conversation.matrix.media.MatrixFile

/** Chat input: text, attachments and send. */
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
) {
    val focusRequester = remember { FocusRequester() }
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
    )
}
