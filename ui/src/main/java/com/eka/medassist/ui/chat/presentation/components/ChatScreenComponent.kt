package com.eka.medassist.ui.chat.presentation.components

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.chat.client.MedAssistPatient
import com.eka.medassist.ui.chat.presentation.screens.ConversationScreen
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistArgs
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistViewModel
import com.eka.ui.theme.EkaTheme
import kotlinx.coroutines.launch
import com.eka.medassist.ui.chat.presentation.voice.rememberMicPermission

/**
 * Full MedAssist chat, ready to drop into any Activity's setContent:
 * owns the ViewModel and the file picker.
 */
@Composable
fun MedAssistChat(
    args: MedAssistArgs,
    onClose: () -> Unit,
    /**
     * Opens the host's patient picker and reports the chosen patient through `onPicked`.
     * Shows the patient chip in the chat when set.
     */
    onPickPatient: ((onPicked: (MedAssistPatient) -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val viewModel: MedAssistViewModel = viewModel(
        key = "medassist-${args.agentId}",
        factory = MedAssistViewModel.Factory(context.applicationContext as Application, args),
    )
    val withMicPermission = rememberMicPermission()
    val scope = rememberCoroutineScope()
    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            val files = uris.take(MatrixFile.MAX_FILES_PER_MESSAGE).mapNotNull { MatrixFile.fromUri(context, it) }
            viewModel.addAttachments(files)
        }
    }
    EkaTheme {
        ConversationScreen(
            viewModel = viewModel,
            onBackClick = onClose,
            onPickFiles = { pickFiles.launch(arrayOf("image/png", "image/jpeg", "application/pdf")) },
            onPickPatient = onPickPatient,
            askMicrophonePermission = withMicPermission,
        )
    }
}
