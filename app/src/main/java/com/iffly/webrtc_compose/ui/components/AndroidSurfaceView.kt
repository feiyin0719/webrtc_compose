package com.iffly.webrtc_compose.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun AndroidSurfaceView(
    surfaceView: SurfaceView,
    isOverlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    AndroidView(factory = {
        surfaceView.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }, update = {
        it.setZOrderMediaOverlay(isOverlay)
    }
    )
}

