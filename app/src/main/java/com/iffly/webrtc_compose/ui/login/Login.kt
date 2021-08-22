package com.iffly.webrtc_compose.ui.login

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.iffly.compose.libredux.storeViewModel
import com.iffly.webrtc_compose.reducer.app.LoginAction
import com.iffly.webrtc_compose.reducer.app.LoginState
import com.iffly.webrtc_compose.reducer.app.LoginStateEnum
import com.iffly.webrtc_compose.ui.LocalNavController
import com.iffly.webrtc_compose.ui.MainDestinations

const val LOGIN_ROUTE = "login/login"


@Composable
fun LoginScreen() {
    val store = storeViewModel()
    var name by remember {
        mutableStateOf("")
    }
    val loginState
            by store.getState(LoginState::class.java)
                .observeAsState(LoginState())
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
            store.dispatch(LoginAction(LoginAction.LoginActionValue.Login, name))
        }
    }
}
