package com.eka.medassist.ui.chat.presentation.models

import com.eka.conversation.matrix.AgentConfig
import com.eka.conversation.matrix.InitialMessage
import com.eka.conversation.matrix.PastSession
import com.eka.conversation.matrix.media.MatrixFile
import kotlinx.serialization.json.JsonObject

enum class Role { USER, ASSISTANT }

enum class ConnectionStatus { CONNECTING, CONNECTED, NOT_CONNECTED }

/** Dictation into the input (voice to text). */
enum class VoiceStatus { IDLE, LISTENING, TRANSCRIBING }

object Feedback {
    const val LIKE = "LIKE"
    const val DISLIKE = "DISLIKE"
    const val NONE = "NONE"
}

data class ChatUiMessage(
    val id: String,
    val content: String,
    val role: Role,
    val files: List<MatrixFile> = emptyList(),
    /** Voice message; content stays blank until Matrix sends its transcript. */
    val isAudio: Boolean = false,
    val toolKeys: List<String> = emptyList(),
    val isResponseFromTool: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCallStatus: String? = null,
    val userHint: String? = null,
    val feedback: String = Feedback.NONE,
    /** Doctor's messages: queued while offline, sent, or failed to send. */
    val delivery: Delivery = Delivery.SENT,
    /** Assistant reply the doctor cut short with Stop. */
    val stopped: Boolean = false,
)

enum class Delivery { QUEUED, SENT, FAILED }

/** Port of the widget's TElicitationEntry; keyed by "<messageId>::<toolId>". */
data class ElicitationEntry(
    val toolId: String,
    val messageId: String,
    val component: String? = null,
    /** Gates callTool routing. */
    val active: Boolean = false,
    /** UI only. */
    val disabled: Boolean = false,
    val responseLabel: String? = null,
    val serverInput: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
    val input: JsonObject? = null,
    val required: List<String> = emptyList(),
    val toolSelectionNames: List<String> = emptyList(),
    val mcpMetaFields: List<String> = emptyList(),
    val pattern: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val dispToastMsg: String? = null,
    val description: String? = null,
    val toolResult: JsonObject? = null,
    val sendToLlm: Boolean = false,
    val answer: String? = null,
    val orientation: String? = null,
    val hideShowMore: Boolean? = null,
)

data class UiError(val title: String, val description: String? = null)

data class MedAssistUiState(
    val title: String = "DocAssist",
    val connection: ConnectionStatus = ConnectionStatus.CONNECTING,
    val messages: List<ChatUiMessage> = emptyList(),
    val elicitations: Map<String, ElicitationEntry> = emptyMap(),
    val isStreaming: Boolean = false,
    val isWaitingForResponse: Boolean = false,
    val progressMessage: String? = null,
    val tips: List<String> = emptyList(),
    val error: UiError? = null,
    val showRetry: Boolean = false,
    val startNewConnection: Boolean = false,
    val initialMessage: InitialMessage? = null,
    val agentConfig: AgentConfig? = null,
    val sessionId: String? = null,
    val attachments: List<MatrixFile> = emptyList(),
    val preparingAttachments: Boolean = false,
    val isLoadingSessionMessages: Boolean = false,
    val pastSessions: List<PastSession> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val showExitFeedback: Boolean = false,
    val authExpired: Boolean = false,
    /** No internet (OS says so, or a send got no reply); cleared when the socket reconnects. */
    val offline: Boolean = false,
    val patient: com.eka.medassist.ui.chat.client.MedAssistPatient? = null,
    val doctor: com.eka.medassist.ui.chat.client.MedAssistDoctor? = null,
    val readingLabel: String? = null,
    val voiceStatus: VoiceStatus = VoiceStatus.IDLE,
    val voiceStartedAt: Long = 0L,
    /** Recent mic levels 0..1 (oldest first) for the live waveform. */
    val voiceLevels: List<Float> = emptyList(),
) {
    /** Last message's last tool entry when it still accepts input (drives the composer swap). */
    val activeElicitation: Pair<String, ElicitationEntry>?
        get() {
            val key = messages.lastOrNull()?.toolKeys?.lastOrNull() ?: return null
            val entry = elicitations[key] ?: return null
            return if (entry.active) key to entry else null
        }

    val isAuthComponent: Boolean
        get() = activeElicitation?.second?.component in com.eka.conversation.matrix.MatrixComponents.AUTH

    val isComposerDisabled: Boolean
        get() = isStreaming || isWaitingForResponse || voiceStatus == VoiceStatus.TRANSCRIBING ||
            authExpired || startNewConnection

    /** Text and files can be queued offline; live-only actions (voice, option taps) need the socket. */
    val canSendLive: Boolean
        get() = connection == ConnectionStatus.CONNECTED && !isComposerDisabled

    /** A reply is on its way and can be stopped. */
    val canStop: Boolean
        get() = connection == ConnectionStatus.CONNECTED && (isStreaming || isWaitingForResponse) &&
            voiceStatus != VoiceStatus.TRANSCRIBING

    val isEmptyChat: Boolean get() = messages.isEmpty()
}

/** Options for one send (port of the widget's TMessageOptions). */
data class SendOptions(
    val message: String? = null,
    val files: List<MatrixFile>? = null,
    val audio: com.eka.conversation.matrix.media.AudioPayload? = null,
    val toolCalled: Boolean = false,
    val selectedComponent: JsonObject? = null,
    val resend: Boolean = false,
    val toolDeclined: Boolean = false,
    val toolResult: JsonObject? = null,
    val toolKey: String? = null,
    /** Already in the list (queued / resent message): send without adding another bubble. */
    val shownMessageId: String? = null,
)
