package com.eka.medassist

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.eka.conversation.client.models.Environment
import com.eka.conversation.common.models.UserInfo
import com.eka.medassist.ui.chat.client.MedAssistSDK
import com.eka.medassist.ui.chat.presentation.activities.ActivityParams
import com.eka.medassist.ui.chat.presentation.activities.ChatScreenActivity
import com.eka.medassist.ui.chat.presentation.screens.ConversationHistoryScreen
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.theme.MedAssistTheme

class MainActivity : ComponentActivity() {
    val viewModel by viewModels<EkaChatViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()
        MedAssistSDK.initialise(
            context = this,
            debugMode = true,
            environment = Environment.PROD,
            agentId = BuildConfig.AGENT_ID
        )
        setContent {
            MedAssistTheme {
                ConversationHistoryScreen(
                    userInfo = UserInfo(
                        userId = "divyesh-test_2",
                        businessId = "divyesh-test_2"
                    ),
                    viewModel = viewModel,
                    onSessionClick = { sessionId ->
                        startActivity(Intent(this, ChatScreenActivity::class.java).apply {
                            putExtra(ActivityParams.SESSION_ID, sessionId)
                            putExtra(ActivityParams.OWNER_ID, "divyesh-test_2")
                            putExtra(ActivityParams.BUSINESS_ID, "divyesh-test_2")
                        })
                    },
                    onBackClick = {},
                )
            }
        }
    }
}