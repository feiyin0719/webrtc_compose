package com.iffly.webrtc_compose

import android.app.Application
import com.iffly.rtcchat.SkyEngineKit

import com.iffly.webrtc_compose.voip.VoipEvent
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    var username = ""
    var roomId = ""
    var otherUserId = ""
    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化信令
        SkyEngineKit.init(VoipEvent)
    }

    companion object {
        var instance: App? = null
            private set
    }
}