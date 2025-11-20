package com.eka.medassist.ui.chat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eka.conversation.client.ChatInit
import com.eka.conversation.client.models.Message
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.components.ConversationHeader
import com.eka.medassist.ui.chat.presentation.components.ConversationInput
import com.eka.medassist.ui.chat.presentation.viewmodels.EkaChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(viewModel: EkaChatViewModel) {
    val message = ChatInit.getResponseStream()?.collectAsState(null)?.value
    val messages = viewModel.messages.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.createNewSession()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ConversationHeader(
            title = stringResource(id = R.string.new_chat),
            subTitle = "ParrotLet 1.0",
            onClick = {

            }
        )

        ConversationContent(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            messages = messages.map {
                when(it) {
                    is Message.Text -> {
                        it.text
                    }
                    else -> {
                        "Default"
                    }
                }
            }.reversed()
        )

        ConversationInput(
            viewModel = viewModel
        )
    }
}

@Composable
private fun ConversationContent(modifier: Modifier = Modifier, messages: List<Any> = emptyList()) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        reverseLayout = true
    ) {
        items(messages) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = it.toString()
            )
        }
    }
}