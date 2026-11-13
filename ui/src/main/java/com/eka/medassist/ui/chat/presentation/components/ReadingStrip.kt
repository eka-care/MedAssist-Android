package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.touchCaption1Regular

private val StripBg = Color(0xFFEDF1FA)
private val StripBorder = Color(0xFFD9E1F3)

/** Tab tucked behind the input's top edge: "Currently reading Transcript" (web's editing strip). */
@Composable
fun ReadingStrip(label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
    Text(
        text = buildAnnotatedString {
            append("Currently reading ")
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = DarwinTouchNeutral1000)) { append(label) }
        },
        style = touchCaption1Regular.copy(fontSize = 12.sp),
        color = DarwinTouchNeutral600,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(shape)
            .background(StripBg)
            .border(1.dp, StripBorder, shape)
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 14.dp),
    )
}
