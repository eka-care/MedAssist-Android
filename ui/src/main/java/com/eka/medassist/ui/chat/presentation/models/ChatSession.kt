package com.eka.medassist.ui.chat.presentation.models

import com.eka.conversation.matrix.PastSession
import com.eka.medassist.ui.chat.utility.getDateHeader
import com.eka.medassist.ui.chat.utility.toFormattedTime

data class ChatSession(
    val sessionId : String,
    val sessionTitle : String,
    val subtitle: String?,
    val date : String,
    val time : String,
)

/** Matrix past session -> list row. `c_ate` may be epoch seconds or millis. */
internal fun PastSession.toChatSession(): ChatSession {
    val millis = createdAtEpoch?.let { if (it < 1_000_000_000_000L) it * 1000 else it }
    return ChatSession(
        sessionId = sessionId,
        sessionTitle = topic ?: lastMessage ?: "Chat Session",
        subtitle = lastMessage?.takeIf { topic != null },
        date = millis?.getDateHeader() ?: "Earlier",
        time = millis?.toFormattedTime() ?: createdAt.orEmpty(),
    )
}
