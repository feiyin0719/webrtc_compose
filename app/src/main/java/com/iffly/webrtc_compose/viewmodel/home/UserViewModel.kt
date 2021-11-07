package com.iffly.webrtc_compose.viewmodel.home

import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import com.iffly.webrtc_compose.viewmodel.BaseMVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class UserViewModel@Inject constructor() :
    BaseMVIViewModel<UserViewModel.UserViewState, UserViewModel.UserViewAction>(UserViewState::class.java) {
    data class UserViewState(val loading: Boolean = true, val list: List<UserItem> = emptyList())

    data class UserViewAction(val action: UserViewActionValue, val data: String) {
        enum class UserViewActionValue {
            Refresh, Call, ChangeLoading
        }
    }

    override suspend fun reduce(action: UserViewAction, state: UserViewState): UserViewState {
        return when (action.action) {
            UserViewAction.UserViewActionValue.Call -> {
                state.copy()
            }
            UserViewAction.UserViewActionValue.ChangeLoading -> {
                state.copy(loading = true)
            }
            UserViewAction.UserViewActionValue.Refresh -> {

                try {
                    val list = UserRepo.getUsers()
                    delay(1000)
                    state.copy(loading = false, list = list)
                } catch (e: Exception) {
                    state.copy(loading = false)
                }
            }
        }
    }
}