package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.common.molecule.ButtonWrapper
import com.eka.medassist.ui.chat.presentation.common.molecule.ButtonWrapperType
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral400
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchCalloutRegular

@Composable
fun SingleSelectSuggestion(
    suggestionList : List<SuggestionModel>,
    onSuggestionClicked : (SuggestionModel) -> Unit,
) {
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
            SuggestionRow(suggestion = suggestion) {
                onSuggestionClicked(suggestion)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SuggestionRow(
    suggestion: SuggestionModel,
    isSelectable : Boolean = false,
    onSuggestionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 8.dp))
            .clickable {
                onSuggestionClick()
            }
            .background(color = DarwinTouchNeutral0, shape = RoundedCornerShape(size = 8.dp))
            .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if(isSelectable) {
            Checkbox(
                modifier = Modifier.size(24.dp).padding(end = 8.dp),
                checked = suggestion.selected,
                onCheckedChange = {
                    onSuggestionClick()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = DarwinTouchPrimary,
                    uncheckedColor = DarwinTouchNeutral400
                )
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = suggestion.label,
            style = touchCalloutRegular,
            color = DarwinTouchPrimary
        )
    }
}

@Composable
fun MultiSelectSuggestions(
    suggestionList: List<SuggestionModel>,
    onConfirm : (SuggestionModel) -> Unit
) {
    val suggestions = remember { suggestionList.toMutableStateList() }

    val onSuggestionClicked : (Int , SuggestionModel) -> Unit = { index, suggestion ->
        suggestions[index] = suggestion.copy(selected = !suggestion.selected)
    }

    Column(
        modifier = Modifier
            .padding(start = 0.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Text(
            modifier = Modifier,
            text = "Suggested questions you can select multiple - ",
            style = touchBodyRegular,
            color = DarwinTouchNeutral1000
        )
        Spacer(modifier = Modifier.height(8.dp))
        suggestions.forEachIndexed { index, suggestion ->
            SuggestionRow(suggestion = suggestion, isSelectable = true) {
                onSuggestionClicked(index, suggestion)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        ButtonWrapper(
            text = "Confirm",
            type = ButtonWrapperType.OUTLINED,
            borderColor = DarwinTouchPrimary,
            onClick = {
                val combinedQuery = suggestions.filter { it.selected }.joinToString(", ") { it.label }
                onConfirm(SuggestionModel(label = combinedQuery))
            }
        )
    }
}