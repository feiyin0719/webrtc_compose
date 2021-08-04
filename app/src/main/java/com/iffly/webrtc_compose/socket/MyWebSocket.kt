package com.iffly.webrtc_compose.socket

import android.annotation.SuppressLint
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.iffly.webrtc_compose.util.StringUtil
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class MyWebSocket(serverUri: URI?, private val iEvent: IEvent) : WebSocketClient(serverUri) {
    private var connectFlag = false
    override fun onClose(code: Int, reason: String, remote: Boolean) {
        Log.e("dds_error", "onClose:" + reason + "remote:" + remote)
        if (connectFlag) {
            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            iEvent.reConnect()
        } else {
            iEvent.logout("onClose")
        }
    }

    override fun onError(ex: Exception) {
        Log.e("dds_error", "onError:$ex")
        iEvent.logout("onError")
        connectFlag = false
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        Log.e("dds_info", "onOpen")
        iEvent.onOpen()
        connectFlag = true
    }

    override fun onMessage(message: String) {
        Log.d(TAG, message)
        handleMessage(message)
    }

    fun setConnectFlag(flag: Boolean) {
        connectFlag = flag
    }

    // ---------------------------------------处理接收消息-------------------------------------
    private fun handleMessage(message: String) {
        val map = JSON.parseObject<Map<*, *>>(message, MutableMap::class.java)
        val eventName = map["eventName"] as String? ?: return
        // 登录成功
        if (eventName == "__login_success") {
            handleLogin(map)
            return
        }
        // 被邀请
        if (eventName == "__invite") {
            handleInvite(map)
            return
        }
        // 取消拨出
        if (eventName == "__cancel") {
            handleCancel(map)
            return
        }
        // 响铃
        if (eventName == "__ring") {
            handleRing(map)
            return
        }
        // 进入房间
        if (eventName == "__peers") {
            handlePeers(map)
            return
        }
        // 新人入房间
        if (eventName == "__new_peer") {
            handleNewPeer(map)
            return
        }
        // 拒绝接听
        if (eventName == "__reject") {
            handleReject(map)
            return
        }
        // offer
        if (eventName == "__offer") {
            handleOffer(map)
            return
        }
        // answer
        if (eventName == "__answer") {
            handleAnswer(map)
            return
        }
        // ice-candidate
        if (eventName == "__ice_candidate") {
            handleIceCandidate(map)
        }
        // 离开房间
        if (eventName == "__leave") {
            handleLeave(map)
        }
        // 切换到语音
        if (eventName == "__audio") {
            handleTransAudio(map)
        }
        // 意外断开
        if (eventName == "__disconnect") {
            handleDisConnect(map)
        }
    }

    private fun handleDisConnect(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val fromId = data["fromID"] as String?
            fromId?.let {
                iEvent.onDisConnect(it)
            }

        }
    }

    private fun handleTransAudio(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val fromId = data["fromID"] as String?
            fromId?.let {
                iEvent.onTransAudio(fromId)
            }

        }
    }

    private fun handleLogin(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val userID = data["userID"] as String?
            val avatar = data["avatar"] as String?
            if (userID != null && avatar != null)
                iEvent.loginSuccess(userID, avatar)
        }
    }

    private fun handleIceCandidate(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val userID = data["fromID"] as String?
            val id = data["id"] as String?
            val label = data["label"] as Int
            val candidate = data["candidate"] as String?
            if (userID != null && id != null)
                iEvent.onIceCandidate(userID, id, label, candidate)
        }
    }

    private fun handleAnswer(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val sdp = data["sdp"] as String?
            val userID = data["fromID"] as String?
            if (userID != null)
                iEvent.onAnswer(userID, sdp)
        }
    }

    private fun handleOffer(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val sdp = data["sdp"] as String?
            val userID = data["fromID"] as String?
            if (userID != null)
                iEvent.onOffer(userID, sdp)
        }
    }

    private fun handleReject(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val fromID = data["fromID"] as String?
            val rejectType = data["refuseType"].toString().toInt()
            if (fromID != null)
                iEvent.onReject(fromID, rejectType)
        }
    }

    private fun handlePeers(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val you = data["you"] as String?
            val connections = data["connections"] as String?
            val roomSize = data["roomSize"] as Int
            if (you != null && connections != null)
                iEvent.onPeers(you, connections, roomSize)
        }
    }

    private fun handleNewPeer(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val userID = data["userID"] as String?
            if (userID != null)
                iEvent.onNewPeer(userID)
        }
    }

    private fun handleRing(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val fromId = data["fromID"] as String?
            if (fromId != null)
                iEvent.onRing(fromId)
        }
    }

    private fun handleCancel(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val inviteID = data["inviteID"] as String?
            val userList = data["userList"] as String?
            if (inviteID != null)
                iEvent.onCancel(inviteID)
        }
    }

    private fun handleInvite(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val room = data["room"] as String?
            val audioOnly = data["audioOnly"] as Boolean
            val inviteID = data["inviteID"] as String?
            val userList = data["userList"] as String?
            if (room != null && inviteID != null && userList != null)
                iEvent.onInvite(room, audioOnly, inviteID, userList)
        }
    }

    private fun handleLeave(map: Map<*, *>) {
        val data = map["data"] as Map<*, *>?
        if (data != null) {
            val fromID = data["fromID"] as String?
            if (fromID != null)
                iEvent.onLeave(fromID)
        }
    }

    /**
     * ------------------------------发送消息----------------------------------------
     */
    fun createRoom(room: String, roomSize: Int, myId: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__create"
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["room"] = room
        childMap["roomSize"] = roomSize
        childMap["userID"] = myId
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // 发送邀请
    fun sendInvite(room: String, myId: String, users: List<String?>?, audioOnly: Boolean) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__invite"
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["room"] = room
        childMap["audioOnly"] = audioOnly
        childMap["inviteID"] = myId
        val join = StringUtil.listToString(users)
        childMap["userList"] = join
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // 取消邀请
    fun sendCancel(mRoomId: String, useId: String, users: List<String>) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__cancel"
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["inviteID"] = useId
        childMap["room"] = mRoomId
        val join = StringUtil.listToString(users)
        childMap["userList"] = join
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // 发送响铃通知
    fun sendRing(myId: String, toId: String, room: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__ring"
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["fromID"] = myId
        childMap["toID"] = toId
        childMap["room"] = room
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    //加入房间
    fun sendJoin(room: String, myId: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__join"
        val childMap: MutableMap<String, String> = HashMap()
        childMap["room"] = room
        childMap["userID"] = myId
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // 拒接接听
    fun sendRefuse(room: String, inviteID: String, myId: String, refuseType: Int) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__reject"
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["room"] = room
        childMap["toID"] = inviteID
        childMap["fromID"] = myId
        childMap["refuseType"] = refuseType.toString()
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // 离开房间
    fun sendLeave(myId: String, room: String, userId: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__leave"
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["room"] = room
        childMap["fromID"] = myId
        childMap["userID"] = userId
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        if (isOpen) {
            send(jsonString)
        }
    }

    // send offer
    fun sendOffer(myId: String, userId: String, sdp: String) {
        val map: MutableMap<String, Any> = HashMap()
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["sdp"] = sdp
        childMap["userID"] = userId
        childMap["fromID"] = myId
        map["data"] = childMap
        map["eventName"] = "__offer"
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // send answer
    fun sendAnswer(myId: String, userId: String, sdp: String) {
        val map: MutableMap<String, Any> = HashMap()
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["sdp"] = sdp
        childMap["fromID"] = myId
        childMap["userID"] = userId
        map["data"] = childMap
        map["eventName"] = "__answer"
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // send ice-candidate
    fun sendIceCandidate(myId: String, userId: String, id: String, label: Int, candidate: String) {
        val map: MutableMap<String, Any> = HashMap()
        map["eventName"] = "__ice_candidate"
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["userID"] = userId
        childMap["fromID"] = myId
        childMap["id"] = id
        childMap["label"] = label
        childMap["candidate"] = candidate
        map["data"] = childMap
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        if (isOpen) {
            send(jsonString)
        }
    }

    // 切换到语音
    fun sendTransAudio(myId: String, userId: String) {
        val map: MutableMap<String, Any> = HashMap()
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["fromID"] = myId
        childMap["userID"] = userId
        map["data"] = childMap
        map["eventName"] = "__audio"
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // 断开重连
    fun sendDisconnect(room: String, myId: String, userId: String) {
        val map: MutableMap<String, Any> = HashMap()
        val childMap: MutableMap<String, Any> = HashMap()
        childMap["fromID"] = myId
        childMap["userID"] = userId
        childMap["room"] = room
        map["data"] = childMap
        map["eventName"] = "__disconnect"
        val `object` = JSONObject(map)
        val jsonString = `object`.toString()
        Log.d(TAG, "send-->$jsonString")
        send(jsonString)
    }

    // 忽略证书
    class TrustManagerTest : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }
    }

    companion object {
        private const val TAG = "dds_WebSocket"
    }
}