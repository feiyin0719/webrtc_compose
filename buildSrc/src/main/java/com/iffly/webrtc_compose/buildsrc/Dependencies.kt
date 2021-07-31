package com.iffly.webrtc_compose

object Versions {
    const val ktlint = "0.41.0"
}

object Libs {
    const val gradleVersion = "7.1.0-alpha05"
    const val koltinVersion = "1.5.10"

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.6.0-alpha03"

        object Compose {
            const val snapshot = ""
            const val version = "1.0.0-rc01"

            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"
            const val ui = "androidx.compose.ui:ui:${version}"
            const val uiUtil = "androidx.compose.ui:ui-util:${version}"
            const val runtime = "androidx.compose.runtime:runtime:${version}"
            const val material = "androidx.compose.material:material:${version}"
            const val animation = "androidx.compose.animation:animation:${version}"
            const val tooling = "androidx.compose.ui:ui-tooling:${version}"
            const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"
            const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"
        }
    }
}