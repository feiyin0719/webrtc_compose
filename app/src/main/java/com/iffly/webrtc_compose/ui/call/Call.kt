package com.iffly.webrtc_compose.ui.call

import android.app.Activity
import android.view.SurfaceView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.iffly.compose.redux.storeViewModel
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.CallSessionCallback
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.R
import com.iffly.webrtc_compose.reducer.call.CallViewAction
import com.iffly.webrtc_compose.reducer.call.CallViewSate
import com.iffly.webrtc_compose.ui.components.AndroidSurfaceView
import com.iffly.webrtc_compose.ui.components.AppImage

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
    val callViewSate by store.getState(CallViewSate::class.java).observeAsState()
    val close = callViewSate!!.closeState
    val outGoing = callViewSate!!.outGoingState
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
        val surfaceView = callViewSate!!.remoteSurfaceView
        val callState = callViewSate!!.callState
        val localSurfaceView = callViewSate!!.localSurfaceView
        val initComplete = callViewSate!!.initCallComplete
        LaunchedEffect(initComplete) {
            if (initComplete) {
                val session = SkyEngineKit.Instance().currentSession
                session?.setSessionCallback(callSessionCallback)
            }
        }

        CallContent(
            remoteSurfaceView = surfaceView,
            localSurfaceView = localSurfaceView,
            outGoing,
            callState = callState,
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Accept,
                        mapOf(

                        )
                    )
                )
            },
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Hang,
                        mapOf(

                        )
                    )
                )
            }
        )
    }

}


@Composable
fun CallContent(
    remoteSurfaceView: SurfaceView?,
    localSurfaceView: SurfaceView?,
    outGoing: Boolean = false,
    callState: CallState,
    videoAnswerClick: () -> Unit,
    hangAnswerCLick: () -> Unit
) {
    if (callState == CallState.Connected) {
        remoteSurfaceView?.let {
            AndroidSurfaceView(
                surfaceView = it, modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            )
        }
    }

    localSurfaceView?.let {
        Box(
            contentAlignment = if (callState == CallState.Connected) Alignment.TopEnd else Alignment.Center,
            modifier = Modifier
                .fillMaxSize()

        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = if (callState == CallState.Connected) Modifier
                    .size(
                        100.dp,
                        180.dp
                    )
                    .statusBarsPadding()
                    .offset(0.dp, 20.dp)
                else
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
            ) {
                AndroidSurfaceView(
                    surfaceView = it,
                    isOverlay = callState == CallState.Connected,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }


    Column(
        verticalArrangement = Arrangement.Bottom, modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .offset(
                0.dp, -20.dp
            )
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()

        ) {
            AppImage(imageId = R.mipmap.av_hang_answer, contentDescription = "hang_answer",
                Modifier
                    .size(75.dp, 75.dp)
                    .clickable {
                        hangAnswerCLick.invoke()
                    }
            )
            if (callState == CallState.Incoming && !outGoing)
                AppImage(imageId = R.mipmap.av_video_answer,
                    contentDescription = "video_answer",
                    Modifier
                        .size(75.dp, 75.dp)
                        .clickable {
                            videoAnswerClick.invoke()
                        }
                )
        }

    }
}

