package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.eka.conversation.common.NetworkChecker
import com.eka.conversation.common.PermissionChecker
import com.eka.conversation.common.Response
import com.eka.medassist.ui.chat.common.models.CTA
import com.eka.medassist.ui.chat.presentation.states.ActionType
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BottomBarMainScreen(
    viewModel: EkaChatViewModel,
    onClick: (CTA) -> Unit,
    openDocumentSelector: () -> Unit,
    isInputBottomSheetVisible: Boolean
) {
    val sessionMessages by viewModel.sessionMessages.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    fun hideKeyboardAndExecute(action: suspend () -> Unit) {
        scope.launch {
            keyboardController?.hide()
            delay(300)
            action()
        }
    }

    fun hideKeyboardAndShowComposable(showComposable: () -> Unit) {
        scope.launch {
            keyboardController?.hide()
            delay(300) // Changed from 3000 to match the other function
            showComposable()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isInputBottomSheetVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(500)
            )
        ) {
            LaunchedEffect(isInputBottomSheetVisible) {
                if (isInputBottomSheetVisible) {
                    delay(100)
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
            EkaChatInputBottom(
                onQueryText = {
                    if (!isSafeInput(it)) {
                        viewModel.showToast("Invalid input detected.")
                        return@EkaChatInputBottom
                    }
                    if (it.isNotEmpty()) {
                        viewModel.askNewQuery(query = it)
                        //TODO ask new query
                    } else {
                        viewModel.showToast("Please enter a query.")
                    }
                },
                viewModel = viewModel,
                isMicrophoneRecording = viewModel.isVoiceToTextRecording,
                isVoice2RxRecording = viewModel.isVoice2RxRecording,
                onMicrophoneClick = {
                    if (PermissionChecker.hasRecordAudioPermission(context) && NetworkChecker.isNetworkAvailable(
                            context
                        )
                    ) {
                        keyboardController?.hide()
                        viewModel.isVoiceToTextRecording = !viewModel.isVoiceToTextRecording
                    } else if (!NetworkChecker.isNetworkAvailable(context)) {
                        viewModel.showToast("Internet not available.")
                    } else {
                        viewModel.showToast("Microphone permission not granted.")
                    }
                },
                textFieldEnabled = !viewModel.isVoiceToTextRecording,
                focusRequester = focusRequester,
                onClick = { cta ->
                    when (cta.action) {
                        ActionType.START_CHAT.stringValue -> {

                        }

                        ActionType.ON_GALLERY_CLICK.stringValue -> {
                            hideKeyboardAndExecute {
                                openDocumentSelector()
                            }
                        }
                    }
                })
        }
        AnimatedVisibility(
            visible = viewModel.isVoiceToTextRecording,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            AudioFeatureLayout(viewModel = viewModel) { response ->
                viewModel.isVoiceToTextRecording = false
                when (response) {
                    is Response.Loading -> {
                    }

                    is Response.Success -> {
                        val transcribedText = response.data.toString()
                        viewModel.updateTextInputState(transcribedText)
                        viewModel.clearRecording()
                        focusRequester.requestFocus()
                    }

                    is Response.Error -> {
                        val errorMsg = response.message.toString()
                        viewModel.showToast(errorMsg)
                        viewModel.clearRecording()
                    }
                }
            }
        }
    }
}

private fun isSafeInput(input: String): Boolean {
    return !Regex("<[^>]*>").containsMatchIn(input)
}