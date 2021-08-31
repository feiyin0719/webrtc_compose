package com.iffly.webrtc_compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
        val outGoing: Boolean = intent.getBooleanExtra(OUTGOING_KEY, false)
        setContent {
            ProvideWindowInsets {
                WebrtcTheme {
                    CallScreen(
                        userId = intent.getStringExtra(USER_KEY) ?: "",
                        outGoing = outGoing
                    )
                }
            }
        }
    }

    companion object {
        const val USER_KEY = "userID"
        const val OUTGOING_KEY = "outGoing"
        fun startCallActivity(userId: String = "", outGoing: Boolean = false, context: Context) {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(USER_KEY, userId)
            intent.putExtra(OUTGOING_KEY, outGoing)
            if (!(context is Activity))
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}