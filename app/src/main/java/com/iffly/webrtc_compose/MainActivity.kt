package com.iffly.webrtc_compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.iffly.webrtc_compose.ui.WebrtcApp
import com.iffly.webrtc_compose.viewmodel.app.AppViewModel

class MainActivity : ComponentActivity() {
    val appViewModel: AppViewModel by viewModels<AppViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            WebrtcApp()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        appViewModel.sendAction(
            AppViewModel.AppAction(
                AppViewModel.AppAction.AppActionValue.StartCall,
                intent?.getStringExtra(
                    USER_KEY
                ) ?: "",
                outGoing = intent?.getBooleanExtra(OUTGOING_KEY, false) ?: false
            )
        )
    }

    companion object {
        const val USER_KEY = "userID"
        const val OUTGOING_KEY = "outGoing"
        fun startActivity(userId: String = "", outGoing: Boolean = false, context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(USER_KEY, userId)
            intent.putExtra(OUTGOING_KEY, outGoing)
            if (!(context is Activity))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}