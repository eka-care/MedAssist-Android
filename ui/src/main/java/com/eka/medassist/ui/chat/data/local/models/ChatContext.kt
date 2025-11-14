package com.eka.medassist.ui.chat.data.local.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChatContext(
    @SerializedName("patientId")
    val patientId: String = "",
    @SerializedName("patientName")
    val patientName: String = "",
)
