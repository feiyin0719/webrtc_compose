package com.iffly.webrtc_compose.voip

import android.media.AsyncPlayer
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import com.iffly.rtcchat.inter.ISkyEvent
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.R
import com.iffly.webrtc_compose.socket.SocketManager
import com.iffly.webrtc_compose.voip.VoipEvent


class VoipEvent : ISkyEvent {
    private val ringPlayer: AsyncPlayer
    override fun createRoom(room: String?, roomSize: Int) {
        SocketManager.createRoom(room, roomSize)
    }

    override fun sendInvite(room: String?, userIds: List<String?>?, audioOnly: Boolean) {
        SocketManager.sendInvite(room, userIds, audioOnly)
    }

    override fun sendRefuse(room: String?, inviteId: String?, refuseType: Int) {
        SocketManager.sendRefuse(room, inviteId, refuseType)
    }

    override fun sendTransAudio(toId: String?) {
        SocketManager.sendTransAudio(toId)
    }

    override fun sendDisConnect(room: String?, toId: String?, isCrashed: Boolean) {
        SocketManager.sendDisconnect(room, toId)
    }

    override fun sendCancel(mRoomId: String?, toIds: List<String?>?) {
        SocketManager.sendCancel(mRoomId, toIds)
    }

    override fun sendJoin(room: String?) {
        SocketManager.sendJoin(room)
    }

    override fun sendRingBack(targetId: String?, room: String?) {
        SocketManager.sendRingBack(targetId, room)
    }

    override fun sendLeave(room: String?, userId: String?) {
        SocketManager.sendLeave(room, userId)
    }

    override fun sendOffer(userId: String?, sdp: String?) {
        SocketManager.sendOffer(userId, sdp)
    }

    override fun sendAnswer(userId: String?, sdp: String?) {
        SocketManager.sendAnswer(userId, sdp)
    }

    override fun sendIceCandidate(userId: String?, id: String?, label: Int, candidate: String?) {
        SocketManager.sendIceCandidate(userId, id, label, candidate)
    }

    override fun onRemoteRing() {}

    //==============================================================================
    override fun shouldStartRing(isComing: Boolean) {
        if (isComing) {
            val uri = Uri.parse(
                "android.resource://" + App.instance!!.getPackageName()
                    .toString() + "/" + R.raw.incoming_call_ring
            )
            ringPlayer.play(App.instance!!, uri, true, AudioManager.STREAM_RING)
        } else {
            val uri = Uri.parse(
                "android.resource://" + App.instance!!
                    .toString() + "/" + R.raw.wr_ringback
            )
            ringPlayer.play(App.instance!!, uri, true, AudioManager.STREAM_RING)
        }
    }

    override fun shouldStopRing() {
        Log.d(TAG, "shouldStopRing begin")
        ringPlayer.stop()
    }

    companion object {
        private const val TAG = "VoipEvent"
    }

    init {
        ringPlayer = AsyncPlayer(null)
    }
}