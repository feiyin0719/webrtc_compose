package com.iffly.webrtc_compose.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.jetsnack.ui.components.JetsnackScaffold
import com.google.accompanist.insets.ProvideWindowInsets
import com.iffly.webrtc_compose.ui.components.BottomBar
import com.iffly.webrtc_compose.ui.home.HomeSections
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

@Composable
fun WebrtcApp() {
    ProvideWindowInsets {
        WebrtcTheme() {
            val tabs = remember { HomeSections.values.toTypedArray() }
            val navController = rememberNavController()
            JetsnackScaffold(
                bottomBar = { BottomBar(navController = navController, tabs = tabs) }
            ) { innerPaddingModifier ->
                WebRtcNavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPaddingModifier)
                )
            }
        }
    }
}