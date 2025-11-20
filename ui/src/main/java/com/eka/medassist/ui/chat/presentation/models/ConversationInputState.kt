package com.eka.medassist.ui.chat.presentation.models

sealed class ConversationInputState {
    object Default : ConversationInputState()
    object Audio : ConversationInputState()
}