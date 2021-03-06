package com.iffly.webrtc_compose.ui.home

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.iffly.webrtc_compose.MainActivity
import com.iffly.webrtc_compose.viewmodel.home.UserViewModel
import com.iffly.webrtc_compose.viewmodel.home.UserViewModel.UserViewAction
import com.iffly.webrtc_compose.viewmodel.home.UserViewModel.UserViewState

@Composable
fun UserScreen() {
    val userViewModel: UserViewModel = hiltViewModel()

    val userViewState: UserViewState
            by userViewModel.viewState.observeAsState(
                UserViewState()
            )
    var init by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(init) {
        if (init) {
            init = false
            refresh(userViewModel = userViewModel)
        }
    }
    val context = LocalContext.current
    UserList(
        userViewState.list,
        userViewState.loading,
        { refresh(userViewModel = userViewModel) }) {
        MainActivity.startActivity(it, true, context = context)
    }


}

private fun refresh(userViewModel: UserViewModel) {
    userViewModel.sendAction(
        UserViewAction(
            UserViewAction.UserViewActionValue.ChangeLoading,
            ""
        )
    )
    userViewModel.sendAction(
        UserViewAction(
            UserViewAction.UserViewActionValue.Refresh,
            ""
        )
    )
}

