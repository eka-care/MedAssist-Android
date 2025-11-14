package com.eka.medassist.ui.chat.presentation.common.atom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800


@Composable
fun IconWrapper(
    icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = DarwinTouchNeutral800,
    boundingBoxSize: Dp = 24.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(boundingBoxSize)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}