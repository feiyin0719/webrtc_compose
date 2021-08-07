package com.iffly.webrtc_compose.reducer.call

import android.view.SurfaceView
import android.view.View
import androidx.compose.runtime.Composable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iffly.compose.redux.Reducer
import com.iffly.rtcchat.*
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.voip.VoipEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*


class CallViewModel(var outGoing: Boolean = false, val userId: String = "") : ViewModel(),
    CallSessionCallback {
    val closeState = MutableLiveData(false)
    val remoteSurfaceState: MutableLiveData<SurfaceView?> = MutableLiveData(null)
    val localSurfaceState: MutableLiveData<SurfaceView?> = MutableLiveData(null)
    val callState = MutableLiveData<CallState>()
    val outGoingState = MutableLiveData(outGoing)
    val initCompleteState = MutableLiveData(false)


    init {
        val session = SkyEngineKit.Instance().currentSession
        if (!outGoing) {
            if (session == null) {
                closeState.postValue(true)
            } else {
                callState.postValue(CallState.Incoming)
                initCompleteState.postValue(true)
                viewModelScope.launch(Dispatchers.Main) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupLocalVideo(false)

                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)
                        localSurfaceState.postValue(surfaceView)
                    }
                }
            }
        } else {
            callState.postValue(CallState.Incoming)
            viewModelScope.launch(Dispatchers.Main) {
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
                        initCompleteState.postValue(true)
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

data class CallViewSate(
    val closeState: Boolean,
    val localSurfaceView: SurfaceView?,
    val remoteSurfaceView: SurfaceView?,
    val callState: CallState,
    val outGoingState: Boolean,
    val initCallComplete: Boolean
) {

}

data class CallViewAction(
    val action: CallViewActionValue,
    val map: Map<String, out Any>
) {
    enum class CallViewActionValue {
        InitCall,
        EndCall,
        ChangeState,
        ChangeMode,
        CreateLocal,
        CreateRemote,
        UserLeave,
        Error,
        Disconnect,
        Accept,
        Hang
    }

    companion object {
        const val USER_KEY = "userId"
        const val OUTGOING_KEY = "outGoing"
        const val MODE_KEY = "mode"
        const val STATE_KEY = "state"
        const val REASON_KEY = "reason"
        const val ERROR_KEY = "error"
    }
}

class CallReducer :
    Reducer<CallViewSate, CallViewAction>(CallViewSate::class.java, CallViewAction::class.java) {
    override suspend fun reduce(state: CallViewSate, action: CallViewAction): CallViewSate {
        when (action.action) {
            CallViewAction.CallViewActionValue.InitCall -> {
                return initCall(state = state, action.map)
            }
            CallViewAction.CallViewActionValue.CreateRemote -> {
                val userId = action.map[CallViewAction.USER_KEY] as String
                return GlobalScope.async(Dispatchers.Main) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupRemoteVideo(userId, false)
                    if (surfaceView != null) {
                        return@async state.copy(remoteSurfaceView = surfaceView as SurfaceView)
                    } else {
                        return@async state.copy(closeState = true)
                    }
                }.await()
            }
            CallViewAction.CallViewActionValue.CreateLocal -> {
                return GlobalScope.async(Dispatchers.Main) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupLocalVideo(true)

                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)
                        return@async state.copy(localSurfaceView = surfaceView)
                    }
                    return@async state.copy()

                }.await()
            }
            CallViewAction.CallViewActionValue.Accept -> {
                return GlobalScope.async(Dispatchers.Main) {
                    val session = SkyEngineKit.Instance().currentSession
                    if (session != null && session.state == CallState.Incoming) {
                        session.joinHome(session.roomId)
                    }
                    return@async state.copy()
                }.await()
            }
            CallViewAction.CallViewActionValue.Hang -> {
                return GlobalScope.async(Dispatchers.Main) {
                    val session = SkyEngineKit.Instance().currentSession
                    if (session != null) {
                        SkyEngineKit.Instance().endCall()

                    }
                    return@async state.copy(closeState = true)
                }.await()
            }
            CallViewAction.CallViewActionValue.ChangeState -> {
                val callState = action.map[CallViewAction.STATE_KEY] as CallState
                return state.copy(
                    callState = callState,
                    outGoingState = if (callState == CallState.Connected) false else state.outGoingState
                )
            }
            CallViewAction.CallViewActionValue.EndCall -> {
                App.instance?.otherUserId = ""
                App.instance?.roomId = ""
                return state.copy(closeState = true)
            }
            CallViewAction.CallViewActionValue.Disconnect->{
                return state.copy(closeState = true)
            }

        }
        return state.copy()
    }

    private suspend fun initCall(state: CallViewSate, map: Map<String, Any>): CallViewSate {
        val session = SkyEngineKit.Instance().currentSession
        val outGoing: Boolean = map[CallViewAction.OUTGOING_KEY] as Boolean
        if (!outGoing) {
            if (session == null) {
                return state.copy(closeState = true)
            } else {
                val surfaceView = GlobalScope.async(Dispatchers.Main) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupLocalVideo(false)

                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)

                    }
                    return@async surfaceView as SurfaceView?
                }.await()
                return state.copy(
                    callState = CallState.Incoming,
                    initCallComplete = true,
                    localSurfaceView = surfaceView
                )
            }
        } else {
            val userId = map[CallViewAction.USER_KEY] as String
            return GlobalScope.async(Dispatchers.Main) {
                SkyEngineKit.init(VoipEvent())
                val room = UUID.randomUUID().toString() + System.currentTimeMillis()
                val b: Boolean =
                    SkyEngineKit.Instance().startOutCall(App.instance!!, room, userId, false)
                if (!b) {
                    return@async state.copy(closeState = true)
                } else {

                    App.instance?.roomId = room
                    App.instance?.otherUserId = userId
                    val session: CallSession? = SkyEngineKit.Instance().currentSession
                    if (session == null) {
                        return@async state.copy(closeState = true)
                    } else {
                        return@async state.copy(
                            callState = CallState.Outgoing,
                            initCallComplete = true
                        )
                    }
                }
            }.await()
        }
    }

    override fun initState(): CallViewSate {
        return CallViewSate(
            false,
            null,
            null,
            CallState.Incoming,
            false,
            false
        )
    }

}