package com.eka.medassist.ui.chat.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eka.conversation.matrix.MatrixEnvironment
import com.eka.medassist.ui.chat.client.MedAssistDoctor
import com.eka.medassist.ui.chat.client.MedAssistPatient
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
            patient = intent.getStringExtra(ActivityParams.PATIENT_NAME)?.let { name ->
                MedAssistPatient(
                    name = name,
                    age = intent.getIntExtra(ActivityParams.PATIENT_AGE, -1).takeIf { it >= 0 },
                    sex = intent.getStringExtra(ActivityParams.PATIENT_SEX),
                    oid = intent.getStringExtra(ActivityParams.PATIENT_OID),
                )
            },
            forceNewChat = intent.getBooleanExtra(ActivityParams.FORCE_NEW_CHAT, false),
            sessionId = intent.getStringExtra(ActivityParams.SESSION_ID),
            environment = intent.getStringExtra(ActivityParams.ENVIRONMENT)
                ?.let { runCatching { MatrixEnvironment.valueOf(it) }.getOrNull() }
                ?: MatrixEnvironment.PRODUCTION,
            extraContext = intent.getStringExtra(ActivityParams.EXTRA_CONTEXT)
                ?.let { runCatching { Json.parseToJsonElement(it).jsonObject }.getOrNull() },
            chatScope = intent.getStringExtra(ActivityParams.CHAT_SCOPE),
            doctor = intent.getStringExtra(ActivityParams.DOCTOR_FIRST_NAME)?.let {
                MedAssistDoctor(firstName = it, salutation = intent.getStringExtra(ActivityParams.DOCTOR_SALUTATION))
            },
            readingLabel = intent.getStringExtra(ActivityParams.READING_LABEL),
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
                args.patient?.let { p ->
                    putExtra(ActivityParams.PATIENT_NAME, p.name)
                    p.age?.let { putExtra(ActivityParams.PATIENT_AGE, it) }
                    putExtra(ActivityParams.PATIENT_SEX, p.sex)
                    putExtra(ActivityParams.PATIENT_OID, p.oid)
                }
                putExtra(ActivityParams.FORCE_NEW_CHAT, args.forceNewChat)
                putExtra(ActivityParams.SESSION_ID, args.sessionId)
                putExtra(ActivityParams.ENVIRONMENT, args.environment.name)
                args.extraContext?.let { putExtra(ActivityParams.EXTRA_CONTEXT, it.toString()) }
                putExtra(ActivityParams.CHAT_SCOPE, args.chatScope)
                args.doctor?.let {
                    putExtra(ActivityParams.DOCTOR_FIRST_NAME, it.firstName)
                    putExtra(ActivityParams.DOCTOR_SALUTATION, it.salutation)
                }
                putExtra(ActivityParams.READING_LABEL, args.readingLabel)
            }
    }
}
