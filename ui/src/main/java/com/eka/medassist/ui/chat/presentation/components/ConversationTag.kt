package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800

@Composable
fun EkaBotTag(
    modifier: Modifier = Modifier,
    tagName: String,
    backgroundColor: Color,
    style: TextStyle,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .height(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick?.invoke() }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            softWrap = true,
            text = tagName,
            style = style,
            color = DarwinTouchNeutral800
        )
    }
}