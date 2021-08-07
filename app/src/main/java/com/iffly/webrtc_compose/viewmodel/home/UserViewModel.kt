package com.iffly.webrtc_compose.viewmodel.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val userReo = UserRepo()

    val loadingState = MutableLiveData(false)


    val users = MutableLiveData<List<UserItem>>()

    init {
        viewModelScope.launch {
            try {
                val list = userReo.getUsers()
                users.postValue(list)
            } catch (e: Exception) {

            }
        }
    }

    fun loadUser() {
        loadingState.postValue(true)

        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            try {
                val list = userReo.getUsers()
                users.postValue(list)
            } catch (e: Exception) {
            }
            loadingState.postValue(false)
        }

    }
}