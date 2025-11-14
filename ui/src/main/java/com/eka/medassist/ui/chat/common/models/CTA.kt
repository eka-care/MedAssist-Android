package com.eka.medassist.ui.chat.common.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CTA(
    @SerializedName("action")
    var action: String? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("pid")
    var pageId: String? = null,
    @SerializedName("params")
    var params: MutableMap<String, String>? = null,
    @SerializedName("syncid")
    val syncId: String? = null
)