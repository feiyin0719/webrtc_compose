package com.iffly.webrtc_compose.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.People
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.iffly.webrtc_compose.model.ChatItem
import com.iffly.webrtc_compose.model.UserItem
import com.iffly.webrtc_compose.ui.components.Sections

val CHAT_KEY = "chats"
val PEOPLE_KEY = "peoples"
val HomeSections = mapOf<String, Sections>(
    CHAT_KEY to Sections("chat", Icons.Outlined.Chat, "home/chat"),
    PEOPLE_KEY to Sections("people", Icons.Outlined.People, "home/people")

)


fun NavGraphBuilder.addHomeGraph(

) {
    HomeSections[CHAT_KEY]?.let {
        composable(it.route) { from ->
            ChatList(
                IntRange(0, 20).map {
                    ChatItem("$it", "$it")
                }.toList()
            )
        }
    }

    HomeSections[PEOPLE_KEY]?.let {
        composable(it.route) { from ->
            UserList(
                IntRange(0, 20)
                    .map {
                        UserItem(
                            "$it",
                            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic.616pic.com%2Fys_bnew_img%2F00%2F04%2F45%2FNElJeK0ngd.jpg&refer=http%3A%2F%2Fpic.616pic.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1630481481&t=2b3253775dc674e0d5443dfd673d1aef",
                            "${it}"
                        )
                    }.toList()
            )
        }
    }

}