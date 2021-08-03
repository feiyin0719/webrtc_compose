package com.iffly.rtcchat.render

import org.webrtc.Logging
import org.webrtc.VideoFrame
import org.webrtc.VideoSink

class ProxyVideoSink : VideoSink {
    companion object {
        const val TAG = "dds_ProxyVideoSink"
    }

    private var target: VideoSink? = null
        set(value) {
            field = value
        }

    override fun onFrame(p0: VideoFrame?) {
        if (target == null) {
            Logging.d(TAG, "Dropping frame in proxy because target is null.")
            return
        }
        target?.onFrame(p0)
    }


}