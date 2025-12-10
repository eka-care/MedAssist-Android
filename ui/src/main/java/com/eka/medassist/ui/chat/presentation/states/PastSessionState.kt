package com.eka.medassist.ui.chat.presentation.states

import com.eka.conversation.client.models.ChatInfo

sealed class PastSessionState {
    data object Loading : PastSessionState()
    data class Error(val message: String) : PastSessionState()
    data class Success(val data: List<ChatInfo>) : PastSessionState()
}