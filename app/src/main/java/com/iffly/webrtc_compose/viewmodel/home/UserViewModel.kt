package com.iffly.webrtc_compose.viewmodel.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

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
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            loadingState.postValue(false)
        }

    }
}