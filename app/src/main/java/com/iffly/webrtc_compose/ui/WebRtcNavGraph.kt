/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iffly.webrtc_compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iffly.webrtc_compose.ui.home.HomeSections
import com.iffly.webrtc_compose.ui.home.PEOPLE_KEY
import com.iffly.webrtc_compose.ui.home.addHomeGraph
import com.iffly.webrtc_compose.ui.login.LOGIN_ROUTE
import com.iffly.webrtc_compose.ui.login.LoginScreen


/**
 * Destinations used in the ([JetsnackApp]).
 */
object MainDestinations {
    const val HOME_ROUTE = "home"
    const val LOGIN_ROUTE = "login"

}

@Composable
fun WebRtcNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainDestinations.LOGIN_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        navigation(
            route = MainDestinations.LOGIN_ROUTE,
            startDestination = LOGIN_ROUTE
        ) {
            composable(route = LOGIN_ROUTE) {
                LoginScreen()
            }
        }
        navigation(
            route = MainDestinations.HOME_ROUTE,
            startDestination = HomeSections[PEOPLE_KEY]!!.route
        ) {
            addHomeGraph()
        }
    }
}

/**
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 *
 * This is used to de-duplicate navigation events.
 */
private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED


private val NavGraph.startDestination: NavDestination?
    get() = findNode(startDestinationId)

/**
 * Copied from similar function in NavigationUI.kt
 *
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation/navigation-ui/src/main/java/androidx/navigation/ui/NavigationUI.kt
 */
public tailrec fun findStartDestination(graph: NavDestination): NavDestination {
    return if (graph is NavGraph) findStartDestination(graph.startDestination!!) else graph
}