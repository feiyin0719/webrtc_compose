package com.iffly.webrtc_compose.ui.login

import android.text.TextUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.statusBarsPadding
import com.iffly.compose.redux.storeViewModel
import com.iffly.webrtc_compose.reducer.app.LoginAction
import com.iffly.webrtc_compose.reducer.app.LoginState
import com.iffly.webrtc_compose.reducer.app.LoginStateEnum
import com.iffly.webrtc_compose.reducer.login.LoginViewModel
import com.iffly.webrtc_compose.ui.LocalNavController
import com.iffly.webrtc_compose.ui.MainDestinations
import com.iffly.webrtc_compose.ui.components.AppButton
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

const val LOGIN_ROUTE = "login/login"


@Composable
fun LoginScreen(loginViewModel: LoginViewModel = viewModel()) {
    val store = storeViewModel()
    var name by remember {
        mutableStateOf("")
    }

    val loginState by store.getState(LoginState::class.java)
        .observeAsState(LoginState(LoginStateEnum.Logout, ""))
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
        LoginContent(name = name,
            onNameChanged = { name = it }) {
            store.dispatch(LoginAction(LoginAction.LoginActionValue.Login, name))
        }
    }

}

@Composable
fun LoginContent(
    name: String,
    onNameChanged: (String) -> Unit,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .statusBarsPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        TextField(
            value = name,
            label = {
                Text(
                    "please input username",
                    color = WebrtcTheme.colors.textPrimary
                )
            },
            onValueChange = onNameChanged
        )
        AppButton(
            onClick = onClick,
            enabled = !TextUtils.isEmpty(name),
            modifier = Modifier.offset(0.dp, 10.dp)
        ) {
            Text(
                text = "login",
            )
        }
    }

}

@Preview
@Composable
private fun LoginContentPre() {
    WebrtcTheme() {
        LoginContent("", {}, {})
    }

}