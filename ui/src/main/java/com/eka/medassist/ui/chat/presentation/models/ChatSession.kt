package com.eka.medassist.ui.chat.presentation.models

import com.eka.conversation.client.models.ChatInfo
import com.eka.medassist.ui.chat.utility.getDateHeader
import com.eka.medassist.ui.chat.utility.toFormattedTime

data class ChatSession(
    val sessionId : String,
    val sessionTitle : String,
    val date : String,
    val time : String,
)

internal fun ChatInfo.toChatSession() : ChatSession {
    return ChatSession(
        sessionId = sessionId,
        sessionTitle = sessionTitle ?: "Chat Session",
        date = createdAt.getDateHeader(),
        time = createdAt.toFormattedTime()
    )
}