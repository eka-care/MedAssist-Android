package com.eka.medassist.ui.chat.data

import android.content.Context
import com.eka.conversation.matrix.StoredCredentials
import com.eka.conversation.matrix.TokenExchangeResponse

/** Per-agent persistence (the web widget keeps the same fields in localStorage). */
class MedAssistPrefs(context: Context, agentId: String) {
    private val prefs = context.getSharedPreferences("medassist_" + agentId.hashCode(), Context.MODE_PRIVATE)

    fun credentials() = StoredCredentials(
        sessToken = prefs.getString(SESS, null),
        refreshToken = prefs.getString(REFRESH, null),
        level = prefs.getString(LEVEL, null),
        userId = prefs.getString(USER, null),
    )

    var sessionId: String?
        get() = prefs.getString(SESSION, null)
        set(value) = prefs.edit().putString(SESSION, value).apply()

    /** Chat to resume for [scope] (null = the regular chat); like the web's session-scoped chat storage. */
    fun sessionId(scope: String?): String? = prefs.getString(sessionKey(scope), null)

    fun setSessionId(scope: String?, value: String?) = prefs.edit().putString(sessionKey(scope), value).apply()

    fun clearSession(scope: String?) = prefs.edit().remove(sessionKey(scope)).apply()

    private fun sessionKey(scope: String?) = if (scope.isNullOrBlank()) SESSION else "$SESSION:$scope"

    var userId: String?
        get() = prefs.getString(USER, null)
        set(value) = prefs.edit().putString(USER, value).apply()

    val level: String? get() = prefs.getString(LEVEL, null)

    var lastAuthToken: String?
        get() = prefs.getString(PARTNER, null)
        set(value) = prefs.edit().putString(PARTNER, value).apply()

    fun saveTokens(t: TokenExchangeResponse) {
        prefs.edit()
            .putString(SESS, t.sessToken)
            .putString(REFRESH, t.refreshToken)
            .apply {
                t.level?.let { putString(LEVEL, it) }
                t.userId?.let { putString(USER, it) }
            }
            .apply()
    }

    fun clearSession() {
        prefs.edit().remove(SESSION).apply()
    }

    /** Drop identity + session (403 / terminal auth failure / doctor switched). */
    fun clearUser() {
        prefs.edit().remove(SESS).remove(REFRESH).remove(LEVEL).remove(USER).apply()
        clearAllSessions()
    }

    /** Every stored chat, scoped ones included (they all belong to the current identity). */
    fun clearAllSessions() {
        prefs.edit().apply {
            prefs.all.keys.filter { it == SESSION || it.startsWith("$SESSION:") }.forEach { remove(it) }
        }.apply()
    }

    private companion object {
        const val SESS = "sess_token"
        const val REFRESH = "refresh_token"
        const val LEVEL = "level"
        const val USER = "user_id"
        const val SESSION = "session_id"
        const val PARTNER = "partner_token_hash"
    }
}
