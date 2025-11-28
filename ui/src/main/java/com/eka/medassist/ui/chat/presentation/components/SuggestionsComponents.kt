package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.presentation.states.SuggestionType

@Composable
fun SuggestionsComponent(
    onSuggestionClicked: (SuggestionModel) -> Unit,
    suggestionList : List<SuggestionModel>,
    suggestionType: SuggestionType,
    showLeftIcon: Boolean,
) {
    val iconAlpha = if (showLeftIcon) 1f else 0f

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
                when(suggestionType) {
                    SuggestionType.SINGLE_SELECT -> SingleSelectSuggestion(
                        suggestionList = suggestionList,
                        onSuggestionClicked = onSuggestionClicked
                    )
                    SuggestionType.MULTI_SELECT -> MultiSelectSuggestions(
                        suggestionList = suggestionList,
                        onConfirm = onSuggestionClicked
                    )
                }
            },
            background = Color.Transparent
        )
    }
}