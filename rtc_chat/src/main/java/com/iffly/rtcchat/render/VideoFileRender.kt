package com.iffly.rtcchat.render

import android.os.Handler
import android.os.HandlerThread
import org.webrtc.*
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch


class VideoFileRender @Throws(IOException::class) constructor(
    outputFile: String,
    private var outputFileWidth: Int,
    private var outputFileHeight: Int,
    sharedContext: EglBase.Context
) {
    private val TAG = "VideoFileRenderer"

    private var renderThread: HandlerThread
    private var renderThreadHandler: Handler
    private var fileThread: HandlerThread
    private var fileThreadHandler: Handler
    private var videoOutFile: FileOutputStream
    private var outputFileName: String = outputFile
    private var outputFrameSize = 0
    private var outputFrameBuffer: ByteBuffer
    private lateinit var eglBase: EglBase
    private lateinit var yuvConverter: YuvConverter
    private var frameCount = 0

    init {
        require(!(outputFileWidth % 2 == 1 || outputFileHeight % 2 == 1)) { "Does not support uneven width or height" }
        outputFrameSize = outputFileWidth * outputFileHeight * 3 / 2
        outputFrameBuffer = ByteBuffer.allocateDirect(outputFrameSize)
        videoOutFile = FileOutputStream(outputFile)
        videoOutFile.write(
            "YUV4MPEG2 C420 W$outputFileWidth H$outputFileHeight Ip F30:1 A1:1\n".toByteArray(
                Charset.forName("US-ASCII")
            )
        )
        renderThread = HandlerThread(TAG + "RenderThread")
        renderThread.start()
        renderThreadHandler = Handler(renderThread.getLooper())
        fileThread = HandlerThread(TAG + "FileThread")
        fileThread.start()
        fileThreadHandler = Handler(fileThread.getLooper())
        ThreadUtils.invokeAtFrontUninterruptibly(renderThreadHandler) {
            eglBase = EglBase.create(sharedContext, EglBase.CONFIG_PIXEL_BUFFER)
            eglBase.createDummyPbufferSurface()
            eglBase.makeCurrent()
            yuvConverter = YuvConverter()
        }
    }

    fun onFrame(frame: VideoFrame) {
        frame.retain()
        renderThreadHandler.post { renderFrameOnRenderThread(frame) }
    }

    private fun renderFrameOnRenderThread(frame: VideoFrame) {
        val buffer = frame.buffer

        // If the frame is rotated, it will be applied after cropAndScale. Therefore, if the frame is
        // rotated by 90 degrees, swap width and height.
        val targetWidth = if (frame.rotation % 180 == 0) outputFileWidth else outputFileHeight
        val targetHeight = if (frame.rotation % 180 == 0) outputFileHeight else outputFileWidth
        val frameAspectRatio = buffer.width.toFloat() / buffer.height.toFloat()
        val fileAspectRatio = targetWidth.toFloat() / targetHeight.toFloat()

        // Calculate cropping to equalize the aspect ratio.
        var cropWidth = buffer.width
        var cropHeight = buffer.height
        if (fileAspectRatio > frameAspectRatio) {
            cropHeight = (cropHeight * (frameAspectRatio / fileAspectRatio)).toInt()
        } else {
            cropWidth = (cropWidth * (fileAspectRatio / frameAspectRatio)).toInt()
        }
        val cropX = (buffer.width - cropWidth) / 2
        val cropY = (buffer.height - cropHeight) / 2
        val scaledBuffer =
            buffer.cropAndScale(cropX, cropY, cropWidth, cropHeight, targetWidth, targetHeight)
        frame.release()
        val i420 = scaledBuffer.toI420()
        scaledBuffer.release()
        fileThreadHandler.post {
            YuvHelper.I420Rotate(
                i420.dataY,
                i420.strideY,
                i420.dataU,
                i420.strideU,
                i420.dataV,
                i420.strideV,
                outputFrameBuffer,
                i420.width,
                i420.height,
                frame.rotation
            )
            i420.release()
            try {
                videoOutFile.write("FRAME\n".toByteArray(Charset.forName("US-ASCII")))
                videoOutFile.write(
                    outputFrameBuffer.array(), outputFrameBuffer.arrayOffset(), outputFrameSize
                )
            } catch (e: IOException) {
                throw RuntimeException("Error writing video to disk", e)
            }
            frameCount++
        }
    }

    /**
     * Release all resources. All already posted frames will be rendered first.
     */
    fun release() {
        val cleanupBarrier = CountDownLatch(1)
        renderThreadHandler.post {
            yuvConverter.release()
            eglBase.release()
            renderThread.quit()
            cleanupBarrier.countDown()
        }
        ThreadUtils.awaitUninterruptibly(cleanupBarrier)
        fileThreadHandler.post {
            try {
                videoOutFile.close()
                Logging.d(
                    TAG,
                    "Video written to disk as " + outputFileName + ". The number of frames is " + frameCount
                            + " and the dimensions of the frames are " + outputFileWidth + "x"
                            + outputFileHeight + "."
                )
            } catch (e: IOException) {
                throw RuntimeException("Error closing output file", e)
            }
            fileThread.quit()
        }
        try {
            fileThread.join()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Logging.e(TAG, "Interrupted while waiting for the write to disk to complete.", e)
        }
    }
}