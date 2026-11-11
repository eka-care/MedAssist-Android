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

/** Mic permission for voice messages. Returns `withMicPermission(onGranted)`. */
@Composable
internal fun rememberMicPermission(): (onGranted: () -> Unit) -> Unit = rememberPermissionRequest(
    permission = Manifest.permission.RECORD_AUDIO,
    deniedMessage = "Microphone permission is needed for voice messages.",
    settingsTitle = "Microphone access needed",
    settingsText = "Turn on Microphone for this app in Settings to send voice messages.",
)

/**
 * Camera permission for "Take photo". Only needed when the host app declares CAMERA in its
 * manifest (then the system camera can't be opened without it); otherwise runs straight away.
 */
@Composable
internal fun rememberCameraPermission(): (onGranted: () -> Unit) -> Unit {
    val context = LocalContext.current
    val request = rememberPermissionRequest(
        permission = Manifest.permission.CAMERA,
        deniedMessage = "Camera permission is needed to take a photo.",
        settingsTitle = "Camera access needed",
        settingsText = "Turn on Camera for this app in Settings to take photos.",
    )
    val declared = remember { context.declaresPermission(Manifest.permission.CAMERA) }
    return remember(request, declared) { { onGranted -> if (declared) request(onGranted) else onGranted() } }
}

/**
 * Asks the system for [permission]; once it's permanently denied, offers to open the app's
 * settings instead.
 */
@Composable
private fun rememberPermissionRequest(
    permission: String,
    deniedMessage: String,
    settingsTitle: String,
    settingsText: String,
): (onGranted: () -> Unit) -> Unit {
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
        if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showSettings = true // "Don't allow" twice / "Don't ask again"
        } else {
            Toast.makeText(context, deniedMessage, Toast.LENGTH_SHORT).show()
        }
    }
    if (showSettings) {
        PermissionAlertDialog(
            title = settingsTitle,
            description = settingsText,
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
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            } else {
                pending = onGranted
                launcher.launch(permission)
            }
        }
    }
}

private fun Context.declaresPermission(permission: String): Boolean = runCatching {
    @Suppress("DEPRECATION")
    packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
        ?.contains(permission) == true
}.getOrDefault(false)

private fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
