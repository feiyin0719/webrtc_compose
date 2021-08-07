package com.iffly.webrtc_compose.viewmodel.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iffly.webrtc_compose.socket.IUserState
import com.iffly.webrtc_compose.socket.SocketManager

class AppViewModel : ViewModel(), IUserState {
    val loginState = MutableLiveData(LoginState.Logout)

    override fun userLogin() {
        loginState.postValue(LoginState.Login)
    }

    override fun userLogout() {
        loginState.postValue(LoginState.Logout)
    }

}

enum class LoginState {
    Login, Logout
}


@Composable
fun appViewModel(): AppViewModel {
    return viewModel(viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner)
}