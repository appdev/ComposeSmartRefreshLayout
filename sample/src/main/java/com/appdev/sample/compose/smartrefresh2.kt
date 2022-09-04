package com.appdev.sample.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appdev.compose.composesmartrefreshlayout.*
import com.appdev.sample.MainViewModel

@Composable
fun SmartRefreshBuilder2(
    viewModel: MainViewModel = viewModel(),
    scrollState: LazyListState = rememberLazyListState(),
    refreshState: SmartSwipeRefreshState,
    footerIndicator: @Composable () -> Unit = { MyRefreshFooter(refreshState.loadMoreFlag, true) },
    content: @Composable () -> Unit
) {
    SmartRefreshLayout(
        onRefresh = {
            viewModel.fillData(true)
        },
        onLoadMore = {
            viewModel.fillData(false)
        },
        state = refreshState,
        isNeedRefresh = true,
        isNeedLoadMore = true,
        footerIndicator = footerIndicator,
//        swipeStyle = SwipeRefreshStyle.Center
    ) {
        LaunchedEffect(refreshState.smartSwipeRefreshAnimateFinishing) {
            if (refreshState.smartSwipeRefreshAnimateFinishing.isFinishing && !refreshState.smartSwipeRefreshAnimateFinishing.isRefresh) {
                scrollState.animateScrollToItem(scrollState.firstVisibleItemIndex + 1)
            }
        }
        content.invoke()
    }
}