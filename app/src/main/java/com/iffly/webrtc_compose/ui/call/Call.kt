package com.iffly.webrtc_compose.ui.call

import android.app.Activity
import android.view.SurfaceView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.iffly.rtcchat.CallEndReason
import com.iffly.rtcchat.CallSessionCallback
import com.iffly.rtcchat.CallState
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.R
import com.iffly.webrtc_compose.ui.components.AndroidSurfaceView
import com.iffly.webrtc_compose.ui.components.AppImage
import com.iffly.webrtc_compose.viewmodel.call.callViewModel

@Composable
fun CallScreen(outGoing: Boolean = false, userId: String = "") {
    val callViewModel = callViewModel(outGoing = outGoing, userId = userId)
    val close by callViewModel.closeState.observeAsState(false)
    val outGoing by callViewModel.outGoingState.observeAsState(false)
    if (close) {
        val activity = LocalContext.current
        LaunchedEffect(key1 = close) {
            if (close && activity is Activity)
                activity.finish()
        }
    } else {
        val callSessionCallback by rememberUpdatedState(object : CallSessionCallback {
            override fun didCallEndWithReason(var1: CallEndReason?) {
                callViewModel.didCallEndWithReason(var1)
            }

            override fun didChangeState(var1: CallState?) {
                callViewModel.didChangeState(var1)
            }

            override fun didChangeMode(isAudioOnly: Boolean) {
                callViewModel.didChangeMode(isAudioOnly = isAudioOnly)
            }

            override fun didCreateLocalVideoTrack() {
                callViewModel.didCreateLocalVideoTrack()
            }

            override fun didReceiveRemoteVideoTrack(userId: String?) {
                callViewModel.didReceiveRemoteVideoTrack(userId = userId)
            }

            override fun didUserLeave(userId: String?) {
                callViewModel.didUserLeave(userId = userId)
            }

            override fun didError(error: String?) {
                callViewModel.didError(error)
            }

            override fun didDisconnected(userId: String?) {
                callViewModel.didDisconnected(userId)
            }

        })
        val surfaceView by callViewModel.remoteSurfaceState.observeAsState(null)
        val callState by callViewModel.callState.observeAsState(CallState.Incoming)
        val localSurfaceView by callViewModel.localSurfaceState.observeAsState(null)
        val initComplete by callViewModel.initCompleteState.observeAsState(false)
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
            { callViewModel.videoAnswerClick() },
            {
                callViewModel.hangAnswerClick()
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

