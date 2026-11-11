package com.eka.medassist.ui.chat.presentation.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.touchCalloutRegular
import com.eka.medassist.ui.chat.theme.touchTitle2Regular

@Composable
fun PermissionAlertDialog(
    title: String,
    description: String,
    confirmButtonLabel: String = "Okay",
    dismissButtonLabel: String = "Cancel",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        containerColor = DarwinTouchNeutral50,
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                style = touchTitle2Regular,
                color = DarwinTouchNeutral1000
            )
        },
        text = {
            Text(
                description,
                style = touchCalloutRegular,
                color = DarwinTouchNeutral800
            )
        },
        confirmButton = {
            if (confirmButtonLabel.isNotEmpty()) {
                TextButton(
                    onClick = onConfirm
                ) {
                    Text(
                        confirmButtonLabel,
                        color = DarwinTouchPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        dismissButton = {
            if (dismissButtonLabel.isNotEmpty()) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        dismissButtonLabel,
                        color = DarwinTouchPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    )
}
fun hasRecordAudioPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

fun navigateToSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/** Rationale dialog, then the system prompt; opens app settings when the permission is permanently denied. */
@Composable
fun MicrophonePermissionAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val permission = Manifest.permission.RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onConfirm()
        } else {
            val activity = context.findActivity()
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                navigateToSettings(context)
            }
            onDismiss()
        }
    }
    PermissionAlertDialog(
        title = "Microphone Access Required",
        description = "We need microphone access to record your voice message",
        confirmButtonLabel = "Allow",
        dismissButtonLabel = "Deny",
        onDismiss = onDismiss,
        onConfirm = { permissionLauncher.launch(permission) },
    )
}

private fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
