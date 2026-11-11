package com.eka.medassist.ui.chat.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eka.conversation.matrix.MatrixEnvironment
import com.eka.medassist.ui.chat.presentation.components.MedAssistChat
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistArgs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class ChatScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()

        val agentId = intent.getStringExtra(ActivityParams.AGENT_ID)
        if (agentId.isNullOrBlank()) {
            finish()
            return
        }
        val args = MedAssistArgs(
            agentId = agentId,
            authToken = intent.getStringExtra(ActivityParams.AUTH_TOKEN),
            title = intent.getStringExtra(ActivityParams.TITLE) ?: "MedAssist",
            patientName = intent.getStringExtra(ActivityParams.PATIENT_NAME),
            patientOid = intent.getStringExtra(ActivityParams.PATIENT_OID),
            forceNewChat = intent.getBooleanExtra(ActivityParams.FORCE_NEW_CHAT, false),
            sessionId = intent.getStringExtra(ActivityParams.SESSION_ID),
            environment = intent.getStringExtra(ActivityParams.ENVIRONMENT)
                ?.let { runCatching { MatrixEnvironment.valueOf(it) }.getOrNull() }
                ?: MatrixEnvironment.PRODUCTION,
            extraContext = intent.getStringExtra(ActivityParams.EXTRA_CONTEXT)
                ?.let { runCatching { Json.parseToJsonElement(it).jsonObject }.getOrNull() },
            chatScope = intent.getStringExtra(ActivityParams.CHAT_SCOPE),
        )
        setContent {
            MedAssistChat(args = args, onClose = { finish() })
        }
    }

    companion object {
        fun intent(context: Context, args: MedAssistArgs): Intent =
            Intent(context, ChatScreenActivity::class.java).apply {
                putExtra(ActivityParams.AGENT_ID, args.agentId)
                putExtra(ActivityParams.AUTH_TOKEN, args.authToken)
                putExtra(ActivityParams.TITLE, args.title)
                putExtra(ActivityParams.PATIENT_NAME, args.patientName)
                putExtra(ActivityParams.PATIENT_OID, args.patientOid)
                putExtra(ActivityParams.FORCE_NEW_CHAT, args.forceNewChat)
                putExtra(ActivityParams.SESSION_ID, args.sessionId)
                putExtra(ActivityParams.ENVIRONMENT, args.environment.name)
                args.extraContext?.let { putExtra(ActivityParams.EXTRA_CONTEXT, it.toString()) }
                putExtra(ActivityParams.CHAT_SCOPE, args.chatScope)
            }
    }
}
