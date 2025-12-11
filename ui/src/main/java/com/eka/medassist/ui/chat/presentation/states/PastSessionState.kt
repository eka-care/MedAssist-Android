package com.eka.medassist.ui.chat.presentation.states

import com.eka.medassist.ui.chat.presentation.models.ChatSession

sealed class PastSessionState {
    data object Loading : PastSessionState()
    data class Error(val message: String) : PastSessionState()
    data class Success(val data: Map<String,List<ChatSession>>) : PastSessionState()
}