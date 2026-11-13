package com.eka.medassist.ui.chat.client

/** Signed-in doctor, for the empty chat's "Good evening, Dr. Name" (same fields web uses: fn, s). */
data class MedAssistDoctor(
    val firstName: String,
    val salutation: String? = null,
) {
    /** "Dr. Imtiyaz" */
    val displayName: String
        get() = "${(salutation?.takeIf { it.isNotBlank() } ?: "Dr").trim().removeSuffix(".")}. $firstName"
}
