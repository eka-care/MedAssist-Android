package com.eka.medassist.ui.chat.presentation.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eka.conversation.matrix.MatrixEnvironment
import com.eka.medassist.ui.chat.client.MedAssistDoctor
import com.eka.medassist.ui.chat.client.MedAssistPatient
import com.eka.medassist.ui.chat.common.DoctorMerge
import com.eka.medassist.ui.chat.data.MedAssistPrefs
import com.eka.conversation.matrix.MatrixApiException
import com.eka.conversation.matrix.MatrixClient
import com.eka.conversation.matrix.MatrixClientConfig
import com.eka.conversation.matrix.MatrixComponents
import com.eka.conversation.matrix.MatrixError
import com.eka.conversation.matrix.MatrixErrorCode
import com.eka.conversation.matrix.MatrixEvent
import com.eka.conversation.matrix.ElicitationStatus
import com.eka.conversation.matrix.OutgoingRequest
import com.eka.conversation.matrix.RealtimeErrorCodes
import com.eka.conversation.matrix.RecoveryReason
import com.eka.conversation.matrix.StoredMessage
import com.eka.conversation.matrix.TokenExchangeResponse
import com.eka.conversation.matrix.connection.DisconnectReason
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.conversation.matrix.str
import com.eka.medassist.ui.chat.presentation.models.ChatUiMessage
import com.eka.medassist.ui.chat.presentation.models.ConnectionStatus
import com.eka.medassist.ui.chat.presentation.models.ElicitationEntry
import com.eka.medassist.ui.chat.presentation.models.Feedback
import com.eka.medassist.ui.chat.presentation.models.MedAssistUiState
import com.eka.medassist.ui.chat.presentation.models.Role
import com.eka.medassist.ui.chat.presentation.models.SendOptions
import com.eka.medassist.ui.chat.presentation.models.UiError
import com.eka.medassist.ui.chat.logger.MedAssistLogger
import com.eka.medassist.ui.chat.presentation.models.VoiceStatus
import com.eka.conversation.matrix.media.AudioMessageRecorder
import com.eka.conversation.matrix.media.AudioPayload
import kotlinx.coroutines.CancellationException
import com.eka.medassist.ui.chat.common.Outbox
import com.eka.medassist.ui.chat.common.SilenceDetector
import com.eka.medassist.ui.chat.data.AuthIdentity
import com.eka.medassist.ui.chat.data.ChatCache
import com.eka.medassist.ui.chat.presentation.models.Delivery
import com.eka.conversation.matrix.AgentConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/** Host-supplied launch parameters. */
data class MedAssistArgs(
    val agentId: String,
    /** Doctor's Eka session token, sent to Matrix as the partner auth_token (null -> guest). */
    val authToken: String?,
    val title: String = "DocAssist",
    /** Patient in focus at start; the doctor can attach / change it from the chat. */
    val patient: MedAssistPatient? = null,
    /** Signed-in doctor, greeted on the empty chat. */
    val doctor: MedAssistDoctor? = null,
    /** Document the chat is reading (e.g. a scribe note), shown above the input. */
    val readingLabel: String? = null,
    /** Start a fresh chat instead of resuming the stored session. */
    val forceNewChat: Boolean = false,
    /** Open this past session instead of the stored one. */
    val sessionId: String? = null,
    val environment: MatrixEnvironment = MatrixEnvironment.PRODUCTION,
    /**
     * Extra session context sent on session create (session_context), e.g. an EkaScribe
     * session: {"session_id", "transcript", "document": {"id", "name", "content"}}.
     */
    val extraContext: JsonObject? = null,
    /**
     * Which stored chat to resume. null = the regular DocAssist chat; a scope such as
     * "scribe:<session id>" keeps one chat per scribe session (as ekascribe-web does) without
     * touching the regular one.
     */
    val chatScope: String? = null,
    /**
     * A message the doctor already typed before opening (e.g. the home "Ask your agent" box).
     * Sent as the first message, queued until the chat is live. Pass it on first open only.
     */
    val initialMessage: String? = null,
    /** Open the + sheet (records, gallery, camera, files) as soon as the chat shows. */
    val openAttach: Boolean = false,
)

/**
 * Native port of the MedAssist widget's `useChat` hook + `useChatWidget` state:
 * message aggregation, elicitation state machine and routing, retries, history, feedback, recording.
 */
class MedAssistViewModel(app: Application, private val args: MedAssistArgs) : AndroidViewModel(app) {

    private val prefs = MedAssistPrefs(app, args.agentId)
    private val _state = MutableStateFlow(MedAssistUiState(title = args.title, patient = args.patient, doctor = args.doctor, readingLabel = args.readingLabel))
    val state: StateFlow<MedAssistUiState> = _state.asStateFlow()
    private val _toasts = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toasts: SharedFlow<String> = _toasts.asSharedFlow()

    private val client: MatrixClient

    private var retryAttempts = 0
    private var hadStreamBreak = false
    private var streamIdleJob: Job? = null
    private var responseTimeoutJob: Job? = null
    private var fileUploadsThisSession = 0
    private var initialPrompts: List<Pair<String, String>> = emptyList()
    private var exitFeedbackShown = false
    /** A reply was pending when the network dropped, so that message was likely lost. */
    private var lostWhileWaiting = false

    /** Recent chats, history and agent config on the phone: instant open, last chat offline. */
    private val cache = ChatCache(app, args.agentId)

    /** Messages to send once connected: typed offline, or one lost to a drop (resent once). */
    private val outbox = Outbox<SendOptions>()

    /** Last text/file message sent, for one automatic resend if the connection drops before the reply. */
    private var lastSent: Pair<String, SendOptions>? = null

    /** After Stop, the rest of the cut-off reply is ignored until the stream reconnects. */
    private var stoppingReply = false

    private var sentAt = 0L
    private var awaitingFirstToken = false

    private val pendingEvents = mutableListOf<Pair<String, Map<String, Any?>>>()

    /** Host analytics sink: ("medassist_<event>", params). Events before it's set are kept and replayed. */
    var analytics: ((String, Map<String, Any?>) -> Unit)? = null
        set(value) {
            field = value
            if (value != null && pendingEvents.isNotEmpty()) {
                pendingEvents.forEach { (n, p) -> value(n, p) }
                pendingEvents.clear()
            }
        }

    private fun track(event: String, vararg params: Pair<String, Any?>) {
        val name = "medassist_$event"
        val p = mapOf(*params) + ("scope" to (args.chatScope?.substringBefore(':') ?: "docassist"))
        analytics?.invoke(name, p) ?: pendingEvents.add(name to p)
    }

    init {
        // A different doctor (or a re-login) must not resume someone else's Matrix identity.
        // Compared by the doctor inside the token, not the token: the Eka token rotates every ~30 min
        val tokenFingerprint = AuthIdentity.of(args.authToken)
        if (prefs.lastDoctorId != tokenFingerprint) {
            prefs.clearUser()
            cache.clear()
            prefs.lastDoctorId = tokenFingerprint
        }
        if (args.forceNewChat) clearStoredSession()

        val ctx = buildContext(args.patient)

        client = MatrixClient(
            context = app,
            config = MatrixClientConfig(
                agentId = args.agentId,
                authToken = args.authToken,
                environment = args.environment,
                context = ctx,
                userId = prefs.userId,
            ),
            seeded = prefs.credentials(),
            onTokenExchange = ::onTokenExchange,
        )

        // Last agent config on the phone (saved by prefetch / the last chat), read before the first
        // frame: a small file, and reading it async let the shimmer flash for a frame or two.
        val cached = runCatching { cache.loadAgentConfigBlocking() }.getOrNull()
        if (cached != null) {
            client.primeAgentConfig(cached)
            applyAgentConfig(cached)
        }

        viewModelScope.launch { client.events.collect(::onEvent) }
        viewModelScope.launch { client.errors.collect(::onError) }
        track("chat_opened", "has_patient" to (args.patient != null), "cached_config" to (cached != null))
        viewModelScope.launch {
            // Greeting and pills are already on screen; the session starts now and the fresh
            // config replaces the cached one when it lands.
            if (cached != null) {
                startInitialSession()
                client.fetchAgentConfig()?.let { fresh ->
                    applyAgentConfig(fresh)
                    cache.saveAgentConfig(fresh)
                }
            } else {
                val cfg = client.getAgentConfig()
                applyAgentConfig(cfg)
                cfg?.let { cache.saveAgentConfig(it) }
                startInitialSession()
            }
        }
        // Typed on the host's screen before opening: shows at once, sends when the chat is live
        args.initialMessage?.takeIf { it.isNotBlank() }?.let { text ->
            track("initial_message")
            queueMessage(SendOptions(message = text.trim()))
        }
    }

    // ================================================================== session

    private fun applyAgentConfig(cfg: AgentConfig?) = _state.update {
        it.copy(
            agentConfig = cfg ?: it.agentConfig,
            title = args.title.ifBlank { cfg?.theme?.title ?: cfg?.name ?: "DocAssist" },
            initialMessage = it.initialMessage ?: cfg?.initialMessage,
        )
    }

    private fun startInitialSession() {
        if (args.sessionId != null) {
            startSession(sessionId = args.sessionId)
            return
        }
        // Opened for a specific patient but the stored chat is about someone else (or no one):
        // start a fresh chat for this patient instead of resuming the wrong one.
        val stored = storedSessionId
        val wanted = args.patient
        if (stored != null && wanted != null && prefs.patientFor(stored)?.let { samePatient(it, wanted) } != true) {
            startSession(forceNewSession = true)
            return
        }
        startSession()
    }

    private fun samePatient(a: MedAssistPatient, b: MedAssistPatient) =
        if (a.oid != null && b.oid != null) a.oid == b.oid else a.name == b.name

    /** Show (and use as context for later new chats) the patient this chat was started with. */
    private fun restorePatient(sessionId: String, preferHost: Boolean) {
        val patient = if (preferHost && args.patient != null) args.patient else prefs.patientFor(sessionId)
        if (patient == _state.value.patient) return
        _state.update { it.copy(patient = patient) }
        client.updateContext(buildContext(patient))
    }

    /**
     * One session switch at a time: a newer one (new chat, history pick, patient change) cancels
     * the one in flight, so a slow earlier switch can't land on the old chat afterwards.
     */
    private var sessionJob: Job? = null

    private fun switchSession(block: suspend () -> Unit) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch { block() }
    }

    /** Reconnect the current chat, unless a switch is already connecting one. */
    private fun reconnectCurrent() {
        if (sessionJob?.isActive == true) return
        sessionJob = viewModelScope.launch { startSessionCore(_state.value.sessionId ?: storedSessionId) }
    }

    fun startSession(forceNewSession: Boolean = false, sessionId: String? = null) {
        switchSession {
            if (forceNewSession) {
                _state.update {
                    it.copy(connection = ConnectionStatus.CONNECTING, messages = emptyList(), elicitations = emptyMap(),
                        sessionId = null, isStreaming = false, isWaitingForResponse = false, progressMessage = null)
                }
                clearStoredSession()
                outbox.clear()
                lastSent = null
                stoppingReply = false
                client.endSession()
                resetConnectionStates()
                fileUploadsThisSession = 0
                startSessionCore(null)
                return@switchSession
            }
            val active = _state.value.sessionId ?: storedSessionId
            val target = sessionId ?: active
            if (target != _state.value.sessionId) {
                outbox.clear()
                lastSent = null
                stoppingReply = false
                client.endSession()
                _state.update { it.copy(messages = emptyList(), elicitations = emptyMap()) }
                fileUploadsThisSession = 0
                if (target != null) {
                    // Resuming keeps the chat's patient chip; a history pick shows that chat's patient
                    restorePatient(target, preferHost = sessionId == null)
                    loadSessionMessages(target)
                }
            }
            startSessionCore(target)
        }
    }

    private suspend fun startSessionCore(existing: String?) {
        if (_state.value.connection != ConnectionStatus.CONNECTED) {
            _state.update { it.copy(connection = ConnectionStatus.CONNECTING) }
        }
        try {
            val t0 = System.currentTimeMillis()
            val session = client.startSession(existing)
            track("session_started", "resumed" to (existing != null), "ms" to (System.currentTimeMillis() - t0))
            storedSessionId = session.sessionId
            prefs.savePatient(session.sessionId, _state.value.patient)
            session.userId?.let { prefs.userId = it }
            _state.update {
                it.copy(
                    sessionId = session.sessionId,
                    initialMessage = session.initialMessage ?: it.initialMessage,
                )
            }
            session.error?.msg?.let { _toasts.tryEmit(it) }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e // superseded by a newer switch
            if (e is MatrixApiException && e.reason != null) return
            if ((e as? MatrixApiException)?.status == 404) {
                _state.update { it.copy(error = null, connection = ConnectionStatus.NOT_CONNECTED, startNewConnection = true) }
            } else {
                _state.update {
                    it.copy(connection = ConnectionStatus.NOT_CONNECTED, error = UiError("Connection Lost!", errorText(e)), showRetry = true)
                }
            }
        }
    }

    /** Server transcript of a past session, flattened the same way as the widget's restoreTranscript. */
    private suspend fun loadSessionMessages(sessionId: String) {
        // The phone's copy shows at once (also offline); the server's replaces it when it lands
        val cached = cache.loadMessages(sessionId)
        if (cached.isNullOrEmpty()) _state.update { it.copy(isLoadingSessionMessages = true) }
        else _state.update { it.copy(messages = cached) }
        try {
            val stored = client.sessionMessages(sessionId)
            val (messages, entries) = restoreTranscript(stored)
            if (messages.isNotEmpty() || cached.isNullOrEmpty()) {
                _state.update { it.copy(messages = messages, elicitations = it.elicitations + entries) }
                cache.saveMessages(sessionId, messages)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
        } finally {
            _state.update { it.copy(isLoadingSessionMessages = false) }
        }
    }

    fun startNewChat() {
        track("new_chat")
        startSession(forceNewSession = true)
    }

    /**
     * Attach, change or remove (null) the patient. Matrix fixes context when a session is
     * created, so this starts a new chat carrying the patient; the old chat stays in history.
     */
    fun attachPatient(patient: MedAssistPatient?) {
        if (patient == _state.value.patient) return
        track(if (patient != null) "patient_attached" else "patient_removed")
        _state.update { it.copy(patient = patient) }
        client.updateContext(buildContext(patient))
        startNewChat()
    }

    /**
     * Session context: the host's extra context plus the patient as `profile`. A scribe
     * snapshot's `patient` field (ekascribe-web shape) follows the attached patient too.
     */
    private fun buildContext(patient: MedAssistPatient?): JsonObject? {
        val extra = args.extraContext
        if (patient == null && extra == null) return null
        return buildJsonObject {
            extra?.forEach { (k, v) -> put(k, v) }
            if (extra?.containsKey("patient") == true) {
                put("patient", patient?.let {
                    buildJsonObject {
                        put("name", it.name)
                        it.age?.let { a -> put("age", a) }
                        it.sex?.let { s -> put("sex", s) }
                    }
                } ?: JsonNull)
            }
            if (patient != null) put("profile", buildJsonObject {
                put("name", patient.name)
                patient.age?.let { put("age", it) }
                patient.sex?.let { put("gender", it) }
                patient.oid?.let { put("oid", it) }
            })
        }
    }

    fun openPastSession(sessionId: String) = startSession(sessionId = sessionId)

    fun loadPastSessions() {
        track("history_opened")
        viewModelScope.launch {
            _state.update { it.copy(isLoadingHistory = true) }
            if (_state.value.pastSessions.isEmpty()) {
                cache.loadPastSessions()?.let { cached -> _state.update { it.copy(pastSessions = cached) } }
            }
            val list = try { client.pastSessions() } catch (e: CancellationException) { throw e } catch (_: Throwable) { null }
            if (list != null) {
                _state.update { it.copy(pastSessions = list) }
                cache.savePastSessions(list)
            }
            _state.update { it.copy(isLoadingHistory = false) }
        }
    }

    private fun persistChat() {
        val sid = _state.value.sessionId ?: return
        val messages = _state.value.messages
        viewModelScope.launch { cache.saveMessages(sid, messages) }
    }

    fun onForeground() = client.onForeground()

    /** The chat to resume next time, kept per [MedAssistArgs.chatScope]. */
    private var storedSessionId: String?
        get() = prefs.sessionId(args.chatScope)
        set(value) = prefs.setSessionId(args.chatScope, value)

    /** The live chat can be replaced by the client when the server drops it; keep our id, prefs and patient on it. */
    private fun syncSessionId() {
        val live = client.sessionId ?: return
        val shown = _state.value.sessionId ?: return
        if (live == shown) return
        MedAssistLogger.d("MedAssist", "live chat replaced by the server: ${shown.take(8)} -> ${live.take(8)}")
        storedSessionId = live
        prefs.savePatient(live, _state.value.patient)
        _state.update { it.copy(sessionId = live) }
    }

    private fun clearStoredSession() = prefs.clearSession(args.chatScope)

    private fun onTokenExchange(t: TokenExchangeResponse) {
        val previous = prefs.userId
        val isNewLogin = t.userId != null && previous != null && t.userId != previous
        if (isNewLogin || t.userIdChangeDetected == true) {
            prefs.clearAllSessions()
            cache.clear()
            _state.update { it.copy(messages = emptyList(), elicitations = emptyMap(), sessionId = null) }
        }
        prefs.saveTokens(t)
    }

    // ================================================================== errors

    private fun onError(e: MatrixError) {
        track("error", "kind" to e.code.name.lowercase(), "status" to e.status)
        if (!isOnline()) {
            _state.update {
                it.copy(connection = ConnectionStatus.NOT_CONNECTED,
                    error = UiError("No internet connection", "Will reconnect automatically when internet is restored."))
            }
            return
        }
        when (e.reason) {
            RecoveryReason.SESSION_FORBIDDEN -> {
                if (prefs.level != "user") {
                    prefs.clearUser()
                    cache.clear()
                    client.clearTokens()
                    handleStartNewConnection()
                } else handleTerminalAuthFailure()
                return
            }
            RecoveryReason.AUTH_EXPIRED -> { handleTerminalAuthFailure(); return }
            RecoveryReason.SESSION_NOT_FOUND -> {
                _state.update { it.copy(error = null, connection = ConnectionStatus.NOT_CONNECTED, startNewConnection = true) }
                return
            }
            RecoveryReason.RETRY_LATER -> { _state.update { it.copy(connection = ConnectionStatus.CONNECTING) }; return }
            null -> Unit
        }
        when (e.code) {
            MatrixErrorCode.API, MatrixErrorCode.SESSION -> if (e.status == 404) {
                _state.update { it.copy(startNewConnection = true) }
            } else {
                _state.update {
                    it.copy(connection = ConnectionStatus.NOT_CONNECTED, error = UiError("Something went wrong!", e.message), showRetry = true)
                }
            }
            MatrixErrorCode.RECORDING -> Unit
            MatrixErrorCode.CONNECTION -> if (!client.isConnected()) {
                _state.update { it.copy(connection = ConnectionStatus.NOT_CONNECTED) }
                if (retryAttempts < 2) {
                    retryAttempts++
                    reconnectCurrent()
                } else {
                    _state.update { it.copy(showRetry = true, error = UiError("Trying to connect...", e.message)) }
                }
            }
            MatrixErrorCode.FILE, MatrixErrorCode.MESSAGE -> _state.update { it.copy(error = UiError("Something went wrong", e.message)) }
            else -> _state.update { it.copy(error = UiError("Something went wrong", e.message), showRetry = true) }
        }
    }

    private fun handleTerminalAuthFailure() {
        prefs.clearUser()
        cache.clear()
        _state.update {
            it.copy(connection = ConnectionStatus.NOT_CONNECTED, authExpired = true,
                error = UiError("Session expired", "Your session has expired. Please close and reopen DocAssist."))
        }
    }

    private fun handleStartNewConnection() {
        resetConnectionStates()
        clearStoredSession()
        startSession(forceNewSession = true)
    }

    private fun resetConnectionStates() {
        retryAttempts = 0
        _state.update { it.copy(showRetry = false, startNewConnection = false, error = null) }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    fun retry() {
        viewModelScope.launch {
            if (!client.isConnected()) {
                _state.update { it.copy(connection = ConnectionStatus.NOT_CONNECTED) }
                when {
                    retryAttempts < 2 -> { retryAttempts++; reconnectCurrent() }
                    retryAttempts == 2 -> { retryAttempts++; _state.update { it.copy(showRetry = true, error = UiError("Trying to connect...")) } }
                    else -> _state.update { it.copy(error = UiError("Something went wrong!", "Please start a new chat."), startNewConnection = true) }
                }
            } else {
                _state.update { it.copy(connection = ConnectionStatus.CONNECTED, error = null, showRetry = false) }
                val last = _state.value.messages.lastOrNull()
                if (last != null && last.role == Role.USER && !last.isAudio) {
                    sendMessage(SendOptions(message = last.content, files = last.files.ifEmpty { null }, shownMessageId = last.id))
                }
            }
        }
    }

    // ================================================================== realtime events

    private fun onEvent(event: MatrixEvent) {
        if (stoppingReply && (event is MatrixEvent.MessageChunk || event == MatrixEvent.EndOfStream ||
                event is MatrixEvent.ToolCall || event is MatrixEvent.Progress || event is MatrixEvent.Tips ||
                event is MatrixEvent.ToolStart || event is MatrixEvent.ToolEnd)
        ) return
        // Any answer to a voice message ends its "transcribing" state
        when (event) {
            is MatrixEvent.MessageChunk, MatrixEvent.EndOfStream, is MatrixEvent.ToolCall,
            is MatrixEvent.AudioTranscript -> clearTranscribing()
            // e.g. "invalid_content: Invalid audio format" - the clip was rejected, say so
            is MatrixEvent.RealtimeError -> failVoiceMessage(event.message)
            MatrixEvent.Offline -> failVoiceMessage("You went offline before it was transcribed.")
            else -> Unit
        }
        when (event) {
            is MatrixEvent.MessageChunk -> onChunk(event)
            MatrixEvent.EndOfStream -> {
                streamIdleJob?.cancel()
                hadStreamBreak = false
                _state.update { it.copy(isStreaming = false, progressMessage = null) }
                setWaiting(false)
                if (sentAt > 0) track("reply_done", "ms" to (System.currentTimeMillis() - sentAt))
                sentAt = 0
                lastSent = null
                persistChat()
                flushOutbox()
            }
            MatrixEvent.Connected -> {
                syncSessionId()
                resetConnectionStates()
                _state.update { it.copy(connection = ConnectionStatus.CONNECTED, offline = false) }
                stoppingReply = false
                if (lostWhileWaiting) {
                    lostWhileWaiting = false
                    val lost = lastSent
                    if (lost != null && outbox.addRetry(lost.first, lost.second.copy(shownMessageId = lost.first))) {
                        // Resend the lost message once, on its own
                        markDelivery(lost.first, Delivery.QUEUED)
                        track("auto_resend")
                    } else {
                        lost?.first?.let { markDelivery(it, Delivery.FAILED) }
                        _state.update {
                            it.copy(error = UiError("Message not delivered", "You went offline before the reply. Tap Retry to send it again."), showRetry = true)
                        }
                    }
                }
                flushOutbox()
            }
            MatrixEvent.Offline -> {
                val s = _state.value
                lostWhileWaiting = lostWhileWaiting || s.isWaitingForResponse || s.isStreaming ||
                    s.messages.lastOrNull()?.role == Role.USER
                streamIdleJob?.cancel()
                hadStreamBreak = false
                _state.update {
                    it.copy(
                        connection = ConnectionStatus.NOT_CONNECTED, isStreaming = false, progressMessage = null, offline = true,
                        error = UiError("No internet connection", "Will reconnect automatically when internet is restored."),
                        showRetry = false,
                    )
                }
                setWaiting(false)
            }
            MatrixEvent.Online -> _state.update { it.copy(connection = ConnectionStatus.CONNECTING, offline = false, error = null) }
            is MatrixEvent.ToolCall -> onToolCall(event)
            is MatrixEvent.Disconnected -> onDisconnected(event)
            is MatrixEvent.RealtimeError -> onRealtimeError(event.code)
            is MatrixEvent.AudioTranscript -> onAudioTranscript(event.text)
            is MatrixEvent.Progress -> {
                hadStreamBreak = true
                setWaiting(false)
                _state.update { it.copy(progressMessage = event.text) }
            }
            is MatrixEvent.Tips -> _state.update { it.copy(tips = event.tips) }
            is MatrixEvent.ToolStart -> setToolStatus(event.messageId, "Calling tool: ${event.name ?: ""}...", event.name)
            is MatrixEvent.ToolEnd -> setToolStatus(event.messageId, "Tool call completed.", event.name)
        }
    }

    private fun onChunk(e: MatrixEvent.MessageChunk) {
        if (awaitingFirstToken && sentAt > 0) {
            awaitingFirstToken = false
            track("first_token", "ms" to (System.currentTimeMillis() - sentAt))
        }
        if (!_state.value.isStreaming) {
            _state.update { it.copy(isStreaming = true, isWaitingForResponse = false) }
            responseTimeoutJob?.cancel()
            resetConnectionStates()
        }
        armStreamIdleTimer()
        if (_state.value.connection != ConnectionStatus.CONNECTED && client.isConnected()) {
            _state.update { it.copy(connection = ConnectionStatus.CONNECTED) }
        }
        val newSegment = hadStreamBreak
        hadStreamBreak = false
        _state.update { s ->
            val last = s.messages.lastOrNull()
            if (last != null && last.id == e.messageId && last.role == Role.ASSISTANT) {
                val sep = if (newSegment && last.content.isNotEmpty() && !last.content.last().isWhitespace() &&
                    !e.text.first().isWhitespace()
                ) "\n\n" else ""
                s.copy(messages = s.messages.dropLast(1) + last.copy(content = last.content + sep + e.text))
            } else {
                s.copy(messages = s.messages + ChatUiMessage(id = e.messageId, content = e.text, role = Role.ASSISTANT))
            }
        }
    }

    private fun armStreamIdleTimer() {
        streamIdleJob?.cancel()
        streamIdleJob = viewModelScope.launch {
            delay(STREAM_IDLE_TIMEOUT_MS)
            hadStreamBreak = false
            _state.update { it.copy(isStreaming = false, progressMessage = null, isWaitingForResponse = false) }
        }
    }

    private fun setWaiting(waiting: Boolean) {
        responseTimeoutJob?.cancel()
        _state.update { it.copy(isWaitingForResponse = waiting) }
        if (waiting) {
            responseTimeoutJob = viewModelScope.launch {
                delay(RESPONSE_TIMEOUT_MS)
                if (!_state.value.isStreaming) _state.update { it.copy(isWaitingForResponse = false) }
            }
        }
    }

    private fun setToolStatus(messageId: String, status: String, name: String?) {
        hadStreamBreak = true
        if (name == null) return
        _state.update { s ->
            val last = s.messages.lastOrNull()
            if (last != null && last.id == messageId && last.role == Role.ASSISTANT) {
                s.copy(messages = s.messages.dropLast(1) + last.copy(toolCallStatus = status))
            } else s
        }
    }

    private fun onDisconnected(e: MatrixEvent.Disconnected) {
        if (e.details.reason == DisconnectReason.CLIENT_CLOSED) return
        _state.update { it.copy(connection = ConnectionStatus.NOT_CONNECTED) }
        if (!isOnline()) {
            _state.update {
                it.copy(error = UiError("No internet connection", "Will reconnect automatically when internet is restored."), showRetry = false)
            }
            return
        }
        if (retryAttempts < 2) {
            retryAttempts++
            _state.update { it.copy(showRetry = true, error = UiError("Something went wrong!", "Please try again.")) }
        } else {
            _state.update { it.copy(error = UiError("Something went wrong!", "Please start a new Chat."), startNewConnection = true) }
        }
    }

    private fun onRealtimeError(code: String?) {
        track("error", "kind" to "realtime", "code" to code)
        when (code) {
            RealtimeErrorCodes.SESSION_INACTIVE -> _state.update { it.copy(startNewConnection = true) }
            RealtimeErrorCodes.SESSION_TOKEN_MISMATCH -> _state.update {
                it.copy(error = UiError("Session not found", "Please start a new chat"), startNewConnection = true)
            }
            RealtimeErrorCodes.SESSION_NOT_OWNED -> handleStartNewConnection()
            RealtimeErrorCodes.INVALID_EVENT, RealtimeErrorCodes.INVALID_CONTENT_TYPE -> Unit
            RealtimeErrorCodes.PARSING_ERROR -> if (retryAttempts < 1) {
                retryAttempts++
                retry()
            } else {
                _state.update { it.copy(error = UiError("Error parsing request", "please try again"), showRetry = true) }
            }
            RealtimeErrorCodes.TIMEOUT -> _state.update {
                it.copy(error = UiError("Request timed out", "please try again"), showRetry = true)
            }
            else -> _state.update { it.copy(error = UiError("Something went wrong", "please try again"), showRetry = true) }
        }
        _state.update { it.copy(isStreaming = false, progressMessage = null) }
        setWaiting(false)
    }

    // ================================================================== tool calls (elicitations)

    private fun onToolCall(e: MatrixEvent.ToolCall) {
        hadStreamBreak = true
        val details = e.data.details
        val toolId = e.data.toolId ?: return
        val component = details?.component ?: return
        if (_state.value.isWaitingForResponse) setWaiting(false)
        val messageId = e.messageId
        val toolKey = "$messageId::$toolId"
        val existing = _state.value.elicitations[toolKey]
        val meta = details.meta
        val toolResult = meta?.toolResult
        val mergedInput = DoctorMerge.mergeInput(existing?.input, details.input, component)
        val required = meta?.schema?.required.orEmpty()
        val prop = meta?.schema?.properties?.get(component)

        val entry: ElicitationEntry = when (details.status) {
            ElicitationStatus.PROGRESS -> {
                val serverInputs = details.input ?: JsonObject(emptyMap())
                val filtered = required.mapNotNull { f -> serverInputs[f]?.let { f to it } }.toMap()
                val mergedServer = (existing?.serverInput ?: emptyMap()) + filtered
                ElicitationEntry(
                    toolId = toolId, messageId = messageId, component = component,
                    active = true, disabled = false, input = mergedInput, serverInput = mergedServer,
                    required = required, pattern = prop?.pattern, minLength = prop?.minLength, maxLength = prop?.maxLength,
                    dispToastMsg = meta?.dispToastMsg, description = prop?.description, toolResult = toolResult,
                    orientation = meta?.orientation, mcpMetaFields = meta?.mcpMetaFields.orEmpty(),
                    toolSelectionNames = required.filter { mergedServer[it].isEmptyValue() },
                    hideShowMore = existing?.hideShowMore ?: meta?.hideShowMore,
                )
            }
            ElicitationStatus.SUCCESS -> (existing ?: ElicitationEntry(toolId, messageId)).copy(
                toolId = toolId, messageId = messageId, component = component, disabled = true,
                active = MatrixComponents.staysActive(component), input = mergedInput, toolResult = toolResult, sendToLlm = true,
            )
            ElicitationStatus.FAILURE -> (existing ?: ElicitationEntry(toolId, messageId, component = component)).copy(
                active = false, disabled = true, toolResult = toolResult,
                responseLabel = meta?.dispMessage ?: "Tool call failed",
            )
            else -> ElicitationEntry(
                toolId = toolId, messageId = messageId, component = component, active = false, disabled = false,
                toolResult = toolResult, input = mergedInput,
            )
        }
        _state.update { it.copy(elicitations = it.elicitations + (toolKey to entry)) }
        meta?.dispToastMsg?.takeIf { it.isNotBlank() }?.let { _toasts.tryEmit(it) }

        val userHint = meta?.userHint
        _state.update { s ->
            val idx = s.messages.indexOfLast { it.id == messageId }
            if (idx == -1) {
                s.copy(messages = s.messages + ChatUiMessage(
                    id = messageId, content = details.input?.str("text").orEmpty(), role = Role.ASSISTANT,
                    toolKeys = listOf(toolKey), isResponseFromTool = true, userHint = userHint,
                ))
            } else {
                val m = s.messages[idx]
                val updated = m.copy(isResponseFromTool = true, toolKeys = (m.toolKeys + toolKey).distinct(), userHint = userHint ?: m.userHint)
                s.copy(messages = s.messages.toMutableList().also { it[idx] = updated })
            }
        }

        // Forward the outcome to the LLM: either the tool is done (inactive) or SUCCESS flagged send_to_llm.
        val latest = _state.value.elicitations[toolKey]
        val disp = meta?.dispMessage
        if ((latest?.active != true || latest.sendToLlm) && (!disp.isNullOrEmpty() || !toolResult.isNullOrEmpty())) {
            sendMessage(SendOptions(message = disp.orEmpty(), toolResult = toolResult, toolCalled = true, toolKey = toolKey))
        }
    }

    /** Port of useChat.sendMessage: routes an elicitation response to callTool, everything else to the chat. */
    fun sendMessage(options: SendOptions) {
        viewModelScope.launch { sendMessageInternal(options) }
    }

    private suspend fun sendMessageInternal(o: SendOptions) {
        val s0 = _state.value
        val anyActive = s0.elicitations.values.any { it.active }
        if (!o.toolCalled && o.files.isNullOrEmpty() && o.message.isNullOrBlank() && o.audio == null &&
            o.selectedComponent == null && !anyActive && o.toolResult.isNullOrEmpty()
        ) return

        o.files?.takeIf { it.isNotEmpty() }?.let { files ->
            validateBatch(files)?.let { _state.update { s -> s.copy(error = it) }; return }
            if (files.size + fileUploadsThisSession > MatrixFile.MAX_FILES_PER_SESSION) {
                val left = MatrixFile.MAX_FILES_PER_SESSION - fileUploadsThisSession
                _state.update {
                    it.copy(error = UiError("You can only upload up to ${MatrixFile.MAX_FILES_PER_SESSION} files per session",
                        if (left <= 0) "Maximum file upload limit reached" else "You have $left file upload${if (left == 1) "" else "s"} left"))
                }
                return
            }
            fileUploadsThisSession += files.size
        }
        _state.update { it.copy(error = null, showRetry = false) }
        if (client.isConnected()) _state.update { it.copy(connection = ConnectionStatus.CONNECTED) }

        val messages = _state.value.messages
        val prevContext = if (messages.size <= 2 && initialPrompts.size == 1) initialPrompts else null
        var isElicitation = false
        var toolCallErrorMessage: String? = null
        val lastMessage = messages.lastOrNull()
        val esMap = _state.value.elicitations
        val currentKey = o.toolKey
        val es = currentKey?.let { esMap[it] }

        val isList = es?.component == MatrixComponents.DOCTOR_CARD || es?.component == MatrixComponents.CARD
        val canCallTool = if (isList) es != null && o.toolResult == null && es.active else es?.active == true
        val shouldNotCallTool = !o.files.isNullOrEmpty() || o.audio != null || o.toolDeclined || !canCallTool || !o.toolCalled
        val pillLabel = (o.selectedComponent?.get("label") as? JsonPrimitive)?.contentOrNull
        val pillMessage = if (es?.component == MatrixComponents.PILL) pillLabel else null

        if (!shouldNotCallTool && currentKey != null && es != null) {
            val fieldKeys = es.toolSelectionNames.ifEmpty { es.required.filter { es.serverInput[it].isEmptyValue() } }
            val selected = LinkedHashMap<String, JsonElement>()
            fieldKeys.forEach { key ->
                val sc = o.selectedComponent
                if (sc != null) sc[key]?.let { selected[key] = it }
                else if (fieldKeys.size == 1) selected[key] = JsonPrimitive(o.message)
            }
            val mcpMeta = LinkedHashMap<String, JsonElement>()
            es.mcpMetaFields.forEach { f ->
                if (f in fieldKeys) selected[f]?.let { mcpMeta[f] = it }
                else es.serverInput[f]?.takeUnless { it.isEmptyValue() }?.let { mcpMeta[f] = it }
                if (o.resend && "is_resend" !in mcpMeta) mcpMeta["is_resend"] = JsonPrimitive(true)
            }
            val filteredInputs = es.serverInput.filter { (k, v) ->
                val notMeta = k !in mcpMeta
                val has = !v.isEmptyValue()
                if (o.resend) notMeta && has else notMeta && k in es.required && has
            }
            val newValues = LinkedHashMap<String, JsonElement>()
            if (!o.resend) {
                es.required.ifEmpty { fieldKeys }.forEach { k ->
                    if (k in mcpMeta) return@forEach
                    (selected[k] ?: o.selectedComponent?.get(k))?.let { newValues[k] = it }
                }
            }
            val params = LinkedHashMap<String, JsonElement>()
            params.putAll(filteredInputs)
            params.putAll(newValues)
            if (mcpMeta.isNotEmpty()) params["mcp_meta_fields"] = JsonObject(mcpMeta)
            if (params.isEmpty()) return
            upsert(currentKey) { it.copy(serverInput = it.serverInput + params) }
            try {
                if (!client.isConnected()) throw IllegalStateException("Not connected to the server")
                client.callTool(es.toolId, es.messageId, JsonObject(params))
                isElicitation = true
            } catch (t: Throwable) {
                val msg = errorText(t)
                val tr = buildJsonObject {
                    es.toolResult?.forEach { (k, v) -> put(k, v) }
                    put("error", "Tool call failed: $msg")
                }
                client.sendMessage(OutgoingRequest(toolResult = tr, toolId = es.toolId))
                toolCallErrorMessage = "Tool call failed"
                upsert(currentKey) { it.copy(active = false, disabled = true, responseLabel = "Tool call failed: $msg") }
            }
        } else {
            val lastId = lastMessage?.id
            val activeForLast = lastId != null && esMap.any { (k, v) -> k.substringBefore("::") == lastId && v.active }
            val resolvedToolResult = o.toolResult ?: es?.toolResult
            val declined = o.toolDeclined || activeForLast
            client.sendMessage(
                OutgoingRequest(
                    message = o.message.orEmpty(),
                    files = o.files?.ifEmpty { null },
                    audio = o.audio,
                    initialPrompts = prevContext,
                    toolDeclined = declined,
                    // tool_id lets the server match a tool answer (auto-forward after success,
                    // card buttons with their own tool_result, declines) to its elicitation.
                    toolId = es?.toolId,
                    toolResult = resolvedToolResult,
                )
            )
        }

        if (!o.message.isNullOrEmpty() || !o.files.isNullOrEmpty() || o.audio != null || pillMessage != null || toolCallErrorMessage != null) {
            if (!isElicitation) {
                val bubbleId = o.shownMessageId ?: System.currentTimeMillis().toString()
                if (o.shownMessageId != null) {
                    markDelivery(bubbleId, Delivery.SENT)
                } else {
                    _state.update { s ->
                        val prefix = if (s.messages.isEmpty() && !s.initialMessage?.text.isNullOrBlank())
                            listOf(ChatUiMessage(INITIAL_BOT_MESSAGE_ID, s.initialMessage?.text.orEmpty(), Role.ASSISTANT)) else emptyList()
                        s.copy(messages = s.messages + prefix + ChatUiMessage(
                            id = bubbleId,
                            content = pillMessage ?: toolCallErrorMessage ?: o.message.orEmpty(),
                            role = Role.USER,
                            files = o.files.orEmpty(),
                            isAudio = o.audio != null,
                        ))
                    }
                }
                // Kept for one automatic resend if the connection drops before the reply
                lastSent = if (o.audio == null && !o.toolCalled && pillMessage == null) bubbleId to o.copy(shownMessageId = null) else null
                sentAt = System.currentTimeMillis()
                awaitingFirstToken = true
                track(
                    "message_sent",
                    "type" to when { o.audio != null -> "voice"; !o.files.isNullOrEmpty() -> "attachment"; else -> "text" },
                    "files" to (o.files?.size ?: 0),
                    "queued" to (o.shownMessageId != null),
                )
                setWaiting(true)
                persistChat()
            }
        }

        val keys = linkedSetOf<String>()
        currentKey?.let { keys += it }
        lastMessage?.toolKeys?.let { keys += it }
        keys.forEach { k ->
            upsert(k) { latest ->
                var u = latest
                if (shouldNotCallTool) u = u.copy(active = false, disabled = true)
                if (k == currentKey) u = u.copy(responseLabel = pillMessage ?: o.message.orEmpty())
                u
            }
        }
    }

    /** Suggestion chip from the initial message: canned response locally when one is configured. */
    fun onInitialSuggestion(value: String) {
        val initial = _state.value.initialMessage ?: return
        val picked = initial.suggestions?.firstOrNull { it.value == value || it.label == value }
        initialPrompts = listOf("assistant" to "${initial.text.orEmpty()} ${picked?.response.orEmpty()}")
        if (picked?.response.isNullOrBlank()) {
            sendMessage(SendOptions(message = picked?.value ?: value))
            return
        }
        viewModelScope.launch {
            appendMessage(ChatUiMessage(System.currentTimeMillis().toString(), picked?.label ?: value, Role.USER))
            setWaiting(true)
            delay(1000)
            setWaiting(false)
            appendMessage(ChatUiMessage(System.currentTimeMillis().toString(), picked?.response.orEmpty(), Role.ASSISTANT))
        }
    }

    fun clearElicitation(toolKey: String, text: String? = null) {
        val entry = _state.value.elicitations[toolKey] ?: return
        viewModelScope.launch {
            try {
                sendMessageInternal(SendOptions(message = text ?: "User aborted elicitation", toolDeclined = true, toolResult = entry.toolResult, toolKey = toolKey))
            } finally {
                upsert(toolKey) { it.copy(active = false, disabled = true) }
            }
        }
    }

    // ================================================================== feedback

    fun sendMessageFeedback(messageId: String, feedback: String, reason: String? = null) {
        track("feedback", "value" to feedback)
        _state.update { s -> s.copy(messages = s.messages.map { if (it.id == messageId) it.copy(feedback = feedback) else it }) }
        if (!client.isConnected()) return
        viewModelScope.launch { try { client.sendMessageFeedback(messageId, feedback, reason) } catch (_: Throwable) {} }
    }

    /** Returns true when the exit-feedback form should be shown instead of closing. */
    fun onCloseRequested(): Boolean {
        val cfg = _state.value.agentConfig?.feedback ?: return false
        val hasConversation = _state.value.messages.any { it.role == Role.USER }
        if (exitFeedbackShown || !hasConversation || !client.isConnected()) return false
        exitFeedbackShown = true
        _state.update { it.copy(showExitFeedback = cfg.type.isNotBlank()) }
        return true
    }

    fun submitSessionFeedback(feedbacks: List<String>?, nps: Int?, onDone: () -> Unit) {
        viewModelScope.launch {
            try { client.sendSessionFeedback(feedbacks, nps) } catch (_: Throwable) {}
            _state.update { it.copy(showExitFeedback = false) }
            onDone()
        }
    }

    fun dismissExitFeedback() = _state.update { it.copy(showExitFeedback = false) }

    // ================================================================== attachments & recording

    fun addAttachments(files: List<MatrixFile>) {
        val current = _state.value.attachments
        val bad = files.firstOrNull { !MatrixFile.isValid(it) }
        if (bad != null) {
            val title = if (bad.mimeType !in MatrixFile.ALLOWED_TYPES) "File type not supported: ${bad.name}" else "File size limit of 3MB exceeded"
            val desc = if (bad.mimeType !in MatrixFile.ALLOWED_TYPES) "Only PDF, images (JPG, JPEG, PNG) are allowed." else "Please reduce the size of the file or try again with a smaller file"
            _state.update { it.copy(error = UiError(title, desc)) }
        }
        val valid = files.filter(MatrixFile::isValid)
        validateBatch(valid, current.size)?.let { err -> _state.update { it.copy(error = err) }; return }
        _state.update { it.copy(attachments = current + valid) }
    }

    /** Files the doctor can still add to this message (3 per message, 5 per chat). */
    fun attachmentSlotsLeft(): Int {
        val attached = _state.value.attachments.size
        return minOf(
            MatrixFile.MAX_FILES_PER_MESSAGE - attached,
            MatrixFile.MAX_FILES_PER_SESSION - fileUploadsThisSession - attached,
        ).coerceAtLeast(0)
    }

    /** Reports that no more files fit; true when the picker shouldn't open. */
    fun checkAttachmentRoom(): Boolean {
        if (attachmentSlotsLeft() > 0) return true
        val perSession = MatrixFile.MAX_FILES_PER_SESSION - fileUploadsThisSession - _state.value.attachments.size <= 0
        _toasts.tryEmit(
            if (perSession) "This chat already has ${MatrixFile.MAX_FILES_PER_SESSION} files. Start a new chat to add more."
            else "You can attach up to ${MatrixFile.MAX_FILES_PER_MESSAGE} files per message."
        )
        return false
    }

    /** Shows a spinner on + while picked files are read / compressed. */
    fun setPreparingAttachments(preparing: Boolean) = _state.update { it.copy(preparingAttachments = preparing) }

    /** Adds what fits and says what didn't (unreadable, or over the per-message limit). */
    fun addPickedAttachments(files: List<MatrixFile>, unreadable: Int = 0, source: String = "files") {
        track("attachment_added", "source" to source, "count" to files.size, "failed" to unreadable)
        val room = attachmentSlotsLeft()
        val fits = files.take(room)
        if (fits.isNotEmpty()) addAttachments(fits)
        val dropped = files.size - fits.size
        when {
            dropped > 0 -> _toasts.tryEmit("Added ${fits.size} of ${files.size}. You can attach up to ${MatrixFile.MAX_FILES_PER_MESSAGE} files per message.")
            unreadable > 0 -> _toasts.tryEmit(if (unreadable == 1) "Couldn't add one file. It may be unsupported or too large." else "Couldn't add $unreadable files. They may be unsupported or too large.")
        }
    }

    fun removeAttachment(file: MatrixFile) = _state.update { it.copy(attachments = it.attachments - file) }

    /** Composer send: text and/or attachments. */
    /** Composer send; false (text stays in the box) when it can't go out right now. */
    fun sendComposer(text: String, toolKey: String? = null, toolCalled: Boolean = false): Boolean {
        val atts = _state.value.attachments
        if (_state.value.isComposerDisabled) return false
        val options = when {
            atts.isNotEmpty() -> SendOptions(message = text.trim(), files = atts)
            text.isNotBlank() -> SendOptions(message = text.trim(), toolKey = toolKey, toolCalled = toolCalled)
            else -> return false
        }
        if (atts.isNotEmpty()) _state.update { it.copy(attachments = emptyList()) }
        if (isLive()) sendMessage(options) else queueMessage(options)
        return true
    }

    private fun isLive() = isOnline() && _state.value.connection == ConnectionStatus.CONNECTED && client.isConnected()

    /** Offline / connecting: show the message as "Waiting to send" and send it once connected. */
    private fun queueMessage(o: SendOptions) {
        val id = "local-${System.currentTimeMillis()}"
        _state.update { s ->
            val prefix = if (s.messages.isEmpty() && !s.initialMessage?.text.isNullOrBlank())
                listOf(ChatUiMessage(INITIAL_BOT_MESSAGE_ID, s.initialMessage?.text.orEmpty(), Role.ASSISTANT)) else emptyList()
            s.copy(
                messages = s.messages + prefix + ChatUiMessage(
                    id = id, content = o.message.orEmpty(), role = Role.USER, files = o.files.orEmpty(), delivery = Delivery.QUEUED,
                ),
                offline = s.offline || !isOnline(),
            )
        }
        outbox.add(id, o.copy(shownMessageId = id))
        track("message_queued", "outbox" to outbox.size)
    }

    /** Next waiting message, once connected and the previous reply is done. */
    private fun flushOutbox() {
        val s = _state.value
        if (outbox.isEmpty() || !isLive() || s.isStreaming || s.isWaitingForResponse) return
        val (_, options) = outbox.next() ?: return
        sendMessage(options)
    }

    private fun markDelivery(messageId: String, delivery: Delivery) = _state.update { s ->
        s.copy(messages = s.messages.map { if (it.id == messageId) it.copy(delivery = delivery) else it })
    }

    /** "Not sent - tap to retry" on a failed bubble. */
    fun retryMessage(message: ChatUiMessage) {
        if (message.role != Role.USER || message.isAudio) return
        track("retry_message")
        val options = SendOptions(message = message.content, files = message.files.ifEmpty { null }, shownMessageId = message.id)
        if (isLive() && !_state.value.isComposerDisabled) sendMessage(options)
        else {
            markDelivery(message.id, Delivery.QUEUED)
            outbox.add(message.id, options)
        }
    }

    /** Long-press "Resend": the same text/files again as a new message. */
    fun resend(message: ChatUiMessage) {
        if (message.role != Role.USER || message.isAudio || _state.value.isComposerDisabled) return
        track("resend")
        val options = SendOptions(message = message.content, files = message.files.ifEmpty { null })
        if (isLive()) sendMessage(options) else queueMessage(options)
    }

    fun trackEdit() = track("edit")

    /**
     * Stop: cut the reply short, keep what arrived, mark it stopped. Matrix has no stop event,
     * so the stream is closed and the same chat reconnected (the web SDK does the same).
     */
    fun stopReply() {
        if (!_state.value.canStop) return
        stoppingReply = true
        streamIdleJob?.cancel()
        responseTimeoutJob?.cancel()
        hadStreamBreak = false
        lostWhileWaiting = false
        lastSent = null
        _state.update { s ->
            val last = s.messages.lastOrNull()
            val messages = if (last != null && last.role == Role.ASSISTANT && s.isStreaming) s.messages.dropLast(1) + last.copy(stopped = true) else s.messages
            s.copy(messages = messages, isStreaming = false, isWaitingForResponse = false, progressMessage = null)
        }
        track("reply_stopped", "ms" to (if (sentAt > 0) System.currentTimeMillis() - sentAt else 0))
        sentAt = 0
        persistChat()
        switchSession { client.stopReply() }
    }

    // ================================================================== helpers

    private fun appendMessage(m: ChatUiMessage) = _state.update { it.copy(messages = it.messages + m) }

    private fun upsert(key: String, transform: (ElicitationEntry) -> ElicitationEntry) {
        _state.update { s ->
            val e = s.elicitations[key] ?: return@update s
            s.copy(elicitations = s.elicitations + (key to transform(e)))
        }
    }

    private fun validateBatch(files: List<MatrixFile>, alreadyAttached: Int = 0): UiError? {
        if (files.size + alreadyAttached > MatrixFile.MAX_FILES_PER_MESSAGE) {
            return UiError("You can only upload up to ${MatrixFile.MAX_FILES_PER_MESSAGE} files at a time", "Please remove some files and try again.")
        }
        if (files.sumOf { it.size } > MatrixFile.MAX_TOTAL_SIZE) {
            return UiError("Total file size limit of 10 MB exceeded", "Please reduce the size of the files or try again with less files")
        }
        return null
    }

    private fun restoreTranscript(stored: List<StoredMessage>): Pair<List<ChatUiMessage>, Map<String, ElicitationEntry>> {
        val out = mutableListOf<ChatUiMessage>()
        val entries = mutableMapOf<String, ElicitationEntry>()
        for (m in stored) {
            val ts = m.timestamp?.let { if (it < 1e12) (it * 1000).toLong() else it.toLong() } ?: System.currentTimeMillis()
            val usable = m.elicitations.orEmpty().filter { it.str("tool_id") != null && it.str("component") != null }
            out += ChatUiMessage(
                id = m.id, content = m.content, role = if (m.role == "user") Role.USER else Role.ASSISTANT, timestamp = ts,
                toolKeys = usable.map { "${m.id}::${it.str("tool_id")}" }, isResponseFromTool = usable.isNotEmpty(),
            )
            for (e in usable) {
                val toolId = e.str("tool_id")!!
                val answer = e.str("answer")
                entries["${m.id}::$toolId"] = ElicitationEntry(
                    toolId = toolId, messageId = m.id, component = e.str("component"),
                    input = e["input"] as? JsonObject, active = false, disabled = true, answer = answer,
                )
                if (!answer.isNullOrBlank()) out += ChatUiMessage("${m.id}::$toolId::answer", answer, Role.USER, timestamp = ts)
            }
        }
        return out to entries
    }

    private fun isOnline(): Boolean {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java) ?: return true
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        // Validated = the network really reaches the internet, not just Wi-Fi/data switched on
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun errorText(t: Throwable): String = (t as? MatrixApiException)?.body?.msg ?: t.message ?: "An unexpected error occurred"

    // ================================================================== voice input

    /*
     * Same as the web widget (synapse useChat): record one clip, send it to Matrix as a single
     * base64 audio message; Matrix answers with audio_transcript (shown as the doctor's message)
     * and then the agent's reply.
     */
    private val recorder = AudioMessageRecorder(app)
    private var voiceTimeoutJob: Job? = null

    /** Call once the mic permission is granted. */
    fun startVoiceInput() {
        val s = _state.value
        if (s.voiceStatus != VoiceStatus.IDLE || recorder.isRecording) return
        if (!s.canSendLive) {
            _toasts.tryEmit(if (s.connection != ConnectionStatus.CONNECTED) "Voice needs a connection. Please try again in a moment." else "Wait for the reply to finish.")
            return
        }
        val startedAt = System.currentTimeMillis()
        recorder.start(
            onDone = { audio -> onVoiceRecorded(audio) },
            onError = { e ->
                MedAssistLogger.d("MedAssist", "voice: recording failed: ${e.message}")
                setVoiceIdle()
                // MediaRecorder.stop() throws when stopped right after start
                _toasts.tryEmit(
                    if (System.currentTimeMillis() - startedAt < MIN_VOICE_MS) "Didn't catch that. Tap the mic and speak a little longer."
                    else "Couldn't use the microphone. It may be in use by another app."
                )
            },
        )
        if (recorder.isRecording) {
            _state.update { it.copy(voiceStatus = VoiceStatus.LISTENING, voiceStartedAt = startedAt, voiceLevels = emptyList()) }
            track("voice_started")
            startVoiceMeter()
        }
    }

    private var voiceMeterJob: Job? = null
    private var voiceDetector = SilenceDetector()

    /** Live waveform from the mic level; ends the clip after the doctor stops talking. */
    private fun startVoiceMeter() {
        voiceDetector = SilenceDetector(speechLevel = SPEECH_LEVEL)
        voiceMeterJob?.cancel()
        voiceMeterJob = viewModelScope.launch {
            while (_state.value.voiceStatus == VoiceStatus.LISTENING) {
                delay(100)
                val amp = recorder.maxAmplitude()
                _state.update { it.copy(voiceLevels = (it.voiceLevels + SilenceDetector.level(amp)).takeLast(48)) }
                when (voiceDetector.onSample(amp, System.currentTimeMillis())) {
                    SilenceDetector.Verdict.SPEECH_ENDED -> {
                        track("voice_auto_stopped")
                        finishVoiceInput()
                        return@launch
                    }
                    SilenceDetector.Verdict.NO_SPEECH -> {
                        cancelVoiceInput()
                        _toasts.tryEmit("Couldn't hear you. Check the mic and try again.")
                        track("voice_no_speech")
                        return@launch
                    }
                    SilenceDetector.Verdict.KEEP_LISTENING -> Unit
                }
            }
        }
    }

    /** Stop listening and send the clip (also called by the recorder's 15-minute cap). */
    fun finishVoiceInput() {
        if (_state.value.voiceStatus != VoiceStatus.LISTENING) return
        voiceMeterJob?.cancel()
        if (!voiceDetector.heardSpeech) {
            // Nothing loud enough was said: don't send silence
            recorder.cancel()
            setVoiceIdle()
            _toasts.tryEmit("Couldn't hear you. Speak a little louder and try again.")
            track("voice_no_speech")
            return
        }
        track("voice_sent", "ms" to (System.currentTimeMillis() - _state.value.voiceStartedAt))
        recorder.stop()
    }

    private fun onVoiceRecorded(audio: AudioPayload) {
        if (audio.durationMs < MIN_VOICE_MS) {
            setVoiceIdle()
            _toasts.tryEmit("Didn't catch that. Tap the mic and speak a little longer.")
            return
        }
        val s = _state.value
        if (!isOnline() || s.connection != ConnectionStatus.CONNECTED) {
            setVoiceIdle()
            _toasts.tryEmit("You're offline, so the voice message wasn't sent. Please try again.")
            return
        }
        _state.update { it.copy(voiceStatus = VoiceStatus.TRANSCRIBING) }
        viewModelScope.launch {
            sendMessageInternal(SendOptions(audio = audio))
            voiceTimeoutJob?.cancel()
            voiceTimeoutJob = viewModelScope.launch {
                delay(VOICE_TIMEOUT_MS)
                failVoiceMessage("It took too long to transcribe.")
            }
        }
    }

    /** The doctor's words: fill the "Voice message" bubble; the reply follows. */
    private fun onAudioTranscript(text: String) {
        streamIdleJob?.cancel()
        _state.update { s ->
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
    private fun failVoiceMessage(reason: String?) {
        voiceTimeoutJob?.cancel()
        if (_state.value.voiceStatus != VoiceStatus.TRANSCRIBING) return
        _state.update { s ->
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
    private fun clearTranscribing() {
        voiceTimeoutJob?.cancel()
        if (_state.value.voiceStatus == VoiceStatus.TRANSCRIBING) setVoiceIdle()
    }

    /** Drop a recording in progress (cancel tapped, app backgrounded, new chat). A sent clip can't be recalled. */
    fun cancelVoiceInput() {
        if (_state.value.voiceStatus != VoiceStatus.LISTENING) return
        voiceMeterJob?.cancel()
        recorder.cancel()
        setVoiceIdle()
        track("voice_cancelled")
    }

    private fun setVoiceIdle() = _state.update { it.copy(voiceStatus = VoiceStatus.IDLE, voiceLevels = emptyList()) }

    override fun onCleared() {
        voiceMeterJob?.cancel()
        recorder.cancel()
        voiceTimeoutJob?.cancel()
        client.destroy()
        super.onCleared()
    }

    class Factory(private val app: Application, private val args: MedAssistArgs) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MedAssistViewModel(app, args) as T
    }

    companion object {
        /** Shorter than this is treated as an accidental tap. */
        private const val MIN_VOICE_MS = 700L
        /** Mic amplitude that counts as speech (0..32767). */
        private const val SPEECH_LEVEL = 1_800
        private const val VOICE_TIMEOUT_MS = 30_000L
        const val INITIAL_BOT_MESSAGE_ID = "initial-bot-message"
        private const val STREAM_IDLE_TIMEOUT_MS = 30_000L
        private const val RESPONSE_TIMEOUT_MS = 30_000L
    }
}

/** "Missing" in the widget's JS truthiness sense: null, JSON null, "", false, 0. */
internal fun JsonElement?.isEmptyValue(): Boolean {
    if (this == null || this is kotlinx.serialization.json.JsonNull) return true
    if (this is JsonPrimitive) {
        val c = contentOrNull ?: return true
        if (isString) return c.isEmpty()
        return c == "false" || c == "0"
    }
    return false
}
