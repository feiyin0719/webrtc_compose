package com.iffly.rtcchat.engine

import com.iffly.rtcchat.CallEndReason
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


interface EngineCallback {
    /**
     * 加入房间成功
     */
    fun joinRoomSucc()

    /**
     * 退出房间成功
     */
    fun exitRoom()

    /**
     * 拒绝连接
     * @param type type
     */
    fun reject(type: Int)

    fun disconnected(reason: CallEndReason?)

    fun onSendIceCandidate(userId: String?, candidate: IceCandidate?)

    fun onSendOffer(userId: String?, description: SessionDescription?)

    fun onSendAnswer(userId: String?, description: SessionDescription?)

    fun onRemoteStream(userId: String?)

    fun onDisconnected(userId: String?)
}