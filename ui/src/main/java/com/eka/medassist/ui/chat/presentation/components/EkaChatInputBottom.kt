package com.eka.medassist.ui.chat.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.eka.conversation.common.PermissionUtils
import com.eka.conversation.common.Utils
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.common.models.CTA
import com.eka.medassist.ui.chat.presentation.states.ActionType
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun EkaChatInputBottom(
    onClick: (CTA) -> Unit,
    onQueryText: (String) -> Unit,
    viewModel: EkaChatViewModel,
    focusRequester: FocusRequester,
    textFieldEnabled: Boolean,
    onMicrophoneClick: () -> Unit,
    isMicrophoneRecording: Boolean,
    isVoice2RxRecording: Boolean,
) {
    val buttonEnabled = viewModel.sendButtonEnabled
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val textValue by viewModel.textInputState.collectAsState()
    val context = LocalContext.current

    var showPermissionDialog by remember {
        mutableStateOf(false)
    }
    if (showPermissionDialog) {
        MicrophonePermissionAlertDialog(
            onDismiss = {
                showPermissionDialog = false
            },
            onConfirm = {
                showPermissionDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val cornerRadius = 16.dp.toPx()

                drawRoundRect(
                    color = DarwinTouchPrimary,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, cornerRadius * 2),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = strokeWidth)
                )
            }
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            )
            .background(
                DarwinTouchNeutral0,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            TextField(
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    disabledTextColor = DarwinTouchNeutral1000,
                    focusedTextColor = DarwinTouchNeutral1000,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                    disabledIndicatorColor = Color.White,
                    cursorColor = DarwinTouchPrimary
                ),
                textStyle = touchBodyRegular,
                value = textValue,
                enabled = textFieldEnabled,
                onValueChange = { newValue ->
                    viewModel.updateTextInputState(newValue)
                    coroutineScope.launch {
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .heightIn(max = 200.dp)
                    .verticalScroll(scrollState),
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
                maxLines = 2,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarwinTouchNeutral0),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            BottomBarOtherAction(
//                showPatientSelection = showPatientSelection,
//                selectedPatientName = patientTagName,
//                openPatientSelection = {
//                    onClick(CTA(action = ActionType.ON_PATIENT_CLICK.stringValue))
//                },
//                openMedicalRecords = { onClick(CTA(action = ActionType.ON_GALLERY_CLICK.stringValue)) }
//            )
            Row {
                IconButton(
                    onClick = {
                        if (!isEnabled(isMicrophoneRecording, isVoice2RxRecording)) {
                            viewModel.showToast("Recording is going on!")
                            return@IconButton
                        }
                        if (!Utils.isNetworkAvailable(context)) {
                            viewModel.showToast("Internet not available.")
                            return@IconButton
                        }
                        if (PermissionUtils.hasRecordAudioPermission(context)) {
                            val params = JSONObject()
                            params.put("type", "voicetx")
                            onMicrophoneClick()
                        } else {
                            showPermissionDialog = true
                        }
                    }
                ) {
                    Icon(
                        painter = if (isEnabled(
                                isMicrophoneRecording,
                                isVoice2RxRecording
                            )
                        ) painterResource(id = R.drawable.ic_microphone_regular) else painterResource(
                            id = R.drawable.ic_microphone_disabled_regular_custom
                        ),
                        contentDescription = "microphone",
                        modifier = Modifier.size(
                            if (isEnabled(
                                    isMicrophoneRecording,
                                    isVoice2RxRecording
                                )
                            ) 20.dp else 36.dp
                        ),
                        tint = if (isEnabled(
                                isMicrophoneRecording,
                                isVoice2RxRecording
                            )
                        ) DarwinTouchNeutral600 else Color.Unspecified
                    )
                }
                if (textValue.isNotBlank()) {
                    IconButton(
                        onClick = {
                            if (buttonEnabled) {
                                if (!Utils.isNetworkAvailable(context)) {
                                    viewModel.showToast("No Internet")
                                } else {
                                    val params = JSONObject()
                                    params.put("type", "send")
                                    onClick(CTA(action = ActionType.START_CHAT.stringValue))
                                    onQueryText(textValue)
                                    viewModel.updateTextInputState("")
                                }
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_bot_send_regular),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

fun navigateToSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

fun isEnabled(
    isMicrophoneRecording: Boolean,
    isVoice2RxRecording: Boolean,
): Boolean {
    return !isMicrophoneRecording && !isVoice2RxRecording
}