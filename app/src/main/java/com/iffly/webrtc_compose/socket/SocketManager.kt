package com.iffly.webrtc_compose.socket

import android.content.ComponentName
import android.content.Intent
import android.util.Log
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.socket.MyWebSocket.TrustManagerTest
import com.iffly.webrtc_compose.voip.Utils
import com.iffly.webrtc_compose.voip.VoipReceiver
import kotlinx.coroutines.*
import java.io.Closeable
import java.lang.ref.WeakReference
import java.net.URI
import java.net.URISyntaxException
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager


object SocketManager : IEvent, Closeable {
    private var webSocket: MyWebSocket? = null

    //===========================================================================================
    var userState = 0
        private set
    private lateinit var myId: String

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    fun connect(url: String, userId: String, device: Int) {
        if (webSocket == null || !webSocket!!.isOpen) {
            val uri: URI = try {
                val urls = "$url/$userId/$device"
                URI(urls)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                return
            }
            webSocket = MyWebSocket(uri, this)
            // 设置wss
            if (url.startsWith("wss")) {
                try {
                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext?.init(
                        null,
                        arrayOf<TrustManager>(TrustManagerTest()),
                        SecureRandom()
                    )
                    var factory: SSLSocketFactory? = null
                    if (sslContext != null) {
                        factory = sslContext.socketFactory
                    }
                    if (factory != null) {
                        webSocket!!.socket = factory.createSocket()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // 开始connect
            webSocket!!.connect()
        }
    }

    fun unConnect() {
        if (webSocket != null) {
            webSocket!!.setConnectFlag(false)
            webSocket!!.close()
            webSocket = null
        }
    }

    override fun onOpen() {
        Log.i(TAG, "socket is open!")
    }

    override fun loginSuccess(userId: String, avatar: String) {
        Log.i(TAG, "loginSuccess:$userId")
        myId = userId
        userState = 1

        iUserState?.get()?.userLogin()

    }

    // ======================================================================================
    fun createRoom(room: String, roomSize: Int) {
        if (webSocket != null) {
            webSocket!!.createRoom(room, roomSize, myId)
        }
    }

    fun sendInvite(room: String, users: List<String?>?, audioOnly: Boolean) {
        if (webSocket != null) {
            webSocket!!.sendInvite(room, myId, users, audioOnly)
        }
    }

    fun sendLeave(room: String, userId: String) {
        if (webSocket != null) {
            webSocket!!.sendLeave(myId, room, userId)
        }
    }

    fun sendRingBack(targetId: String, room: String) {
        if (webSocket != null) {
            webSocket!!.sendRing(myId, targetId, room)
        }
    }

    fun sendRefuse(room: String, inviteId: String, refuseType: Int) {
        if (webSocket != null) {
            webSocket!!.sendRefuse(room, inviteId, myId, refuseType)
        }
    }

    fun sendCancel(mRoomId: String, userIds: List<String>) {
        if (webSocket != null) {
            webSocket!!.sendCancel(mRoomId, myId, userIds)
        }
    }

    fun sendJoin(room: String) {
        if (webSocket != null) {
            webSocket!!.sendJoin(room, myId)
        }
    }

    fun sendMeetingInvite(userList: String) {}
    fun sendOffer(userId: String, sdp: String) {
        if (webSocket != null) {
            webSocket!!.sendOffer(myId, userId, sdp)
        }
    }

    fun sendAnswer(userId: String, sdp: String) {
        if (webSocket != null) {
            webSocket!!.sendAnswer(myId, userId, sdp)
        }
    }

    fun sendIceCandidate(userId: String, id: String, label: Int, candidate: String) {
        if (webSocket != null) {
            webSocket!!.sendIceCandidate(myId, userId, id, label, candidate)
        }
    }

    fun sendTransAudio(userId: String) {
        if (webSocket != null) {
            webSocket!!.sendTransAudio(myId, userId)
        }
    }

    fun sendDisconnect(room: String, userId: String) {
        if (webSocket != null) {
            webSocket!!.sendDisconnect(room, myId, userId)
        }
    }

    // ========================================================================================
    override fun onInvite(room: String, audioOnly: Boolean, inviteId: String, userList: String) {
        val intent = Intent()
        intent.putExtra("room", room)
        intent.putExtra("audioOnly", audioOnly)
        intent.putExtra("inviteId", inviteId)
        intent.putExtra("userList", userList)
        intent.action = Utils.ACTION_VOIP_RECEIVER
        intent.component = ComponentName(
            App.instance!!.getPackageName(),
            VoipReceiver::class.java.getName()
        )
        // 发送广播
        App.instance!!.sendBroadcast(intent)
    }

    override fun onCancel(inviteId: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onCancel(inviteId)
        }
    }

    override fun onRing(fromId: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onRingBack(fromId)
        }
    }

    // 加入房间
    override fun onPeers(myId: String, connections: String, roomSize: Int) {
        coroutineScope.launch {

            //自己进入了房间，然后开始发送offer
            SkyEngineKit.instance().currentSession?.onJoinHome(myId, connections, roomSize)
        }
    }

    override fun onNewPeer(userId: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.newPeer(userId)
        }
    }

    override fun onReject(userId: String, type: Int) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onRefuse(userId, type)
        }
    }

    override fun onOffer(userId: String, sdp: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onReceiveOffer(userId, sdp)
        }
    }

    override fun onAnswer(userId: String, sdp: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onReceiverAnswer(userId, sdp)
        }
    }

    override fun onIceCandidate(userId: String, id: String, label: Int, candidate: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onRemoteIceCandidate(
                userId,
                id,
                label,
                candidate
            )
        }
    }

    override fun onLeave(userId: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onLeave(userId)
        }
    }

    override fun logout(str: String) {
        Log.i(TAG, "logout:$str")
        userState = 0

        iUserState?.get()?.userLogout()

    }

    override fun onTransAudio(userId: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onTransAudio(userId)
        }
    }

    override fun onDisConnect(userId: String) {
        coroutineScope.launch {
            SkyEngineKit.instance().currentSession?.onDisConnect(
                userId,
                CallEndReason.RemoteSignalError
            )
        }
    }

    override fun reConnect() {
        coroutineScope.launch { webSocket!!.reconnect() }
    }

    private var iUserState: WeakReference<IUserState>? = null
    fun addUserStateCallback(userState: IUserState) {
        iUserState = WeakReference(userState)
    }


    private const val TAG = "dds_SocketManager"

    override fun close() {
        coroutineScope.cancel()
    }

}