package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.ui.graphics.Color

data class EkaChatPatientsRowData(
    val nameTag: String? = null,
    val headlineText: String? = null,
    val subHeadline: String? = null,
    val draftNumber: Int? = null,
    val draftNumberColor: Color? = null,
    val spaceBetweenSubHeadlineAndTime: Boolean,
    val time: String? = null,
    val icon: Int? = null,
    val iconSize: Int? = null,
    val iconColor : Color? = null,
    val backgroundColor: Color? = null
)
