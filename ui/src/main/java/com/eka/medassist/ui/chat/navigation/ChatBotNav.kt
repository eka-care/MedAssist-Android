package com.eka.medassist.ui.chat.navigation

import kotlinx.serialization.Serializable

@Serializable
data class DocAssistNavModel(val sessionId: String)

@Serializable
data class ChatBotPatientSessionNavModel(
    val patientId: String? = null,
    val visitId: String? = null,
    val mode: String? = null
)

enum class ChatScreenOpenType(val type: String) {
    EKA_SCRIBE("V2RX"),
    VOICE_TO_TEXT("V2TX")
}

@Serializable
data class ChatScreenNavModel(
    val sessionId: String? = null
)