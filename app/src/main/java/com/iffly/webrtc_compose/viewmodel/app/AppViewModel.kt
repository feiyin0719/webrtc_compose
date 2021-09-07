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


class AppViewModel : BaseMVIViewModel<AppViewModel.AppState, AppViewModel.AppAction>() {
    data class AppAction(
        val action: AppActionValue,
        val data: String,
        val outGoing: Boolean = false,
        val needStartCall: Boolean = false
    ) {
        enum class AppActionValue {
            Login, Logout, ChangeState, StartCall, StartCallComplete
        }
    }

    data class AppState(
        val state: LoginStateEnum = LoginStateEnum.Logout,
        val userName: String = "",
        val callUserId: String = "",
        val outGoing: Boolean = false,
        val needStartCall: Boolean = false
    )

    override suspend fun reduce(action: AppAction, state: AppState): AppState {
        return if (action.action == AppAction.AppActionValue.StartCall) {
            state.copy(callUserId = action.data, outGoing = action.outGoing, needStartCall = true)
        } else if (action.action == AppAction.AppActionValue.StartCallComplete) {
            state.copy(needStartCall = action.needStartCall, callUserId = "", outGoing = false)
        } else if (action.action == AppAction.AppActionValue.Login) {
            App.instance?.username = action.data
            SocketManager.connect(ServiceCreator.WS, action.data, 0)
            state.copy(state = LoginStateEnum.Logining, userName = action.data)
        } else if (action.action == AppAction.AppActionValue.Logout) {
            App.instance?.username = ""
            state.copy(state = LoginStateEnum.Logout, userName = "")
        } else {
            if (action.data == "login")
                state.copy(state = LoginStateEnum.Login, userName = action.data)
            else
                state.copy(state = LoginStateEnum.Logout, userName = "")
        }
    }

    override fun initState() = AppState()
}

@Composable
fun appViewModel() =
    viewModel<AppViewModel>(viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner)


