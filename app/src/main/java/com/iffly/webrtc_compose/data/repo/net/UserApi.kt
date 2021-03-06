package com.iffly.webrtc_compose.data.repo.net

import com.iffly.webrtc_compose.data.bean.ChatRoomItem
import com.iffly.webrtc_compose.data.bean.UserItem
import retrofit2.http.GET

interface UserApi {
    @GET("userList")
    suspend fun getUsers(): List<UserItem>

    @GET("roomList")
    suspend fun getRooms(): List<ChatRoomItem>
}