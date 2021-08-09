package com.iffly.webrtc_compose.ui.login

import android.text.TextUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.iffly.webrtc_compose.ui.components.AppButton
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme


@Composable
fun LoginContent(
    name: String,
    onNameChanged: (String) -> Unit,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .statusBarsPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        TextField(
            value = name,
            label = {
                Text(
                    "please input username",
                    color = WebrtcTheme.colors.textPrimary
                )
            },
            onValueChange = onNameChanged
        )
        AppButton(
            onClick = onClick,
            enabled = !TextUtils.isEmpty(name),
            modifier = Modifier.offset(0.dp, 10.dp)
        ) {
            Text(
                text = "login",
            )
        }
    }

}

@Preview
@Composable
private fun LoginContentPre() {
    WebrtcTheme() {
        LoginContent("", {}, {})
    }

}