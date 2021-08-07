package com.iffly.webrtc_compose.data.repo.net

object UserRepo {
    private val userApi: UserApi = ServiceCreator.create(UserApi::class.java)
    suspend fun getUsers() = userApi.getUsers()
}