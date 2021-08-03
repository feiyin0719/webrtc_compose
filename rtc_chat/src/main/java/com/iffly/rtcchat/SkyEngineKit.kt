package com.iffly.rtcchat

import android.content.Context
import android.util.Log
import com.iffly.rtcchat.except.NotInitializedException
import com.iffly.rtcchat.inter.ISkyEvent


class SkyEngineKit {
    // 获取对话实例
    var currentSession: CallSession? = null
        private set
    private var mEvent: ISkyEvent? = null
    var isAudioOnly = false
        private set
    var isOutGoing = false
        private set

    fun sendRefuseOnPermissionDenied(room: String, inviteId: String) {
        // 未初始化
        if (avEngineKit == null) {
            Log.e(TAG, "startOutCall error,please init first")
            return
        }
        if (currentSession != null) {
            endCall()
        } else {
            avEngineKit!!.mEvent!!.sendRefuse(room, inviteId, RefuseType.Hangup.ordinal)
        }
    }

    fun sendDisconnected(room: String, toId: String, isCrashed: Boolean) {
        // 未初始化
        if (avEngineKit == null) {
            Log.e(TAG, "startOutCall error,please init first")
            return
        }
        avEngineKit!!.mEvent!!.sendDisConnect(room, toId, isCrashed)
    }

    // 拨打电话
    fun startOutCall(
        context: Context, room: String, targetId: String,
        audioOnly: Boolean
    ): Boolean {
        // 未初始化
        if (avEngineKit == null) {
            Log.e(TAG, "startOutCall error,please init first")
            return false
        }
        // 忙线中
        if (currentSession != null && currentSession!!.state !== CallState.Idle) {
            Log.i(TAG, "startCall error,currentCallSession is exist")
            return false
        }
        isAudioOnly = audioOnly
        isOutGoing = true
        // 初始化会话
        currentSession = CallSession(context, room!!, audioOnly, mEvent!!)
        currentSession!!.setTargetId(targetId)
        currentSession!!.isComing = false
        currentSession!!.setCallState(CallState.Outgoing)
        // 创建房间
        currentSession!!.createHome(room, 2)
        return true
    }

    // 接听电话
    fun startInCall(
        context: Context, room: String, targetId: String,
        audioOnly: Boolean
    ): Boolean {
        if (avEngineKit == null) {
            Log.e(TAG, "startInCall error,init is not set")
            return false
        }
        // 忙线中
        if (currentSession != null && currentSession!!.state !== CallState.Idle) {
            // 发送->忙线中...
            Log.i(TAG, "startInCall busy,currentCallSession is exist,start sendBusyRefuse!")
            currentSession!!.sendBusyRefuse(room, targetId)
            return false
        }
        isOutGoing = false
        isAudioOnly = audioOnly
        // 初始化会话
        currentSession = CallSession(context, room!!, audioOnly, mEvent!!)
        currentSession!!.setTargetId(targetId)
        currentSession!!.isComing = true
        currentSession!!.setCallState(CallState.Incoming)

        // 开始响铃并回复
        currentSession!!.shouldStartRing()
        currentSession!!.sendRingBack(targetId, room)
        return true
    }

    // 挂断会话
    fun endCall() {
        Log.d(TAG, "endCall mCurrentCallSession != null is " + (currentSession != null))
        if (currentSession != null) {
            // 停止响铃
            currentSession!!.shouldStopRing()
            if (currentSession!!.isComing) {
                if (currentSession!!.state === CallState.Incoming) {
                    // 接收到邀请，还没同意，发送拒绝
                    currentSession!!.sendRefuse()
                } else {
                    // 已经接通，挂断电话
                    currentSession!!.leave()
                }
            } else {
                if (currentSession!!.state === CallState.Outgoing) {
                    currentSession!!.sendCancel()
                } else {
                    // 已经接通，挂断电话
                    currentSession!!.leave()
                }
            }
            currentSession!!.setCallState(CallState.Idle)
        }
    }

    // 加入房间
    fun joinRoom(context: Context, room: String) {
        if (avEngineKit == null) {
            Log.e(TAG, "joinRoom error,init is not set")
            return
        }
        // 忙线中
        if (currentSession != null && currentSession!!.state !== CallState.Idle) {
            Log.e(TAG, "joinRoom error,currentCallSession is exist")
            return
        }
        currentSession = CallSession(context, room!!, false, mEvent!!)
        currentSession!!.isComing = true
        currentSession!!.joinHome(room)
    }

    fun createAndJoinRoom(context: Context, room: String) {
        if (avEngineKit == null) {
            Log.e(TAG, "joinRoom error,init is not set")
            return
        }
        // 忙线中
        if (currentSession != null && currentSession!!.state !== CallState.Idle) {
            Log.e(TAG, "joinRoom error,currentCallSession is exist")
            return
        }
        currentSession = CallSession(context, room!!, false, mEvent!!)
        currentSession!!.isComing = false
        currentSession!!.createHome(room, 9)
    }

    // 离开房间
    fun leaveRoom() {
        if (avEngineKit == null) {
            Log.e(TAG, "leaveRoom error,init is not set")
            return
        }
        if (currentSession != null) {
            currentSession!!.leave()
            currentSession!!.setCallState(CallState.Idle)
        }
    }

    fun transferToAudio() {
        isAudioOnly = true
    }

    companion object {
        private const val TAG = "dds_AVEngineKit"
        private var avEngineKit: SkyEngineKit? = null
        fun Instance(): SkyEngineKit {
            var skyEngineKit: SkyEngineKit
            return if (avEngineKit.also { skyEngineKit = it!! } != null) {
                skyEngineKit
            } else {
                throw NotInitializedException()
            }
        }

        // 初始化
        fun init(iSocketEvent: ISkyEvent?) {
            if (avEngineKit == null) {
                avEngineKit = SkyEngineKit()
                avEngineKit!!.mEvent = iSocketEvent
            }
        }
    }
}
