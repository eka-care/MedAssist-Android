package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.presentation.common.organism.AppBar
import com.eka.medassist.ui.chat.theme.Blue50
import com.eka.medassist.ui.chat.theme.FuchsiaViolet100

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConversationHeader(
    title: String,
    subTitle: String? = null,
    onBackClick : () -> Unit,
) {
    AppBar(
        borderColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.4f to Blue50,
                    1f to FuchsiaViolet100
                )
            )
        ),
        containerColor = Color.Transparent,
        title = title.replaceFirstChar { it.uppercaseChar() },
        subTitle = subTitle,
        navigationIcon = {
            IconButtonWrapper(
                onClick = onBackClick,
                icon = R.drawable.ic_arrow_left_regular,
                iconSize = 16.dp
            )
        }
    )
}
