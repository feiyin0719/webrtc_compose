package com.iffly.webrtc_compose.ui.call

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.iffly.rtcchat.CallState
import com.iffly.webrtc_compose.R
import com.iffly.webrtc_compose.ui.components.AndroidSurfaceView
import com.iffly.webrtc_compose.ui.components.AppImage
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

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

