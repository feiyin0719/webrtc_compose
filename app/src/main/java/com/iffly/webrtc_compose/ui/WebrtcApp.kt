package com.iffly.webrtc_compose.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.iffly.webrtc_compose.ui.components.AppScaffold
import com.iffly.webrtc_compose.ui.components.BottomBar
import com.iffly.webrtc_compose.ui.home.HomeSections
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme
import com.iffly.webrtc_compose.viewmodel.app.appViewModel

@Composable
fun WebrtcApp() {
    val appViewModel = appViewModel()
    ProvideWindowInsets {
        WebrtcTheme {
            val tabs = remember { HomeSections.values.toTypedArray() }
            val navController = rememberNavController()
            ProvideNavController(navController = navController) {
                AppScaffold(
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
}

@Composable
private fun ProvideNavController(
    navController: NavController,
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(
        LocalNavController provides navController,
        content = content
    )
}

val LocalNavController = staticCompositionLocalOf<NavController?> {
    null
}