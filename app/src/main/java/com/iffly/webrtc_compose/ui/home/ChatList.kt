package com.iffly.webrtc_compose.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iffly.webrtc_compose.model.ChatItem
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme


@Composable
fun ChatList(list: Array<ChatItem>) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        list.map {
            ChatItemLayout(item = it)
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
            tint = WebrtcTheme.colors.iconInteractive,
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
    ChatItemLayout(item = ChatItem("1234", "iffly"))
}

@Preview
@Composable
fun ChatPre() {
    ChatList(
        list = arrayOf(
            ChatItem("1234", "iffly"),
            ChatItem("1235", "iffly1")
        )
    )
}