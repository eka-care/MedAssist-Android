package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.touchBodyRegular

@Composable
fun SuggestionsComponent(
    viewModel: EkaChatViewModel,
    onSuggestionClicked: (SuggestionModel) -> Unit,
    showLeftIcon: Boolean,
) {
    val iconAlpha = if (showLeftIcon) 1f else 0f

    val suggestionList by viewModel.suggestionList.collectAsState()

    Row(
        modifier = Modifier
            .padding(top = 4.dp)
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .alpha(iconAlpha)
                .size(32.dp),
            painter = painterResource(id = R.drawable.ic_ai_chat_custom),
            tint = Color.Unspecified,
            contentDescription = ""
        )
        BorderCard(
            modifier = Modifier
                .weight(1f),
            border = BorderStroke(width = 0.dp, color = Color.Transparent),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            content = {
                Column(
                    modifier = Modifier
                        .padding(start = 0.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "Suggested questions you can ask me-",
                        style = touchBodyRegular,
                        color = DarwinTouchNeutral1000
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    suggestionList.forEach { suggestion ->
                        if (suggestion != null) {
                            Suggestion(suggestion = suggestion) {
                                onSuggestionClicked(suggestion)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            },
            background = Color.Transparent
        )
    }
}