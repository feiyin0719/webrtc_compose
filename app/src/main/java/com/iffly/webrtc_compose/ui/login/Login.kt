package com.iffly.webrtc_compose.ui.login

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.iffly.webrtc_compose.ui.LocalNavController
import com.iffly.webrtc_compose.ui.MainDestinations
import com.iffly.webrtc_compose.viewmodel.app.AppViewModel
import com.iffly.webrtc_compose.viewmodel.app.LoginStateEnum
import com.iffly.webrtc_compose.viewmodel.app.appViewModel

const val LOGIN_ROUTE = "login/login"


@Composable
fun LoginScreen() {
    val appViewModel = appViewModel()
    var name by remember {
        mutableStateOf("")
    }
    val loginState
            by appViewModel.viewState.observeAsState(AppViewModel.LoginState())
    if (loginState.state == LoginStateEnum.Login) {
        val navController = LocalNavController.current
        LaunchedEffect(key1 = loginState.state, block = {
            if (loginState.state == LoginStateEnum.Login)
                navController?.navigate(MainDestinations.HOME_ROUTE) {
                    popUpTo(LOGIN_ROUTE) {
                        inclusive = true
                    }
                }
        })
    } else {
        LoginContent(
            name = name,
            onNameChanged = { name = it }
        ) {
            appViewModel.sendAction(
                AppViewModel.LoginAction(
                    AppViewModel.LoginAction.LoginActionValue.Login,
                    name
                )
            )
        }
    }
}
