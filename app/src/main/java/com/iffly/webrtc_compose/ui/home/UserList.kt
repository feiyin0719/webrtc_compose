package com.iffly.webrtc_compose.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iffly.webrtc_compose.model.UserItem
import com.iffly.webrtc_compose.ui.LocalNavController
import com.iffly.webrtc_compose.ui.components.AppImage
import com.iffly.webrtc_compose.ui.components.AppTitleBar
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

@Composable
fun UserList(peoples: List<UserItem>) {

    Column {
        AppTitleBar(title = "user")
        LazyColumn(Modifier.fillMaxSize()) {
            items(peoples) {
                UserItemLayout(user = it)
            }
        }
    }
}

@Composable
fun UserItemLayout(user: UserItem) {
    val navController = LocalNavController.current
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp)
    ) {
        AppImage(
            user.avatar,
            contentDescription = "${user.userId}",
            modifier = Modifier.size(
                50.dp, 50.dp
            )
        )
        Text(
            text = user.nickName,
            style = Typography.h4,
            modifier = Modifier.offset(20.dp, 0.dp)
        )
    }
}

@Preview
@Composable
private fun UserListPre() {
    WebrtcTheme {
        UserList(
            peoples =
            IntRange(0, 20)
                .map {
                    UserItem(
                        "$it",
                        "https://source.unsplash.com/UsSdMZ78Q3E",
                        "${it}"
                    )
                }.toList()
        )

    }
}