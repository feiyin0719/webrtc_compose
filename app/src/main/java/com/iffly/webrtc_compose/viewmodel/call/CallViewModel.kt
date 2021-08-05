package com.iffly.webrtc_compose.viewmodel.call

import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.CallSessionCallback
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit


class CallViewModel : ViewModel(), CallSessionCallback {
    val handler = Handler(Looper.getMainLooper())
    val closeState = MutableLiveData(false)
    val remoteSurfaceState: MutableLiveData<SurfaceView?> = MutableLiveData(null)
    val localSurfaceState: MutableLiveData<SurfaceView?> = MutableLiveData(null)
    val callState = MutableLiveData<CallState>()

    init {

        val session = SkyEngineKit.Instance().currentSession
        if (session == null) {
            closeState.postValue(true)
        } else {
            session.setSessionCallback(this)
            callState.postValue(CallState.Incoming)
            handler.post {
                val surfaceView: View? =
                    SkyEngineKit.Instance().currentSession?.setupLocalVideo(false)

                if (surfaceView != null && surfaceView is SurfaceView) {
                    surfaceView.setZOrderMediaOverlay(true)
                    localSurfaceState.postValue(surfaceView)
                }
            }
        }
    }

    override fun didCallEndWithReason(var1: CallEndReason?) {
        closeState.postValue(true)
    }

    override fun didChangeState(var1: CallState?) {
        var1?.let {
            callState.postValue(var1)
        }

    }

    override fun didChangeMode(isAudioOnly: Boolean) {

    }

    override fun didCreateLocalVideoTrack() {
        handler.post {
            val surfaceView: View? = SkyEngineKit.Instance().currentSession?.setupLocalVideo(true)

            if (surfaceView != null && surfaceView is SurfaceView) {
                surfaceView.setZOrderMediaOverlay(true)
                localSurfaceState.postValue(surfaceView)
            }

        }
    }

    override fun didReceiveRemoteVideoTrack(userId: String?) {
        handler.post {
            val surfaceView: View? =
                SkyEngineKit.Instance().currentSession?.setupRemoteVideo(userId, false)
            if (surfaceView != null) {
                remoteSurfaceState.postValue(surfaceView as SurfaceView)
            } else {
                closeState.postValue(true)
            }
        }

    }

    override fun didUserLeave(userId: String?) {

    }

    override fun didError(error: String?) {

    }

    override fun didDisconnected(userId: String?) {
        SkyEngineKit.Instance().endCall()
    }

    fun videoAnswerClick() {
        handler.post {
            val session = SkyEngineKit.Instance().currentSession
            if (session != null && session.state == CallState.Incoming) {
                session.joinHome(session.roomId)
            }
        }

    }

    fun hangAnswerClick() {
        handler.post {
            val session = SkyEngineKit.Instance().currentSession
            if (session != null) {
                SkyEngineKit.Instance().endCall()
                closeState.postValue(true)
            }
        }
    }
}