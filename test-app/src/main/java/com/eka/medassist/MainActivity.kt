package com.eka.medassist

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eka.conversation.client.models.Environment
import com.eka.conversation.common.models.UserInfo
import com.eka.medassist.ui.chat.client.MedAssistSDK
import com.eka.medassist.ui.chat.presentation.screens.ConversationScreen
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.theme.MedAssistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()
        MedAssistSDK.initialise(
            context = this,
            environment = Environment.PROD,
            agentId = "agentId"
        )
        setContent {
            MedAssistTheme {
                ConversationScreen(
                    userInfo = UserInfo(
                        userId = "divyesh-test_2",
                        businessId = "divyesh-test_2"
                    ),
                    EkaChatViewModel(app = application),
                    onBackClick = {},
                    askMicrophonePermission = {}
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MedAssistTheme {
        Greeting("Android")
    }
}