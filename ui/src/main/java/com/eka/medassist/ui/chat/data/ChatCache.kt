package com.eka.medassist.ui.chat.data

import android.content.Context
import com.eka.conversation.matrix.AgentConfig
import com.eka.conversation.matrix.PastSession
import com.eka.medassist.ui.chat.presentation.models.ChatUiMessage
import com.eka.medassist.ui.chat.presentation.models.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * On-phone copy of recent chats, the history list and the agent config, so DocAssist opens
 * instantly (and shows the last chat offline) while the server copy loads. Lives in the app's
 * private cache dir; cleared when a different doctor signs in. Text only - no files or tool UI.
 */
internal class ChatCache(context: Context, agentId: String) {

    private val dir = File(context.cacheDir, "medassist/${agentId.hashCode()}").apply { mkdirs() }
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun loadMessages(sessionId: String): List<ChatUiMessage>? = io {
        val file = sessionFile(sessionId).takeIf { it.exists() } ?: return@io null
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ChatUiMessage(
                id = o.getString("id"),
                content = o.optString("content"),
                role = if (o.optString("role") == "user") Role.USER else Role.ASSISTANT,
                isAudio = o.optBoolean("audio"),
                timestamp = o.optLong("ts"),
                feedback = o.optString("feedback", "NONE"),
            )
        }
    }

    suspend fun saveMessages(sessionId: String, messages: List<ChatUiMessage>) = io {
        val arr = JSONArray()
        messages.filter { it.content.isNotBlank() }.takeLast(MAX_MESSAGES).forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("content", m.content)
                put("role", if (m.role == Role.USER) "user" else "assistant")
                put("audio", m.isAudio)
                put("ts", m.timestamp)
                put("feedback", m.feedback)
            })
        }
        sessionFile(sessionId).writeText(arr.toString())
        prune()
    }

    suspend fun loadPastSessions(): List<PastSession>? = io {
        File(dir, HISTORY).takeIf { it.exists() }?.readText()
            ?.let { runCatching { json.decodeFromString(PastSessionList.serializer, it) }.getOrNull() }
    }

    suspend fun savePastSessions(sessions: List<PastSession>) = io {
        File(dir, HISTORY).writeText(json.encodeToString(PastSessionList.serializer, sessions))
    }

    fun loadAgentConfigBlocking(): AgentConfig? = runCatching {
        File(dir, CONFIG).takeIf { it.exists() }?.readText()?.let { json.decodeFromString(AgentConfig.serializer(), it) }
    }.getOrNull()

    suspend fun saveAgentConfig(cfg: AgentConfig) = io {
        File(dir, CONFIG).writeText(json.encodeToString(AgentConfig.serializer(), cfg))
    }

    /**
     * A different doctor signed in: nothing of the previous one may show. The agent config is the
     * agent's, not the doctor's, so it stays (the next chat opens without a shimmer).
     */
    fun clear() {
        dir.listFiles()?.filter { it.name != CONFIG }?.forEach { it.delete() }
    }

    private fun sessionFile(sessionId: String) = File(dir, "chat_${sessionId.filter { it.isLetterOrDigit() || it == '-' }}.json")

    /** Keep the most recent chats only. */
    private fun prune() {
        dir.listFiles { f -> f.name.startsWith("chat_") }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_CHATS)
            ?.forEach { it.delete() }
    }

    private suspend fun <T> io(block: () -> T): T? = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (_: Exception) {
            null
        }
    }

    private object PastSessionList {
        val serializer = kotlinx.serialization.builtins.ListSerializer(PastSession.serializer())
    }

    private companion object {
        const val MAX_CHATS = 10
        const val MAX_MESSAGES = 200
        const val HISTORY = "history.json"
        const val CONFIG = "agent_config.json"
    }
}
