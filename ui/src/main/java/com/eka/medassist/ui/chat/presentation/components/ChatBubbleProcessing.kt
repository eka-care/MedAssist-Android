package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchCaption1Regular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import kotlinx.coroutines.delay

/** "Thinking..." while the bot works, with the server's progress line and rotating tips. */
@Composable
fun ChatBubbleProcessing(
    progressMessage: String? = null,
    tips: List<String> = emptyList(),
) {
    var dotCount by remember { mutableIntStateOf(0) }
    var tipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(true) {
        while (true) {
            delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }
    LaunchedEffect(tips) {
        tipIndex = 0
        while (tips.size > 1) {
            delay(4000)
            tipIndex = (tipIndex + 1) % tips.size
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp),
                painter = painterResource(id = R.drawable.ic_ai_chat_custom),
                tint = Color.Unspecified,
                contentDescription = ""
            )
            Text(
                text = "Thinking" + ".".repeat(dotCount),
                style = touchBodyRegular,
                color = DarwinTouchNeutral800
            )
        }
        progressMessage?.let {
            Text(it, style = touchFootnoteRegular, color = DarwinTouchNeutral600, modifier = Modifier.padding(start = 40.dp, top = 2.dp))
        }
        tips.getOrNull(tipIndex)?.let {
            Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral600, modifier = Modifier.padding(start = 40.dp, top = 2.dp))
        }
    }
}
