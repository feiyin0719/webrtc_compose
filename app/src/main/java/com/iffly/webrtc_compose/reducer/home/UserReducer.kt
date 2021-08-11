package com.iffly.webrtc_compose.reducer.home

import com.iffly.compose.redux.Reducer
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


data class UserViewState(val loading: Boolean = true, val list: List<UserItem> = emptyList())

data class UserViewAction(val action: UserViewActionValue, val data: String) {
    enum class UserViewActionValue {
        Refresh, Call, ChangeLadoing
    }
}

class UserReducer :
    Reducer<UserViewState, UserViewAction>(UserViewState::class.java, UserViewAction::class.java) {
    override suspend fun reduce(state: UserViewState, action: UserViewAction): UserViewState {
        when (action.action) {
            UserViewAction.UserViewActionValue.Call -> {
                return state.copy()
            }
            UserViewAction.UserViewActionValue.Refresh -> {
                try {
                    val list = withContext(Dispatchers.IO) {
                        delay(1000)
                        UserRepo.getUsers()
                    }
                    return state.copy(loading = false, list = list)
                } catch (e: Exception) {
                    return state.copy(loading = false)
                }

            }
            UserViewAction.UserViewActionValue.ChangeLadoing -> {
                return state.copy(loading = true)
            }

        }
    }


}