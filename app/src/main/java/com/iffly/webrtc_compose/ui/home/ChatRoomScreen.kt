package com.iffly.webrtc_compose.ui.home

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iffly.webrtc_compose.viewmodel.home.ChatRoomViewModel
import com.iffly.webrtc_compose.viewmodel.home.ChatRoomViewModel.ChatRoomAction
import com.iffly.webrtc_compose.viewmodel.home.ChatRoomViewModel.ChatRoomViewState


@Composable
fun ChatRoomScreen() {
    val chatRoomViewModel: ChatRoomViewModel = viewModel()
    val viewState by chatRoomViewModel.viewState.observeAsState(initial = ChatRoomViewState())
    var init by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(init) {
        if (init) {
            init = false
            refresh(chatRoomViewModel = chatRoomViewModel)
        }
    }

    ChatList(list = viewState.list, isLoading = viewState.loading, refreshListener = {
        refresh(chatRoomViewModel = chatRoomViewModel)
    }) {

    }
}

private fun refresh(chatRoomViewModel: ChatRoomViewModel) {
    chatRoomViewModel.sendAction(
        ChatRoomAction(
            ChatRoomAction.ChatRoomActionValue.ChangeLoading,
            ""
        )
    )
    chatRoomViewModel.sendAction(ChatRoomAction(ChatRoomAction.ChatRoomActionValue.Refresh, ""))
}