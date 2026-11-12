package com.eka.medassist.ui.chat.client

import android.content.Context
import com.eka.conversation.matrix.MatrixClient
import com.eka.conversation.matrix.MatrixClientConfig
import com.eka.conversation.matrix.MatrixEnvironment
import com.eka.medassist.ui.chat.data.AuthIdentity
import com.eka.medassist.ui.chat.data.ChatCache
import com.eka.medassist.ui.chat.data.MedAssistPrefs
import com.eka.medassist.ui.chat.logger.MedAssistLogger
import com.eka.medassist.ui.chat.presentation.activities.ChatScreenActivity
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistArgs

/**
 * Entry point. MedAssist talks to Matrix (eka-care/ai-chat :matrix) directly, so there is
 * nothing to initialise: open [ChatScreenActivity] with [launch], or embed
 * [com.eka.medassist.ui.chat.presentation.components.MedAssistChat] in your own Activity.
 */
object MedAssistSDK {
    const val TAG = "MedAssistSDK"

    fun setDebugMode(debugMode: Boolean) = MedAssistLogger.changeLogsVisibility(debugMode = debugMode)

    fun launch(context: Context, args: MedAssistArgs) {
        context.startActivity(ChatScreenActivity.intent(context, args))
    }

    /**
     * Warm the chat up ahead of time (e.g. when home opens): exchanges / refreshes the Matrix
     * tokens and caches the agent config, so the chat shows its greeting at once and skips the
     * token round trip when the doctor opens it. Cheap (two small calls); safe to call often.
     */
    suspend fun prefetch(
        context: Context,
        agentId: String,
        authToken: String?,
        environment: MatrixEnvironment = MatrixEnvironment.PRODUCTION,
    ) {
        if (agentId.isBlank()) return
        val app = context.applicationContext
        val prefs = MedAssistPrefs(app, agentId)
        val cache = ChatCache(app, agentId)
        // Same rule as the chat: another doctor's identity and chats must not carry over
        val fingerprint = AuthIdentity.of(authToken)
        if (prefs.lastAuthToken != fingerprint) {
            prefs.clearUser()
            cache.clear()
            prefs.lastAuthToken = fingerprint
        }
        val client = MatrixClient(
            context = app,
            config = MatrixClientConfig(agentId = agentId, authToken = authToken, environment = environment, userId = prefs.userId),
            seeded = prefs.credentials(),
            onTokenExchange = { t ->
                val previous = prefs.userId
                if ((t.userId != null && previous != null && t.userId != previous) || t.userIdChangeDetected == true) {
                    prefs.clearAllSessions()
                    cache.clear()
                }
                prefs.saveTokens(t)
            },
        )
        try {
            client.warmUp()
            client.getAgentConfig()?.let { cache.saveAgentConfig(it) }
        } finally {
            client.destroy()
        }
    }
}
