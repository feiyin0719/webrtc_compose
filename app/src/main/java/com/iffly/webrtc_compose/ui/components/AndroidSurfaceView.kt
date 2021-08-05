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

class TestSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    var mIsDrawing = true

    init {
        holder.addCallback(this)
        layoutParams = ViewGroup.LayoutParams(500, 500)

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Thread(this).start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mIsDrawing = false
    }

    override fun run() {

        while (mIsDrawing) {
            var startTime = System.currentTimeMillis()
            val canvas = holder.lockCanvas()
            onMyDraw(canvas)
            holder.unlockCanvasAndPost(canvas)
            val delta = System.currentTimeMillis() - startTime
            if (delta < 16)
                Thread.sleep(16 - delta)
        }
    }

    fun onMyDraw(canvas: Canvas) {
        val paint = Paint()
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(100f, 100f, 50f, paint)
    }

}

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

