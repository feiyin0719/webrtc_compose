package com.iffly.webrtc_compose.ui.call

import android.app.Activity
import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

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
    val callViewSate by store.getState(CallViewSate::class.java).observeAsState(
        CallViewSate(
            "",
            false,
            null,
            null,
            CallState.Incoming,
            false,
            false
        )
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

        val answerClick = remember {
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Accept,
                        mapOf()
                    )
                )
            }
        }
        val hangAnswerClick = remember {
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.Hang,
                        mapOf()
                    )
                )
            }
        }

        val audioAnswerClick = remember {
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.ChangeAudio,
                        mapOf()
                    )
                )

            }
        }

        val switchCameraClick = remember {
            {
                store.dispatch(
                    CallViewAction(
                        CallViewAction.CallViewActionValue.SwitchCamera,
                        mapOf()
                    )
                )

            }
        }

        CallContent(
            callViewSate.userid,
            remoteSurfaceView = surfaceView,
            localSurfaceView = localSurfaceView,
            outGoing,
            callState = callState,
            answerClick,
            hangAnswerClick,
            audioAnswerClick,
            isAudioOnly = callViewSate.audioOnly,
            switchCameraClick
        )
    }

}


@Composable
fun CallContent(
    userId: String,
    remoteSurfaceView: SurfaceView?,
    localSurfaceView: SurfaceView?,
    outGoing: Boolean = false,
    callState: CallState,
    videoAnswerClick: () -> Unit,
    hangAnswerClick: () -> Unit,
    audioAnswerClick: () -> Unit = {},
    isAudioOnly: Boolean = false,
    switchCameraClick: () -> Unit = {}
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
                modifier = if (callState == CallState.Connected && remoteSurfaceView != null) Modifier
                    .size(
                        100.dp,
                        180.dp
                    )
                    .statusBarsPadding()
                    .offset(-20.dp, 20.dp)
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

    if (isAudioOnly) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .navigationBarsPadding()
                .statusBarsPadding()
                .fillMaxSize()
                .background(WebrtcTheme.colors.uiBackground)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.wrapContentSize()
            ) {
                AppImage(
                    imageId = R.mipmap.av_default_header,
                    contentDescription = "hang_answer",
                    Modifier
                        .size(150.dp, 150.dp)
                )
                Text(
                    text = userId,
                    style = Typography.h3,
                    textAlign = TextAlign.Center
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

            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom

        ) {
            if (callState == CallState.Connected && !isAudioOnly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "切换摄像头",
                        color = Color.LightGray,
                        style = Typography.subtitle1,
                        modifier = Modifier.offset(0.dp, -5.dp)
                    )
                    AppImage(imageId = R.mipmap.av_camera, contentDescription = "camera",
                        Modifier
                            .size(75.dp, 75.dp)
                            .clickable {
                                switchCameraClick.invoke()
                            }
                    )
                }
            }

            AppImage(imageId = R.mipmap.av_hang_answer, contentDescription = "hang_answer",
                Modifier
                    .size(75.dp, 75.dp)
                    .clickable {
                        hangAnswerClick.invoke()
                    }
            )
            if (callState == CallState.Incoming && !outGoing)
                AppImage(
                    imageId = if (!isAudioOnly) R.mipmap.av_video_answer else R.mipmap.av_audio_trans,
                    contentDescription = "video_answer",
                    Modifier
                        .size(75.dp, 75.dp)
                        .clickable {
                            videoAnswerClick.invoke()
                        },
                    backgroundColor = Color.Green
                )
            if ((callState == CallState.Connected || callState == CallState.Incoming || callState == CallState.Outgoing) && !isAudioOnly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "切换到语音",
                        color = Color.LightGray,
                        style = Typography.subtitle1,
                        modifier = Modifier.offset(0.dp, -5.dp)
                    )
                    if (callState != CallState.Connected)
                        AppImage(
                            imageId = R.mipmap.av_audio_trans,
                            contentDescription = "audio_answer",
                            Modifier
                                .size(75.dp, 75.dp)
                                .clickable {
                                    audioAnswerClick.invoke()
                                },
                            backgroundColor = Color.Green
                        )
                    else {
                        AppImage(
                            imageId = R.mipmap.av_phone,
                            contentDescription = "audio_answer",
                            Modifier
                                .size(75.dp, 75.dp)
                                .clickable {
                                    audioAnswerClick.invoke()
                                },
                        )
                    }
                }


            }
        }

    }
}

