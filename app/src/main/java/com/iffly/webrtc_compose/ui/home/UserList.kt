package com.iffly.webrtc_compose.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.iffly.compose.redux.StoreViewModel
import com.iffly.compose.redux.storeViewModel
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.CallActivity
import com.iffly.webrtc_compose.R
import com.iffly.webrtc_compose.data.bean.UserItem
import com.iffly.webrtc_compose.reducer.home.UserViewAction
import com.iffly.webrtc_compose.reducer.home.UserViewState
import com.iffly.webrtc_compose.ui.components.AppImage
import com.iffly.webrtc_compose.ui.components.AppSurface
import com.iffly.webrtc_compose.ui.components.AppTitleBar
import com.iffly.webrtc_compose.ui.theme.Typography
import com.iffly.webrtc_compose.ui.theme.WebrtcTheme

@Composable
fun UserScreen() {
    val store = storeViewModel()

    val userViewState: UserViewState
            by store.getState(UserViewState::class.java).observeAsState(
                UserViewState(
                    true,
                    emptyList()
                )
            )
    var init by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(init) {
        if (init) {
            init = false
            refresh(store)
        }
    }
    val refreshListener = remember {
        {
            refresh(store)
        }
    }
    val context = LocalContext.current
    val callClick = remember {
        { it: String ->
            CallActivity.startCallActivity(it, true, context = context)
        }
    }

    UserList(userViewState.list, userViewState.loading, refreshListener, callClick)


}

private fun refresh(store: StoreViewModel) {
    store.dispatch(
        UserViewAction(
            UserViewAction.UserViewActionValue.ChangeLadoing,
            ""
        )
    )
    store.dispatch(
        UserViewAction(
            UserViewAction.UserViewActionValue.Refresh,
            ""
        )
    )
}

@Composable
fun UserList(
    peoples: List<UserItem>?,
    isLoading: Boolean = false,
    refreshListener: () -> Unit = {},
    callClick: (String) -> Unit = {}
) {
    AppSurface(Modifier.fillMaxSize()) {
        Column {
            AppTitleBar(title = "user")
            peoples?.let {
                SwipeRefresh(
                    state =
                    rememberSwipeRefreshState(isRefreshing = isLoading),
                    onRefresh = refreshListener,
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        items(it) {
                            UserItemLayout(
                                user = it,
                                callClick = callClick
                            )
                        }
                    }

                }

            }

        }
    }
}

@Composable
fun UserItemLayout(user: UserItem, callClick: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Chat,
            contentDescription = "",
            tint = WebrtcTheme.colors.brand,
            modifier = Modifier.size(
                50.dp, 50.dp
            )
        )
        Text(
            text = user.userId,
            style = Typography.h4,
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight()
                .offset(20.dp)
        )
        if (user.userId != App.instance?.username)
            AppImage(imageId = R.mipmap.av_video_answer,
                contentDescription = "video_answer",
                Modifier
                    .size(40.dp, 40.dp)
                    .offset(40.dp)
                    .clickable {
                        callClick.invoke(user.userId)
                    }
            )
    }
}

@Preview
@Composable
private fun UserListPre() {
    WebrtcTheme {
        UserList(
            peoples =
            IntRange(0, 20)
                .map {
                    UserItem(
                        "$it",
                        "https://source.unsplash.com/UsSdMZ78Q3E",
                        true
                    )
                }.toList()
        )

    }
}