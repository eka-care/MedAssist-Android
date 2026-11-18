package com.eka.medassist.ui.chat.client

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Words the chat uses for who it is about. The defaults are the doctor app's ("Attach patient");
 * a patient-facing app passes its own (e.g. "Choose profile", greeting the signed-in user by name).
 */
data class MedAssistLabels(
    val attachPatient: String = "Attach patient",
    val removePatient: String = "Remove patient",
    /** What changing it means in the "start a new chat" dialog: "patient", "profile". */
    val patientNoun: String = "patient",
    /** Shown after the greeting when there is no doctor: "Good evening, Ranjan". */
    val greetingName: String? = null,
    /** Line under the greeting on an empty chat, when the agent has no welcome message. */
    val emptyHint: String = "Add a patient and I'll bring up their earlier visits, or ask me anything clinical to get started.",
    /** Starter pills on an empty chat when the agent's welcome message has none; tapping one sends it. */
    val suggestions: List<String> = emptyList(),
)

val LocalMedAssistLabels = staticCompositionLocalOf { MedAssistLabels() }
