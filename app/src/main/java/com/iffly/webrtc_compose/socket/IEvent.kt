package com.iffly.webrtc_compose.socket

interface IEvent {
    fun onOpen()
    fun loginSuccess(userId: String, avatar: String)
    fun onInvite(room: String, audioOnly: Boolean, inviteId: String, userList: String)
    fun onCancel(inviteId: String)
    fun onRing(userId: String)
    fun onPeers(myId: String, userList: String, roomSize: Int)
    fun onNewPeer(myId: String)
    fun onReject(userId: String, type: Int)

    // onOffer
    fun onOffer(userId: String, sdp: String)

    // onAnswer
    fun onAnswer(userId: String, sdp: String)

    // ice-candidate
    fun onIceCandidate(userId: String, id: String, label: Int, candidate: String)
    fun onLeave(userId: String)
    fun logout(str: String)
    fun onTransAudio(userId: String)
    fun onDisConnect(userId: String)
    fun reConnect()
}