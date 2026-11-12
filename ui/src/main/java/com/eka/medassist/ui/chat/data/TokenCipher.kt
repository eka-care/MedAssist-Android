package com.eka.medassist.ui.chat.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts the stored Matrix tokens with an AES-GCM key kept in the Android Keystore (the key never
 * leaves it). Stored form: "enc:" + base64(iv + ciphertext). A value without the prefix is a token
 * saved before encryption and is returned as is (the caller re-saves it encrypted).
 * If the key is gone (app data restored on another phone, keystore reset) decrypt returns null,
 * which just means a fresh token exchange.
 */
internal object TokenCipher {
    private const val KEY_ALIAS = "medassist_tokens"
    private const val PREFIX = "enc:"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    fun isEncrypted(value: String?) = value?.startsWith(PREFIX) == true

    fun encrypt(plain: String?): String? {
        if (plain == null) return null
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
            val out = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            PREFIX + Base64.encodeToString(out, Base64.NO_WRAP)
        }.getOrNull() // keystore unavailable: don't store the token at all (next open re-exchanges)
    }

    fun decrypt(stored: String?): String? {
        if (stored == null) return null
        if (!isEncrypted(stored)) return stored
        return runCatching {
            val raw = Base64.decode(stored.removePrefix(PREFIX), Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, raw, 0, IV_BYTES))
            }
            String(cipher.doFinal(raw, IV_BYTES, raw.size - IV_BYTES), Charsets.UTF_8)
        }.getOrNull()
    }

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }
}
