package com.eka.medassist.ui.chat.utility

internal object AudioWaveUtils {
    fun generateFakeWaveform(count: Int): List<Float> {
        return List(count) {
            (0.2f + kotlin.random.Random.nextFloat() * 0.6f)
        }
    }
}