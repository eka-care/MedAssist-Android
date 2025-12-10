package com.eka.medassist

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.eka.conversation.common.models.UserInfo
import com.eka.medassist.ui.chat.presentation.screens.ConversationScreen
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.theme.MedAssistTheme

class ChatScreenActivity : ComponentActivity() {
    val viewModel by viewModels<EkaChatViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()
        val sessionId = intent.getStringExtra("sessionId")
        val userInfo = UserInfo(
            userId = "divyesh-test_2",
            businessId = "divyesh-test_2"
        )
        setContent {
            MedAssistTheme {
                ConversationScreen(
                    userInfo = userInfo,
                    viewModel = viewModel,
                    sessionId = sessionId,
                    onBackClick = {
                        onBackPressedDispatcher.onBackPressed()
                    },
                    askMicrophonePermission = {}
                )
            }
        }
    }
}