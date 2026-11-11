package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedLoopingWaveformSmooth(
    modifier: Modifier = Modifier,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp,
    minBarHeight: Dp = 2.dp,
    maxBarHeight: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 1.5.dp,
    cycleDurationMs: Int = 2000 // Full cycle duration
) {
    val amplitudePattern = remember {
        listOf(
            0.2f, 0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.3f,
            0.5f, 0.8f, 0.4f, 0.6f, 0.2f, 0.7f, 0.5f, 0.3f, 0.9f, 0.6f,
            0.4f, 0.7f, 0.5f, 0.8f, 0.3f, 0.6f, 0.4f, 0.9f, 0.5f, 0.2f,
            0.6f, 0.8f, 0.3f, 0.5f, 0.7f, 0.4f, 0.6f, 0.2f, 0.8f, 0.5f
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = amplitudePattern.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = cycleDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Canvas(modifier = modifier) {
        val minHeightPx = minBarHeight.toPx()
        val maxHeightPx = maxBarHeight.toPx()
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        val cornerRadiusPx = cornerRadius.toPx()
        val totalBarWidth = barWidthPx + barSpacingPx

        val barCount = ((size.width + barSpacingPx) / totalBarWidth).toInt()
        val patternSize = amplitudePattern.size

        for (i in 0 until barCount) {
            // Smooth interpolation between amplitude values
            val floatIndex = (i + animatedOffset) % patternSize
            val lowerIndex = floatIndex.toInt() % patternSize
            val upperIndex = (lowerIndex + 1) % patternSize
            val fraction = floatIndex - floatIndex.toInt()

            // Lerp between two adjacent amplitudes
            val amp = lerp(
                amplitudePattern[lowerIndex],
                amplitudePattern[upperIndex],
                fraction
            )

            val barHeight = minHeightPx + (amp * (maxHeightPx - minHeightPx))
            val x = i * totalBarWidth
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        }
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}