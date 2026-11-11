package com.eka.medassist.ui.chat.client

import android.content.Context
import com.eka.medassist.ui.chat.logger.MedAssistLogger
import com.eka.medassist.ui.chat.presentation.activities.ChatScreenActivity
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistArgs

/**
 * Entry point. MedAssist talks to Matrix (eka-care/ai-chat :matrix) directly, so there is
 * nothing to initialise: open [ChatScreenActivity] with [launch], or embed
 * [com.eka.medassist.ui.chat.presentation.components.MedAssistChat] in your own Activity.
 */
object MedAssistSDK {
    const val TAG = "MedAssistSDK"

    fun setDebugMode(debugMode: Boolean) = MedAssistLogger.changeLogsVisibility(debugMode = debugMode)

    fun launch(context: Context, args: MedAssistArgs) {
        context.startActivity(ChatScreenActivity.intent(context, args))
    }
}
