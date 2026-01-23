package com.eka.medassist.ui.chat.client

import android.content.Context
import com.eka.conversation.client.ChatSDK
import com.eka.conversation.client.models.Environment
import com.eka.conversation.common.models.AuthConfiguration
import com.eka.conversation.common.models.ChatConfiguration
import com.eka.medassist.ui.chat.logger.MedAssistLogger
import com.eka.networking.client.NetworkConfig

object MedAssistSDK {
    const val TAG = "MedAssistSDK"
    fun initialise(
        agentId : String,
        context: Context,
        environment: Environment,
        networkConfig: NetworkConfig,
        debugMode : Boolean = false
    ) {
        MedAssistLogger.changeLogsVisibility(debugMode = debugMode)
        ChatSDK.initialize(
            context = context,
            chatConfiguration = ChatConfiguration(
                environment = environment,
                networkConfig = networkConfig,
                authConfiguration = AuthConfiguration(
                    agentId = agentId
                ),
            )
        )
    }
}