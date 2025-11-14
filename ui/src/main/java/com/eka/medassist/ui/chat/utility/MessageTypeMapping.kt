package com.eka.medassist.ui.chat.utility

import com.eka.medassist.ui.chat.data.local.models.MessageType

class MessageTypeMapping {
    companion object {
        fun getSubHeadline(messageType: String): String {
            return when (messageType) {
                MessageType.TEXT.stringValue -> "Chat"
                else -> ""
            }
        }
    }
}