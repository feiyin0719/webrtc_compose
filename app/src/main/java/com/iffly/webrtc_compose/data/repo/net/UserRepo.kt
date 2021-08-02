package com.iffly.webrtc_compose.data.repo.net

class UserRepo {
    private val userApi: UserApi = ServiceCreator.create(UserApi::class.java)
    fun getUsers() = userApi.getUsers()
}