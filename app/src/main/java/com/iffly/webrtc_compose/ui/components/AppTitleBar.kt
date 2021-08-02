package com.iffly.webrtc_compose.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.insets.statusBarsPadding
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

@Composable
fun AppTitleBar(title: String, modifier: Modifier = Modifier) {
    Column {
        TopAppBar(
            backgroundColor = WebrtcTheme.colors.uiBackground,
            modifier = modifier.statusBarsPadding()
        ) {
            Text(
                title,
                style = MaterialTheme.typography.subtitle1,
                color = WebrtcTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )

        }
        AppDivider()
    }
}