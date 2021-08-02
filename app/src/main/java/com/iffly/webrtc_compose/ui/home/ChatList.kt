package com.iffly.webrtc_compose.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iffly.webrtc_compose.data.bean.ChatItem
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.ui.components.AppSurface
import com.iffly.webrtc_compose.ui.components.AppTitleBar
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme
import com.iffly.webrtc_compose.viewmodel.home.UserViewModel



@Composable
fun ChatList(list: List<ChatItem>) {
    AppSurface(Modifier.fillMaxSize()) {
        Column {
            AppTitleBar(title = "chat")
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                items(list) {
                    ChatItemLayout(item = it)

                }
            }
        }
    }
}

@Composable
fun ChatItemLayout(item: ChatItem) {
    Row(
        Modifier
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
        Text(
            text = item.name,
            style = Typography.h4,
            modifier = Modifier.offset(20.dp, 0.dp)
        )
    }
}

@Preview
@Composable
fun ChatItemPre() {
    WebrtcTheme() {
        ChatItemLayout(item = ChatItem("1234", "iffly"))
    }
}

@Preview
@Composable
fun ChatPre() {
    WebrtcTheme() {
        ChatList(
            IntRange(0, 20).map {
                ChatItem("$it", "$it")
            }.toList()
        )
    }

}