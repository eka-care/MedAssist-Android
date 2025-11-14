package com.eka.medassist.ui.chat.presentation.viewmodels

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eka.conversation.ChatInit
import com.eka.conversation.common.Response
import com.eka.conversation.common.Utils
import com.eka.conversation.data.local.db.entities.MessageEntity
import com.eka.conversation.data.local.db.entities.models.MessageFileType
import com.eka.conversation.data.local.db.entities.models.MessageRole
import com.eka.conversation.features.audio.AndroidAudioRecorder
import com.eka.medassist.ui.chat.data.local.models.ChatContext
import com.eka.medassist.ui.chat.data.local.models.MessageType
import com.eka.medassist.ui.chat.presentation.models.ChatMessage
import com.eka.medassist.ui.chat.presentation.models.ChatSession
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.presentation.screens.BotViewMode
import com.eka.medassist.ui.chat.presentation.states.SessionMessagesState
import com.eka.medassist.ui.chat.utility.MessageTypeMapping
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import com.eka.medassist.ui.chat.common.Response as EkaResponse

class EkaChatViewModel(
    val app: Application
) : AndroidViewModel(app) {

    var sessionId by mutableStateOf("")
    var sendButtonEnabled by mutableStateOf(true)

    private val _sessionMessages =
        MutableStateFlow(SessionMessagesState(isLoading = true, messageEntityResp = emptyList()))
    val sessionMessages = _sessionMessages.asStateFlow()

    var voice2RxContext by mutableStateOf<String>("")
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

    var job: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    var playingSessionId by mutableStateOf("")

    var isVoice2RxRecording: Boolean by mutableStateOf(false)
    var isVoiceToTextRecording: Boolean by mutableStateOf(false)
    var isQueryResponseLoading: Boolean by mutableStateOf(false)

    init {
        ChatInit.initialize(
            context = app,
            chatInitConfiguration = null
        )
    }

    fun updateBotViewMode(newMode: BotViewMode) {
        _botViewMode.value = newMode
    }

    fun resetSuggestions() {
        _suggestionList.value = emptyList()
        _moreSuggestions.value = EkaResponse.Success(true)
    }

    fun updateTextInputState(newValue: String) {
        _textInputState.value = newValue
    }

    fun updateSessionId(session: String) {
        sessionId = session
        clearSessionMessages()
    }

    fun clearSessionMessages() {
        _sessionMessages.value = SessionMessagesState(
            isLoading = false,
            messageEntityResp = emptyList(),
        )
        clearSuggestionList()
    }

    fun getNewMsgId(messages: List<ChatMessage>): Int {
        if (messages.isEmpty()) {
            return 0
        }
        return messages.first().message.msgId + 1
    }

    suspend fun getNewMsgIdBySessionId(sessionId: String): Int {
        val sessionMessages =
            ChatInit.getMessagesBySessionId(sessionId = sessionId)?.data?.firstOrNull()
        if (sessionMessages.isNullOrEmpty()) {
            return 0
        }
        return sessionMessages.maxBy { it.createdAt }.msgId + 1
    }

    fun getSearchResults(searchQuery: String, ownerId: String? = null) {
        if (searchQuery.isBlank()) {
            return
        }
        viewModelScope.launch {
            val response = ChatInit.getSearchResult(query = searchQuery, ownerId = ownerId)
            response?.collect { messages ->

            }
        }
    }

    private fun groupBySessionId(messages: List<MessageEntity>): List<MessageEntity> {
        val groupedMessages = messages.groupBy { it.sessionId }
        val lastMessages = mutableListOf<MessageEntity>()
        groupedMessages.forEach { (_, value) ->
            lastMessages.add(value.last())
        }
        return lastMessages
    }

    fun getChatSessions() {
//        _isSessionsLoading.value = true
//        viewModelScope.launch {
//            var response: Response<List<MessageEntity>>? = null
//            if (chatContext.isNullOrEmpty()) {
//                response = ChatInit.getAllSessions(ChatUtils.getOwnerId())
//            } else {
//                response = ChatInit.getAllSessionByChatContext(chatContext = chatContext)
//            }
//            when (response) {
//                is Response.Loading -> {
//
//                }
//
//                is Response.Success -> {
//                    response.data?.let { messages ->
//                        getDataChatSessions(messages)
//
//                    }
//                }
//
//                is Response.Error -> {
//
//                }
//
//                else -> {}
//            }
//        }
    }

//    private fun getDataChatSessions(messages: List<MessageEntity>) {
//        val chatSessions = mutableListOf<ChatSession>()
//        val deferredMessages = mutableListOf<Deferred<ChatSession>>()
//        viewModelScope.launch {
//            messages.forEach { message ->
//                deferredMessages.add(
//                    viewModelScope.async {
//                        val chatMessages = ChatInit.getMessagesBySessionId(message.sessionId)
//                        var totalRecords = 0
//                        var totalConv = 0
//                        chatMessages?.data?.firstOrNull()?.forEach {
//                            totalRecords += it.messageFiles?.size ?: 0
//                            if (it.msgType == MessageType.VOICE_2_RX_PRESCRIPTION_DRAFT.stringValue) {
//                                val voiceSession =
//                                    Voice2Rx.getSessionBySessionId(sessionId = it.messageText ?: "")
//                                if (voiceSession != null) {
//                                    if (voiceSession.status == Voice2RxSessionStatus.COMPLETED) {
//                                        totalRecords += 1
//                                    } else {
//                                        totalConv += 1
//                                    }
//                                }
//                            }
//                        }
//
//                        return@async ChatSession(
//                            message = message,
//                            totalConversations = totalConv,
//                            totalRecords = totalRecords
//                        )
//                    }
//                )
//            }
//            deferredMessages.forEach {
//                chatSessions.add(it.await())
//            }
//            _chatSessions.value = chatSessions
//            groupChatSessionByChatContext()
//            _isSessionsLoading.value = false
//        }
//    }

//    private fun groupChatSessionByChatContext() {
//        val sessions = mutableMapOf<ChatContext, MutableList<ChatSession>>()
//        _chatSessions.value?.forEach {
//            if (!it.message.chatContext.isNullOrEmpty()) {
//                try {
//                    val chatContext =
//                        Gson().fromJson(it.message.chatContext, ChatContext::class.java)
//                    if (sessions.containsKey(chatContext)) {
//                        sessions[chatContext]?.add(
//                            ChatSession(
//                                message = it.message,
//                                totalRecords = it.totalRecords,
//                                totalConversations = it.totalConversations
//                            )
//                        )
//                    } else {
//                        sessions[chatContext] = mutableListOf(it)
//                    }
//                } catch (e: Exception) {
//                    Log.e("groupChatSessionByChatContext", "Error: ${e.message}")
//                }
//            }
//        }
//        _groupedSessionsByContext.value = sessions
//    }

//    fun getSessionMessages(sessionId: String) {
//        if (job != null) {
//            job?.cancel()
//            job = null
//        }
//        job = viewModelScope.launch {
//            val response = ChatInit.getMessagesBySessionId(sessionId = sessionId)
//            if (response != null) {
//                when (response) {
//                    is Response.Loading -> {
//                        _sessionMessages.value =
//                            SessionMessagesState(isLoading = true, messageEntityResp = emptyList())
//                    }
//
//                    is Response.Success -> {
//                        response.data?.collect {
//                            OrbiLogger.d("getSessionMessages", "getSessionMessages: $it")
//                            getMessagesWithVoice2RxSession(it)
//                            getChatSessions(null)
//                        }
//                    }
//
//                    is Response.Error -> {
//                    }
//
//                    else -> {}
//                }
//            }
//        }
//    }

//    fun getMessagesWithVoice2RxSession(messages: List<MessageEntity>) {
//        val chatMessages = mutableListOf<ChatMessage>()
//        val deferredMessages = mutableListOf<Deferred<ChatMessage>>()
//        viewModelScope.launch {
//            messages.forEach { message ->
//                deferredMessages.add(
//                    viewModelScope.async {
//                        when (message.msgType) {
//                            MessageType.VOICE_2_RX_PRESCRIPTION_COMPLETED.stringValue,
//                            MessageType.VOICE_2_RX_PRESCRIPTION_DRAFT.stringValue -> {
//                                val session =
//                                    Voice2Rx.getSessionBySessionId(sessionId = message.messageText.toString())
//                                return@async ChatMessage(
//                                    message = message,
//                                    voice2RxSession = session
//                                )
//                            }
//
//                            else -> {
//                                if (message.messageFiles.isNullOrEmpty()) {
//                                    return@async ChatMessage(
//                                        message = message,
//                                        voice2RxSession = null
//                                    )
//                                } else {
//                                    val files = getFilesForMessage(message)
//
//                                    return@async ChatMessage(
//                                        message = message,
//                                        voice2RxSession = null,
//                                        files = files
//                                    )
//                                }
//                            }
//                        }
//                    }
//                )
//            }
//            deferredMessages.forEach {
//                chatMessages.add(it.await())
//            }
//            _sessionMessages.value =
//                SessionMessagesState(
//                    isLoading = false,
//                    messageEntityResp = chatMessages.sortedByDescending { it.message.msgId })
//        }
//    }

//    suspend fun getFilesForMessage(message: MessageEntity): List<RecordModel> {
//        val deferredFiles = mutableListOf<Deferred<RecordModel?>>()
//        val files = mutableListOf<RecordModel>()
//
//        message.messageFiles?.forEach { fileId ->
//            deferredFiles.add(
//                viewModelScope.async {
//                    Records.getInstance(context = app.applicationContext, token = "")
//                        .getRecordDetailsById(id = fileId)
//                }
//            )
//        }
//        deferredFiles.forEach {
//            val file = it.await()
//            if (file != null) {
//                files.add(file)
//            }
//        }
//
//        return deferredFiles.mapNotNull { it.await() }
//    }

//    fun askNewQueryFireStore(
//        query: String,
//        chatContext: ChatContext? = null,
//        ownerId: String,
//        docId: String,
//        selectedRecords: List<RecordModel?>
//    ) {
//        resetSuggestions()
//        sendButtonEnabled = false
//        isQueryResponseLoading = true
//        val lastMsgId = _sessionMessages.value.messageEntityResp.size
//        val patientId = chatContext?.patientId ?: ""
//        val files = mutableListOf<String>()
//        selectedRecords.forEach {
//            it?.let { record ->
//                files.add(record.id)
//            }
//        }
//        val message = MessageEntity(
//            msgId = getNewMsgId(_sessionMessages.value.messageEntityResp),
//            sessionId = sessionId,
//            sessionIdentity = patientId,
//            ownerId = ownerId,
//            messageText = query,
//            role = MessageRole.USER,
//            msgType = MessageType.TEXT.stringValue,
//            createdAt = Utils.getCurrentUTCEpochMillis(),
//            chatContext = chatContext?.let { Gson().toJson(it) },
//            messageFiles = files
//        )
//        val messageResponse = message.copy(
//            msgId = message.msgId + 1,
//            createdAt = Utils.getCurrentUTCEpochMillis(),
//            messageFiles = null,
//            messageText = "Loading...",
//            role = MessageRole.CUSTOM,
//        )
//        viewModelScope.launch {
//            ChatInit.insertMessages(listOf(message, messageResponse))
//            docAssistFireStoreManager.askNewQuery(
//                app,
//                messageEntity = message,
//                doctorId = docId,
//                patientId = patientId,
//                onSuccess = { queryMessage ->
//                    startListeningResponse(queryMessage = queryMessage, patientId = patientId)
//                },
//                onFailure = { error ->
//                    isQueryResponseLoading = false
//                    sendButtonEnabled = true
//                    showToast(error?.localizedMessage?.toString() ?: "Something went wrong!")
//                }
//            )
//        }
//    }

    fun askNewQuery(
        query: String,
        chatContext: ChatContext? = null,
        ownerId: String,
    ) {
        sendButtonEnabled = false
        val lastMsgId = _sessionMessages.value.messageEntityResp.size
        val patientId = chatContext?.patientId ?: ""
        val files = mutableListOf<String>()

        val message = MessageEntity(
            msgId = getNewMsgId(_sessionMessages.value.messageEntityResp),
            sessionId = sessionId,
            sessionIdentity = patientId,
            ownerId = ownerId,
            messageText = query,
            role = MessageRole.USER,
            msgType = MessageType.TEXT.stringValue,
            createdAt = Utils.getCurrentUTCEpochMillis(),
            chatContext = chatContext?.let { Gson().toJson(it) },
            messageFiles = files
        )
    }

    fun startAudioRecording(onError: (String) -> Unit) {
        if (::audioRecorder.isInitialized) {
            audioRecorder.stopRecording()
        }
        audioRecorder = AndroidAudioRecorder(app)
        currentAudioFile = File(app.filesDir, "${Utils.getNewFileName(MessageFileType.AUDIO)}.m4a")
        audioRecorder.startRecording(currentAudioFile!!, onError = onError)
    }

    fun stopRecording() {
        if (::audioRecorder.isInitialized) {
            audioRecorder.stopRecording()
        }
        try {
//            getTranscribeDataFromAudioFile(currentAudioFile!!) { response ->
//                OrbiLogger.d("stopRecording", "Transcribe Data: ${response.data}")
//                _currentTranscribeData.value = response
//            }
        } catch (e: Exception) {
            Log.e("stopRecording", "Error: ${e.message}")
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

    fun getMessageEntity(
        sessionInfo: String,
        msgType: MessageType,
        msgId: Int,
    ): MessageEntity {
        return MessageEntity(
            msgId = msgId,
            sessionId = sessionId,
            messageText = sessionInfo,
            role = MessageRole.AI,
            msgType = msgType.stringValue,
            createdAt = Utils.getCurrentUTCEpochMillis(),
            chatContext = currentChatContext?.let { Gson().toJson(it) },
        )
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

    fun playAudio(sessionId: String, filePath: String, onCompletion: (() -> Unit)? = null) {
        stopAudio()
        playingSessionId = sessionId
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    onCompletion?.invoke()
                    stopAudio()
                }
            }
        } catch (e: Exception) {
//            OrbiLogger.d("EkaChatViewModel", "play Audio ${e.message.toString()}")
        }
    }

    fun stopAudio() {
        try {
            playingSessionId = ""
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
//            OrbiLogger.d("EkaChatViewModel", "stop Audio ${e.message.toString()}")
        }
    }
//
//    fun startVoiceSession(
//        visitId: String? = null,
//        patientId: String? = null,
//        modelType: ModelType = ModelType.PRO,
//        onError: (EkaScribeError) -> Unit = {}
//    ) {
//        isVoice2RxRecording = true
//        var visit = visitId
//        val initialPreferences = EkaScribePreferenceUtils.getInitialVoice2RxPreferences()
//        val languages =
//            getVoice2RxPreferences()?.languages?.toList() ?: initialPreferences.languages
//        val outputFormats =
//            getVoice2RxPreferences()?.outputFormats?.toList() ?: initialPreferences.outputFormats
//        var docAssistSessionId = ChatUtils.getNewSessionId()
//        if (sessionId.isNotBlank()) {
//            docAssistSessionId = sessionId
//        }
////        viewModelScope.launch(Dispatchers.IO) {
////            (app as IAmCommon).startVoiceConversation(
////                mode = getVoice2RxPreferences()?.selectedMode ?: initialPreferences.selectedMode,
////                docAssistSessionInfo = DocAssistSessionInfo(
////                    docAssistSessionId = docAssistSessionId,
////                    ownerId = ChatUtils.getOwnerId(),
////                    patientName = currentChatContext?.patientName,
////                    patientId = currentChatContext?.patientId
////                ),
////                additionalData = AdditionalData(
////                    visitid = visit,
////                    doctor = Doctor(
////                        id = OrbiUserManager.getUserTokenData()?.oid ?: "",
////                        profile = Profile(
////                            personal = Personal(
////                                name = Name(
////                                    f = OrbiUserManager.getUserTokenData()?.name ?: "",
////                                    l = ""
////                                )
////                            )
////                        )
////                    ),
////                    patient = Patient(id = patientId)
////                ),
////                modelType = modelType,
////                outputFormats = outputFormats,
////                languages = languages,
////                onError = {
////                    onError(it)
////                    showToast(
////                        it.errorDetails?.displayMessage
////                            ?: "Unknown error while starting the session!"
////                    )
////                }
////            )
////        }
//    }
//
//    fun getVoice2RxStatus(
//        voiceSessionId: String,
//        maxRetries: Int = 5,
//        onComplete: (SessionStatus) -> Unit,
//        onError: (String) -> Unit,
//    ) {
//        var retryCount = 0
//        val successStates = Voice2RxUtils.getOutputSuccessStates()
//        viewModelScope.launch {
//            while (true) {
//                if (retryCount > maxRetries) {
//                    Log.d("OrbiApp", "Max retries reached")
//                    onError("Max retries reached")
//                    break
//                }
//                Log.d("OrbiApp", "Checking voice2rx status")
//                val response = Voice2Rx.getVoice2RxSessionStatus(sessionId = voiceSessionId)
//                Log.d("OrbiApp", "Voice2Rx Status $response")
//                Log.d("OrbiApp", "voice2rx status ${response.status}")
//                if (response.status != Voice2RxStatus.IN_PROGRESS) {
//                    if (response.status in successStates) {
//                        onComplete(response)
//                    } else {
//                        onError("${response.error?.message}")
//                    }
//                    break
//                }
//                retryCount++
//            }
//        }
//    }

    fun getSubHeadline(session: ChatSession): String {
        return if (session.totalRecords > 0) {
            var label = "${session.totalRecords} record saved"
            if (session.totalConversations > 0) {
                label += "  •  "
            }
            label
        } else if (session.totalConversations > 0) {
            ""
        } else {
            MessageTypeMapping.getSubHeadline(session.message.msgType)
        }
    }

    private val _suggestionList = MutableStateFlow<List<SuggestionModel?>>(emptyList())
    val suggestionList = _suggestionList.asStateFlow()

    fun clearSuggestionList() {
        _suggestionList.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        if (::audioRecorder.isInitialized) {
            audioRecorder.stopRecording()
        }
        isVoice2RxRecording = false
        stopAudio()
    }
}