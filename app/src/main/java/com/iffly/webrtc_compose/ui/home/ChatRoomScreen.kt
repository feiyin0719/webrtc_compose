package com.iffly.webrtc_compose.ui.home

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.iffly.compose.redux.StoreViewModel
import com.iffly.compose.redux.storeViewModel
import com.iffly.webrtc_compose.reducer.home.ChatRoomAction
import com.iffly.webrtc_compose.reducer.home.ChatRoomViewState


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

    ChatList(list = viewState.list, isLoading = viewState.loading) {
        refresh(store)
    }
}

private fun refresh(store: StoreViewModel) {
    store.dispatch(ChatRoomAction(ChatRoomAction.ChatRoomActionValue.Refresh, ""))
}