package com.iffly.webrtc_compose.reducer.call

import android.view.SurfaceView
import android.view.View
import com.iffly.compose.redux.Reducer
import com.iffly.rtcchat.CallSession
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.voip.VoipEvent
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import java.util.*


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
                return withContext(currentCoroutineContext()) {
                    val surfaceView: View? =
                        SkyEngineKit.Instance().currentSession?.setupRemoteVideo(userId, false)
                    if (surfaceView != null) {
                        return@withContext state.copy(remoteSurfaceView = surfaceView as SurfaceView)
                    } else {
                        return@withContext state.copy(closeState = true)
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
                    val session = SkyEngineKit.Instance().currentSession
                    if (session != null) {
                        SkyEngineKit.Instance().endCall()

                    }
                    return@withContext state.copy(closeState = true)
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
                return state.copy(closeState = true)
            }
            CallViewAction.CallViewActionValue.Disconnect -> {
                return state.copy(closeState = true)
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
                return state.copy(closeState = true)
            } else {
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
                    localSurfaceView = surfaceView
                )
            }
        } else {
            val userId = map[CallViewAction.USER_KEY] as String
            return withContext(currentCoroutineContext()) {
                SkyEngineKit.init(VoipEvent())
                val room = UUID.randomUUID().toString() + System.currentTimeMillis()
                val b: Boolean =
                    SkyEngineKit.Instance().startOutCall(App.instance!!, room, userId, false)
                if (!b) {
                    return@withContext state.copy(closeState = true)
                } else {

                    App.instance?.roomId = room
                    App.instance?.otherUserId = userId
                    val session: CallSession? = SkyEngineKit.Instance().currentSession
                    if (session == null) {
                        return@withContext state.copy(closeState = true)
                    } else {
                        return@withContext state.copy(
                            callState = CallState.Outgoing,
                            initCallComplete = true
                        )
                    }
                }
            }
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