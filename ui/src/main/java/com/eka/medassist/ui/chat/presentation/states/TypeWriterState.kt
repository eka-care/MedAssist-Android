package com.eka.medassist.ui.chat.presentation.states

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Typewriter for a streaming reply: reveals [text] smoothly while [streaming], catching up
 * faster when far behind, then finishes the remainder after the stream ends.
 * Messages that aren't streaming (history, restored chats) render in full straight away.
 */
@Composable
fun rememberTypewriterText(
    key: String,
    text: String,
    streaming: Boolean,
    frameMs: Long = 16L,
): String {
    var shown by remember(key) { mutableStateOf("") }
    var done by remember(key) { mutableStateOf(!streaming) }
    val latestText by rememberUpdatedState(text)
    val latestStreaming by rememberUpdatedState(streaming)

    LaunchedEffect(key) {
        if (done) return@LaunchedEffect
        while (true) {
            val target = latestText
            // Text was rewritten (not just appended) - restart from the shared prefix.
            if (!target.startsWith(shown)) shown = target.commonPrefixWith(shown)
            if (shown.length < target.length) {
                val behind = target.length - shown.length
                shown = target.take(shown.length + maxOf(1, behind / 12))
                delay(frameMs)
            } else if (latestStreaming) {
                delay(frameMs * 2)
            } else {
                break
            }
        }
        done = true
    }
    return if (done) text else shown
}
