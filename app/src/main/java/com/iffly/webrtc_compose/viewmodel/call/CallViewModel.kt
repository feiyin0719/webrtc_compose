package com.iffly.webrtc_compose.viewmodel.call

import android.view.SurfaceView
import android.view.View
import androidx.compose.runtime.Composable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iffly.rtcchat.*
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.voip.VoipEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class CallViewModel(var outGoing: Boolean = false, val userId: String = "") : ViewModel(),
    CallSessionCallback {
    val closeState = MutableLiveData(false)
    val remoteSurfaceState: MutableLiveData<SurfaceView?> = MutableLiveData(null)
    val localSurfaceState: MutableLiveData<SurfaceView?> = MutableLiveData(null)
    val callState = MutableLiveData<CallState>()
    val outGoingState = MutableLiveData(outGoing)

    init {

        val session = SkyEngineKit.Instance().currentSession
        if (!outGoing) {
            if (session == null) {
                closeState.postValue(true)
            } else {
                session.setSessionCallback(this)
                callState.postValue(CallState.Incoming)
                GlobalScope.launch(Dispatchers.Main) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupLocalVideo(false)

                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)
                        localSurfaceState.postValue(surfaceView)
                    }
                }
            }
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                SkyEngineKit.init(VoipEvent())
                val room = UUID.randomUUID().toString() + System.currentTimeMillis()
                val b: Boolean =
                    SkyEngineKit.Instance().startOutCall(App.instance!!, room, userId, false)
                if (!b) {
                    closeState.postValue(true)
                } else {
                    App.instance?.roomId = room
                    App.instance?.otherUserId = userId
                    val session: CallSession? = SkyEngineKit.Instance().currentSession
                    if (session == null) {
                        closeState.postValue(true)
                    } else {
                        session.setSessionCallback(this@CallViewModel)
                    }
                }
            }
        }
    }

    override fun didCallEndWithReason(var1: CallEndReason?) {
        App.instance?.otherUserId = ""
        App.instance?.roomId = ""
        closeState.postValue(true)
    }

    override fun didChangeState(var1: CallState?) {
        var1?.let {
            if (it == CallState.Connected)
                outGoing = false
            callState.postValue(var1)
        }

    }

    override fun didChangeMode(isAudioOnly: Boolean) {

    }

    override fun didCreateLocalVideoTrack() {
        GlobalScope.launch(Dispatchers.Main) {
            val surfaceView: View? = SkyEngineKit.Instance().currentSession?.setupLocalVideo(true)

            if (surfaceView != null && surfaceView is SurfaceView) {
                surfaceView.setZOrderMediaOverlay(true)
                localSurfaceState.postValue(surfaceView)
            }

        }
    }

    override fun didReceiveRemoteVideoTrack(userId: String?) {
        GlobalScope.launch(Dispatchers.Main) {
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
        GlobalScope.launch(Dispatchers.Main) {
            val session = SkyEngineKit.Instance().currentSession
            if (session != null && session.state == CallState.Incoming) {
                session.joinHome(session.roomId)
            }
        }

    }

    fun hangAnswerClick() {
        GlobalScope.launch(Dispatchers.Main) {
            val session = SkyEngineKit.Instance().currentSession
            if (session != null) {
                SkyEngineKit.Instance().endCall()
                closeState.postValue(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        SkyEngineKit.Instance().currentSession?.setSessionCallback(null)
    }
}

class CallViewModelFactory(val outGoing: Boolean, val userId: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (CallViewModel::class.java.isAssignableFrom(modelClass)) {
            return CallViewModel(outGoing = outGoing, userId = userId) as T
        }
        throw RuntimeException("unknown class:" + modelClass.name)
    }
}

@Composable
fun callViewModel(outGoing: Boolean = false, userId: String = ""): CallViewModel {
    return viewModel(
        factory =
        CallViewModelFactory(outGoing = outGoing, userId = userId)
    )
}