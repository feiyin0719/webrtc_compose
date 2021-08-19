package com.iffly.webrtc_compose.reducer.call

import android.view.SurfaceView
import android.view.View
import com.iffly.compose.redux.Reducer
import com.iffly.rtcchat.CallSession
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.reducer.call.CallViewAction.Companion.PERMISSION_KEY
import com.iffly.webrtc_compose.voip.VoipEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.*


data class CallViewSate(
    val userid: String = "",
    val closeState: Boolean = false,
    val localSurfaceView: SurfaceView? = null,
    val remoteSurfaceView: SurfaceView? = null,
    val callState: CallState = CallState.Incoming,
    val outGoingState: Boolean = false,
    val initCallComplete: Boolean = false,
    val audioOnly: Boolean = false,
    val havePermission: Boolean = false,
    val isMute: Boolean = false,
    val isSpeaker: Boolean = false
) {

    fun copyCloseState(): CallViewSate {
        return this.copy(closeState = true, localSurfaceView = null, remoteSurfaceView = null)
    }

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
        Hang,
        ChangeAudio,
        SwitchCamera,
        ChangePermission,
        ToggleMute,
        ToggleSpeaker
    }

    companion object {
        const val USER_KEY = "userId"
        const val OUTGOING_KEY = "outGoing"
        const val MODE_KEY = "mode"
        const val STATE_KEY = "state"
        const val REASON_KEY = "reason"
        const val ERROR_KEY = "error"
        const val PERMISSION_KEY = "havePermission"
    }
}

class CallReducer :
    Reducer<CallViewSate, CallViewAction>(CallViewSate::class.java, CallViewAction::class.java) {
    override fun reduce(state: CallViewSate, flow: Flow<CallViewAction>): Flow<CallViewSate> {
        return flow.map { action ->
            return@map when (action.action) {
                CallViewAction.CallViewActionValue.InitCall -> {
                    initCall(state = state, action.map)
                }
                CallViewAction.CallViewActionValue.CreateRemote -> {
                    val userId = action.map[CallViewAction.USER_KEY] as String

                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupRemoteVideo(userId, false)
                    if (surfaceView != null) {
                        state.copy(
                            remoteSurfaceView = surfaceView as SurfaceView,
                            userid = userId
                        )
                    } else {
                        state.copyCloseState()
                    }

                }
                CallViewAction.CallViewActionValue.CreateLocal -> {

                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupLocalVideo(true)
                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)
                        state.copy(localSurfaceView = surfaceView)
                    } else {
                        state.copy()
                    }
                }
                CallViewAction.CallViewActionValue.Accept -> {
                    val session = SkyEngineKit.Instance().currentSession
                    if (session != null && session.state == CallState.Incoming) {
                        session.joinHome(session.roomId)
                    }
                    state.copy()
                }
                CallViewAction.CallViewActionValue.Hang -> {
                    SkyEngineKit.Instance().endCall()
                    state.copyCloseState()
                }
                CallViewAction.CallViewActionValue.ChangeState -> {
                    val callState = action.map[CallViewAction.STATE_KEY] as CallState
                    state.copy(
                        callState = callState,
                        outGoingState = if (callState == CallState.Connected) false else state.outGoingState
                    )
                }
                CallViewAction.CallViewActionValue.EndCall -> {
                    App.instance?.otherUserId = ""
                    App.instance?.roomId = ""
                    state.copyCloseState()
                }
                CallViewAction.CallViewActionValue.Disconnect -> {
                    state.copyCloseState()
                }
                CallViewAction.CallViewActionValue.ChangeAudio -> {
                    SkyEngineKit.Instance().currentSession?.switchToAudio()
                    state.copy()
                }
                CallViewAction.CallViewActionValue.ChangeMode -> {
                    state.copy(
                        audioOnly = true,
                        localSurfaceView = null,
                        remoteSurfaceView = null
                    )
                }
                CallViewAction.CallViewActionValue.SwitchCamera -> {
                    SkyEngineKit.Instance().currentSession?.switchCamera()
                    state.copy()
                }
                CallViewAction.CallViewActionValue.ChangePermission -> {
                    val havePermission = action.map[PERMISSION_KEY] as Boolean
                    if (havePermission)
                        state.copy(havePermission = true)
                    else {
                        SkyEngineKit.Instance().sendRefuseOnPermissionDenied(
                            App.instance!!.roomId,
                            App.instance!!.otherUserId
                        )
                        state.copy(havePermission = false, closeState = true)
                    }
                }
                CallViewAction.CallViewActionValue.ToggleMute -> {
                    var isMute = state.isMute
                    if (SkyEngineKit.Instance().currentSession?.toggleMuteAudio(!isMute) == true) {
                        isMute = !isMute
                    }
                    state.copy(isMute = isMute)
                }
                CallViewAction.CallViewActionValue.ToggleSpeaker -> {
                    var isSpeaker = state.isSpeaker
                    if (SkyEngineKit.Instance().currentSession?.toggleSpeaker(!isSpeaker) == true) {
                        isSpeaker = !isSpeaker
                    }
                    state.copy(isSpeaker = isSpeaker)
                }
                else -> state.copy()
            }
        }.flowOn(Dispatchers.Main)


    }

    private fun initCall(state: CallViewSate, map: Map<String, Any>): CallViewSate {

        val outGoing: Boolean = map[CallViewAction.OUTGOING_KEY] as Boolean
        if (!outGoing) {
            val b = SkyEngineKit.Instance().startInCall(
                App.instance!!,
                App.instance!!.roomId, App.instance!!.otherUserId, state.audioOnly
            )

            if (!b) {
                return state.copyCloseState()
            } else {
                val session = SkyEngineKit.Instance().currentSession
                if (session != null) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupLocalVideo(false)

                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)

                    }

                    return state.copy(
                        callState = CallState.Incoming,
                        initCallComplete = true,
                        localSurfaceView = surfaceView as SurfaceView?,
                        userid = App.instance?.otherUserId ?: ""
                    )
                } else
                    return state.copyCloseState()
            }
        } else {
            val userId = map[CallViewAction.USER_KEY] as String

            SkyEngineKit.init(VoipEvent())
            val room = UUID.randomUUID().toString() + System.currentTimeMillis()
            val b: Boolean =
                SkyEngineKit.Instance().startOutCall(App.instance!!, room, userId, false)
            if (!b) {
                return state.copyCloseState()
            } else {

                App.instance?.roomId = room
                App.instance?.otherUserId = userId
                val session: CallSession? = SkyEngineKit.Instance().currentSession
                return if (session == null) {
                    state.copyCloseState()
                } else {
                    state.copy(
                        callState = CallState.Outgoing,
                        initCallComplete = true,
                        userid = userId
                    )
                }
            }

        }
    }

}