package com.iffly.webrtc_compose.viewmodel.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.data.repo.net.ServiceCreator
import com.iffly.webrtc_compose.socket.IUserState
import com.iffly.webrtc_compose.socket.SocketManager

class LoginViewModel : ViewModel(), IUserState {
    var userName = MutableLiveData("")
    var loginState = MutableLiveData(1)
    fun loginClick() {
        App.instance?.username = userName.value.toString()
        SocketManager.addUserStateCallback(this)
        SocketManager.connect(ServiceCreator.WS, userName.value.toString(), 0)

    }

    fun onNameChanged(name: String) {
        userName.value = name
    }

    override fun userLogin() {
        loginState.postValue(2)
    }

    override fun userLogout() {

    }


}