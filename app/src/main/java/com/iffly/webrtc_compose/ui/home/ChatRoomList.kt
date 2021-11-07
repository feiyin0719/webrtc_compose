package com.iffly.webrtc_compose.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.iffly.webrtc_compose.data.bean.ChatRoomItem
import com.iffly.webrtc_compose.ui.components.AppSurface
import com.iffly.webrtc_compose.ui.components.AppTitleBar
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme


@Composable
fun ChatList(
    list: List<ChatRoomItem>?,
    isLoading: Boolean = false,
    refreshListener: () -> Unit = {},
    callClick: (String) -> Unit = {}
) {
    AppSurface(Modifier.fillMaxSize()) {
        Column {
            AppTitleBar(title = "room")

            SwipeRefresh(
                state =
                rememberSwipeRefreshState(isRefreshing = isLoading),
                onRefresh = refreshListener,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                list?.let {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        items(list) {
                            ChatItemLayout(roomItem = it)

                        }
                    }

                }

            }

        }
    }
}

@Composable
fun ChatItemLayout(roomItem: ChatRoomItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Chat,
            contentDescription = "",
            tint = WebrtcTheme.colors.brand,
            modifier = Modifier.size(
                50.dp, 50.dp
            )
        )
        Column(
            Modifier
                .wrapContentSize()
                .offset(20.dp, 0.dp)
        ) {
            Text(
                text = roomItem.roomId,
                style = Typography.h6,

                )
            Text(
                text = "${roomItem.currentSize}/${roomItem.maxSize}",
                style = Typography.h6
            )
        }

    }
}

@Preview
@Composable
fun ChatItemPre() {
    WebrtcTheme() {
        ChatItemLayout(roomItem = ChatRoomItem("1234", "iffly", 0, 0))
    }
}

@Preview
@Composable
fun ChatPre() {
    WebrtcTheme() {
        ChatList(
            IntRange(0, 20).map {
                ChatRoomItem("$it", "$it", 0, 0)
            }.toList()
        )
    }

}