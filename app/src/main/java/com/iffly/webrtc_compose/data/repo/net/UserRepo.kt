package com.iffly.webrtc_compose.data.repo.net

import com.iffly.webrtc_compose.data.bean.UserItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepo {
    private val userApi: UserApi = ServiceCreator.create(UserApi::class.java)
    fun getUsers(listener: (List<UserItem>) -> Unit = {}) {
        userApi.getUsers().enqueue(object : Callback<List<UserItem>> {
            override fun onResponse(
                call: Call<List<UserItem>>,
                response: Response<List<UserItem>>
            ) {
                if (response.body() != null)
                    listener.invoke(response.body() as List<UserItem>)
                else
                    listener.invoke(emptyList())

            }

            override fun onFailure(call: Call<List<UserItem>>, t: Throwable) {
                listener.invoke(emptyList())
            }
        })
    }
}