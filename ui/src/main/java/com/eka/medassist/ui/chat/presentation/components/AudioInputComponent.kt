package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50

@Composable
fun AudioInputComponent(onStopRecording: () -> Unit) {
    val recordingLottieComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(
            resId = R.raw.recording_started
        )
    )
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        keyboardController?.show()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(16.dp),
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
                onClick = onStopRecording
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarwinTouchNeutral50)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LottieAnimation(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    composition = recordingLottieComposition,
                    iterations = LottieConstants.IterateForever,
                    contentScale = ContentScale.Fit
                )
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
                    onClick = onStopRecording
                )
            }
        }
    )
}