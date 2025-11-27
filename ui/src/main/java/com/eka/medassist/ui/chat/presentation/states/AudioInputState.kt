package com.eka.medassist.ui.chat.presentation.states

sealed class AudioInputState {
    object Idle : AudioInputState()
    object Recording : AudioInputState()
    object Loading : AudioInputState()
}