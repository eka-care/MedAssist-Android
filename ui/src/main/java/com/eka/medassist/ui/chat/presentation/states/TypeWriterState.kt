package com.eka.medassist.ui.chat.presentation.states

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.eka.conversation.client.models.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
class TypewriterState(
    private val charDelayMs: Long = 30L,
    private val scope: CoroutineScope
) {
    private val _displayedMessage = MutableStateFlow("")
    val displayedMessage: StateFlow<String> = _displayedMessage.asStateFlow()

    val currentMessage = mutableStateOf<Message.Text?>(null)

    private var targetText = ""
    private var emitJob: Job? = null

    private var jobCompleted : Boolean = false

    fun updateFullMessage(fullMessage : Message?) {
        val newMessage = fullMessage as? Message.Text
        if(newMessage == null) {
            return
        }
        // Only process if there's actually new content
        if (newMessage.text.length <= targetText.length) return

        currentMessage.value = fullMessage

        targetText = newMessage.text

        // Start emitting if not already running
        if (emitJob?.isActive != true) {
            startEmitting()
        }
    }

    private fun startEmitting() {
        jobCompleted = false
        emitJob = scope.launch {
            while (true) {
                if(_displayedMessage.value.length >= targetText.length) {
                    if(jobCompleted) {
                        reset()
                        cancel()
                        break
                    } else {
                        delay(timeMillis = charDelayMs)
                        continue
                    }
                }
                val currentLength = _displayedMessage.value.length
                val nextChar = targetText[currentLength]
                _displayedMessage.update { it + nextChar }
                delay(timeMillis = charDelayMs)
            }
        }
        emitJob?.start()
    }

    fun reset() {
        _displayedMessage.value = ""
        jobCompleted = false
        targetText = ""
        currentMessage.value = null
    }

    fun complete() {
        jobCompleted = true
    }
}