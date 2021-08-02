package com.iffly.webrtc_compose.data.repo.net

import androidx.lifecycle.LiveData
import com.iffly.webrtc_compose.data.bean.UserItem
import retrofit2.http.GET

interface UserApi {
    @GET("userList")
    fun getUsers(): LiveData<List<UserItem>?>
}