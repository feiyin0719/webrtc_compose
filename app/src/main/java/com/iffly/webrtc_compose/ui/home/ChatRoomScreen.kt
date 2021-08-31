package com.iffly.webrtc_compose.ui.home

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.iffly.compose.libredux.StoreViewModel
import com.iffly.compose.libredux.storeViewModel
import com.iffly.webrtc_compose.viewmodel.home.ChatRoomAction
import com.iffly.webrtc_compose.viewmodel.home.ChatRoomViewState


@Composable
fun ChatRoomScreen() {
    val store = storeViewModel()
    val viewState by store.getState(ChatRoomViewState::class.java)
        .observeAsState(initial = ChatRoomViewState())
    var init by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(init) {
        if (init) {
            init = false
            refresh(store)
        }
    }

    ChatList(list = viewState.list, isLoading = viewState.loading,refreshListener = {
        refresh(store)
    }) {

    }
}

private fun refresh(store: StoreViewModel) {
    store.dispatch(ChatRoomAction(ChatRoomAction.ChatRoomActionValue.ChangeLoading, ""))
    store.dispatch(ChatRoomAction(ChatRoomAction.ChatRoomActionValue.Refresh, ""))
}