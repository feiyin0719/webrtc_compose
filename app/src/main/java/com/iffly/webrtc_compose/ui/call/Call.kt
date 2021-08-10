package com.iffly.webrtc_compose.ui.call

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.iffly.compose.redux.storeViewModel
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.CallSessionCallback
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.reducer.call.CallViewAction
import com.iffly.webrtc_compose.reducer.call.CallViewSate

@Composable
fun CallScreen(outGoing: Boolean = false, userId: String = "") {
    val store = storeViewModel()
    var init by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(key1 = init) {
        if (init) {
            init = false
            store.dispatch(
                CallViewAction(
                    CallViewAction.CallViewActionValue.InitCall,
                    mapOf(
                        CallViewAction.OUTGOING_KEY to outGoing,
                        CallViewAction.USER_KEY to userId
                    )
                )
            )
        }
    }
    val callViewSate by
    store.getState(CallViewSate::class.java)
        .observeAsState(
            CallViewSate()
        )
    val close = callViewSate.closeState
    val outGoing = callViewSate.outGoingState
    if (close) {
        val activity = LocalContext.current
        LaunchedEffect(close) {
            if (close && activity is Activity)
                activity.finish()
        }
    } else {
        val callSessionCallback by rememberUpdatedState(object : CallSessionCallback {
            override fun didCallEndWithReason(var1: CallEndReason?) {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.EndCall,
                        mapOf(
                            CallViewAction.REASON_KEY to (var1 ?: CallEndReason.SignalError)
                        )
                    )
                )
            }

            override fun didChangeState(var1: CallState?) {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.ChangeState,
                        mapOf(
                            CallViewAction.STATE_KEY to (var1 ?: CallState.Incoming)
                        )
                    )
                )
            }

            override fun didChangeMode(isAudioOnly: Boolean) {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.ChangeMode,
                        mapOf(
                            CallViewAction.MODE_KEY to isAudioOnly
                        )
                    )
                )
            }

            override fun didCreateLocalVideoTrack() {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.CreateLocal,
                        mapOf(

                        )
                    )
                )
            }

            override fun didReceiveRemoteVideoTrack(userId: String?) {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.CreateRemote,
                        mapOf(
                            CallViewAction.USER_KEY to (userId ?: "")
                        )
                    )
                )
            }

            override fun didUserLeave(userId: String?) {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.UserLeave,
                        mapOf(
                            CallViewAction.USER_KEY to (userId ?: "")
                        )
                    )
                )
            }

            override fun didError(error: String?) {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Error,
                        mapOf(
                            CallViewAction.ERROR_KEY to (error ?: "")
                        )
                    )
                )
            }

            override fun didDisconnected(userId: String?) {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Disconnect,
                        mapOf(
                            CallViewAction.USER_KEY to (userId ?: "")
                        )
                    )
                )
            }

        })
        val surfaceView = callViewSate.remoteSurfaceView
        val callState = callViewSate.callState
        val localSurfaceView = callViewSate.localSurfaceView
        val initComplete = callViewSate.initCallComplete
        LaunchedEffect(initComplete) {
            if (initComplete) {
                val session = SkyEngineKit.Instance().currentSession
                session?.setSessionCallback(callSessionCallback)
            }
        }



        CallContent(
            callViewSate.userid,
            remoteSurfaceView = surfaceView,
            localSurfaceView = localSurfaceView,
            outGoing,
            callState = callState,
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Accept,
                        mapOf()
                    )
                )
            },
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Hang,
                        mapOf()
                    )
                )
            },
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.ChangeAudio,
                        mapOf()
                    )
                )
            },
            isAudioOnly = callViewSate.audioOnly,
        ) {
            store.dispatch(
                CallViewAction(
                    CallViewAction.CallViewActionValue.SwitchCamera,
                    mapOf()
                )
            )
        }
    }
}


