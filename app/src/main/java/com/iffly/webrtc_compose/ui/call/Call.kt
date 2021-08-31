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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.CallSessionCallback
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.viewmodel.call.CallViewModel
import com.iffly.webrtc_compose.viewmodel.call.CallViewModel.CallViewAction
import com.iffly.webrtc_compose.viewmodel.call.CallViewModel.CallViewAction.Companion.PERMISSION_KEY
import com.iffly.webrtc_compose.viewmodel.call.CallViewModel.CallViewSate

@Composable
fun CallScreen(outGoing: Boolean = false, userId: String = "") {
    val callViewModel: CallViewModel = viewModel()
    var init by remember {
        mutableStateOf(true)
    }
    val activity = LocalContext.current
    val requestPermission =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { map ->
            handlePermissionResult(callViewModel = callViewModel, map = map)
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
    callViewModel.viewState
        .observeAsState(
            CallViewSate()
        )
    LaunchedEffect(key1 = callViewSate.havePermission) {
        if (callViewSate.havePermission) {
            callViewModel.sendAction(
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
            if (close && activity is Activity) {
                SkyEngineKit.instance().endCall()
                activity.finish()
            }
        }
    } else {
        if (callViewSate.havePermission) {
            CallScreenWithPermission(callViewSate = callViewSate, callViewModel = callViewModel)
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
fun CallScreenWithPermission(callViewSate: CallViewSate, callViewModel: CallViewModel) {
    val callSessionCallback = remember {
        StoreCallSessionCallback(callViewModel = callViewModel)
    }

    val surfaceView = callViewSate.remoteSurfaceView
    val callState = callViewSate.callState
    val localSurfaceView = callViewSate.localSurfaceView
    val initComplete = callViewSate.initCallComplete
    LaunchedEffect(initComplete) {
        if (initComplete) {
            val session = SkyEngineKit.instance().currentSession
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
            callViewModel.sendAction(
                CallViewAction(
                    CallViewAction.CallViewActionValue.Accept,
                    mapOf()
                )
            )
        },
        {
            callViewModel.sendAction(
                CallViewAction(
                    CallViewAction.CallViewActionValue.Hang,
                    mapOf()
                )
            )
        },
        {
            callViewModel.sendAction(
                CallViewAction(
                    CallViewAction.CallViewActionValue.ChangeAudio,
                    mapOf()
                )
            )
        },
        isAudioOnly = callViewSate.audioOnly,
        {
            callViewModel.sendAction(
                CallViewAction(
                    CallViewAction.CallViewActionValue.SwitchCamera,
                    mapOf()
                )
            )
        },
        isMute = callViewSate.isMute,
        {
            callViewModel.sendAction(
                CallViewAction(
                    CallViewAction.CallViewActionValue.ToggleMute,
                    mapOf()
                )
            )
        },
        isSpeaker = callViewSate.isSpeaker,
        {
            callViewModel.sendAction(
                CallViewAction(
                    CallViewAction.CallViewActionValue.ToggleSpeaker,
                    mapOf()
                )
            )
        }
    )
}

private fun handlePermissionResult(callViewModel: CallViewModel, map: MutableMap<String, Boolean>) {
    var isAllGranted = true
    map.forEach { (t, u) ->
        if (!u) {
            isAllGranted = false
            return@forEach
        }
    }
    callViewModel.sendAction(
        CallViewAction(
            CallViewAction.CallViewActionValue.ChangePermission,
            mapOf(PERMISSION_KEY to isAllGranted)
        )
    )

}


private class StoreCallSessionCallback(val callViewModel: CallViewModel) : CallSessionCallback {

    override fun didCallEndWithReason(var1: CallEndReason) {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.EndCall,
                mapOf(
                    CallViewAction.REASON_KEY to (var1)
                )
            )
        )
    }

    override fun didChangeState(var1: CallState) {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.ChangeState,
                mapOf(
                    CallViewAction.STATE_KEY to (var1)
                )
            )
        )
    }

    override fun didChangeMode(isAudioOnly: Boolean) {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.ChangeMode,
                mapOf(
                    CallViewAction.MODE_KEY to isAudioOnly
                )
            )
        )
    }

    override fun didCreateLocalVideoTrack() {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.CreateLocal,
                mapOf(

                )
            )
        )
    }

    override fun didReceiveRemoteVideoTrack(userId: String) {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.CreateRemote,
                mapOf(
                    CallViewAction.USER_KEY to (userId)
                )
            )
        )
    }

    override fun didUserLeave(userId: String) {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.UserLeave,
                mapOf(
                    CallViewAction.USER_KEY to (userId)
                )
            )
        )
    }

    override fun didError(error: String) {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.Error,
                mapOf(
                    CallViewAction.ERROR_KEY to (error)
                )
            )
        )
    }

    override fun didDisconnected(userId: String) {
        callViewModel.sendAction(
            CallViewAction(
                CallViewAction.CallViewActionValue.Disconnect,
                mapOf(
                    CallViewAction.USER_KEY to (userId)
                )
            )
        )
    }
}


