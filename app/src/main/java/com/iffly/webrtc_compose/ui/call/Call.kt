package com.iffly.webrtc_compose.ui.call

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.iffly.compose.redux.StoreViewModel
import com.iffly.compose.redux.storeViewModel
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.CallSessionCallback
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.reducer.call.CallViewAction
import com.iffly.webrtc_compose.reducer.call.CallViewAction.Companion.PERMISSION_KEY
import com.iffly.webrtc_compose.reducer.call.CallViewSate
import com.iffly.webrtc_compose.ui.theme.Typography

@Composable
fun CallScreen(outGoing: Boolean = false, userId: String = "") {
    val store = storeViewModel()
    var init by remember {
        mutableStateOf(true)
    }
    val activity = LocalContext.current
    val requestPermission =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { map ->
            handlePermissionResult(store = store, map = map)
        }
    LaunchedEffect(key1 = init) {
        if (init) {
            init = false
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }
    val callViewSate by
    store.getState(CallViewSate::class.java)
        .observeAsState(
            CallViewSate()
        )
    LaunchedEffect(key1 = callViewSate.havePermission) {
        if (callViewSate.havePermission) {
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
    val close = callViewSate.closeState
    if (close) {
        LaunchedEffect(close) {
            if (close && activity is Activity)
                activity.finish()
        }
    } else {
        if (callViewSate.havePermission) {
            CallScreenWithPermission(callViewSate = callViewSate, store = store)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "You need request permission",
                    style = Typography.body1
                )
            }
        }
    }
}

@Composable
fun CallScreenWithPermission(callViewSate: CallViewSate, store: StoreViewModel) {
    val callSessionCallback by rememberUpdatedState(StoreCallSessionCallback(store = store))
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
        callViewSate.outGoingState,
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
        {
            store.dispatch(
                CallViewAction(
                    CallViewAction.CallViewActionValue.SwitchCamera,
                    mapOf()
                )
            )
        },
        isMute = callViewSate.isMute,
        {
            store.dispatch(
                CallViewAction(
                    CallViewAction.CallViewActionValue.ToggleMute,
                    mapOf()
                )
            )
        },
        isSpeaker = callViewSate.isSpeaker,
        {
            store.dispatch(
                CallViewAction(
                    CallViewAction.CallViewActionValue.ToggleSpeaker,
                    mapOf()
                )
            )
        }
    )
}

private fun handlePermissionResult(store: StoreViewModel, map: MutableMap<String, Boolean>) {
    var isAllGranted = true
    map.forEach { (t, u) ->
        if (!u) {
            isAllGranted = false
            return@forEach
        }
    }
    store.dispatch(
        CallViewAction(
            CallViewAction.CallViewActionValue.ChangePermission,
            mapOf(PERMISSION_KEY to isAllGranted)
        )
    )

}


private class StoreCallSessionCallback(val store: StoreViewModel) : CallSessionCallback {
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
}


