package com.iffly.webrtc_compose.reducer.app

import com.iffly.compose.redux.Reducer
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.data.repo.net.ServiceCreator
import com.iffly.webrtc_compose.socket.SocketManager

data class LoginState(val state: LoginStateEnum, val userName: String)

data class LoginAction(val action: LoginActionValue, val data: String) {
    enum class LoginActionValue {
        Login, Logout, ChangeState
    }
}

class LoginReducer :
    Reducer<LoginState, LoginAction>(LoginState::class.java, LoginAction::class.java) {
    override suspend fun reduce(state: LoginState, action: LoginAction): LoginState {
        if (action.action == LoginAction.LoginActionValue.Login) {
            App.instance?.username = action.data
            SocketManager.connect(ServiceCreator.WS, action.data, 0)
            return LoginState(LoginStateEnum.Logining, action.data)
        } else if (action.action == LoginAction.LoginActionValue.Logout) {
            App.instance?.username = ""
            return LoginState(LoginStateEnum.Logout, action.data)
        } else {
            if (action.data == "login")
                return LoginState(LoginStateEnum.Login, state.userName)
            else
                return LoginState(LoginStateEnum.Logout, "")
        }
    }

    override fun initState(): LoginState {
        return LoginState(LoginStateEnum.Logout, "")
    }

}

enum class LoginStateEnum {
    Login, Logout, Logining
}


