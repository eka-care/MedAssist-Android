package com.eka.medassist.ui.chat.common

fun String.isValidInput(): Boolean {
    return this.isNotBlank() && this.any { !it.isWhitespace() }
}