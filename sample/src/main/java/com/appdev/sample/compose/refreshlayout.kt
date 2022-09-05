package com.appdev.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.appdev.compose.composesmartrefreshlayout.RefreshLayout
import com.appdev.compose.composesmartrefreshlayout.rememberRefreshLayoutState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 *刷新状态测试
 */
@Composable
fun TestRefreshLayout() {
    val refreshState = rememberRefreshLayoutState(
        enableLoadMore = true,
        enableTwoLevel = false
    )
    var size by remember {
        mutableStateOf(20)
    }
    val state = rememberLazyListState()
    Box {
        RefreshLayout(
            state = refreshState,
            onRefresh = {
                delay(1000)
                size = 20
            },
            onLoadMore = {
                delay(1000)
                size += 20
            },
//            twoLevel = {
//                Box(
//                    Modifier
//                    .fillMaxSize()
//                    .background(Color.Gray)) {
//                    Text(text = "twoLevel", modifier = Modifier.align(Alignment.Center))
//                }
//            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                content = {
                    items(size) {
                        Column() {
                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.Gray))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(Color.White)
                            ) {
                                Text(
                                    text = it.toString(),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                    }
                })
        }
        val scope = rememberCoroutineScope()
        Column(Modifier.align(Alignment.BottomEnd)) {
            Button(onClick = {
                scope.launch {
                    state.animateScrollToItem(0)
                    refreshState.autoRefresh()
                }

            }) {
                Text(text = "自动刷新")
            }

            Button(onClick = {
                refreshState.toggleTwoLevel()
            }) {
                Text(text = if (!refreshState.isTwoLevelOpen()) "打开二楼" else "关闭二楼")
            }

        }
    }

}