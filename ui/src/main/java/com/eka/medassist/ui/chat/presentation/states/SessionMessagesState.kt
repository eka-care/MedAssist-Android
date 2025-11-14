package com.eka.medassist.ui.chat.presentation.states

import com.eka.medassist.ui.chat.presentation.models.ChatMessage

data class SessionMessagesState(
    val isLoading : Boolean = false,
    val messageEntityResp: List<ChatMessage>,
    val error : String? = null
)