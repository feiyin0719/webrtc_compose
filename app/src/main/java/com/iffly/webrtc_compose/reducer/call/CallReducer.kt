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
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
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
    val havePermission: Boolean = false
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
        ChangePermission
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
    override suspend fun reduce(state: CallViewSate, action: CallViewAction): CallViewSate {
        when (action.action) {
            CallViewAction.CallViewActionValue.InitCall -> {
                return initCall(state = state, action.map)
            }
            CallViewAction.CallViewActionValue.CreateRemote -> {
                val userId = action.map[CallViewAction.USER_KEY] as String
                return withContext(currentCoroutineContext()) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupRemoteVideo(userId, false)
                    if (surfaceView != null) {
                        return@withContext state.copy(
                            remoteSurfaceView = surfaceView as SurfaceView,
                            userid = userId
                        )
                    } else {
                        return@withContext state.copyCloseState()
                    }
                }
            }
            CallViewAction.CallViewActionValue.CreateLocal -> {
                return withContext(currentCoroutineContext()) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupLocalVideo(true)

                    if (surfaceView != null && surfaceView is SurfaceView) {
                        surfaceView.setZOrderMediaOverlay(true)
                        return@withContext state.copy(localSurfaceView = surfaceView)
                    }
                    return@withContext state.copy()

                }
            }
            CallViewAction.CallViewActionValue.Accept -> {
                return withContext(currentCoroutineContext()) {
                    val session = SkyEngineKit.Instance().currentSession
                    if (session != null && session.state == CallState.Incoming) {
                        session.joinHome(session.roomId)
                    }
                    return@withContext state.copy()
                }
            }
            CallViewAction.CallViewActionValue.Hang -> {
                return withContext(currentCoroutineContext()) {
                    SkyEngineKit.Instance().endCall()
                    return@withContext state.copyCloseState()
                }
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
                return state.copyCloseState()
            }
            CallViewAction.CallViewActionValue.Disconnect -> {
                return state.copyCloseState()
            }
            CallViewAction.CallViewActionValue.ChangeAudio -> {
                SkyEngineKit.Instance().currentSession?.switchToAudio()
                return state.copy()
            }
            CallViewAction.CallViewActionValue.ChangeMode -> {
                return state.copy(
                    audioOnly = true,
                    localSurfaceView = null,
                    remoteSurfaceView = null
                )
            }
            CallViewAction.CallViewActionValue.SwitchCamera -> {
                SkyEngineKit.Instance().currentSession?.switchCamera()
                return state.copy()
            }
            CallViewAction.CallViewActionValue.ChangePermission -> {
                val havePermission = action.map[PERMISSION_KEY] as Boolean
                if (havePermission)
                    return state.copy(havePermission = true)
                else {
                    SkyEngineKit.Instance().sendRefuseOnPermissionDenied(
                        App.instance!!.roomId,
                        App.instance!!.otherUserId
                    )
                    return state.copy(havePermission = false, closeState = true)
                }
            }
            else -> state.copy()
        }
        return state.copy()
    }

    private suspend fun initCall(state: CallViewSate, map: Map<String, Any>): CallViewSate {
        val session = SkyEngineKit.Instance().currentSession
        val outGoing: Boolean = map[CallViewAction.OUTGOING_KEY] as Boolean
        if (!outGoing) {
            if (session == null) {
                return state.copyCloseState()
            } else {
                val b = SkyEngineKit.Instance().startInCall(
                    App.instance!!,
                    App.instance!!.roomId, App.instance!!.otherUserId, state.audioOnly
                )

                if (b) {
                    val surfaceView = withContext(currentCoroutineContext()) {
                        val surfaceView: View? =
                            SkyEngineKit.Instance().currentSession?.setupLocalVideo(false)

                        if (surfaceView != null && surfaceView is SurfaceView) {
                            surfaceView.setZOrderMediaOverlay(true)

                        }
                        return@withContext surfaceView as SurfaceView?
                    }
                    return state.copy(
                        callState = CallState.Incoming,
                        initCallComplete = true,
                        localSurfaceView = surfaceView,
                        userid = App.instance?.otherUserId ?: ""
                    )
                } else
                    return state.copyCloseState()
            }
        } else {
            val userId = map[CallViewAction.USER_KEY] as String
            return withContext(currentCoroutineContext()) {
                SkyEngineKit.init(VoipEvent())
                val room = UUID.randomUUID().toString() + System.currentTimeMillis()
                val b: Boolean =
                    SkyEngineKit.Instance().startOutCall(App.instance!!, room, userId, false)
                if (!b) {
                    return@withContext state.copyCloseState()
                } else {

                    App.instance?.roomId = room
                    App.instance?.otherUserId = userId
                    val session: CallSession? = SkyEngineKit.Instance().currentSession
                    if (session == null) {
                        return@withContext state.copyCloseState()
                    } else {
                        return@withContext state.copy(
                            callState = CallState.Outgoing,
                            initCallComplete = true,
                            userid = userId
                        )
                    }
                }
            }
        }
    }

}