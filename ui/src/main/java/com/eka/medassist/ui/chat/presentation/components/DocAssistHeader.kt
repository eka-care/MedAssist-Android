package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.screens.BotViewMode
import com.eka.medassist.ui.chat.presentation.screens.EkaBotModeData
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral300
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocAssistHeader(
    modifier: Modifier = Modifier,
    selectedOption: BotViewMode,
    onOptionSelected: (BotViewMode) -> Unit,
    options: List<EkaBotModeData>
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                colors = SegmentedButtonDefaults.colors().copy(
                    activeBorderColor = DarwinTouchPrimary,
                    activeContainerColor = DarwinTouchPrimaryBgLight,
                    activeContentColor = DarwinTouchPrimary,
                    inactiveBorderColor = DarwinTouchNeutral300,
                    inactiveContainerColor = DarwinTouchNeutral0,
                    inactiveContentColor = DarwinTouchNeutral1000
                ),
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onOptionSelected(option.type) },
                selected = selectedOption == option.type,
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}