package com.eka.medassist.ui.chat

import com.eka.medassist.ui.chat.common.SilenceDetector
import com.eka.medassist.ui.chat.common.SilenceDetector.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SilenceDetectorTest {

    private fun detector() = SilenceDetector(speechLevel = 1_800, silenceMs = 3_000, noSpeechMs = 8_000)

    @Test
    fun keepsListeningWhileTalking() {
        val d = detector()
        for (t in 0L..5_000L step 100) assertEquals(Verdict.KEEP_LISTENING, d.onSample(5_000, t))
        assertTrue(d.heardSpeech)
    }

    @Test
    fun endsAfterSilenceFollowingSpeech() {
        val d = detector()
        d.onSample(6_000, 0)
        assertEquals(Verdict.KEEP_LISTENING, d.onSample(200, 2_900))
        assertEquals(Verdict.SPEECH_ENDED, d.onSample(200, 3_000))
    }

    @Test
    fun shortPausesDoNotEndIt() {
        val d = detector()
        d.onSample(6_000, 0)
        d.onSample(100, 2_000)
        d.onSample(6_000, 2_500) // talking again resets the silence clock
        assertEquals(Verdict.KEEP_LISTENING, d.onSample(100, 5_000))
        assertEquals(Verdict.SPEECH_ENDED, d.onSample(100, 5_500))
    }

    @Test
    fun abandonsWhenNothingIsSaid() {
        val d = detector()
        assertEquals(Verdict.KEEP_LISTENING, d.onSample(300, 0))
        assertEquals(Verdict.KEEP_LISTENING, d.onSample(300, 7_900))
        assertEquals(Verdict.NO_SPEECH, d.onSample(300, 8_000))
        assertFalse(d.heardSpeech)
    }

    @Test
    fun levelIsClampedForTheWaveform() {
        assertEquals(0.05f, SilenceDetector.level(0), 0.0001f)
        assertEquals(1f, SilenceDetector.level(32_767), 0.0001f)
        assertTrue(SilenceDetector.level(4_000) in 0.05f..1f)
    }
}
