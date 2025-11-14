package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchCalloutRegular

@Composable
fun Suggestion(
    suggestion: SuggestionModel,
    onSuggestionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 8.dp))
            .clickable {
                onSuggestionClick()
            }
            .background(color = DarwinTouchNeutral0, shape = RoundedCornerShape(size = 8.dp))
            .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
    ) {
        Text(
            text = suggestion.label,
            style = touchCalloutRegular,
            color = DarwinTouchPrimary
        )
    }
}