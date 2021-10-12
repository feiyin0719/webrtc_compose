package com.iffly.webrtc_compose.viewmodel.home

import com.iffly.webrtc_compose.data.bean.ChatRoomItem
import com.iffly.webrtc_compose.data.repo.net.UserRepo
import com.iffly.webrtc_compose.viewmodel.BaseMVIViewModel
import kotlinx.coroutines.delay


class ChatRoomViewModel :
    BaseMVIViewModel<ChatRoomViewModel.ChatRoomViewState, ChatRoomViewModel.ChatRoomAction>(
        ChatRoomViewState::class.java
    ) {
    data class ChatRoomViewState(
        val loading: Boolean = true,
        val list: List<ChatRoomItem> = emptyList()
    )

    data class ChatRoomAction(val action: ChatRoomActionValue, val data: String) {
        enum class ChatRoomActionValue {
            Refresh, Call, ChangeLoading
        }
    }

    override suspend fun reduce(
        action: ChatRoomAction,
        state: ChatRoomViewState
    ): ChatRoomViewState {
        return when (action.action) {
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