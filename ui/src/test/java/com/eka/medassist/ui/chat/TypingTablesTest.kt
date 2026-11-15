package com.eka.medassist.ui.chat

import com.eka.medassist.ui.chat.presentation.components.isLongPrompt
import com.eka.medassist.ui.chat.presentation.components.markdownBlocks
import com.eka.medassist.ui.chat.presentation.components.promptPreview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TypingTablesTest {
    private val table = "| Panel | Findings |\n|---|---|\n| Lipids | TC 242 |"

    @Test fun finishedTableShowsWhileTextAfterItTypes() {
        val blocks = markdownBlocks("Summary:\n$table\nNext ste", typing = true)
        assertEquals(listOf("Summary:", table, "Next ste"), blocks.map { it.text })
        assertEquals(listOf(false, true, false), blocks.map { it.isTable })
        assertTrue(blocks.none { it.preparingTable })
    }

    @Test fun tableStillArrivingIsPreparing() {
        val blocks = markdownBlocks("Summary:\n| Panel | Fin", typing = true)
        assertTrue(blocks.last().preparingTable)
    }

    @Test fun lastTableDrawnOnceTypingEnds() {
        assertFalse(markdownBlocks("Summary:\n$table", typing = false).last().preparingTable)
    }

    @Test fun noTableIsOneBlock() {
        assertEquals(1, markdownBlocks("a | b inline\nsecond line", typing = true).size)
    }

    @Test fun longPromptCollapses() {
        assertFalse(isLongPrompt("short question"))
        val long = (1..10).joinToString("\n") { "line $it" }
        assertTrue(isLongPrompt(long))
        assertEquals((1..6).joinToString("\n") { "line $it" } + "…", promptPreview(long))
    }
}
