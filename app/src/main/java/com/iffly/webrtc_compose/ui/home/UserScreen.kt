package com.iffly.webrtc_compose.ui.home

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.iffly.compose.libredux.StoreViewModel
import com.iffly.compose.libredux.storeViewModel
import com.iffly.webrtc_compose.CallActivity
import com.iffly.webrtc_compose.viewmodel.home.UserViewAction
import com.iffly.webrtc_compose.viewmodel.home.UserViewState

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
            UserViewAction.UserViewActionValue.ChangeLoading,
            ""
        )
    )
    store.dispatch(
        UserViewAction(
            UserViewAction.UserViewActionValue.Refresh,
            ""
        )
    )
}

