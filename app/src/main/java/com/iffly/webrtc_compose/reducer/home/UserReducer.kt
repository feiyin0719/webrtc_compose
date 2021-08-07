package com.iffly.webrtc_compose.reducer.home

import com.iffly.compose.redux.Reducer
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay


data class UserViewState(val loading: Boolean, val list: List<UserItem>)

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
                val list = GlobalScope.async(Dispatchers.IO) {
                    delay(1000)
                    return@async UserRepo.getUsers()
                }.await()
                return state.copy(loading = false, list = list)
            }
            UserViewAction.UserViewActionValue.ChangeLadoing -> {
                return state.copy(loading = true)
            }

        }
    }

    override fun initState(): UserViewState {
        return UserViewState(false, emptyList())
    }

}