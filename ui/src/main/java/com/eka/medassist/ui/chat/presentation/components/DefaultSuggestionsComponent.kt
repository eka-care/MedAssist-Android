package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.touchBodyRegular

@Composable
fun DefaultSuggestionsComponent(
    onSuggestionClicked: (SuggestionModel) -> Unit,
) {
    var defaultSuggestions by remember { mutableStateOf(emptyList<SuggestionModel>()) }
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Hello $name, how can I help you today?",
            style = touchBodyRegular.copy(color = DarwinTouchNeutral600)
        )
        Spacer(modifier = Modifier.height(16.dp))
        defaultSuggestions.forEach { suggestion ->
            Suggestion(suggestion = suggestion) {
                onSuggestionClicked(suggestion)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}