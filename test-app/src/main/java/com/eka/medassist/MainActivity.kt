package com.eka.medassist

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eka.medassist.ui.chat.client.MedAssistSDK
import com.eka.medassist.ui.chat.presentation.components.MedAssistChat
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistArgs

/** Sample host: guest (no partner token) MedAssist chat for AGENT_ID from local.properties. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()
        MedAssistSDK.setDebugMode(true)
        setContent {
            MedAssistChat(
                args = MedAssistArgs(
                    agentId = BuildConfig.AGENT_ID,
                    authToken = null,
                    title = "MedAssist",
                ),
                onClose = { finish() },
            )
        }
    }
}
