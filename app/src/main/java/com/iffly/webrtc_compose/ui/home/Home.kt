package com.iffly.webrtc_compose.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.People
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.iffly.webrtc_compose.model.ChatItem
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
            ChatList(
                list = listOf(
                    ChatItem("1234", "user 1"),
                    ChatItem("1235", "user 2")
                )
            )
        }
    }

}