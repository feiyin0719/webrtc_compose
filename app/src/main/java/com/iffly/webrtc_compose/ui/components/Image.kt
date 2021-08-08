package com.iffly.webrtc_compose.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.iffly.webrtc_compose.R

@Composable
fun AppImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray,
    elevation: Dp = 0.dp
) {
    AppSurface(
        color = backgroundColor,
        elevation = elevation,
        shape = CircleShape,
        modifier = modifier,

        ) {
        Image(
            painter = rememberImagePainter(
                data = imageUrl,
                builder = {
                    crossfade(true)
                    placeholder(drawableResId = R.mipmap.ic_launcher)
                }
            ),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun AppImage(
    @DrawableRes imageId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    backgroundColor: Color = Color.LightGray
) {
    AppSurface(
        color = backgroundColor,
        elevation = elevation,
        shape = CircleShape,
        modifier = modifier
    ) {
        Image(
            painter = rememberImagePainter(
                data = imageId,
                builder = {
                    crossfade(true)
                    placeholder(drawableResId = R.mipmap.ic_launcher)
                }
            ),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}