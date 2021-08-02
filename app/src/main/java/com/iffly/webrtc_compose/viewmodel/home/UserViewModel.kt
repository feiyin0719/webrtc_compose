package com.iffly.webrtc_compose.viewmodel.home

import androidx.lifecycle.ViewModel
import com.iffly.webrtc_compose.data.repo.net.UserRepo

class UserViewModel : ViewModel() {
    private val userReo = UserRepo()

    private val _users =
        userReo.getUsers()
    val users get() = _users
}