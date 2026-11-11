package com.eka.medassist.ui.chat.common

/**
 * Decides, from mic levels sampled while recording, when a voice message is done:
 * after the doctor has spoken, [silenceMs] of quiet ends it; if nothing is ever said within
 * [noSpeechMs], it's abandoned. Pure logic, so it's unit-tested.
 */
class SilenceDetector(
    private val speechLevel: Int = 2_500,
    private val silenceMs: Long = 3_000,
    private val noSpeechMs: Long = 8_000,
) {
    enum class Verdict { KEEP_LISTENING, SPEECH_ENDED, NO_SPEECH }

    var heardSpeech = false
        private set
    private var startedAt = -1L
    private var lastLoudAt = -1L

    /** [amplitude] = MediaRecorder.getMaxAmplitude() (0..32767) since the last sample. */
    fun onSample(amplitude: Int, nowMs: Long): Verdict {
        if (startedAt < 0) startedAt = nowMs
        if (amplitude >= speechLevel) {
            heardSpeech = true
            lastLoudAt = nowMs
        }
        return when {
            heardSpeech && nowMs - lastLoudAt >= silenceMs -> Verdict.SPEECH_ENDED
            !heardSpeech && nowMs - startedAt >= noSpeechMs -> Verdict.NO_SPEECH
            else -> Verdict.KEEP_LISTENING
        }
    }

    companion object {
        /** Mic amplitude to a 0..1 bar height (log-ish so quiet speech still shows). */
        fun level(amplitude: Int): Float =
            (kotlin.math.sqrt(amplitude.coerceIn(0, 32_767) / 32_767f) * 1.4f).coerceIn(0.05f, 1f)
    }
}
