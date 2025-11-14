package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.conversation.common.Response
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchRed
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AudioFeatureLayout(
    viewModel: EkaChatViewModel,
    onTranscriptionResult: (Response<String>) -> Unit,
) {
    var dotCount by remember { mutableIntStateOf(0) }
    var recordingTime by remember { mutableLongStateOf(0L) }
    val currentTranscribedData by viewModel.currentTranscribeData.collectAsState(Response.Loading())
    var isRecording by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        while (true) {
            delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    LaunchedEffect(Unit) {
        viewModel.clearRecording()
        isRecording = true
        viewModel.startAudioRecording(
            onError = { error ->
                onTranscriptionResult.invoke(Response.Error(error))
            }
        )
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTime = 0
            while (isRecording) {
                delay(10L)
                recordingTime += 10L
            }
        }
    }

    LaunchedEffect(currentTranscribedData) {
        when (currentTranscribedData) {
            is Response.Loading -> {
                if (!isRecording) {
                    isLoading = true
                }
            }

            is Response.Success -> {
                isLoading = false
                onTranscriptionResult(currentTranscribedData)
            }

            is Response.Error -> {
                isLoading = false
                onTranscriptionResult(currentTranscribedData)
            }
        }
    }

    val timeString = remember(recordingTime) {
        val minutes = recordingTime / 60000
        val seconds = (recordingTime % 60000) / 1000
        val millis = (recordingTime % 1000) / 10
        String.format(Locale.getDefault(),"%02d:%02d:%02d", minutes, seconds, millis)
    }

    val recordingLottieComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(
            resId = R.raw.recording_started
        )
    )

    Column(
        Modifier
            .clickable {
                isRecording = false
                isLoading = true
                coroutineScope.launch {
                    viewModel.stopRecording()
                }
            }
            .fillMaxWidth()
            .height(267.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = recordingLottieComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .fillMaxWidth(fraction = 0.5f)
                .height(80.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        if(isRecording){
            Text(
                text = timeString,
                style = touchBodyRegular,
                color = DarwinTouchNeutral1000,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .border(width = 2.dp, color = DarwinTouchRed, shape = CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(DarwinTouchRed)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.tap_to_stop_recording),
                    style = touchBodyRegular,
                    color = DarwinTouchNeutral1000,
                )
            }
        }else if(isLoading){
            Text(text = "Converting to text${".".repeat(dotCount)}", style = touchBodyRegular, color = DarwinTouchNeutral1000)
        }
    }
}