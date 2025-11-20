package com.eka.medassist.ui.chat.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.eka.conversation.common.Utils
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.data.local.models.MessageType
import com.eka.medassist.ui.chat.navigation.ChatScreenNavModel
import com.eka.medassist.ui.chat.presentation.components.BottomBarMainScreen
import com.eka.medassist.ui.chat.presentation.components.ConversationHeader
import com.eka.medassist.ui.chat.presentation.components.EkaNewChatContent
import com.eka.medassist.ui.chat.presentation.components.MicrophonePermissionAlertDialog
import com.eka.medassist.ui.chat.presentation.states.ActionType
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkaChatBotMainScreen(
    navData: ChatScreenNavModel,
    viewModel: EkaChatViewModel,
    onBackClick: () -> Unit,
) {
    var isInputBottomSheetVisible by remember { mutableStateOf(true) }
    val sessionMessages by viewModel.sessionMessages.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    var screenTitle by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current

    val onDismissBottomSheet: () -> Unit = {
        scope.launch {
            showBottomSheet = false
        }
    }
    var sessionId by remember {
        mutableStateOf(navData.sessionId)
    }

    LaunchedEffect(Unit) {
        viewModel.createNewSession()
        viewModel.updateTextInputState("")
        if (sessionId.isNullOrEmpty()) {
            sessionId = Utils.getNewSessionId()
            viewModel.updateSessionId(sessionId.toString())
            // TODO Get Session Messages
//            viewModel.getSessionMessages(sessionId.toString())
        } else {
            viewModel.updateSessionId(sessionId.toString())
//            viewModel.getSessionMessages(sessionId.toString())
        }
        viewModel.sendButtonEnabled = true
    }

    LaunchedEffect(sessionMessages) {
        if (sessionMessages.messageEntityResp.isEmpty()) {
            screenTitle = context.getString(R.string.new_chat)
        } else {
            screenTitle = when (sessionMessages.messageEntityResp.lastOrNull()?.message?.msgType) {
                MessageType.TEXT.stringValue -> {
                    sessionMessages.messageEntityResp.lastOrNull()?.message?.messageText
                        ?: "Conversation"
                }

                null -> {
                    context.getString(R.string.new_chat)
                }

                else -> {
                    "Conversation"
                }
            }
        }
    }

    val onOpenBottomSheet: () -> Unit = {
        scope.launch {
            showBottomSheet = true
        }
    }

    var showPermissionDialog by remember {
        mutableStateOf(false)
    }
    if (showPermissionDialog) {
        MicrophonePermissionAlertDialog(
            onDismiss = {
                showPermissionDialog = false
            },
            onConfirm = {
                showPermissionDialog = false
                // TODO Voice Recording
//                handleVoiceRecordingOperation(
//                    context = context,
//                    openType = openType,
//                    onRecordingAllowed = {
//                        viewModel.isVoiceToTextRecording = true
//                        isInputBottomSheetVisible = true
//                    },
//                    onNetworkUnavailable = {
//                        viewModel.showToast("Internet not available.")
//                    },
//                    onPermissionRequired = {
//                        showPermissionDialog = true
//                    }
//                )
            }
        )
    }

    BackHandler(enabled = showBottomSheet) {
        onDismissBottomSheet()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            ConversationHeader(
                title = screenTitle.ifEmpty { stringResource(id = R.string.new_chat) },
                subTitle = "ParrotLet 1.0",
                onClick = { cta ->
                    when (cta.action) {
                        ActionType.ON_BACK.stringValue -> {
                            onBackClick()
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomBarMainScreen(
                viewModel = viewModel,
                onClick = {
                    when (it.action) {
                        ActionType.SHOW_INPUT_BOTTOM_SHEET.stringValue -> {
                            isInputBottomSheetVisible = true
                        }
                    }
                },
                openDocumentSelector = {
                },
                isInputBottomSheetVisible = isInputBottomSheetVisible
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .imePadding()
        ) {
            EkaNewChatContent(
                viewModel = viewModel,
                sessionId = sessionId.toString(),
                onSuggestionClick = {
                    // TODO Suggestion click new query
//                    viewModel.askNewQueryFireStore(
//                        query = it.label,
//                        chatContext = chatContext,
//                        docId = docId!!,
//                        ownerId = ChatUtils.getOwnerId(),
//                        selectedRecords = emptyList()
//                    )
//                    viewModel.updateSelectedRecords(emptyList())
                },
                onClick = { cta ->
                    // TODO Have a look to remove
                }
            )
        }
    }
}

//private fun processVoice2RxClick(
//    onConfirm: () -> Unit,
//    onPermissionNotGiven: () -> Unit,
//    viewModel: EkaChatViewModel,
//    context: Context,
//) {
//    if (!PermissionUtils.hasRecordAudioPermission(context = context)) {
//        onPermissionNotGiven()
//        return
//    }
//    if (viewModel.isVoice2RxRecording || viewModel.isVoiceToTextRecording) {
//        viewModel.showToast("Recording is in progress!")
//        return
//    }
//    onConfirm()
//}

//@Composable
//private fun DefaultDragHandle() {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//            .alpha(0.4f),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Box(
//            modifier = Modifier
//                .width(32.dp)
//                .height(4.dp)
//                .clip(RoundedCornerShape(100.dp))
//                .background(DarwinTouchNeutral600)
//        )
//    }
//}