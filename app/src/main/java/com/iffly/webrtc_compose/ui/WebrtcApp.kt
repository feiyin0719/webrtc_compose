package com.iffly.webrtc_compose.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.iffly.webrtc_compose.socket.IUserState
import com.iffly.webrtc_compose.socket.SocketManager
import com.iffly.webrtc_compose.ui.components.AppScaffold
import com.iffly.webrtc_compose.ui.components.BottomBar
import com.iffly.webrtc_compose.ui.home.HomeSections
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme
import com.iffly.webrtc_compose.viewmodel.app.AppViewModel
import com.iffly.webrtc_compose.viewmodel.app.AppViewModel.AppAction
import com.iffly.webrtc_compose.viewmodel.app.appViewModel

@Composable
fun WebrtcApp() {

    val appViewModel = appViewModel()
    ProvideWindowInsets {
        WebrtcTheme {
            val tabs = remember { HomeSections.values.toTypedArray() }
            val navController = rememberNavController()
            ProvideNavController(navController = navController) {
                val appState by appViewModel.viewState.observeAsState(AppViewModel.AppState())
                LaunchedEffect(key1 = appState.needStartCall) {
                    if (appState.needStartCall) {
                        startCall(
                            navController = navController,
                            appState.callUserId,
                            appState.outGoing
                        )
                        appViewModel.sendAction(
                            AppAction(
                                AppAction.AppActionValue.StartCallComplete,
                                ""
                            )
                        )
                    }
                }
                val userStateCallback = remember {
                    object : IUserState {
                        override fun userLogin() {
                            appViewModel.sendAction(
                                AppAction(
                                    AppAction.AppActionValue.ChangeState,
                                    "login"
                                )
                            )
                        }

                        override fun userLogout() {
                            appViewModel.sendAction(
                                AppAction(
                                    AppAction.AppActionValue.ChangeState,
                                    "logout"
                                )
                            )
                        }
                    }
                }
                var init by remember {
                    mutableStateOf(true)
                }
                LaunchedEffect(init) {
                    if (init) {
                        init = false
                        SocketManager.addUserStateCallback(userStateCallback)
                    }
                }

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