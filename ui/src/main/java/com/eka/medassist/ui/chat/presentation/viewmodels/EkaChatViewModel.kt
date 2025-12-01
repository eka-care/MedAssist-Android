package com.eka.medassist.ui.chat.presentation.viewmodels

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eka.conversation.client.ChatSDK
import com.eka.conversation.client.interfaces.ResponseStreamCallback
import com.eka.conversation.client.interfaces.SessionCallback
import com.eka.conversation.client.models.Message
import com.eka.conversation.common.FileNameGenerator
import com.eka.conversation.common.Response
import com.eka.conversation.common.models.SpeechToTextConfiguration
import com.eka.conversation.common.models.UserInfo
import com.eka.conversation.data.local.db.entities.MessageEntity
import com.eka.conversation.data.local.db.entities.models.MessageFileType
import com.eka.conversation.data.remote.socket.models.AudioFormat
import com.eka.conversation.data.remote.socket.states.SocketConnectionState
import com.eka.conversation.features.audio.AndroidAudioRecorder
import com.eka.conversation.features.audio.ISpeechToText
import com.eka.medassist.ui.chat.data.local.models.ChatContext
import com.eka.medassist.ui.chat.logger.MedAssistLogger
import com.eka.medassist.ui.chat.presentation.models.ChatSession
import com.eka.medassist.ui.chat.presentation.models.ConversationInputState
import com.eka.medassist.ui.chat.presentation.screens.BotViewMode
import com.eka.medassist.ui.chat.presentation.states.SessionMessagesState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.eka.medassist.ui.chat.common.Response as EkaResponse

class EkaChatViewModel(
    val app: Application
) : AndroidViewModel(app) {

    companion object {
        const val TAG = "EkaChatViewModel"
    }

    var sessionId by mutableStateOf("")
    var sendButtonEnabled by mutableStateOf(false)

    private val _inputState =
        MutableStateFlow<ConversationInputState>(ConversationInputState.Default)
    val inputState = _inputState.asStateFlow()

    private val _sessionMessages =
        MutableStateFlow(SessionMessagesState(isLoading = true, messageEntityResp = emptyList()))
    val sessionMessages = _sessionMessages.asStateFlow()
    var currentChatContext by mutableStateOf<ChatContext?>(null)

    private lateinit var audioRecorder: AndroidAudioRecorder

    private var currentAudioFile: File? = null

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions = _chatSessions.asStateFlow()

    private val _isSessionsLoading = MutableStateFlow<Boolean>(false)
    val isSessionLoading = _isSessionsLoading.asStateFlow()

    private val _groupedSessionsByContext =
        MutableStateFlow<Map<ChatContext, List<ChatSession>>>(emptyMap())
    val groupedSessionsByContext = _groupedSessionsByContext.asStateFlow()

    private val _currentTranscribeData = MutableStateFlow<Response<String>>(Response.Loading())
    val currentTranscribeData = _currentTranscribeData.asStateFlow()

    private val _moreSuggestions = MutableStateFlow<EkaResponse<Boolean>>(EkaResponse.Success(true))
    val moreSuggestions = _moreSuggestions.asStateFlow()

    private val _botViewMode = MutableStateFlow(BotViewMode.ALL_CHATS)
    val botViewMode = _botViewMode.asStateFlow()

    private val _textInputState = MutableStateFlow("")
    val textInputState = _textInputState.asStateFlow()

    private val _connectionState =
        MutableStateFlow<SocketConnectionState>(SocketConnectionState.Idle)
    val connectionState = _connectionState.asStateFlow()

    var job: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    var playingSessionId by mutableStateOf("")

    var isVoice2RxRecording: Boolean by mutableStateOf(false)
    var isVoiceToTextRecording: Boolean by mutableStateOf(false)
    var isQueryResponseLoading: Boolean by mutableStateOf(false)
    private var currentSessionId: String? = null

    val messages = MutableStateFlow<List<Message>>(emptyList())

    fun createNewSession(userInfo: UserInfo) {
        viewModelScope.launch {
            val lastSessionId = ChatSDK.getLastSession(userInfo)?.getOrNull()?.sessionId
            if (!lastSessionId.isNullOrBlank()) {
                ChatSDK.startSession(
                    sessionId = lastSessionId,
                    callback = object : SessionCallback {
                        override fun onFailure(error: Exception) {
                            startNewSession(userInfo = userInfo)
                            MedAssistLogger.d(TAG, error.message.toString())
                        }

                        override fun onSuccess(
                            sessionId: String,
                            connectionState: StateFlow<SocketConnectionState>,
                            sessionMessages: Response<Flow<List<Message>>>,
                            queryEnabled: StateFlow<Boolean>
                        ) {
                            onSessionStartSuccess(
                                sessionId = sessionId,
                                connectionState = connectionState,
                                sessionMessages = sessionMessages,
                                queryEnabled = queryEnabled
                            )
                        }
                    })
            } else {
                startNewSession(userInfo = userInfo)
            }
        }
    }

    fun startNewSession(userInfo: UserInfo) {
        ChatSDK.startSession(
            userInfo = userInfo,
            callback = object : SessionCallback {
            override fun onFailure(error: Exception) {
                MedAssistLogger.d(TAG, error.message.toString())
                // TODO handle error
            }

            override fun onSuccess(
                sessionId: String,
                connectionState: StateFlow<SocketConnectionState>,
                sessionMessages: Response<Flow<List<Message>>>,
                queryEnabled: StateFlow<Boolean>
            ) {
                onSessionStartSuccess(
                    sessionId = sessionId,
                    connectionState = connectionState,
                    sessionMessages = sessionMessages,
                    queryEnabled = queryEnabled
                )
            }
        })
    }

    fun onSessionStartSuccess(
        sessionId: String,
        connectionState: StateFlow<SocketConnectionState>,
        sessionMessages: Response<Flow<List<Message>>>,
        queryEnabled: StateFlow<Boolean>
    ) {
        sendButtonEnabled = true
        updateSessionId(session = sessionId)
        viewModelScope.launch {
            sessionMessages.data?.collect {
                messages.value = it
            }
        }
        viewModelScope.launch {
            connectionState.collect {
                _connectionState.value = it
            }
        }
        viewModelScope.launch {
            queryEnabled.collect {
                sendButtonEnabled = it
            }
        }
    }

    private val _responseStream = MutableStateFlow<Message?>(null)
    val responseStream = _responseStream.asStateFlow()

    fun askNewQuery(query: String, toolUseId : String? = null) {
        ChatSDK.sendQuery(
            query = query,
            toolUseId = toolUseId,
            callback = object : ResponseStreamCallback {
                override fun onComplete() {
                    isQueryResponseLoading = false
                    _responseStream.value = null
                }

                override fun onFailure(error: Exception) {
                    MedAssistLogger.d(TAG, error.message.toString())
                    _responseStream.value = null
                    isQueryResponseLoading = false
                }

                override fun onNewEvent(event: Message) {
                    isQueryResponseLoading = false
                    _responseStream.value = event
                }

                override fun onSuccess() {
                    isQueryResponseLoading = true
                    _responseStream.value = null
                }
            })
    }

    fun updateBotViewMode(newMode: BotViewMode) {
        _botViewMode.value = newMode
    }

    fun resetSuggestions() {
        _moreSuggestions.value = EkaResponse.Success(true)
    }

    fun updateTextInputState(newValue: String) {
        _textInputState.value = newValue
    }

    fun updateSessionId(session: String) {
        currentSessionId = session
        clearSessionMessages()
    }

    fun clearSessionMessages() {
        _sessionMessages.value = SessionMessagesState(
            isLoading = false,
            messageEntityResp = emptyList(),
        )
    }

    fun getSearchResults(searchQuery: String, ownerId: String? = null) {
//        if (searchQuery.isBlank()) {
//            return
//        }
//        viewModelScope.launch {
//            val response = ChatSDK.getSearchResult(query = searchQuery, ownerId = ownerId)
//            response?.collect { messages ->
//
//            }
//        }
    }

    fun setInputState(state: ConversationInputState) {
        _inputState.value = state
    }

    private fun groupBySessionId(messages: List<MessageEntity>): List<MessageEntity> {
        val groupedMessages = messages.groupBy { it.sessionId }
        val lastMessages = mutableListOf<MessageEntity>()
        groupedMessages.forEach { (_, value) ->
            lastMessages.add(value.last())
        }
        return lastMessages
    }

    fun startAudioRecording(onError: (String) -> Unit) {
        clearRecording()
        if (::audioRecorder.isInitialized) {
            audioRecorder.stopRecording()
        }
        isVoiceToTextRecording = true
        audioRecorder = AndroidAudioRecorder(app)
        currentAudioFile = File(app.filesDir, "${FileNameGenerator.generateFileName(MessageFileType.AUDIO)}.mp4")
        audioRecorder.startRecording(currentAudioFile!!, onError = onError)
    }

    fun stopRecording() {
        if (::audioRecorder.isInitialized) {
            audioRecorder.stopRecording()
        }
        isVoiceToTextRecording = false
        if(!sendButtonEnabled) return
        if (currentAudioFile == null) {
            sendButtonEnabled = true
            _currentTranscribeData.value = Response.Error("No audio file found")
            return
        }
        try {
            currentAudioFile?.let { audioFile ->
                _currentTranscribeData.value = Response.Loading()

                viewModelScope.launch {
                    val result = withTimeoutOrNull(15_000L) {
                        suspendCancellableCoroutine { continuation ->
                            ChatSDK.convertAudioToText(
                                audioFilePath = audioFile.absolutePath,
                                audioFormat = AudioFormat.MP4,
                                speechToTextConfiguration = SpeechToTextConfiguration(
                                    speechToText = object : ISpeechToText {
                                        override fun onSpeechToTextComplete(result: Result<String?>) {
                                            if (continuation.isActive) {
                                                result.onSuccess {
                                                    continuation.resume(it)
                                                }.onFailure {
                                                    continuation.resumeWithException(it)
                                                }
                                            }
                                        }
                                    }
                                )
                            )
                        }
                    }
                    sendButtonEnabled = true
                    when {
                        result.isNullOrBlank() -> {
                            _currentTranscribeData.value = Response.Error("Something went wrong!")
                        }
                        else -> {
                            _currentTranscribeData.value = Response.Success(result)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            sendButtonEnabled = true
            MedAssistLogger.e("stopRecording", "Error: ${e.message}")
            _currentTranscribeData.value = Response.Error(e.message)
        }
    }

    fun clearRecording() {
        _currentTranscribeData.value = Response.Loading()
        currentAudioFile = null
    }

    fun showToast(msg: String) {
        try {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
        }
    }

    fun generatePdf(data: String, context: Context) {
//        viewModelScope.launch(Dispatchers.Main) {
//            try {
//                val progressDialog =
//                    ProgressDialog(context).apply {
//                        setMessage("Generating PDF...")
//                        setCancelable(false)
//                        show()
//                    }
//
//                markdownPdfGenerator = MarkdownPdfGenerator(context = context)
//                delay(5000L)
//                val headerHtml = OrbiUserManager.getPdfConfigHeader(
//                    OrbiUserManager.getUserTokenData()?.oid ?: ""
//                )
//                val footerHtml = OrbiUserManager.getPdfConfigFooter(
//                    OrbiUserManager.getUserTokenData()?.oid ?: ""
//                )
//                val headHtml = OrbiUserManager.getPdfConfigHead(
//                    OrbiUserManager.getUserTokenData()?.oid ?: ""
//                )
//
//                markdownPdfGenerator?.convertMarkdownToPdfAndShare(
//                    markdown = data,
//                    head = headHtml,
//                    header = headerHtml,
//                    footer = footerHtml,
//                )?.fold(
//                    onSuccess = { file ->
//                        viewModelScope.launch(Dispatchers.Main) {
//                            progressDialog.dismiss()
//                            PDFUtility.previewPdf(file, context)
//                        }
//                    },
//                    onFailure = { error ->
//                        showToast("Failed: ${error.message}")
//                    }
//                )
//            } catch (e: Exception) {
//                showToast("Error generating PDF: ${e.message}")
//            }
//        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::audioRecorder.isInitialized) {
            audioRecorder.stopRecording()
        }
        isVoice2RxRecording = false
    }
}