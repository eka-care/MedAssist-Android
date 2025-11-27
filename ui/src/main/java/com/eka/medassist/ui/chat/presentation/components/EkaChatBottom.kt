package com.eka.medassist.ui.chat.presentation.components

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.eka.conversation.common.NetworkChecker
import com.eka.conversation.common.PermissionChecker
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.common.models.CTA
import com.eka.medassist.ui.chat.presentation.states.ActionType
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import org.json.JSONObject

@Composable
fun EkaChatBottom(
    modifier: Modifier = Modifier,
    showPatientSelection: Boolean = true,
    isVoice2RxRecording: Boolean,
    isMicrophoneRecording: Boolean,
    onMicrophoneClick: () -> Unit,
    viewModel: EkaChatViewModel,
    onClick: (CTA) -> Unit,
) {
    val selected by viewModel.textInputState.collectAsState()
    var isFocused by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
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

    BackHandler(enabled = isFocused) {
        isFocused = false
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // TODO Have a look for file support
//            BottomBarOtherAction(
//                showPatientSelection = showPatientSelection,
//                openPatientSelection = { onClick(CTA(ActionType.ON_PATIENT_CLICK.stringValue)) },
//                openMedicalRecords = { onClick(CTA(ActionType.ON_GALLERY_CLICK.stringValue)) }
//            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        isFocused = true
                        onClick(CTA(action = ActionType.OPEN_INPUT_BOTTOM_SHEET.stringValue))
                    }
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = DarwinTouchPrimary.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = selected,
                    onValueChange = { newValue -> viewModel.updateTextInputState(newValue) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    readOnly = true,
                    maxLines = 1,
                    textStyle = touchBodyRegular.copy(color = DarwinTouchNeutral600),
                    cursorBrush = SolidColor(DarwinTouchPrimary),
                    enabled = false,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selected.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.start_typing),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = DarwinTouchNeutral600,
                            style = touchBodyRegular
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (!isMicrophoneRecording && !isVoice2RxRecording) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_microphone_regular),
                            contentDescription = "microphone",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                                .clickable {
                                    if (!NetworkChecker.isNetworkAvailable(context.applicationContext)) {
                                        viewModel.showToast("Internet not available.")
                                        return@clickable
                                    }
                                    if (PermissionChecker.hasRecordAudioPermission(context)) {
                                        val params = JSONObject()
                                        params.put("type", "voicetx")
                                        onMicrophoneClick()
                                    } else {
                                        showPermissionDialog = true
                                    }
                                },
                            colorFilter = ColorFilter.tint(DarwinTouchNeutral1000)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun MicrophonePermissionAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val permission = Manifest.permission.RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (!isGranted) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                permission
            )
            if (!showRationale) {
                navigateToSettings(context)
            }
            onDismiss()
        } else {
            onConfirm()
        }
    }
    PermissionAlertDialog(
        title = "Microphone Access Required",
        description = "We need microphone access to record audio during your consultation and generate smart clinical notes",
        confirmButtonLabel = "Allow",
        dismissButtonLabel = "Deny",
        onDismiss = {
            onDismiss.invoke()
        },
        onConfirm = {
            permissionLauncher.launch(permission)
        }
    )
}