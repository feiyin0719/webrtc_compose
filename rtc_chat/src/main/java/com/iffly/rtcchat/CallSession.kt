package com.iffly.rtcchat

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.iffly.rtcchat.engine.EngineCallback
import com.iffly.rtcchat.engine.webrtc.WebRtcEngine
import com.iffly.rtcchat.inter.ISkyEvent
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface CallSessionCallback {
    fun didCallEndWithReason(var1: CallEndReason?)
    fun didChangeState(var1: CallState?)
    fun didChangeMode(isAudioOnly: Boolean)
    fun didCreateLocalVideoTrack()
    fun didReceiveRemoteVideoTrack(userId: String?)
    fun didUserLeave(userId: String?)
    fun didError(error: String?)
    fun didDisconnected(userId: String?)
}

class CallSession(context: Context, roomId: String, audioOnly: Boolean, event: ISkyEvent) :
    EngineCallback {
    private var sessionCallback: WeakReference<CallSessionCallback>? = null
    private val executor: ExecutorService
    private val handler: Handler = Handler(Looper.getMainLooper())

    //------------------------------------各种参数----------------------------------------------/
    // session参数
    var isAudioOnly: Boolean

    // 房间人列表
    private var mUserIDList: List<String>? = null

    // 单聊对方Id/群聊邀请人
    var mTargetId: String? = null

    // 房间Id
    var roomId: String
        private set

    // myId
    var mMyId: String? = null

    // 房间大小
    private var mRoomSize = 0
    var isComing = false
    var state: CallState = CallState.Idle
        private set

    // --------------------------------界面显示相关--------------------------------------------/
    var startTime: Long = 0
        private set
    private val iEngine: AVEngine?
    private val mEvent: ISkyEvent?

    // ----------------------------------------各种控制--------------------------------------------
    // 创建房间
    fun createHome(room: String?, roomSize: Int) {
        executor.execute {
            mEvent?.createRoom(room, roomSize)
        }
    }

    // 加入房间
    fun joinHome(roomId: String?) {
        executor.execute {
            state = CallState.Connecting
            Log.d(TAG, "joinHome mEvent = $mEvent")
            isComing = true
            mEvent?.sendJoin(roomId)
        }
    }

    //开始响铃
    fun shouldStartRing() {
        mEvent?.shouldStartRing(true)
    }

    // 关闭响铃
    fun shouldStopRing() {
        Log.d(TAG, "shouldStopRing mEvent != null is " + (mEvent != null))
        mEvent?.shouldStopRing()
    }

    // 发送响铃回复
    fun sendRingBack(targetId: String?, room: String?) {
        executor.execute {
            mEvent?.sendRingBack(targetId, room)
        }
    }

    // 发送拒绝信令
    fun sendRefuse() {
        executor.execute {
            mEvent?.sendRefuse(roomId, mTargetId, RefuseType.Hangup.ordinal)
        }
        release(CallEndReason.Hangup)
    }

    // 发送忙时拒绝
    fun sendBusyRefuse(room: String?, targetId: String?) {
        executor.execute {
            mEvent?.sendRefuse(room, targetId, RefuseType.Busy.ordinal)
        }
        release(CallEndReason.Hangup)
    }

    // 发送取消信令
    fun sendCancel() {
        executor.execute {
            if (mEvent != null) {
                // 取消拨出
                val list: MutableList<String?> = ArrayList()
                list.add(mTargetId)
                mEvent.sendCancel(roomId, list)
            }
        }
        release(CallEndReason.Hangup)
    }

    // 离开房间
    fun leave() {
        executor.execute {
            mEvent?.sendLeave(roomId, mMyId)
        }
        // 释放变量
        release(CallEndReason.Hangup)
    }

    // 切换到语音接听
    fun sendTransAudio() {
        executor.execute {
            mEvent?.sendTransAudio(mTargetId)
        }
    }

    // 设置静音
    fun toggleMuteAudio(enable: Boolean): Boolean {
        return iEngine!!.muteAudio(enable)
    }

    // 设置扬声器
    fun toggleSpeaker(enable: Boolean): Boolean {
        return iEngine!!.toggleSpeaker(enable)
    }

    // 设置扬声器
    fun toggleHeadset(isHeadset: Boolean): Boolean {
        return iEngine!!.toggleHeadset(isHeadset)
    }

    // 切换到语音通话
    fun switchToAudio() {
        isAudioOnly = true
        // 告诉远端
        sendTransAudio()
        // 本地切换

        sessionCallback?.get()?.didChangeMode(true)

    }

    // 调整摄像头前置后置
    fun switchCamera() {
        iEngine!!.switchCamera()
    }

    // 释放资源
    private fun release(reason: CallEndReason) {
        executor.execute {

            // 释放内容
            iEngine!!.release()
            // 状态设置为Idle
            state = CallState.Idle

            //界面回调

            sessionCallback?.get()?.didCallEndWithReason(reason)


        }
    }

    //------------------------------------receive---------------------------------------------------
    // 加入房间成功
    fun onJoinHome(myId: String?, users: String, roomSize: Int) {
        // 开始计时
        mRoomSize = roomSize
        startTime = 0
        handler.post {
            executor.execute {
                mMyId = myId
                val strings: List<String>
                if (!TextUtils.isEmpty(users)) {
                    val split = users.split(",").toTypedArray()
                    strings = split.toList()
                    mUserIDList = strings
                }

                // 发送邀请
                if (!isComing) {
                    if (roomSize == 2) {
                        val inviteList: MutableList<String?> = ArrayList()
                        inviteList.add(mTargetId)
                        mEvent!!.sendInvite(roomId, inviteList, isAudioOnly)
                    }
                } else {
                    iEngine!!.joinRoom(mUserIDList!!)
                }
                if (!isAudioOnly) {
                    // 画面预览

                    sessionCallback?.get()?.didCreateLocalVideoTrack()

                }
            }
        }
    }

    // 新成员进入
    fun newPeer(userId: String?) {
        handler.post {
            executor.execute {

                // 其他人加入房间
                iEngine!!.userIn(userId!!)

                // 关闭响铃
                mEvent?.shouldStopRing()
                // 更换界面
                state = CallState.Connected
                if (sessionCallback != null && sessionCallback?.get() != null) {
                    startTime = System.currentTimeMillis()
                    sessionCallback?.get()?.didChangeState(state)
                }
            }
        }
    }

    // 对方已拒绝
    fun onRefuse(userId: String?, type: Int) {
        iEngine!!.userReject(userId!!, type)
    }

    // 对方已响铃
    fun onRingBack(userId: String?) {
        mEvent?.onRemoteRing()
    }

    // 切换到语音
    fun onTransAudio(userId: String?) {
        isAudioOnly = true
        // 本地切换

        sessionCallback?.get()?.didChangeMode(true)

    }

    // 对方网络断开
    fun onDisConnect(userId: String?, reason: CallEndReason?) {
        executor.execute { iEngine!!.disconnected(userId!!, reason!!) }
    }

    // 对方取消拨出
    fun onCancel(userId: String) {
        Log.d(TAG, "onCancel userId = $userId")
        shouldStopRing()
        release(CallEndReason.RemoteHangup)
    }

    fun onReceiveOffer(userId: String?, description: String?) {
        executor.execute { iEngine!!.receiveOffer(userId!!, description!!) }
    }

    fun onReceiverAnswer(userId: String?, sdp: String?) {
        executor.execute { iEngine!!.receiveAnswer(userId!!, sdp!!) }
    }

    fun onRemoteIceCandidate(userId: String?, id: String?, label: Int, candidate: String?) {
        executor.execute { iEngine!!.receiveIceCandidate(userId!!, id!!, label, candidate!!) }
    }

    // 对方离开房间
    fun onLeave(userId: String?) {
        if (mRoomSize > 2) {
            // 返回到界面上

            sessionCallback?.get()?.didUserLeave(userId)

        }
        executor.execute { iEngine!!.leaveRoom(userId!!) }
    }

    fun setupLocalVideo(isOverlay: Boolean): View? {
        return iEngine!!.startPreview(isOverlay)
    }

    fun setupRemoteVideo(userId: String?, isOverlay: Boolean): View? {
        return iEngine!!.setupRemoteVideo(userId!!, isOverlay)
    }

    fun setTargetId(targetIds: String?) {
        mTargetId = targetIds
    }

    fun setRoom(_room: String) {
        roomId = _room
    }

    fun setCallState(callState: CallState) {
        state = callState
    }

    fun setSessionCallback(sessionCallback: CallSessionCallback?) {
        this.sessionCallback = WeakReference(sessionCallback)
    }

    //-----------------------------Engine回调-----------------------------------------
    override fun joinRoomSucc() {
        // 关闭响铃
        mEvent?.shouldStopRing()
        // 更换界面
        state = CallState.Connected
        //Log.d(TAG, "joinRoomSucc, sessionCallback.get() = " + sessionCallback.get());
        if (sessionCallback != null && sessionCallback?.get() != null) {
            startTime = System.currentTimeMillis()
            sessionCallback?.get()?.didChangeState(state)
        }
    }

    override fun exitRoom() {
        // 状态设置为Idle
        if (mRoomSize == 2) {
            handler.post { release(CallEndReason.RemoteHangup) }
        }
    }

    override fun reject(type: Int) {
        shouldStopRing()
        Log.d(TAG, "reject type = $type")
        when (type) {
            0 -> release(CallEndReason.Busy)
            1 -> release(CallEndReason.RemoteHangup)
        }
        //        });
    }

    override fun disconnected(reason: CallEndReason) {
        handler.post {
            shouldStopRing()
            release(reason)
        }
    }

    override fun onSendIceCandidate(userId: String, candidate: IceCandidate?) {
        executor.execute {
            if (mEvent != null) {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                Log.d("dds_test", "onSendIceCandidate")
                mEvent.sendIceCandidate(
                    userId,
                    candidate!!.sdpMid,
                    candidate.sdpMLineIndex,
                    candidate.sdp
                )
            }
        }
    }

    override fun onSendOffer(userId: String, description: SessionDescription?) {
        executor.execute {
            if (mEvent != null) {
                Log.d("dds_test", "onSendOffer")
                mEvent.sendOffer(userId, description!!.description)
            }
        }
    }

    override fun onSendAnswer(userId: String, description: SessionDescription?) {
        executor.execute {
            if (mEvent != null) {
                Log.d("dds_test", "onSendAnswer")
                mEvent.sendAnswer(userId, description!!.description)
            }
        }
    }

    override fun onRemoteStream(userId: String) {
        // 画面预览
        if (sessionCallback != null && sessionCallback?.get() != null) {
            Log.d(TAG, "onRemoteStream sessionCallback.get() != null ")
            sessionCallback?.get()?.didReceiveRemoteVideoTrack(userId)
        } else {
            Log.d(TAG, "onRemoteStream sessionCallback.get() == null ")
        }
    }

    override fun onDisconnected(userId: String) {
        //断线了，需要关闭通话界面
        if (sessionCallback != null && sessionCallback?.get() != null) {
            Log.d(TAG, "onDisconnected sessionCallback.get() != null ")
            sessionCallback?.get()?.didDisconnected(userId)
        } else {
            Log.d(TAG, "onDisconnected sessionCallback.get() == null ")
        }
    }


    companion object {
        private const val TAG = "CallSession"
    }

    init {
        executor = Executors.newSingleThreadExecutor()
        isAudioOnly = audioOnly
        this.roomId = roomId
        mEvent = event
        iEngine = AVEngine.createEngine(WebRtcEngine(audioOnly, context))
        iEngine!!.init(this)
    }
}