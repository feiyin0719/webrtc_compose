package com.iffly.rtcchat.engine

import android.view.View
import com.iffly.rtcchat.CallEndReason

interface IEngine {
    /**
     * 初始化
     */
    fun init(callback: EngineCallback?)

    /**
     * 加入房間
     */
    fun joinRoom(userIds: List<String?>?)

    /**
     * 有人进入房间
     */
    fun userIn(userId: String?)

    /**
     * 用户拒绝
     * @param userId userId
     * @param type type
     */
    fun userReject(userId: String?, type: Int)

    /**
     * 用户网络断开
     * @param userId userId
     * @param reason
     */
    fun disconnected(userId: String?, reason: CallEndReason?)

    /**
     * receive Offer
     */
    fun receiveOffer(userId: String?, description: String?)

    /**
     * receive Answer
     */
    fun receiveAnswer(userId: String?, sdp: String?)

    /**
     * receive IceCandidate
     */
    fun receiveIceCandidate(userId: String?, id: String?, label: Int, candidate: String?)

    /**
     * 离开房间
     *
     * @param userId userId
     */
    fun leaveRoom(userId: String?)

    /**
     * 开启本地预览
     */
    fun startPreview(isOverlay: Boolean): View?

    /**
     * 关闭本地预览
     */
    fun stopPreview()

    /**
     * 开始远端推流
     */
    fun startStream()

    /**
     * 停止远端推流
     */
    fun stopStream()

    /**
     * 开始远端预览
     */
    fun setupRemoteVideo(userId: String?, isO: Boolean): View?

    /**
     * 关闭远端预览
     */
    fun stopRemoteVideo()

    /**
     * 切换摄像头
     */
    fun switchCamera()

    /**
     * 设置静音
     */
    fun muteAudio(enable: Boolean): Boolean

    /**
     * 开启扬声器
     */
    fun toggleSpeaker(enable: Boolean): Boolean

    /**
     * 切换外放和耳机
     */
    fun toggleHeadset(isHeadset: Boolean): Boolean

    /**
     * 释放所有内容
     */
    fun release()
}