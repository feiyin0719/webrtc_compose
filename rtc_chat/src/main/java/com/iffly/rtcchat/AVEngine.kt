package com.iffly.rtcchat

import android.util.Log
import android.view.View
import com.iffly.rtcchat.engine.EngineCallback
import com.iffly.rtcchat.engine.IEngine


class AVEngine private constructor(var iEngine: IEngine) : IEngine {

    override fun init(callback: EngineCallback) {
        iEngine.init(callback)
    }

    override fun joinRoom(userIds: List<String>) {
        iEngine.joinRoom(userIds)
    }

    override fun userIn(userId: String) {
        iEngine.userIn(userId)
    }

    override fun userReject(userId: String, type: Int) {
        iEngine.userReject(userId, type)
    }

    override fun disconnected(userId: String, reason: CallEndReason) {
        iEngine.disconnected(userId!!, reason!!)
    }

    override fun receiveOffer(userId: String, description: String) {
        iEngine.receiveOffer(userId, description)
    }

    override fun receiveAnswer(userId: String, sdp: String) {
        iEngine.receiveAnswer(userId, sdp)
    }

    override fun receiveIceCandidate(userId: String, id: String, label: Int, candidate: String) {
        iEngine.receiveIceCandidate(userId, id, label, candidate)
    }

    override fun leaveRoom(userId: String) {
        Log.d(TAG, "leaveRoom iEngine = $iEngine")
        iEngine.leaveRoom(userId)
    }

    override fun startPreview(isO: Boolean): View? {
        return iEngine.startPreview(isO)
    }

    override fun stopPreview() {
        iEngine.stopPreview()
    }

    override fun startStream() {
        iEngine.startStream()
    }

    override fun stopStream() {
        iEngine.stopStream()
    }

    override fun setupRemoteVideo(userId: String, isO: Boolean): View? {
        return iEngine.setupRemoteVideo(userId, isO)
    }

    override fun stopRemoteVideo() {
        iEngine.stopRemoteVideo()
    }

    override fun switchCamera() {
        iEngine.switchCamera()
    }

    override fun muteAudio(enable: Boolean): Boolean {
        return iEngine.muteAudio(enable)
    }

    override fun toggleSpeaker(enable: Boolean): Boolean {
        return iEngine.toggleSpeaker(enable)
    }

    override fun toggleHeadset(isHeadset: Boolean): Boolean {
        return iEngine.toggleHeadset(isHeadset)
    }

    override fun release() {
        Log.d(TAG, "release")
        iEngine.release()
    }

    companion object {
        private const val TAG = "AVEngine"

        @Volatile
        private var instance: AVEngine? = null
        fun createEngine(engine: IEngine): AVEngine? {
            if (null == instance) {
                synchronized(AVEngine::class.java) {
                    if (null == instance) {
                        instance = AVEngine(engine)
                    }
                }
            }
            return instance
        }
    }


}
