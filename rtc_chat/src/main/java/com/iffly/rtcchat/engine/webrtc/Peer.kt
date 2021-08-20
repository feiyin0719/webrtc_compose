package com.iffly.rtcchat.engine.webrtc

import android.content.Context
import android.util.Log
import com.iffly.rtcchat.render.ProxyVideoSink
import org.webrtc.*
import org.webrtc.PeerConnection.*
import org.webrtc.RendererCommon.RendererEvents


interface IPeerEvent {

    fun onSendIceCandidate(userId: String, candidate: IceCandidate?)

    fun onSendOffer(userId: String, description: SessionDescription?)

    fun onSendAnswer(userId: String, description: SessionDescription?)

    fun onRemoteStream(userId: String, stream: MediaStream?)

    fun onRemoveStream(userId: String, stream: MediaStream?)


    fun onDisconnected(userId: String)
}

class Peer(
    factory: PeerConnectionFactory,
    list: List<IceServer>,
    userId: String,
    event: IPeerEvent
) : SdpObserver, Observer {
    companion object {
        const val TAG = "dds_Peer"
    }

    private var pc: PeerConnection? = null
    private var mUserId: String = userId
    private var queuedRemoteCandidates: MutableList<IceCandidate>? = null
    private var localSdp: SessionDescription? = null
    private var mFactory: PeerConnectionFactory = factory
    private var mIceLis: List<IceServer> = list
    private var mEvent: IPeerEvent = event
    private var isOffer = false

    private var _remoteStream: MediaStream? = null
    var renderer: SurfaceViewRenderer? = null
    var sink: ProxyVideoSink? = null


    init {
        queuedRemoteCandidates = ArrayList()
        pc = createPeerConnection()
        Log.d("dds_test", "create Peer:$mUserId")
    }

    fun createPeerConnection(): PeerConnection? {
        val rtcConfig = RTCConfiguration(mIceLis)
        return mFactory.createPeerConnection(rtcConfig, this)
    }

    fun setOffer(isOffer: Boolean) {
        this.isOffer = isOffer
    }

    // 创建offer
    fun createOffer() {
        if (pc == null) return
        Log.d("dds_test", "createOffer")
        pc?.createOffer(this, offerOrAnswerConstraint())
    }

    // 创建answer
    fun createAnswer() {
        pc?.let {
            Log.d("dds_test", "createAnswer")
            it.createAnswer(this, offerOrAnswerConstraint())
        }

    }

    // 设置LocalDescription
    fun setLocalDescription(sdp: SessionDescription?) {
        pc?.let {
            Log.d("dds_test", "setLocalDescription")
            it.setLocalDescription(this, sdp)
        }

    }

    // 设置RemoteDescription
    fun setRemoteDescription(sdp: SessionDescription?) {
        pc?.let {
            Log.d("dds_test", "setRemoteDescription")
            it.setRemoteDescription(this, sdp)
        }

    }

    //添加本地流
    fun addLocalStream(stream: MediaStream?) {
        pc?.let {
            Log.d("dds_test", "addLocalStream$mUserId")
            it.addStream(stream)
        }
    }

    // 添加RemoteIceCandidate
    @Synchronized
    fun addRemoteIceCandidate(candidate: IceCandidate) {
        Log.d("dds_test", "addRemoteIceCandidate")
        if (pc != null) {
            if (queuedRemoteCandidates != null) {
                Log.d("dds_test", "addRemoteIceCandidate  2222")
                synchronized(Peer::class.java) {
                    if (queuedRemoteCandidates != null) {
                        queuedRemoteCandidates!!.add(candidate)
                    }
                }
            } else {
                Log.d("dds_test", "addRemoteIceCandidate1111")
                pc?.addIceCandidate(candidate)
            }
        }
    }

    // 移除RemoteIceCandidates
    fun removeRemoteIceCandidates(candidates: Array<IceCandidate?>?) {
        pc?.let {
            drainCandidates()
            it.removeIceCandidates(candidates)
        }

    }

    fun createRender(mRootEglBase: EglBase, context: Context?, isOverlay: Boolean) {
        renderer = SurfaceViewRenderer(context)
        renderer!!.init(mRootEglBase.eglBaseContext, object : RendererEvents {
            override fun onFirstFrameRendered() {
                Log.d(TAG, "createRender onFirstFrameRendered")
            }

            override fun onFrameResolutionChanged(
                videoWidth: Int,
                videoHeight: Int,
                rotation: Int
            ) {
                Log.d(TAG, "createRender onFrameResolutionChanged")
            }
        })
        renderer!!.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        renderer!!.setMirror(true)
        renderer!!.setZOrderMediaOverlay(isOverlay)
        sink = ProxyVideoSink()
        sink!!.target = renderer
        if (_remoteStream != null && _remoteStream!!.videoTracks.size > 0) {
            _remoteStream!!.videoTracks[0].addSink(sink)
        }
    }

    // 关闭Peer
    fun close() {
        if (renderer != null) {
            renderer!!.release()
            renderer = null
        }
        if (sink != null) {
            sink!!.target = null
        }
        if (pc != null) {
            try {
                pc?.close()
                pc?.dispose()
            } catch (e: Exception) {
            }
        }
    }

    //------------------------------Observer-------------------------------------
    override fun onSignalingChange(signalingState: SignalingState) {
        Log.i(TAG, "onSignalingChange: $signalingState")
    }

    override fun onIceConnectionChange(newState: IceConnectionState) {
        Log.i(TAG, "onIceConnectionChange: $newState")
        if (newState == IceConnectionState.DISCONNECTED || newState == IceConnectionState.FAILED) {
            mEvent.onDisconnected(mUserId)
        }
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {
        Log.i(TAG, "onIceConnectionReceivingChange:$receiving")
    }

    override fun onIceGatheringChange(newState: IceGatheringState) {
        Log.i(TAG, "onIceGatheringChange:$newState")
    }

    override fun onIceCandidate(candidate: IceCandidate?) {
        // 发送IceCandidate
        mEvent.onSendIceCandidate(mUserId, candidate)
    }

    override fun onIceCandidatesRemoved(candidates: Array<IceCandidate?>?) {
        Log.i(TAG, "onIceCandidatesRemoved:")
    }

    override fun onAddStream(stream: MediaStream) {
        Log.i(TAG, "onAddStream:")
        stream.audioTracks[0].setEnabled(true)
        _remoteStream = stream

        mEvent.onRemoteStream(mUserId, stream)

    }

    override fun onRemoveStream(stream: MediaStream?) {
        Log.i(TAG, "onRemoveStream:")

        mEvent.onRemoveStream(mUserId, stream)

    }

    override fun onDataChannel(dataChannel: DataChannel?) {
        Log.i(TAG, "onDataChannel:")
    }

    override fun onRenegotiationNeeded() {
        Log.i(TAG, "onRenegotiationNeeded:")
    }

    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<MediaStream?>) {
        Log.i(TAG, "onAddTrack:" + mediaStreams.size)
    }


    //-------------SdpObserver--------------------
    override fun onCreateSuccess(origSdp: SessionDescription) {
        Log.d(TAG, "sdp创建成功       " + origSdp.type)
        val sdpString = origSdp.description
        val sdp = SessionDescription(origSdp.type, sdpString)
        localSdp = sdp
        setLocalDescription(sdp)
    }

    override fun onSetSuccess() {
        Log.d(TAG, "sdp连接成功   " + pc!!.signalingState().toString())
        if (pc == null) return
        // 发送者
        if (isOffer) {
            if (pc?.remoteDescription == null) {
                Log.d(TAG, "Local SDP set succesfully")
                if (!isOffer) {
                    //接收者，发送Answer
                    mEvent.onSendAnswer(mUserId, localSdp)
                } else {
                    //发送者,发送自己的offer
                    mEvent.onSendOffer(mUserId, localSdp)
                }
            } else {
                Log.d(TAG, "Remote SDP set succesfully")
                drainCandidates()
            }
        } else {
            if (pc?.localDescription != null) {
                Log.d(TAG, "Local SDP set succesfully")
                if (!isOffer) {
                    //接收者，发送Answer
                    mEvent.onSendAnswer(mUserId, localSdp)
                } else {
                    //发送者,发送自己的offer
                    mEvent.onSendOffer(mUserId, localSdp)
                }
                drainCandidates()
            } else {
                Log.d(TAG, "Remote SDP set succesfully")
            }
        }
    }

    override fun onCreateFailure(error: String) {
        Log.i(TAG, " SdpObserver onCreateFailure:$error")
    }

    override fun onSetFailure(error: String) {
        Log.i(TAG, "SdpObserver onSetFailure:$error")
    }


    private fun drainCandidates() {
        Log.i("dds_test", "drainCandidates")
        synchronized(Peer::class.java) {
            if (queuedRemoteCandidates != null) {
                Log.d(TAG, "Add " + queuedRemoteCandidates!!.size + " remote candidates")
                for (candidate in queuedRemoteCandidates!!) {
                    pc!!.addIceCandidate(candidate)
                }
                queuedRemoteCandidates = null
            }
        }
    }

    private fun offerOrAnswerConstraint(): MediaConstraints {
        val mediaConstraints = MediaConstraints()
        val keyValuePairs: ArrayList<MediaConstraints.KeyValuePair> = ArrayList()
        keyValuePairs.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        keyValuePairs.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mediaConstraints.mandatory.addAll(keyValuePairs)
        return mediaConstraints
    }

    // ----------------------------回调-----------------------------------
}