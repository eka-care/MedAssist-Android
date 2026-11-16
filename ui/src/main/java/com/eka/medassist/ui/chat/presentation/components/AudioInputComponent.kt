package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.models.VoiceStatus
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral300
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import kotlinx.coroutines.delay

/**
 * Recording bar that replaces the input while the doctor speaks: ✕ discard, waveform + timer,
 * ✓ to send the voice message (web widget behaviour).
 */
@Composable
fun AudioInputComponent(
    status: VoiceStatus,
    startedAt: Long,
    /** Recent mic levels 0..1 (oldest first): the waveform follows the voice. */
    levels: List<Float> = emptyList(),
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val listening = status == VoiceStatus.LISTENING
    var elapsedMs by remember(startedAt) { mutableLongStateOf(0L) }
    LaunchedEffect(startedAt, listening) {
        while (listening) {
            elapsedMs = System.currentTimeMillis() - startedAt
            delay(250)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black)
                .padding(10.dp),
            onClick = onCancel,
        ) {
            Icon(painterResource(R.drawable.ic_xmark_regular), contentDescription = "Cancel voice input", tint = Color.White)
        }

        if (listening) {
            LiveWaveform(
                levels = levels,
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .padding(horizontal = 8.dp),
                color = DarwinTouchPrimary,
            )
            Text(
                text = "%d:%02d".format(elapsedMs / 60_000, (elapsedMs / 1000) % 60),
                style = touchFootnoteRegular,
                color = DarwinTouchNeutral600,
            )
        } else {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = DarwinTouchPrimary)
                Spacer(Modifier.width(8.dp))
                Text(text = "Converting to text...", style = touchBodyRegular, color = DarwinTouchNeutral1000)
            }
        }

        IconButton(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50))
                .background(if (listening) Color.Black else DarwinTouchNeutral300)
                .padding(8.dp),
            enabled = listening,
            onClick = onDone,
        ) {
            Icon(painterResource(R.drawable.ic_check_regular), contentDescription = "Send voice message", tint = Color.White)
        }
    }
}

/** Bars for the latest mic levels, newest on the right; flat bars while quiet. */
@Composable
private fun LiveWaveform(levels: List<Float>, modifier: Modifier, color: Color, bars: Int = 32) {
    Canvas(modifier = modifier) {
        val shown = levels.takeLast(bars)
        val padded = List(bars - shown.size) { 0.05f } + shown
        val gap = 2.dp.toPx()
        val barWidth = ((size.width - gap * (bars - 1)) / bars).coerceAtLeast(1f)
        padded.forEachIndexed { i, level ->
            val h = (size.height * level.coerceIn(0.05f, 1f)).coerceAtLeast(2.dp.toPx())
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barWidth + gap), (size.height - h) / 2),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
            )
        }
    }
}
