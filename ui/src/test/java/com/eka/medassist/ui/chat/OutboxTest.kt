package com.eka.medassist.ui.chat

import com.eka.medassist.ui.chat.common.Outbox
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutboxTest {

    @Test
    fun sendsInTheOrderTyped() {
        val box = Outbox<String>()
        box.add("a", "first")
        box.add("b", "second")
        assertEquals("first", box.next()?.second)
        assertEquals("second", box.next()?.second)
        assertNull(box.next())
    }

    @Test
    fun sameMessageIsNotQueuedTwice() {
        val box = Outbox<String>()
        box.add("a", "x")
        box.add("a", "x")
        assertEquals(1, box.size)
    }

    @Test
    fun lostMessageIsResentOnceAndFirst() {
        val box = Outbox<String>()
        box.add("queued", "typed offline")
        assertTrue(box.addRetry("lost", "lost one"))
        assertEquals("lost", box.next()?.first) // the lost message goes before newer ones
        assertFalse(box.addRetry("lost", "lost one")) // only one automatic resend
        assertEquals(listOf("queued"), box.ids())
    }

    @Test
    fun clearForgetsRetries() {
        val box = Outbox<String>()
        box.addRetry("lost", "x")
        box.clear()
        assertTrue(box.isEmpty())
        assertTrue(box.addRetry("lost", "x"))
    }
}
