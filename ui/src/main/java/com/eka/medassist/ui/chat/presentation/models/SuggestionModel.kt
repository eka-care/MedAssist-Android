package com.eka.medassist.ui.chat.presentation.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SuggestionModel(
    @SerializedName("label")
    val label: String,
    @SerializedName("selected")
    val selected: Boolean = false,
)
