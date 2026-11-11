package com.eka.medassist.ui.chat.presentation.voice

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eka.medassist.ui.chat.presentation.components.PermissionAlertDialog

/**
 * Mic permission for voice input: asks the system; once it's permanently denied, offers to open
 * the app's settings instead. Returns `withMicPermission(onGranted)`.
 */
@Composable
internal fun rememberMicPermission(): (onGranted: () -> Unit) -> Unit {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pending
        pending = null
        if (granted) {
            action?.invoke()
            return@rememberLauncherForActivityResult
        }
        val activity = context.findActivity()
        if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
            showSettings = true // "Don't allow" twice / "Don't ask again"
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice input.", Toast.LENGTH_SHORT).show()
        }
    }
    if (showSettings) {
        PermissionAlertDialog(
            title = "Microphone access needed",
            description = "Turn on Microphone for this app in Settings to use voice input.",
            confirmButtonLabel = "Open Settings",
            dismissButtonLabel = "Not now",
            onDismiss = { showSettings = false },
            onConfirm = {
                showSettings = false
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
        )
    }
    return remember(launcher) {
        { onGranted ->
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            } else {
                pending = onGranted
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
