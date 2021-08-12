package com.iffly.webrtc_compose.ui.home

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.iffly.compose.redux.StoreViewModel
import com.iffly.compose.redux.storeViewModel
import com.iffly.webrtc_compose.CallActivity
import com.iffly.webrtc_compose.reducer.home.UserViewAction
import com.iffly.webrtc_compose.reducer.home.UserViewState

@Composable
fun UserScreen() {
    val store = storeViewModel()

    val userViewState: UserViewState
            by store.getState(UserViewState::class.java).observeAsState(
                UserViewState()
            )
    var init by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(init) {
        if (init) {
            init = false
            refresh(store)
        }
    }
    val context = LocalContext.current
    UserList(userViewState.list, userViewState.loading, { refresh(store) }) {
        CallActivity.startCallActivity(it, true, context = context)
    }


}

private fun refresh(store: StoreViewModel) {
    store.dispatch(
        UserViewAction(
            UserViewAction.UserViewActionValue.Refresh,
            ""
        )
    )
}

