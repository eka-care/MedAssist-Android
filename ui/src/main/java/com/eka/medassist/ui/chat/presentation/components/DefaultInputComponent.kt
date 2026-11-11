package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral300
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular

/**
 * Text input: attach (+) on the left, send on the right, pending attachments above.
 * The text is hoisted so the screen can clear it on send / new chat.
 */
@Composable
fun DefaultInputComponent(
    focusRequester: FocusRequester,
    query: String,
    onQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean = true,
    onAttachClick: (() -> Unit)? = null,
    attachments: List<MatrixFile> = emptyList(),
    onRemoveAttachment: (MatrixFile) -> Unit = {},
    /** "Currently reading <label>" tab above the field. */
    readingLabel: String? = null,
) {
    val hasContent = query.isNotBlank() || attachments.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        if (attachments.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                attachments.forEach { file -> FileChip(file) { onRemoveAttachment(file) } }
            }
        }
        readingLabel?.let {
            // Drawn first, so the field covers its bottom edge
            ReadingStrip(label = it, modifier = Modifier.offset(y = 10.dp))
        }
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
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
            textStyle = touchBodyRegular,
            value = query,
            onValueChange = onQueryChange,
            maxLines = 5,
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
            leadingIcon = onAttachClick?.let { attach ->
                {
                    IconButton(onClick = attach, enabled = sendEnabled) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus_regular),
                            contentDescription = "Attach",
                            tint = if (sendEnabled) DarwinTouchNeutral800 else DarwinTouchNeutral300,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            trailingIcon = {
                InputIcon(
                    modifier = Modifier.rotate(90f),
                    icon = R.drawable.ic_arrow_left_regular,
                    enabled = sendEnabled && hasContent,
                    onClick = {
                        onSend()
                        focusRequester.freeFocus()
                    }
                )
            }
        )
    }
}

@Composable
private fun InputIcon(
    modifier: Modifier = Modifier,
    icon: Int,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) Color.Black else DarwinTouchNeutral300)
            .padding(8.dp),
        enabled = enabled,
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
