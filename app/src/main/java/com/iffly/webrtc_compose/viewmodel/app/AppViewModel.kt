package com.iffly.webrtc_compose.viewmodel.app


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.data.repo.net.ServiceCreator
import com.iffly.webrtc_compose.socket.SocketManager
import com.iffly.webrtc_compose.viewmodel.BaseMVIViewModel


enum class LoginStateEnum {
    Login, Logout, Logining
}


class AppViewModel : BaseMVIViewModel<AppViewModel.LoginState, AppViewModel.LoginAction>() {
    data class LoginAction(val action: LoginActionValue, val data: String) {
        enum class LoginActionValue {
            Login, Logout, ChangeState
        }
    }

    data class LoginState(
        val state: LoginStateEnum = LoginStateEnum.Logout,
        val userName: String = ""
    )

    override fun reduce(action: LoginAction): LoginState {
        return if (action.action == LoginAction.LoginActionValue.Login) {
            App.instance?.username = action.data
            SocketManager.connect(ServiceCreator.WS, action.data, 0)
            LoginState(LoginStateEnum.Logining, action.data)
        } else if (action.action == LoginAction.LoginActionValue.Logout) {
            App.instance?.username = ""
            LoginState(LoginStateEnum.Logout, action.data)
        } else {
            if (action.data == "login")
                LoginState(LoginStateEnum.Login, viewState.value?.userName ?: "")
            else
                LoginState(LoginStateEnum.Logout, "")
        }
    }
}

@Composable
fun appViewModel() =
    viewModel<AppViewModel>(viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner)


