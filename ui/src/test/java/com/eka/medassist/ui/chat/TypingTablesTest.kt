package com.eka.medassist.ui.chat

import com.eka.medassist.ui.chat.presentation.screens.hideTablesWhileTyping
import org.junit.Assert.assertEquals
import org.junit.Test

class TypingTablesTest {
    @Test fun tableBecomesOneLine() {
        val text = "Summary:\n| Panel | Findings |\n|---|---|\n| Lipids | TC 242 |\nNext steps"
        assertEquals("Summary:\n_Preparing table…_\nNext steps", hideTablesWhileTyping(text))
    }

    @Test fun halfTypedTableAtTheEnd() {
        assertEquals("Intro\n_Preparing table…_", hideTablesWhileTyping("Intro\n| Panel | Fin"))
    }

    @Test fun noTableUnchanged() {
        assertEquals("a | b is fine inline", hideTablesWhileTyping("a | b is fine inline"))
    }
}
