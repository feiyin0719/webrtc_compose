package com.iffly.webrtc_compose.reducer.home

import com.iffly.compose.redux.Reducer
import com.iffly.webrtc_compose.data.bean.ChatRoomItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

data class ChatRoomViewState(
    val loading: Boolean = true,
    val list: List<ChatRoomItem> = emptyList()
)

data class ChatRoomAction(val action: ChatRoomActionValue, val data: String) {
    enum class ChatRoomActionValue {
        Refresh, Call
    }
}

class ChatRoomReducer :
    Reducer<ChatRoomViewState, ChatRoomAction>(
        ChatRoomViewState::class.java,
        ChatRoomAction::class.java
    ) {
    override fun reduce(
        state: ChatRoomViewState,
        flow: Flow<ChatRoomAction>
    ): Flow<ChatRoomViewState> {
        return flow.map { action ->
            return@map when (action.action) {
                ChatRoomAction.ChatRoomActionValue.Call -> {
                    state.copy()
                }
                ChatRoomAction.ChatRoomActionValue.Refresh -> {

                    try {
                        val list = UserRepo.getRooms()
                        delay(1000)
                        state.copy(loading = false, list = list)
                    } catch (e: Exception) {
                        state.copy(loading = false)
                    }
                }
            }

        }.flowOn(Dispatchers.IO).onStart {
            emit(state.copy(loading = true))
        }
    }
}