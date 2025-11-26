package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import com.eka.conversation.common.Response
import com.eka.medassist.ui.chat.presentation.models.ConversationInputState
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel

@Composable
fun ConversationInput(viewModel: EkaChatViewModel, sendEnabled : Boolean) {
    val state = viewModel.inputState.collectAsState().value
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            val enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            val exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            enter togetherWith exit using SizeTransform(clip = false)
        }
    ) {
        when (it) {
            is ConversationInputState.Default -> {
                DefaultInputComponent(
                    focusRequester = focusRequester,
                    input = viewModel.textInputState.collectAsState().value,
                    onSend = { query ->
                        viewModel.askNewQuery(query = query)
                    },
                    onMicrophoneClick = {
                        viewModel.setInputState(ConversationInputState.Audio)
                    },
                    onCancel = {

                    },
                    sendEnabled = sendEnabled
                )
            }

            is ConversationInputState.Audio -> {
                AudioInputComponent(
                    ekaChatViewModel = viewModel,
                    onTranscriptionResult = { response ->
                        when (response) {
                            is Response.Loading -> {
                            }

                            is Response.Success -> {
                                val transcribedText = response.data.toString()
                                viewModel.updateTextInputState(transcribedText)
                                viewModel.clearRecording()
                            }

                            is Response.Error -> {
                                val errorMsg = response.message.toString()
                                viewModel.showToast(errorMsg)
                                viewModel.clearRecording()
                            }
                        }
                        viewModel.setInputState(ConversationInputState.Default)
                    },
                )
            }
        }
    }
}