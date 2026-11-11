package com.eka.medassist.ui.chat.client

/**
 * Patient the chat is about. Sent at session create as `profile` (name, age, gender, oid) and,
 * for scribe chats, as the snapshot's `patient` ({name, age, sex}) like ekascribe-web.
 */
data class MedAssistPatient(
    val name: String,
    val age: Int? = null,
    /** "M" / "F" / "O". */
    val sex: String? = null,
    val oid: String? = null,
) {
    /** "Ravi Kumar · 42 M" */
    val label: String
        get() = listOfNotNull(name, listOfNotNull(age?.toString(), sex).joinToString(" ").ifBlank { null })
            .joinToString(" · ")

    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}
