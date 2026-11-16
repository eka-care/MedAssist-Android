package com.eka.medassist.ui.chat.presentation.voice

import android.app.Application
import com.eka.conversation.matrix.media.AudioPayload
import com.eka.conversation.matrix.media.AudioMessageRecorder
import com.eka.medassist.ui.chat.logger.MedAssistLogger
import com.eka.medassist.ui.chat.presentation.models.ChatUiMessage
import com.eka.medassist.ui.chat.presentation.models.ConnectionStatus
import com.eka.medassist.ui.chat.presentation.models.Role
import com.eka.medassist.ui.chat.presentation.models.UiError
import com.eka.medassist.ui.chat.presentation.models.VoiceStatus
import com.eka.medassist.ui.chat.common.SilenceDetector
import com.eka.medassist.ui.chat.presentation.models.MedAssistUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The voice message flow, lifted out of the ViewModel: hold the mic, draw the waveform, stop on
 * silence, send one clip and show the transcript it comes back as. It owns the recorder and its
 * jobs; everything it needs from the chat arrives as the callbacks below.
 */
internal class VoiceInput(
    private val app: Application,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<MedAssistUiState>,
    private val toast: (String) -> Unit,
    private val track: (String, Array<out Pair<String, Any?>>) -> Unit,
    private val isOnline: () -> Boolean,
    private val sendAudio: suspend (AudioPayload) -> Unit,
    private val setWaiting: (Boolean) -> Unit,
    /** A transcript arrived, so the stream-idle timer no longer applies. */
    private val onTranscript: () -> Unit,
) {

    private val recorder = AudioMessageRecorder(app)
    private var voiceTimeoutJob: Job? = null

    /** Call once the mic permission is granted. */
    fun start() {
        val s = state.value
        if (s.voiceStatus != VoiceStatus.IDLE || recorder.isRecording) return
        if (!s.canSendLive) {
            toast(if (s.connection != ConnectionStatus.CONNECTED) "Voice needs a connection. Please try again in a moment." else "Wait for the reply to finish.")
            return
        }
        val startedAt = System.currentTimeMillis()
        recorder.start(
            onDone = { audio -> onRecorded(audio) },
            onError = { e ->
                MedAssistLogger.d("MedAssist", "voice: recording failed: ${e.message}")
                setVoiceIdle()
                // MediaRecorder.stop() throws when stopped right after start
                toast(
                    if (System.currentTimeMillis() - startedAt < MIN_VOICE_MS) "Didn't catch that. Tap the mic and speak a little longer."
                    else "Couldn't use the microphone. It may be in use by another app."
                )
            },
        )
        if (recorder.isRecording) {
            state.update { it.copy(voiceStatus = VoiceStatus.LISTENING, voiceStartedAt = startedAt, voiceLevels = emptyList()) }
            track("voice_started", emptyArray())
            startVoiceMeter()
        }
    }

    private var voiceMeterJob: Job? = null
    private var voiceDetector = SilenceDetector()

    /** Live waveform from the mic level; ends the clip after the doctor stops talking. */
    private fun startVoiceMeter() {
        voiceDetector = SilenceDetector(speechLevel = SPEECH_LEVEL)
        voiceMeterJob?.cancel()
        voiceMeterJob = scope.launch {
            while (state.value.voiceStatus == VoiceStatus.LISTENING) {
                delay(100)
                val amp = recorder.maxAmplitude()
                state.update { it.copy(voiceLevels = (it.voiceLevels + SilenceDetector.level(amp)).takeLast(48)) }
                when (voiceDetector.onSample(amp, System.currentTimeMillis())) {
                    SilenceDetector.Verdict.SPEECH_ENDED -> {
                        track("voice_auto_stopped", emptyArray())
                        finish()
                        return@launch
                    }
                    SilenceDetector.Verdict.NO_SPEECH -> {
                        cancel()
                        toast("Couldn't hear you. Check the mic and try again.")
                        track("voice_no_speech", emptyArray())
                        return@launch
                    }
                    SilenceDetector.Verdict.KEEP_LISTENING -> Unit
                }
            }
        }
    }

    /** Stop listening and send the clip (also called by the recorder's 15-minute cap). */
    fun finish() {
        if (state.value.voiceStatus != VoiceStatus.LISTENING) return
        voiceMeterJob?.cancel()
        if (!voiceDetector.heardSpeech) {
            // Nothing loud enough was said: don't send silence
            recorder.cancel()
            setVoiceIdle()
            toast("Couldn't hear you. Speak a little louder and try again.")
            track("voice_no_speech", emptyArray())
            return
        }
        track("voice_sent", arrayOf("ms" to (System.currentTimeMillis() - state.value.voiceStartedAt)))
        recorder.stop()
    }

    private fun onRecorded(audio: AudioPayload) {
        if (audio.durationMs < MIN_VOICE_MS) {
            setVoiceIdle()
            toast("Didn't catch that. Tap the mic and speak a little longer.")
            return
        }
        val s = state.value
        if (!isOnline() || s.connection != ConnectionStatus.CONNECTED) {
            setVoiceIdle()
            toast("You're offline, so the voice message wasn't sent. Please try again.")
            return
        }
        state.update { it.copy(voiceStatus = VoiceStatus.TRANSCRIBING) }
        scope.launch {
            sendAudio(audio)
            voiceTimeoutJob?.cancel()
            voiceTimeoutJob = scope.launch {
                delay(VOICE_TIMEOUT_MS)
                fail("It took too long to transcribe.")
            }
        }
    }

    /** The doctor's words: fill the "Voice message" bubble; the reply follows. */
    fun onTranscript(text: String) {
        onTranscript()
        state.update { s ->
            val i = s.messages.indexOfLast { it.role == Role.USER && it.isAudio && it.content.isBlank() }
            val messages = if (i >= 0) s.messages.toMutableList().also { it[i] = it[i].copy(content = text) }
            else s.messages + ChatUiMessage(id = System.currentTimeMillis().toString(), content = text, role = Role.USER)
            s.copy(messages = messages, isStreaming = false, progressMessage = null)
        }
        // The reply follows the transcript, so keep "Thinking" up
        setWaiting(true)
    }

    /**
     * A sent clip got no transcript (rejected, offline, timed out): drop its "Voice message"
     * bubble and tell the doctor, instead of leaving it on "Transcribing...".
     */
    fun fail(reason: String?) {
        voiceTimeoutJob?.cancel()
        if (state.value.voiceStatus != VoiceStatus.TRANSCRIBING) return
        state.update { s ->
            val i = s.messages.indexOfLast { it.role == Role.USER && it.isAudio && it.content.isBlank() }
            s.copy(
                messages = if (i >= 0) s.messages.filterIndexed { idx, _ -> idx != i } else s.messages,
                voiceStatus = VoiceStatus.IDLE,
                error = UiError("Voice message not sent", listOfNotNull(reason, "Please try again or type your question.").joinToString(" ")),
                showRetry = false,
            )
        }
        setWaiting(false)
    }

    /** The server answered (transcript, reply, tool, error or offline): stop showing "transcribing". */
    fun clearTranscribing() {
        voiceTimeoutJob?.cancel()
        if (state.value.voiceStatus == VoiceStatus.TRANSCRIBING) setVoiceIdle()
    }

    /** Drop a recording in progress (cancel tapped, app backgrounded, new chat). A sent clip can't be recalled. */
    fun cancel() {
        if (state.value.voiceStatus != VoiceStatus.LISTENING) return
        voiceMeterJob?.cancel()
        recorder.cancel()
        setVoiceIdle()
        track("voice_cancelled", emptyArray())
    }

    private fun setVoiceIdle() = state.update { it.copy(voiceStatus = VoiceStatus.IDLE, voiceLevels = emptyList()) }

    /** The screen is gone: drop the mic and the timers. */
    fun release() {
        voiceMeterJob?.cancel()
        recorder.cancel()
        voiceTimeoutJob?.cancel()
    }

    private companion object {
        const val MIN_VOICE_MS = 700L
        const val SPEECH_LEVEL = 1_800
        const val VOICE_TIMEOUT_MS = 30_000L
    }
}
