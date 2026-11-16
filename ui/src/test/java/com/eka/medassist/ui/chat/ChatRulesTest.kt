package com.eka.medassist.ui.chat

import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.chat.client.MedAssistPatient
import com.eka.medassist.ui.chat.logic.ChatRules
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatRulesTest {

    private fun file(name: String, size: Int = 1) =
        MatrixFile(name = name, mimeType = "application/pdf", bytes = ByteArray(size))

    private fun patient(name: String, oid: String? = null) =
        MedAssistPatient(name = name, oid = oid)

    @Test fun oidDecidesIdentityOverName() {
        assertTrue(ChatRules.samePatient(patient("Ravi K", "o1"), patient("Ravi Kumar", "o1")))
        assertFalse(ChatRules.samePatient(patient("Ravi", "o1"), patient("Ravi", "o2")))
    }

    @Test fun nameDecidesWhenEitherHasNoOid() {
        assertTrue(ChatRules.samePatient(patient("Ravi"), patient("Ravi", "o1")))
        assertFalse(ChatRules.samePatient(patient("Ravi"), patient("Sam")))
    }

    @Test fun slotsLeftTakesTheTighterOfMessageAndSessionLimits() {
        assertEquals(MatrixFile.MAX_FILES_PER_MESSAGE, ChatRules.attachmentSlotsLeft(0, 0))
        assertEquals(MatrixFile.MAX_FILES_PER_MESSAGE - 1, ChatRules.attachmentSlotsLeft(1, 0))
        assertEquals(0, ChatRules.attachmentSlotsLeft(0, MatrixFile.MAX_FILES_PER_SESSION))
    }

    @Test fun slotsLeftNeverGoesNegative() {
        assertEquals(0, ChatRules.attachmentSlotsLeft(99, MatrixFile.MAX_FILES_PER_SESSION + 5))
    }

    @Test fun aBatchWithinBothLimitsIsAccepted() {
        assertNull(ChatRules.validateBatch(listOf(file("a.pdf"))))
    }

    @Test fun tooManyFilesIsRejected() {
        val files = List(MatrixFile.MAX_FILES_PER_MESSAGE + 1) { file("f$it.pdf") }
        assertNotNull(ChatRules.validateBatch(files))
    }

    @Test fun alreadyAttachedCountsTowardsTheLimit() {
        val files = List(MatrixFile.MAX_FILES_PER_MESSAGE) { file("f$it.pdf") }
        assertNull(ChatRules.validateBatch(files))
        assertNotNull(ChatRules.validateBatch(files, alreadyAttached = 1))
    }

    @Test fun oversizeBatchIsRejected() {
        val big = file("big.pdf", MatrixFile.MAX_TOTAL_SIZE + 1)
        assertNotNull(ChatRules.validateBatch(listOf(big)))
    }

    @Test fun noPatientAndNoExtraContextMeansNoContext() {
        assertNull(ChatRules.buildContext(null, null))
    }

    @Test fun aPatientBecomesTheProfile() {
        val ctx = ChatRules.buildContext(null, MedAssistPatient(name = "Ravi", age = 42, sex = "M", oid = "o1"))
        val profile = ctx!!["profile"]!!.jsonObject
        assertEquals("Ravi", profile["name"]!!.jsonPrimitive.content)
        assertEquals("42", profile["age"]!!.jsonPrimitive.content)
        assertEquals("M", profile["gender"]!!.jsonPrimitive.content)
        assertEquals("o1", profile["oid"]!!.jsonPrimitive.content)
    }

    @Test fun hostContextIsCarriedThrough() {
        val ctx = ChatRules.buildContext(mapOf("visit" to JsonPrimitive("v1")), null)
        assertEquals("v1", ctx!!["visit"]!!.jsonPrimitive.content)
    }

    @Test fun errorTextFallsBackToTheExceptionMessage() {
        assertEquals("boom", ChatRules.errorText(IllegalStateException("boom")))
        assertEquals("An unexpected error occurred", ChatRules.errorText(RuntimeException()))
    }
}
