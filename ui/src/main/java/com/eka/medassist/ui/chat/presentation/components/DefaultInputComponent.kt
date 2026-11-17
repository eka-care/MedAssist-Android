package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral300
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular

private val ComposerBorder = Color(0xFFE6E9F1)
private val SendDisabled = Color(0xFFE3E6EE)

/**
 * The composer: one white card. Pending attachments on top, the text field, then a row with
 * attach (+), the attached patient, mic and send. Stop replaces mic and send while a reply comes.
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
    /** Starts dictation; null hides the mic. */
    onMicrophoneClick: (() -> Unit)? = null,
    /** Picked files are being read / resized: spinner in place of +. */
    preparingAttachments: Boolean = false,
    /** A reply is coming: Stop replaces send (and the mic). */
    onStop: (() -> Unit)? = null,
    /** After +: the attached patient, or "Attach patient". */
    patientSlot: (@Composable () -> Unit)? = null,
) {
    val hasContent = query.isNotBlank() || attachments.isNotEmpty()
    val shape = RoundedCornerShape(24.dp)
    val textStyle = touchBodyRegular.copy(fontSize = 15.sp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp)
            .shadow(elevation = 10.dp, shape = shape, ambientColor = Color(0x14151A2D), spotColor = Color(0x1F151A2D))
            .clip(shape)
            .background(DarwinTouchNeutral0)
            .border(1.dp, ComposerBorder, shape)
            .padding(bottom = 8.dp)
    ) {
        if (attachments.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                attachments.forEach { file -> FileChip(file) { onRemoveAttachment(file) } }
            }
        }
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = DarwinTouchNeutral1000,
                unfocusedTextColor = DarwinTouchNeutral1000,
                cursorColor = DarwinTouchPrimary
            ),
            textStyle = textStyle,
            value = query,
            onValueChange = onQueryChange,
            maxLines = 5,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences
            ),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.start_new_chat),
                    style = textStyle,
                    color = DarwinTouchNeutral600
                )
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            onAttachClick?.let { attach ->
                RoundAction(
                    size = 34.dp,
                    enabled = sendEnabled && !preparingAttachments,
                    bordered = true,
                    onClick = attach,
                ) {
                    if (preparingAttachments) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = DarwinTouchPrimary)
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus_regular),
                            contentDescription = "Attach",
                            tint = if (sendEnabled) DarwinTouchNeutral800 else DarwinTouchNeutral300,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) { patientSlot?.invoke() }
            if (onStop != null) {
                // Black circle with a white square, as in the original MedAssist input
                RoundAction(size = 36.dp, background = Color.Black, onClick = onStop) {
                    Box(modifier = Modifier.size(11.dp).background(Color.White, RoundedCornerShape(2.dp)))
                }
            } else {
                onMicrophoneClick?.let { mic ->
                    RoundAction(size = 32.dp, onClick = mic) {
                        Icon(
                            painter = painterResource(R.drawable.ic_microphone_regular),
                            contentDescription = "Voice input",
                            tint = DarwinTouchNeutral800,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                val canSend = sendEnabled && hasContent
                RoundAction(
                    size = 36.dp,
                    enabled = canSend,
                    background = if (canSend) DarwinTouchPrimary else SendDisabled,
                    onClick = {
                        onSend()
                        focusRequester.freeFocus()
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left_regular),
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp).rotate(90f)
                    )
                }
            }
        }
    }
}

/** A round button drawn at exactly [size]; IconButton would grow it to 48dp. */
@Composable
private fun RoundAction(
    size: Dp,
    enabled: Boolean = true,
    background: Color = Color.Transparent,
    bordered: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .then(if (bordered) Modifier.border(1.dp, ComposerBorder, CircleShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
