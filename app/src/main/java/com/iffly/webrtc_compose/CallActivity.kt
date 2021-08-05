package com.iffly.webrtc_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import com.iffly.webrtc_compose.ui.call.CallScreen
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

class CallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ProvideWindowInsets {
                WebrtcTheme {
                    CallScreen()
                }
            }
        }
    }
}