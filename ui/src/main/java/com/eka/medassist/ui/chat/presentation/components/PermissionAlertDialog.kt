package com.eka.medassist.ui.chat.presentation.components

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