package com.iffly.webrtc_compose.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.iffly.webrtc_compose.model.ChatItem
import com.iffly.webrtc_compose.ui.components.Sections

val CHAT_KEY = "chats"
val HomeSections = mapOf<String, Sections>(
    CHAT_KEY to Sections("chat", Icons.Outlined.Chat, "home/chat")

)


fun NavGraphBuilder.addHomeGraph(

) {
    HomeSections[CHAT_KEY]?.let {
        composable(it.route) { from ->
            ChatList(
                list = arrayOf(
                    ChatItem("1234", "iffly"),
                    ChatItem("1235", "iffly1")
                )
            )
        }
    }

}