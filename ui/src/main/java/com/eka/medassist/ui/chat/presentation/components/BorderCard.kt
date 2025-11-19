package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorderCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    elevation: Dp = 0.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
    background: Color = Color.White,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        enabled = enabled,
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        onClick = {
            onClick?.invoke()
        },
        content = {
            content.invoke()
        }
    )
}