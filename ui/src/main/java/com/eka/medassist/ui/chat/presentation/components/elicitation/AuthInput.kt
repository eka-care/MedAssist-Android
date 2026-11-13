@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.eka.medassist.ui.chat.presentation.components.elicitation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.conversation.matrix.MatrixComponents
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.chat.presentation.models.ElicitationEntry
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral200
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral400
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchRed
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchCaption1Regular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import kotlinx.coroutines.delay

/** Mobile / OTP / email elicitation that replaces the composer (port of ElicitationInput). */
@Composable
fun AuthElicitationInput(
    entry: ElicitationEntry,
    disabled: Boolean,
    onSubmit: (String) -> Unit,
    onResend: () -> Unit,
    onCancel: () -> Unit,
) {
    var value by remember(entry.toolId, entry.component) { mutableStateOf("") }
    val component = entry.component
    val (placeholder, keyboard, valid) = when (component) {
        MatrixComponents.MOBILE -> Triple(
            entry.description ?: "Enter your mobile number", KeyboardType.Phone,
            value.length == (entry.minLength ?: 10) && Regex(entry.pattern ?: "^[6-9][0-9]{9}$").matches(value),
        )
        MatrixComponents.OTP -> Triple(
            entry.description ?: "Enter OTP", KeyboardType.NumberPassword,
            value.isNotEmpty() && value.length >= (entry.minLength ?: 4) && value.length <= (entry.maxLength ?: 8) &&
                (entry.pattern?.let { Regex(it).matches(value) } ?: value.all(Char::isDigit)),
        )
        else -> Triple(
            entry.description ?: "Enter your email address", KeyboardType.Email,
            Regex(entry.pattern ?: "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value),
        )
    }
    var resendIn by remember(entry.toolId) { mutableIntStateOf(30) }
    if (component == MatrixComponents.OTP) LaunchedEffect(entry.toolId, resendIn) { if (resendIn > 0) { delay(1000); resendIn-- } }

    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().background(DarwinTouchNeutral50).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            entry.dispToastMsg?.takeIf { it.isNotBlank() && component == MatrixComponents.OTP }?.let {
                Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral600, modifier = Modifier.weight(1f))
            } ?: Spacer(Modifier.weight(1f))
            TextButton(onClick = onCancel, enabled = !disabled) { Text("Cancel verification", style = touchFootnoteRegular, color = DarwinTouchPrimary) }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(DarwinTouchNeutral0)
                .border(1.dp, DarwinTouchPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).padding(start = 16.dp, end = 4.dp),
        ) {
            if (component == MatrixComponents.MOBILE) Text("+91  ", style = touchBodyRegular, color = DarwinTouchNeutral800)
            Box(Modifier.weight(1f).padding(vertical = 12.dp)) {
                if (value.isEmpty()) Text(placeholder, style = touchBodyRegular, color = DarwinTouchNeutral400)
                BasicTextField(
                    value = value,
                    onValueChange = { v ->
                        value = when (component) {
                            MatrixComponents.MOBILE -> v.filter(Char::isDigit).take(entry.maxLength ?: 10)
                            MatrixComponents.OTP -> v.filter(Char::isDigit).take(entry.maxLength ?: 8)
                            else -> v.trim()
                        }
                    },
                    singleLine = true,
                    enabled = !disabled,
                    textStyle = touchBodyRegular.copy(color = DarwinTouchNeutral1000),
                    cursorBrush = SolidColor(DarwinTouchPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboard, imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (valid && !disabled) onSubmit(value) }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IconButton(onClick = { onSubmit(value) }, enabled = valid && !disabled) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(if (valid && !disabled) androidx.compose.ui.graphics.Color.Black else DarwinTouchNeutral200),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_left_regular), "Submit", tint = DarwinTouchNeutral0,
                        modifier = Modifier.size(16.dp).rotate(90f),
                    )
                }
            }
        }
        if (component == MatrixComponents.OTP) {
            Text(
                if (resendIn > 0) "Resend OTP in ${resendIn}s" else "Resend OTP",
                style = touchFootnoteRegular,
                color = if (resendIn > 0) DarwinTouchNeutral400 else DarwinTouchPrimary,
                modifier = Modifier.padding(top = 6.dp, start = 8.dp).alpha(if (disabled) 0.5f else 1f)
                    .clickable(enabled = resendIn == 0 && !disabled) { resendIn = 30; onResend() },
            )
        }
    }
}
