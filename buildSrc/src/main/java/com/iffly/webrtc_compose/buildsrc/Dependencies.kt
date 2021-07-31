package com.iffly.webrtc_compose

object Versions {
    const val ktlint = "0.41.0"
}

object Libs {
    const val gradleVersion = "7.1.0-alpha05"
    const val koltinVersion = "1.5.10"

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.3.2"
        object Lifecycle {
            const val runtimeKtx =
                "androidx.lifecycle:lifecycle-runtime-ktx:2.3.1"
        }
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

            object Activity {
                const val activityCompose = "androidx.activity:activity-compose:1.3.0-rc01"
            }

        }

        object Test {
            private const val version = "1.3.0"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"
            object Ext {
                private const val version = "1.1.2"
                const val junit = "androidx.test.ext:junit-ktx:$version"
            }
            const val espressoCore = "androidx.test.espresso:espresso-core:3.2]3.0"
        }
    }

    object JUnit {
        private const val version = "4.13"
        const val junit = "junit:junit:$version"
    }

}