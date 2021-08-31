package com.iffly.webrtc_compose.viewmodel.home

import com.iffly.compose.libredux.Reducer
import com.iffly.webrtc_compose.data.bean.ChatRoomItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ChatRoomViewState(
    val loading: Boolean = true,
    val list: List<ChatRoomItem> = emptyList()
)

data class ChatRoomAction(val action: ChatRoomActionValue, val data: String) {
    enum class ChatRoomActionValue {
        Refresh, Call, ChangeLoading
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
                ChatRoomAction.ChatRoomActionValue.ChangeLoading -> {
                    state.copy(loading = true)
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
        }
    }
}