package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eka.conversation.common.models.UserInfo
import com.eka.medassist.ui.chat.presentation.screens.ConversationScreen
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import com.eka.ui.theme.EkaTheme

@Composable
fun ChatScreenComponent(
    viewModel : EkaChatViewModel,
    userInfo: UserInfo,
    sessionId: String?,
    onBackClick : () -> Unit,
    onNewClick : () -> Unit ?= {}
) {
    var showPermissionDialog by remember { mutableStateOf(false) }
    if (showPermissionDialog) {
        MicrophonePermissionAlertDialog(
            onConfirm = {
                showPermissionDialog = false
            },
            onDismiss = {
                showPermissionDialog = false
            }
        )
    }
    EkaTheme {
        ConversationScreen(
            userInfo = userInfo,
            viewModel = viewModel,
            sessionId = sessionId,
            onBackClick = onBackClick,
            askMicrophonePermission = {
                showPermissionDialog = true
            },
            onNewClick = onNewClick
        )
    }
}