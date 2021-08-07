package com.iffly.webrtc_compose.viewmodel.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

class AppViewModel : ViewModel() {
    val loginState = MutableLiveData(LoginState.Logout)

    fun changeToLogin() {
        loginState.postValue(LoginState.Login)
    }

    fun changeToLogout() {
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