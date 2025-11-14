package com.eka.medassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eka.medassist.ui.chat.navigation.ChatScreenNavModel
import com.eka.medassist.ui.chat.presentation.screens.EkaChatBotMainScreen
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.theme.MedAssistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedAssistTheme {
                EkaChatBotMainScreen(
                    navData = ChatScreenNavModel(sessionId = null),
                    viewModel = EkaChatViewModel(app = application)
                ) { }
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