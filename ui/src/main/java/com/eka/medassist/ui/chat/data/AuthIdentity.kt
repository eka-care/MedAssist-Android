package com.eka.medassist.ui.chat.data

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Who a host auth token belongs to, for the "different doctor?" check. The token itself rotates
 * (the Eka sess token every ~30 min), so comparing it would wipe the chat on every refresh.
 * Uses the JWT's user id (oid, then uuid, then sub); falls back to the whole token when it isn't a JWT.
 */
internal object AuthIdentity {
    private val json = Json { ignoreUnknownKeys = true }

    fun of(
        token: String?,
        decode: (String) -> ByteArray = { Base64.decode(it, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP) },
    ): String? {
        if (token.isNullOrBlank()) return null
        val user = runCatching {
            val payload = token.split('.').takeIf { it.size == 3 }?.get(1) ?: return@runCatching null
            val claims = json.parseToJsonElement(String(decode(payload), Charsets.UTF_8)).jsonObject
            listOf("oid", "uuid", "sub").firstNotNullOfOrNull { key ->
                claims[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            }
        }.getOrNull()
        return if (user != null) "user:${user.hashCode()}" else "token:${token.hashCode()}"
    }
}
