@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.eka.medassist.ui.chat.presentation.components.elicitation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.conversation.matrix.AgentFeedbackConfig
import com.eka.conversation.matrix.FeedbackOption
import com.eka.conversation.matrix.PastSession
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral200
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchCaption1Regular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import com.eka.medassist.ui.chat.theme.touchHeadlineBold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Exit feedback form from agent config (`feedback.type` = default | nps). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExitFeedbackSheet(config: AgentFeedbackConfig, onSubmit: (List<String>?, Int?) -> Unit, onSkip: () -> Unit) {
    var nps by remember { mutableStateOf<Int?>(null) }
    val picked = remember { mutableStateListOf<String>() }
    val isNps = config.type == "nps"
    val options: List<FeedbackOption> = when {
        !isNps -> config.form?.default.orEmpty()
        nps == null -> emptyList()
        nps!! <= 2 -> config.form?.detractors.orEmpty()
        nps == 3 -> config.form?.passives.orEmpty()
        else -> config.form?.promoters.orEmpty()
    }
    ModalBottomSheet(onDismissRequest = onSkip, containerColor = DarwinTouchNeutral0) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) {
            Text(config.title ?: "How was your experience?", style = touchHeadlineBold, color = DarwinTouchNeutral1000)
            config.description?.let { Text(it, style = touchFootnoteRegular, color = DarwinTouchNeutral600, modifier = Modifier.padding(top = 4.dp)) }
            if (isNps) {
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (1..5).forEach { n -> Chip("$n", enabled = true, selected = nps == n) { nps = n; picked.clear() } }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                options.forEach { o ->
                    Chip(o.label, enabled = true, selected = o.value in picked) {
                        if (o.value in picked) picked.remove(o.value) else picked.add(o.value)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) { Text("Skip", color = DarwinTouchNeutral800) }
                Button(
                    onClick = { onSubmit(picked.toList().ifEmpty { null }, nps) },
                    enabled = picked.isNotEmpty() || nps != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarwinTouchPrimary),
                ) { Text("Submit") }
            }
        }
    }
}
