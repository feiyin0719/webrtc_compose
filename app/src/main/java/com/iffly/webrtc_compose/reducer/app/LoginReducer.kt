package com.iffly.webrtc_compose.reducer.app


import com.iffly.compose.libredux.Reducer
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.data.repo.net.ServiceCreator
import com.iffly.webrtc_compose.socket.SocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class LoginState(val state: LoginStateEnum = LoginStateEnum.Logout, val userName: String = "")

data class LoginAction(val action: LoginActionValue, val data: String) {
    enum class LoginActionValue {
        Login, Logout, ChangeState
    }
}

class LoginReducer :
    Reducer<LoginState, LoginAction>(LoginState::class.java, LoginAction::class.java) {
    override fun reduce(state: LoginState, flow: Flow<LoginAction>): Flow<LoginState> {
        return flow.map { action ->
            if (action.action == LoginAction.LoginActionValue.Login) {
                App.instance?.username = action.data
                SocketManager.connect(ServiceCreator.WS, action.data, 0)
                LoginState(LoginStateEnum.Logining, action.data)
            } else if (action.action == LoginAction.LoginActionValue.Logout) {
                App.instance?.username = ""
                LoginState(LoginStateEnum.Logout, action.data)
            } else {
                if (action.data == "login")
                    LoginState(LoginStateEnum.Login, state.userName)
                else
                    LoginState(LoginStateEnum.Logout, "")
            }
        }
    }
}

enum class LoginStateEnum {
    Login, Logout, Logining
}


