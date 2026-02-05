package com.eka.medassist.ui.chat.presentation.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.eka.conversation.common.models.UserInfo
import com.eka.medassist.ui.chat.presentation.components.ChatScreenComponent
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel

class ChatScreenActivity : ComponentActivity() {
    val viewModel by viewModels<EkaChatViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()
        val sessionId = intent.getStringExtra(ActivityParams.SESSION_ID)
        val ownerId = intent.getStringExtra(ActivityParams.OWNER_ID) ?: "default"
        val businessId = intent.getStringExtra(ActivityParams.BUSINESS_ID) ?: "default"
        val userInfo = UserInfo(
            userId = ownerId,
            businessId = businessId
        )
        setContent {
            ChatScreenComponent(
                viewModel = viewModel,
                userInfo = userInfo,
                sessionId = sessionId,
                onBackClick = { onBackPressedDispatcher.onBackPressed() }
            )
        }
    }
}