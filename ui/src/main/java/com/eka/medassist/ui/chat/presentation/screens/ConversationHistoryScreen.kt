package com.eka.medassist.ui.chat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eka.conversation.common.models.UserInfo
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonType
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.presentation.components.ConversationHeader
import com.eka.medassist.ui.chat.presentation.components.SessionListShimmer
import com.eka.medassist.ui.chat.presentation.models.ChatSession
import com.eka.medassist.ui.chat.presentation.states.PastSessionState
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import com.eka.ui.theme.EkaTheme
import com.eka.ui.theme.bodyLargeEmphasized

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationHistoryScreen(
    userInfo: UserInfo,
    viewModel: EkaChatViewModel,
    onSessionClick : (sessionId : String) -> Unit,
    onBackClick : () -> Unit,
) {
    val pastSessions by viewModel.pastSessions.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.getPastSession(userInfo = userInfo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(DarwinTouchNeutral50)
    ) {
        ConversationHeader(
            title = "Past Sessions",
            onBackClick = onBackClick,
        )

        when(pastSessions) {
            is PastSessionState.Loading -> {
                SessionListShimmer(modifier = Modifier.fillMaxSize())
            }
            is PastSessionState.Success -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                ) {
                    (pastSessions as PastSessionState.Success).data.toList().forEach { sessions ->
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = DarwinTouchNeutral50)
                                    .padding(vertical = 4.dp, horizontal = 16.dp)
                            ) {
                                Text(
                                    text = sessions.first,
                                    style = EkaTheme.typography.titleSmall,
                                    color = EkaTheme.colors.onSurfaceVariant,
                                )
                            }
                        }
                        items(items = sessions.second) {
                            ChatSessionItem(
                                session = it,
                                onClick = {
                                    onSessionClick(it.sessionId)
                                }
                            )
                        }
                    }
                }
            }
            is PastSessionState.Error -> {
                // show the error message
            }
        }
    }
}

@Composable
fun ChatSessionItem(
    session : ChatSession,
    onClick : () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = DarwinTouchNeutral0
        ),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.sessionTitle,
                    color = EkaTheme.colors.onSurface,
                    style = EkaTheme.typography.bodyLargeEmphasized(),
                )
            }
        },
        supportingContent = {
            Text(
                text = session.time,
                style = EkaTheme.typography.labelLarge,
                color = EkaTheme.colors.onSecondaryContainer
            )
        },
        trailingContent = {
            IconButtonWrapper(
                icon = R.drawable.arrow_right,
                contentDescription = "Go",
                type = IconButtonType.CUSTOM,
                onClick = onClick,
                iconSize = 16.dp
            )
        }
    )
}