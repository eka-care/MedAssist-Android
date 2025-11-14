package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.common.molecule.ButtonWrapper
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0

@Composable
fun BottomSheetButtonWrapper(
    buttonTitle: String,
    leadingIcon: Int? = null,
    leadingIconTint: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarwinTouchNeutral0)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ButtonWrapper(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClick,
                icon = leadingIcon,
                text = buttonTitle
            )
        }
    }
}