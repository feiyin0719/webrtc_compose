package com.iffly.webrtc_compose.viewmodel.home

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo

class UserViewModel : ViewModel() {
    private val handler = Handler(Looper.getMainLooper())
    private val userReo = UserRepo()

    val loadingState = MutableLiveData(false)


    val users = MutableLiveData<List<UserItem>>()

    init {
        userReo.getUsers {
            users.postValue(it)
        }
    }

    fun loadUser() {
        loadingState.postValue(true)
        userReo.getUsers {
            users.postValue(it)
        }
        handler.postDelayed({
            loadingState.postValue(false)
        }, 1000)

    }
}