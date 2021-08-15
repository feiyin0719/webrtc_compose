package com.iffly.webrtc_compose.data.bean

data class ChatRoomItem(
    val roomId: String,
    val userId: String,
    val maxSize: Int,
    val currentSize: Int
)