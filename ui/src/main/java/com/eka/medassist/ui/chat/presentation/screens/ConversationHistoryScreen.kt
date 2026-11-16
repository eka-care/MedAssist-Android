package com.eka.medassist.ui.chat.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.conversation.matrix.PastSession
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonType
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.presentation.components.ChatSessionsEmptyState
import com.eka.medassist.ui.chat.presentation.components.ConversationHeader
import com.eka.medassist.ui.chat.presentation.components.SessionListShimmer
import com.eka.medassist.ui.chat.presentation.models.ChatSession
import com.eka.medassist.ui.chat.presentation.models.toChatSession
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight
import com.eka.ui.theme.EkaTheme
import com.eka.ui.theme.bodyLargeEmphasized

/** Past chats grouped by Today / Yesterday / date, newest first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationHistoryScreen(
    sessions: List<PastSession>,
    loading: Boolean,
    currentSessionId: String? = null,
    onSessionClick : (sessionId : String) -> Unit,
    onNewChatClick: () -> Unit,
    onBackClick : () -> Unit,
) {
    BackHandler(onBack = onBackClick)
    val grouped = remember(sessions) {
        sessions
            .sortedByDescending { it.createdAtEpoch ?: 0L }
            .map { it.toChatSession() }
            .groupBy { it.date }
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
            onNewClick = onNewChatClick,
        )
        when {
            loading && sessions.isEmpty() -> SessionListShimmer(modifier = Modifier.fillMaxSize())
            sessions.isEmpty() -> ChatSessionsEmptyState(padding = PaddingValues(0.dp))
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                grouped.forEach { (header, items) ->
                    stickyHeader(key = "header-$header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = DarwinTouchNeutral50)
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = header,
                                style = EkaTheme.typography.titleSmall,
                                color = EkaTheme.colors.onSurfaceVariant,
                            )
                        }
                    }
                    items(items = items, key = { it.sessionId }) {
                        ChatSessionItem(
                            session = it,
                            selected = it.sessionId == currentSessionId,
                            onClick = { onSessionClick(it.sessionId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSessionItem(
    session : ChatSession,
    selected: Boolean = false,
    onClick : () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = if (selected) DarwinTouchPrimaryBgLight else DarwinTouchNeutral0
        ),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.sessionTitle,
                    color = EkaTheme.colors.onSurface,
                    style = EkaTheme.typography.bodyLargeEmphasized(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        supportingContent = {
            Text(
                text = listOfNotNull(session.subtitle, session.time.ifBlank { null }).joinToString("  ·  "),
                style = EkaTheme.typography.labelLarge,
                color = EkaTheme.colors.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
