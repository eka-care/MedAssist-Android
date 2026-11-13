package com.eka.medassist.ui.chat

import com.eka.medassist.ui.chat.data.AuthIdentity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class AuthIdentityTest {
    private val decode: (String) -> ByteArray = { Base64.getUrlDecoder().decode(it) }

    private fun jwt(claims: String): String {
        val enc = Base64.getUrlEncoder().withoutPadding()
        return enc.encodeToString("""{"alg":"HS256"}""".toByteArray()) + "." +
            enc.encodeToString(claims.toByteArray()) + ".sig"
    }

    @Test fun sameDoctorAcrossTokenRefreshes() {
        val a = jwt("""{"oid":"175620","iat":1,"exp":2,"jti":"a"}""")
        val b = jwt("""{"oid":"175620","iat":3,"exp":4,"jti":"b"}""")
        assertNotEquals(a, b)
        assertEquals(AuthIdentity.of(a, decode), AuthIdentity.of(b, decode))
    }

    @Test fun differentDoctorsDiffer() {
        assertNotEquals(
            AuthIdentity.of(jwt("""{"oid":"1"}"""), decode),
            AuthIdentity.of(jwt("""{"oid":"2"}"""), decode),
        )
    }

    @Test fun fallsBackToUuidThenWholeToken() {
        assertEquals(
            AuthIdentity.of(jwt("""{"uuid":"u-1","jti":"x"}"""), decode),
            AuthIdentity.of(jwt("""{"uuid":"u-1","jti":"y"}"""), decode),
        )
        assertEquals("token:${"opaque".hashCode()}", AuthIdentity.of("opaque", decode))
    }

    @Test fun noTokenIsNoIdentity() {
        assertNull(AuthIdentity.of(null, decode))
        assertNull(AuthIdentity.of("", decode))
    }
}
