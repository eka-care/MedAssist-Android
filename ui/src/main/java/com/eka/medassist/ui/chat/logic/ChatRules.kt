package com.eka.medassist.ui.chat.logic

import com.eka.conversation.matrix.MatrixApiException
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.conversation.matrix.StoredMessage
import com.eka.conversation.matrix.str
import com.eka.medassist.ui.chat.client.MedAssistPatient
import com.eka.medassist.ui.chat.presentation.models.ChatUiMessage
import com.eka.medassist.ui.chat.presentation.models.ElicitationEntry
import com.eka.medassist.ui.chat.presentation.models.Role
import com.eka.medassist.ui.chat.presentation.models.UiError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Chat rules that depend only on their arguments: limits, identity, the session context payload
 * and the stored-transcript shape. Out of the ViewModel so they can be read and unit-tested
 * without a session, a connection or Android.
 */
internal object ChatRules {

    /** Two references to the same person. Ids win; a chat started by name has no oid. */
    fun samePatient(a: MedAssistPatient, b: MedAssistPatient) =
        if (a.oid != null && b.oid != null) a.oid == b.oid else a.name == b.name

    /** How many more files this message can take, per message and per session. */
    fun attachmentSlotsLeft(attached: Int, usedThisSession: Int): Int = minOf(
        MatrixFile.MAX_FILES_PER_MESSAGE - attached,
        MatrixFile.MAX_FILES_PER_SESSION - usedThisSession - attached,
    ).coerceAtLeast(0)

    /** Why a batch of picked files cannot be attached, or null when it can. */
    fun validateBatch(files: List<MatrixFile>, alreadyAttached: Int = 0): UiError? {
        if (files.size + alreadyAttached > MatrixFile.MAX_FILES_PER_MESSAGE) {
            return UiError("You can only upload up to ${MatrixFile.MAX_FILES_PER_MESSAGE} files at a time", "Please remove some files and try again.")
        }
        if (files.sumOf { it.size } > MatrixFile.MAX_TOTAL_SIZE) {
            return UiError("Total file size limit of 10 MB exceeded", "Please reduce the size of the files or try again with less files")
        }
        return null
    }

    /** What the server says went wrong, else the exception's own message. */
    fun errorText(t: Throwable): String =
        (t as? MatrixApiException)?.body?.msg ?: t.message ?: "An unexpected error occurred"

    /** session_context for a new chat: the host's extra context plus the attached patient. */
    fun buildContext(extra: Map<String, JsonElement>?, patient: MedAssistPatient?): JsonObject? {
        if (patient == null && extra == null) return null
        return buildJsonObject {
            extra?.forEach { (k, v) -> put(k, v) }
            if (extra?.containsKey("patient") == true) {
                put("patient", patient?.let {
                    buildJsonObject {
                        put("name", it.name)
                        it.age?.let { a -> put("age", a) }
                        it.sex?.let { s -> put("sex", s) }
                    }
                } ?: JsonNull)
            }
            if (patient != null) put("profile", buildJsonObject {
                put("name", patient.name)
                patient.age?.let { put("age", it) }
                patient.sex?.let { put("gender", it) }
                patient.oid?.let { put("oid", it) }
            })
        }
    }

    /** A stored transcript as messages plus the elicitations that belong to them. */
    fun restoreTranscript(stored: List<StoredMessage>): Pair<List<ChatUiMessage>, Map<String, ElicitationEntry>> {
        val out = mutableListOf<ChatUiMessage>()
        val entries = mutableMapOf<String, ElicitationEntry>()
        for (m in stored) {
            val ts = m.timestamp?.let { if (it < 1e12) (it * 1000).toLong() else it.toLong() } ?: System.currentTimeMillis()
            val usable = m.elicitations.orEmpty().filter { it.str("tool_id") != null && it.str("component") != null }
            out += ChatUiMessage(
                id = m.id, content = m.content, role = if (m.role == "user") Role.USER else Role.ASSISTANT, timestamp = ts,
                toolKeys = usable.map { "${m.id}::${it.str("tool_id")}" }, isResponseFromTool = usable.isNotEmpty(),
            )
            for (e in usable) {
                val toolId = e.str("tool_id")!!
                val answer = e.str("answer")
                entries["${m.id}::$toolId"] = ElicitationEntry(
                    toolId = toolId, messageId = m.id, component = e.str("component"),
                    input = e["input"] as? JsonObject, active = false, disabled = true, answer = answer,
                )
                if (!answer.isNullOrBlank()) out += ChatUiMessage("${m.id}::$toolId::answer", answer, Role.USER, timestamp = ts)
            }
        }
        return out to entries
    }
}
