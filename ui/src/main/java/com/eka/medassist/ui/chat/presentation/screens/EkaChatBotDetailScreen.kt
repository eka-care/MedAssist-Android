package com.eka.medassist.ui.chat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.data.local.models.ChatContext
import com.eka.medassist.ui.chat.presentation.common.atom.IconWrapper
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.presentation.common.organism.DocAssistAppBarCustom
import com.eka.medassist.ui.chat.presentation.components.DocAssistHeader
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.Blue50
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral100
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.FuchsiaViolet100
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkaChatBotDetailScreen(
    openDrawer: () -> Unit = {},
    viewModel: EkaChatViewModel,
    onPatientChatClick: (ChatContext) -> Unit,
    navigateToChatScreen: (String) -> Unit,
    onEmptyScreen: () -> Unit,
) {
    val backgroundBrush =
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0.4f to Blue50,
                1f to FuchsiaViolet100
            )
        )

    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val isSessionLoading by viewModel.isSessionLoading.collectAsState()
    val selectedOption = viewModel.botViewMode.collectAsState().value
    val context = LocalContext.current
    val onOptionSelected: (BotViewMode) -> Unit = {
        val params = JSONObject()
        params.put("type", it.type)
        viewModel.updateBotViewMode(it)
    }
    val options = listOf(
        EkaBotModeData(
            BotViewMode.PATIENT,
            "Patients",
        ),
        EkaBotModeData(BotViewMode.ALL_CHATS, "All chats")
    )
    val listState = rememberLazyListState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val sessionByContext by viewModel.groupedSessionsByContext.collectAsState()

    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
//            viewModel.syncDocAssistHistory()
            viewModel.getChatSessions()
        } else {
//            viewModel.getSearchResults(searchQuery = searchText, ownerId = ChatUtils.getOwnerId())
        }
    }
    LaunchedEffect(chatSessions) {
        if (chatSessions.isNotEmpty()) {
            listState.animateScrollToItem(chatSessions.size - 1)
        } else if (!isSessionLoading && searchText.isBlank()) {
            onEmptyScreen.invoke()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = DarwinTouchNeutral100,
        topBar = {
            DocAssistAppBarCustom(
                navigationIcon = {
                    //TODO Back button
//                    Box(
//                        modifier = Modifier
//                            .clip(CircleShape)
//                            .size(48.dp)
//                            .clickable(
//                                onClick = openDrawer
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        ProfileImage(
//                            ProfileImageProps(
//                                oid = loggedInUser?.toLongOrNull(),
//                                url = docProfilePic,
//                                initials = ProfileHelper.getInitials(name),
//                            )
//                        )
//                    }
                },
                title = "DocAssist",
                titleColor = DarwinTouchNeutral1000,
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = padding.calculateTopPadding()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                DocAssistHeader(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 4.dp
                    ),
                    selectedOption = selectedOption,
                    onOptionSelected = onOptionSelected,
                    options = options
                )
                if ((!sessionByContext.isNullOrEmpty()) || selectedOption == BotViewMode.ALL_CHATS) {
                    TextField(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .onFocusChanged {
                                if (it.isFocused) {
                                    val params = JSONObject()
                                    params.put("type", "search")
                                }
                            }
                            .focusRequester(focusRequester),
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            // TODO Search
//                            viewModel.getSearchResults(
//                                searchQuery = searchText,
//                                ownerId = ChatUtils.getOwnerId()
//                            )
                        },
                        placeholder = {
                            Text(
                                text = "Search chat...",
                                style = touchBodyRegular,
                                color = DarwinTouchNeutral600
                            )
                        },
                        leadingIcon = {
                            IconWrapper(
                                icon = R.drawable.ic_magnifying_glass_regular,
                                contentDescription = "Search",
                                tint = DarwinTouchNeutral800,
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButtonWrapper(
                                    icon = R.drawable.ic_circle_xmark_solid,
                                    contentDescription = "Clear",
                                    onClick = { searchText = "" }
                                )
                            }
                        },
                        maxLines = 1,
                        singleLine = true
                    )
                }
//                when (selectedOption) {
//                    BotViewMode.ALL_CHATS -> {
//                        LazyColumn(
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            itemsIndexed(chatSessions) { _, message ->
//                                var headline = when (message.message.msgType) {
//                                    MessageType.TEXT.stringValue -> message.message.messageText
//                                    else -> "Conversation"
//                                }
//                                if (headline.isNullOrEmpty()) {
//                                    headline = "Conversation"
//                                }
//                                if (message.message.chatContext != null) {
//                                    val chatContext =
//                                        ChatUtils.getChatContextFromString(message.message.chatContext)
//                                    EkaChatPatientsRow(
//                                        data = EkaChatPatientsRowData(
//                                            headlineText = headline,
//                                            subHeadline = viewModel.getSubHeadline(message),
//                                            draftNumber = message.totalConversations,
//                                            draftNumberColor = DarwinTouchYellowDark,
//                                            icon = R.drawable.ic_messages_regular,
//                                            time = ChatUtils.getTimeStampString(message.message.createdAt),
//                                            spaceBetweenSubHeadlineAndTime = true,
//                                            nameTag = chatContext?.patientName
//                                        ),
//                                        onClick = {
//                                            navigateToChatScreen(message.message.sessionId)
//                                        }
//                                    )
//                                } else {
//                                    EkaChatPatientsRow(
//                                        data = EkaChatPatientsRowData(
//                                            headlineText = headline,
//                                            icon = R.drawable.ic_messages_regular,
//                                            draftNumber = message.totalConversations,
//                                            draftNumberColor = DarwinTouchYellowDark,
//                                            subHeadline = viewModel.getSubHeadline(message),
//                                            time = ChatUtils.getTimeStampString(message.message.createdAt),
//                                            spaceBetweenSubHeadlineAndTime = true,
//                                        ),
//                                        onClick = {
//                                            navigateToChatScreen(message.message.sessionId)
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
            }
        },
        floatingActionButton = {
//            FloatingActionButton(
//                modifier = Modifier.padding(bottom = 80.dp),
//                onClick = {
//                    val params = JSONObject()
//                    params.put("type", "start_new_chat")
//                    AnalyticsUtil.logEvent(
//                        context = context,
//                        data = Event(
//                            eventName = "doc_assist_history_clicks",
//                            params = params
//                        )
//                    )
//                    navigateToChatScreen.invoke(ChatUtils.getNewSessionId())
//                },
//                shape = RoundedCornerShape(16.dp),
//                containerColor = DarwinTouchPrimaryBgLight
//            ) {
//                Row(
//                    modifier = Modifier.padding(horizontal = 16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    IconWrapper(
//                        icon = R.drawable.ic_pen_solid,
//                        tint = DarwinTouchNeutral1000,
//                        contentDescription = "New Chat",
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Text(
//                        text = "New",
//                        style = touchCalloutBold,
//                        color = DarwinTouchNeutral1000
//                    )
//                }
//            }
        }
    )
}

enum class BotViewMode(val type: String) {
    PATIENT("patients"),
    ALL_CHATS("all_chats")
}

data class EkaBotModeData(
    val type: BotViewMode,
    val title: String
)