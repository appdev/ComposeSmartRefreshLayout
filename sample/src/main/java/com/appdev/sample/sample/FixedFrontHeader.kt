package com.appdev.sample.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.appdev.sample.RefreshLayout
import com.appdev.sample.rememberRefreshLayoutState
import com.appdev.sample.widget.RefreshColumnItem
import com.appdev.sample.widget.TitleBar
import kotlinx.coroutines.delay

@Composable
fun fixedFrontHeaderSample() {
    var refreshing by remember { mutableStateOf(false) }
    val list = (1..20).toList()
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(2000)
            refreshing = false
        }
    }
    val refreshState = rememberRefreshLayoutState(
        enableLoadMore = true,
        enableTwoLevel = true
    )
    var size by remember {
        mutableStateOf(20)
    }
    Column() {
        TitleBar(title = "FixedFront Header", true) {
//            activity?.finish()
        }
        RefreshLayout(
            state = refreshState,
            onRefresh = {
                delay(3000)
                size = 20
            },
            onLoadMore = {
                delay(3000)
                size += 20
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(0.dp, 16.dp, 0.dp, 0.dp)
                    .background(Color.White)
            ) {
                items(list) {
                    val title = "第${it}条数据"
                    val subTitle = "这是测试的第${it}条数据"
                    RefreshColumnItem(title = title, subTitle = subTitle)
                }
            }
        }
    }
}