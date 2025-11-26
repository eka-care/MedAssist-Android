package com.eka.medassist.ui.chat.client

import android.content.Context
import com.eka.conversation.client.ChatInit
import com.eka.conversation.client.models.Environment
import com.eka.conversation.common.models.AuthConfiguration
import com.eka.conversation.common.models.ChatInitConfiguration
import com.eka.medassist.ui.chat.logger.MedAssistLogger
import com.eka.medassist.ui.chat.utility.MedAssistConstants
import com.eka.networking.client.NetworkConfig
import com.eka.networking.token.TokenStorage

object MedAssistSDK {
    const val TAG = "MedAssistSDK"
    fun initialise(
        agentId : String = "NDBkNmM4OTEtNGEzMC00MDBlLWE4NjEtN2ZkYjliMDY2MDZhI2VrYV9waHI=",
        debugMode : Boolean = false,
        context: Context
    ) {
        MedAssistLogger.changeLogsVisibility(debugMode = debugMode)
        ChatInit.initialize(
            context = context,
            chatInitConfiguration = ChatInitConfiguration(
                environment = Environment.PROD,
                networkConfig = NetworkConfig(
                    appId = MedAssistConstants.APP_ID,
                    baseUrl = MedAssistConstants.BASE_URL,
                    appVersionCode = 1,
                    appVersionName = MedAssistConstants.APP_VERSION_NAME,
                    headers = emptyMap(),
                    isDebugApp = debugMode,
                    tokenStorage = object : TokenStorage {
                        override fun getAccessToken(): String {
                            MedAssistLogger.d(TAG, "getAccessToken")
                            return ""
                        }

                        override fun getRefreshToken(): String {
                            MedAssistLogger.d(TAG, "getRefreshToken")
                            return ""
                        }

                        override fun saveTokens(
                            accessToken: String,
                            refreshToken: String
                        ) {
                            MedAssistLogger.d(TAG, "saveTokens")
                        }

                        override fun onSessionExpired() {
                            MedAssistLogger.d(TAG, "onSessionExpired")
                        }
                    }
                ),
                authConfiguration = AuthConfiguration(
                    agentId = agentId
                ),
            )
        )
    }
}