package com.iffly.webrtc_compose.viewmodel.call

import android.view.SurfaceView
import android.view.View
import com.iffly.rtcchat.CallSession
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.viewmodel.BaseMVIViewModel
import com.iffly.webrtc_compose.voip.VoipEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor() :
    BaseMVIViewModel<CallViewModel.CallViewSate, CallViewModel.CallViewAction>(CallViewSate::class.java) {
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

    override suspend fun reduce(action: CallViewAction, state: CallViewSate): CallViewSate {
        return when (action.action) {
            CallViewAction.CallViewActionValue.InitCall -> {
                initCall(state = state, action.map)
            }
            CallViewAction.CallViewActionValue.CreateRemote -> {
                val userId = action.map[CallViewAction.USER_KEY] as String

                val surfaceView: View? =
                    SkyEngineKit.instance().currentSession?.setupRemoteVideo(userId, false)
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
                if (state.localSurfaceView == null) {
                    val surfaceView: View? =
                        SkyEngineKit.instance().currentSession?.setupLocalVideo(true)
                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)
                        state.copy(localSurfaceView = surfaceView)
                    } else
                        state.copy()
                } else {
                    state.copy()
                }
            }
            CallViewAction.CallViewActionValue.Accept -> {
                val session = SkyEngineKit.instance().currentSession
                if (session != null && session.state == CallState.Incoming) {
                    session.joinHome(session.roomId)
                }
                state.copy()
            }
            CallViewAction.CallViewActionValue.Hang -> {
                SkyEngineKit.instance().endCall()
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
                SkyEngineKit.instance().currentSession?.switchToAudio()
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
                SkyEngineKit.instance().currentSession?.switchCamera()
                state.copy()
            }
            CallViewAction.CallViewActionValue.ChangePermission -> {
                val havePermission = action.map[CallViewAction.PERMISSION_KEY] as Boolean
                if (havePermission)
                    state.copy(havePermission = true)
                else {
                    SkyEngineKit.instance().sendRefuseOnPermissionDenied(
                        App.instance!!.roomId,
                        App.instance!!.otherUserId
                    )
                    state.copy(havePermission = false, closeState = true)
                }
            }
            CallViewAction.CallViewActionValue.ToggleMute -> {
                var isMute = state.isMute
                if (SkyEngineKit.instance().currentSession?.toggleMuteAudio(!isMute) == true) {
                    isMute = !isMute
                }
                state.copy(isMute = isMute)
            }
            CallViewAction.CallViewActionValue.ToggleSpeaker -> {
                var isSpeaker = state.isSpeaker
                if (SkyEngineKit.instance().currentSession?.toggleSpeaker(!isSpeaker) == true) {
                    isSpeaker = !isSpeaker
                }
                state.copy(isSpeaker = isSpeaker)
            }
            else -> state.copy()
        }
    }


    private fun initCall(state: CallViewSate, map: Map<String, Any>): CallViewSate {

        val outGoing: Boolean = map[CallViewAction.OUTGOING_KEY] as Boolean
        SkyEngineKit.init(VoipEvent)
        if (!outGoing) {
            val b = SkyEngineKit.instance().startInCall(
                App.instance!!,
                App.instance!!.roomId, App.instance!!.otherUserId, state.audioOnly
            )

            if (!b) {
                return state.copyCloseState()
            } else {
                val session = SkyEngineKit.instance().currentSession
                if (session != null) {
                    val surfaceView: View? =
                        SkyEngineKit.instance().currentSession?.setupLocalVideo(false)

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

            val room = UUID.randomUUID().toString() + System.currentTimeMillis()
            val b: Boolean =
                SkyEngineKit.instance().startOutCall(App.instance!!, room, userId, false)
            if (!b) {
                return state.copyCloseState()
            } else {

                App.instance?.roomId = room
                App.instance?.otherUserId = userId
                val session: CallSession? = SkyEngineKit.instance().currentSession
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