package com.iffly.webrtc_compose.reducer.home

import com.iffly.compose.libredux.Reducer
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*


data class UserViewState(val loading: Boolean = true, val list: List<UserItem> = emptyList())

data class UserViewAction(val action: UserViewActionValue, val data: String) {
    enum class UserViewActionValue {
        Refresh, Call, ChangeLoading
    }
}

class UserReducer :
    Reducer<UserViewState, UserViewAction>(UserViewState::class.java, UserViewAction::class.java) {
    override fun reduce(state: UserViewState, flow: Flow<UserViewAction>): Flow<UserViewState> {
        return flow.map { action ->
            return@map when (action.action) {
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

        }.flowOn(Dispatchers.IO)
    }
}