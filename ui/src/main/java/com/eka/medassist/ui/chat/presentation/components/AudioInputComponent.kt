package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.conversation.common.Response
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.states.AudioInputState
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular

@Composable
fun AudioInputComponent(
    ekaChatViewModel: EkaChatViewModel,
    onTranscriptionResult: (Response<String>) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var audioInputState : AudioInputState by remember { mutableStateOf(AudioInputState.Idle) }
    val currentTranscribedData by ekaChatViewModel.currentTranscribeData.collectAsState()

    LaunchedEffect(Unit) {
        keyboardController?.show()
        audioInputState = AudioInputState.Recording
        ekaChatViewModel.startAudioRecording(
            onError = {
                onTranscriptionResult(Response.Error(it))
            }
        )
    }

    LaunchedEffect(currentTranscribedData) {
        when (currentTranscribedData) {
            is Response.Loading -> {
            }

            is Response.Success -> {
                onTranscriptionResult(currentTranscribedData)
            }

            is Response.Error -> {
                onTranscriptionResult(currentTranscribedData)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
            .background(DarwinTouchNeutral0, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = {
            IconButton(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black)
                    .padding(8.dp),
                content = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White)
                    )
                },
                onClick = {
                    audioInputState = AudioInputState.Loading
                    ekaChatViewModel.stopRecording()
                },
                enabled = audioInputState is AudioInputState.Recording
            )

            if (audioInputState is AudioInputState.Recording) {
                AnimatedLoopingWaveformSmooth(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .padding(horizontal = 24.dp),
                    color = DarwinTouchPrimary,
                    cycleDurationMs = 10000
                )
            }
            if (audioInputState is AudioInputState.Loading) {
                Text(
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    text = "Converting to text...",
                    overflow = TextOverflow.Ellipsis,
                    style = touchBodyRegular,
                    color = DarwinTouchNeutral1000,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(
                modifier = Modifier
                    .rotate(90f)
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black)
                    .padding(8.dp),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left_regular),
                        contentDescription = "Send Icon",
                        tint = Color.White
                    )
                },
                onClick = {
                    audioInputState = AudioInputState.Loading
                    ekaChatViewModel.stopRecording()
                }
            )
        }
    )
}