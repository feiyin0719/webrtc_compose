package com.iffly.webrtc_compose.ui.call

import android.view.SurfaceView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.navigationBarsPadding
import com.iffly.rtcchat.CallState
import com.iffly.webrtc_compose.R
import com.iffly.webrtc_compose.ui.components.AndroidSurfaceView
import com.iffly.webrtc_compose.ui.components.AppImage
import com.iffly.webrtc_compose.viewmodel.call.CallViewModel

@Composable
fun CallScreen(callViewModel: CallViewModel = viewModel()) {
    val surfaceView by callViewModel.remoteSurfaceState.observeAsState(null)
    val callState by callViewModel.callState.observeAsState(CallState.Incoming)
    val localSurfaceView by callViewModel.localSurfaceState.observeAsState(null)
    CallContent(
        remoteSurfaceView = surfaceView,
        localSurfaceView = localSurfaceView,
        callState = callState,
        { callViewModel.videoAnswerClick() },
        {
            callViewModel.hangAnswerClick()
        }
    )

}


@Composable
fun CallContent(
    remoteSurfaceView: SurfaceView?,
    localSurfaceView: SurfaceView?,
    callState: CallState,
    videoAnswerClick: () -> Unit,
    hangAnswerCLick: () -> Unit
) {
    if (callState == CallState.Connected) {
        remoteSurfaceView?.let {
            AndroidSurfaceView(
                surfaceView = it, modifier = Modifier.fillMaxSize()
            )
        }
    }

    localSurfaceView?.let {
        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = if (callState == CallState.Connected) Modifier.size(
                    100.dp,
                    200.dp
                ) else Modifier.fillMaxSize()
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
            if (callState == CallState.Incoming)
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
