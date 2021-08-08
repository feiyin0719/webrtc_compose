package com.iffly.rtcchat.engine.webrtc

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.projection.MediaProjection
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.engine.EngineCallback
import com.iffly.rtcchat.engine.IEngine
import com.iffly.rtcchat.render.ProxyVideoSink
import org.webrtc.*
import org.webrtc.CameraVideoCapturer.CameraSwitchHandler
import org.webrtc.PeerConnection.IceServer
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.concurrent.ConcurrentHashMap


class WebRtcEngine : IEngine, IPeerEvent {
    private val TAG = "WebRTCEngine"
    private var _factory: PeerConnectionFactory? = null
    private var mRootEglBase: EglBase? = null
    private var _localStream: MediaStream? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var _localAudioTrack: AudioTrack? = null
    private var captureAndroid: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var localSink: ProxyVideoSink? = null
    private var localRenderer: SurfaceViewRenderer? = null


    private val VIDEO_TRACK_ID = "ARDAMSv0"
    private val AUDIO_TRACK_ID = "ARDAMSa0"
    val VIDEO_CODEC_H264 = "H264"
    private val VIDEO_RESOLUTION_WIDTH = 640
    private val VIDEO_RESOLUTION_HEIGHT = 480
    private val FPS = 20

    // 对话实例列表
    private val peers: ConcurrentHashMap<String, Peer> = ConcurrentHashMap()

    // 服务器实例列表
    private val iceServers: MutableList<IceServer> = ArrayList()

    private lateinit var mCallback: EngineCallback

    var mIsAudioOnly = false
    private var mContext: Context? = null
    private var audioManager: AudioManager? = null
    private var isSpeakerOn = true

    constructor(mIsAudioOnly: Boolean, mContext: Context) {
        this.mIsAudioOnly = mIsAudioOnly
        this.mContext = mContext
        audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 初始化ice地址
        initIceServer()
    }


    // -----------------------------------对外方法------------------------------------------
    override fun init(callback: EngineCallback) {
        mCallback = callback
        if (mRootEglBase == null) {
            mRootEglBase = EglBase.create()
        }
        if (_factory == null) {
            _factory = createConnectionFactory()
        }
        if (_localStream == null) {
            createLocalStream()
        }
    }

    override fun joinRoom(userIds: List<String>) {
        for (id in userIds) {
            // create Peer
            val peer = Peer(_factory!!, iceServers, id, this)
            peer.setOffer(false)
            // add localStream
            peer.addLocalStream(_localStream)
            // 添加列表
            peers.put(id, peer)
        }

        mCallback.joinRoomSucc()

        if (isHeadphonesPlugged()) {
            toggleHeadset(true)
        } else {
            if (mIsAudioOnly) toggleSpeaker(false) else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioManager?.setMode(AudioManager.MODE_IN_COMMUNICATION)
                } else {
                    audioManager?.setMode(AudioManager.MODE_IN_CALL)
                }
            }
        }
    }

    override fun userIn(userId: String) {
        // create Peer
        val peer = Peer(_factory!!, iceServers, userId, this)
        peer.setOffer(true)
        // add localStream
        peer.addLocalStream(_localStream)
        // 添加列表
        peers.put(userId, peer)
        // createOffer
        peer.createOffer()
    }

    override fun userReject(userId: String, type: Int) {
        //拒绝接听userId应该是没有添加进peers里去不需要remove
//       Peer peer = peers.get(userId);
//        if (peer != null) {
//            peer.close();
//            peers.remove(userId);
//        }
//        if (peers.size() == 0) {

        mCallback.reject(type)

//        }
    }

    override fun disconnected(userId: String, reason: CallEndReason) {

        mCallback.disconnected(reason)

    }

    override fun receiveOffer(userId: String, description: String) {
        val peer: Peer? = peers.get(userId)
        peer?.let {
            val sdp = SessionDescription(SessionDescription.Type.OFFER, description)
            it.setOffer(false)
            it.setRemoteDescription(sdp)
            it.createAnswer()
        }
    }

    override fun receiveAnswer(userId: String, sdp: String) {
        Log.d("dds_test", "receiveAnswer--$userId")
        val peer: Peer? = peers.get(userId)
        peer?.let {
            val sessionDescription = SessionDescription(SessionDescription.Type.ANSWER, sdp)
            it.setRemoteDescription(sessionDescription)
        }
    }

    override fun receiveIceCandidate(userId: String, id: String, label: Int, candidate: String) {
        Log.d("dds_test", "receiveIceCandidate--$userId")
        val peer: Peer? = peers.get(userId)
        peer?.let {
            val iceCandidate = IceCandidate(id, label, candidate)
            it.addRemoteIceCandidate(iceCandidate)
        }
    }

    override fun leaveRoom(userId: String) {
        val peer: Peer? = peers.get(userId)
        if (peer != null) {
            peer.close()
            peers.remove(userId)
        }
        Log.d(
            TAG,
            "leaveRoom peers.size() = " + peers.size.toString() + "; mCallback = " + mCallback
        )
        if (peers.size <= 1) {

            mCallback.exitRoom()

            if (peers.size === 1) {
                peers.forEach {
                    it.value.close()
                }
                peers.clear()
            }
        }
    }

    override fun startPreview(isOverlay: Boolean): View? {
        if (mRootEglBase == null) {
            return null
        }
        localRenderer = SurfaceViewRenderer(mContext)
        localRenderer!!.init(mRootEglBase!!.eglBaseContext, null)
        localRenderer!!.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        localRenderer!!.setMirror(true)
        localRenderer!!.setZOrderMediaOverlay(isOverlay)
        localSink = ProxyVideoSink()
        localSink!!.target = localRenderer
        if (_localStream!!.videoTracks.size > 0) {
            _localStream!!.videoTracks[0].addSink(localSink)
        }
        return localRenderer
    }

    override fun stopPreview() {
        if (localSink != null) {
            localSink!!.target = null
            localSink = null
        }
        if (audioSource != null) {
            audioSource!!.dispose()
            audioSource = null
        }
        // 释放摄像头
        if (captureAndroid != null) {
            try {
                captureAndroid!!.stopCapture()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            captureAndroid!!.dispose()
            captureAndroid = null
        }
        // 释放画布
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper!!.dispose()
            surfaceTextureHelper = null
        }
        if (videoSource != null) {
            videoSource!!.dispose()
            videoSource = null
        }
        if (_localStream != null) {
            _localStream = null
        }
        if (localRenderer != null) {
            localRenderer!!.release()
        }
    }

    override fun startStream() {}

    override fun stopStream() {}


    override fun setupRemoteVideo(userId: String, isO: Boolean): View? {
        if (TextUtils.isEmpty(userId)) {
            Log.e(TAG, "setupRemoteVideo userId is null ")
            return null
        }
        val peer: Peer = peers.get(userId) ?: return null
        if (peer.renderer == null) {
            peer.createRender(mRootEglBase!!, mContext, isO)
        }
        return peer.renderer
    }

    override fun stopRemoteVideo() {}

    private var isSwitch = false // 是否正在切换摄像头


    override fun switchCamera() {
        if (isSwitch) return
        isSwitch = true
        if (captureAndroid == null) return
        if (captureAndroid is CameraVideoCapturer) {
            val cameraVideoCapturer = captureAndroid as CameraVideoCapturer
            try {
                cameraVideoCapturer.switchCamera(object : CameraSwitchHandler {
                    override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                        if(isFrontCamera){
                            localRenderer?.setMirror(true)
                        }
                        else{
                            localRenderer?.setMirror(false)
                        }
                        isSwitch = false
                    }

                    override fun onCameraSwitchError(errorDescription: String) {
                        isSwitch = false
                    }
                })
            } catch (e: Exception) {
                isSwitch = false
            }
        } else {
            Log.d(TAG, "Will not switch camera, video caputurer is not a camera")
        }
    }

    override fun muteAudio(enable: Boolean): Boolean {
        if (_localAudioTrack != null) {
            _localAudioTrack!!.setEnabled(!enable)
            return true
        }
        return false
    }

    override fun toggleSpeaker(enable: Boolean): Boolean {
        audioManager?.let { audioManager ->
            isSpeakerOn = enable
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
            if (enable) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.FX_KEY_CLICK
                )
                audioManager.setSpeakerphoneOn(true)
            } else {
                //5.0以上
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
                } else {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_CALL)
                }
                //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
                audioManager.setStreamVolume(
                    AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.FX_KEY_CLICK
                )
                audioManager.setSpeakerphoneOn(false)
            }
            return true
        }
        return false
    }

    override fun toggleHeadset(isHeadset: Boolean): Boolean {
        audioManager?.let { audioManager ->
            if (isHeadset) {
                //5.0以上
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
                } else {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_CALL)
                }
                audioManager.setSpeakerphoneOn(false)
            } else {
                if (mIsAudioOnly) {
                    toggleSpeaker(isSpeakerOn)
                }
            }
        }
        return false
    }

    private fun isHeadphonesPlugged(): Boolean {
        audioManager?.let { audioManager ->
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val audioDevices: Array<AudioDeviceInfo> =
                    audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (deviceInfo in audioDevices) {
                    if (deviceInfo.getType() === AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() === AudioDeviceInfo.TYPE_WIRED_HEADSET
                    ) {
                        return true
                    }
                }
                false
            } else {
                audioManager.isWiredHeadsetOn()
            }
        }
        return false

    }

    override fun release() {
        if (audioManager != null) {
            audioManager!!.setMode(AudioManager.MODE_NORMAL)
        }

        peers.forEach {
            it.value.close()
        }
        peers.clear()


        // 停止预览
        stopPreview()
        if (_factory != null) {
            _factory!!.dispose()
            _factory = null
        }
        if (mRootEglBase != null) {
            mRootEglBase!!.release()
            mRootEglBase = null
        }
    }

    // -----------------------------其他方法--------------------------------

    // -----------------------------其他方法--------------------------------
    private fun initIceServer() {
        // 初始化一些stun和turn的地址
        val var1 = IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
        iceServers.add(var1)
        val var11 = IceServer.builder("stun:42.192.40.58:3478?transport=udp")
            .createIceServer()
        val var12 = IceServer.builder("turn:42.192.40.58:3478?transport=udp")
            .setUsername("ddssingsong")
            .setPassword("123456")
            .createIceServer()
        val var13 = IceServer.builder("turn:42.192.40.58:3478?transport=tcp")
            .setUsername("ddssingsong")
            .setPassword("123456")
            .createIceServer()
        iceServers.add(var11)
        iceServers.add(var12)
        iceServers.add(var13)
    }

    /**
     * 构造PeerConnectionFactory
     *
     * @return PeerConnectionFactory
     */
    fun createConnectionFactory(): PeerConnectionFactory? {

        // 1. 初始化的方法，必须在开始之前调用
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(mContext)
                .createInitializationOptions()
        )

        // 2. 设置编解码方式：默认方法
        val encoderFactory: VideoEncoderFactory
        val decoderFactory: VideoDecoderFactory
        encoderFactory = DefaultVideoEncoderFactory(
            mRootEglBase!!.eglBaseContext,
            true,
            true
        )
        decoderFactory = DefaultVideoDecoderFactory(mRootEglBase!!.eglBaseContext)

        // 构造Factory
        val audioDeviceModule: AudioDeviceModule =
            JavaAudioDeviceModule.builder(mContext).createAudioDeviceModule()
        val options = PeerConnectionFactory.Options()
        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    /**
     * 创建本地流
     */
    fun createLocalStream() {
        if (_factory != null) {
            _localStream = _factory!!.createLocalMediaStream("ARDAMS")
            // 音频
            audioSource = _factory!!.createAudioSource(createAudioConstraints())
            _localAudioTrack = _factory!!.createAudioTrack(AUDIO_TRACK_ID, audioSource)
            _localStream?.addTrack(_localAudioTrack)

            // 视频
            if (!mIsAudioOnly) {
                captureAndroid = createVideoCapture()
                surfaceTextureHelper =
                    SurfaceTextureHelper.create("CaptureThread", mRootEglBase!!.eglBaseContext)
                videoSource = _factory!!.createVideoSource(captureAndroid!!.isScreencast)
                captureAndroid!!.initialize(
                    surfaceTextureHelper,
                    mContext,
                    videoSource?.getCapturerObserver()
                )
                captureAndroid!!.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS)
                val _localVideoTrack = _factory!!.createVideoTrack(VIDEO_TRACK_ID, videoSource)
                _localStream?.addTrack(_localVideoTrack)
            }
        }
    }


    // 是否使用录屏
    private val screencaptureEnabled = false

    /**
     * 创建媒体方式
     *
     * @return VideoCapturer
     */
    private fun createVideoCapture(): VideoCapturer? {
        val videoCapturer: VideoCapturer?
        if (screencaptureEnabled) {
            return createScreenCapturer()
        }
        videoCapturer = if (Camera2Enumerator.isSupported(mContext)) {
            createCameraCapture(Camera2Enumerator(mContext))
        } else {
            createCameraCapture(Camera1Enumerator(true))
        }
        return videoCapturer
    }

    /**
     * 创建相机媒体流
     */
    private fun createCameraCapture(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        // First, try to find front facing camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try something else
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }


    private val mediaProjectionPermissionResultData: Intent? = null
    private val mediaProjectionPermissionResultCode = 0

    @TargetApi(21)
    private fun createScreenCapturer(): VideoCapturer? {
        return if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            null
        } else ScreenCapturerAndroid(
            mediaProjectionPermissionResultData, object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.e(TAG, "User revoked permission to capture the screen.")
                }
            })
    }

    //**************************************各种约束******************************************/
    private val AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"
    private val AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl"
    private val AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter"
    private val AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"

    // 配置音频参数
    private fun createAudioConstraints(): MediaConstraints? {
        val audioConstraints = MediaConstraints()
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true")
        )
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false")
        )
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false")
        )
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true")
        )
        return audioConstraints
    }

    //------------------------------------回调---------------------------------------------
    override fun onSendIceCandidate(userId: String, candidate: IceCandidate?) {

        mCallback.onSendIceCandidate(userId, candidate)

    }

    override fun onSendOffer(userId: String, description: SessionDescription?) {

        mCallback.onSendOffer(userId, description)

    }

    override fun onSendAnswer(userId: String, description: SessionDescription?) {

        mCallback.onSendAnswer(userId, description)

    }

    override fun onRemoteStream(userId: String, stream: MediaStream?) {

        mCallback.onRemoteStream(userId)

    }

    override fun onRemoveStream(userId: String, stream: MediaStream?) {
        leaveRoom(userId)
    }

    override fun onDisconnected(userId: String) {

        Log.d(TAG, "onDisconnected mCallback != null")
        mCallback.onDisconnected(userId)

    }
}