package com.iffly.webrtc_compose.viewmodel.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.data.repo.net.ServiceCreator
import com.iffly.webrtc_compose.socket.SocketManager

class LoginViewModel : ViewModel() {
    var userName = MutableLiveData("")

    fun login() {
        App.instance?.username = userName.value.toString()
        SocketManager.connect(ServiceCreator.WS, userName.value.toString(), 0)
    }

    fun onNameChanged(name: String) {
        userName.value = name
    }

}