package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular

@Composable
fun DefaultInputComponent(
    focusRequester: FocusRequester,
    input: String,
    onSend: (String) -> Unit,
    onMicrophoneClick: () -> Unit,
    onCancel: () -> Unit,
    sendEnabled: Boolean = true,
) {
    var query by remember { mutableStateOf("") }

    LaunchedEffect(input) {
        if (isSafeInput(input)) {
            query = input
        }
    }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .navigationBarsPadding()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DarwinTouchNeutral0,
            unfocusedContainerColor = DarwinTouchNeutral0,
            disabledContainerColor = DarwinTouchNeutral0,
            disabledIndicatorColor = DarwinTouchNeutral0,
            focusedTextColor = DarwinTouchNeutral1000,
            focusedIndicatorColor = DarwinTouchNeutral0,
            unfocusedIndicatorColor = DarwinTouchNeutral0,
            cursorColor = DarwinTouchPrimary
        ),
        enabled = sendEnabled,
        textStyle = touchBodyRegular,
        value = query,
        onValueChange = { newValue ->
            query = newValue
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences
        ),
        placeholder = {
            Text(
                text = stringResource(id = R.string.start_new_chat),
                style = touchBodyRegular,
                color = DarwinTouchNeutral600
            )
        },
        trailingIcon = {
            if (!sendEnabled) {
                IconButton(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black)
                        .padding(8.dp),
                    content = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White)
                        )
                    },
                    onClick = onCancel
                )
            } else {
                if (query.isNotBlank()) {
                    InputIcon(
                        modifier = Modifier.rotate(90f),
                        icon = R.drawable.ic_arrow_left_regular,
                        onClick = {
                            onSend(query.trim())
                            query = ""
                            focusRequester.freeFocus()
                        }
                    )
                } else {
                    InputIcon(
                        icon = R.drawable.ic_microphone_regular,
                        onClick = onMicrophoneClick
                    )
                }
            }
        }
    )
}

@Composable
private fun InputIcon(
    modifier: Modifier = Modifier,
    icon: Int,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .padding(8.dp),
        content = {
            Icon(
                painter = painterResource(icon),
                contentDescription = "Send Icon",
                tint = Color.White
            )
        },
        onClick = onClick,
    )
}

private fun isSafeInput(input: String): Boolean {
    return !Regex("<[^>]*>").containsMatchIn(input)
}