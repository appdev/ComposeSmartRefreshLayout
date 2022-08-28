package com.appdev.compose.composesmartrefreshlayout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

/**
 * Created by appdev on 2022/6/13
 * Description -> 支持下拉刷新&加载更多的通用组件
 */
@Composable
fun SmartSwipeRefresh(
    modifier: Modifier = Modifier,
    onRefresh: (suspend () -> Unit)? = null,
    onLoadMore: (suspend () -> Unit)? = null,
    state: SmartSwipeRefreshState,
    isNeedRefresh: Boolean = true,
    isNeedLoadMore: Boolean = true,
    headerThreshold: Dp? = null,
    footerThreshold: Dp? = null,
    headerIndicator: @Composable () -> Unit = { MyRefreshHeader(flag = state.refreshFlag) },
    footerIndicator: @Composable () -> Unit = { MyRefreshHeader(flag = state.loadMoreFlag) },
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    LaunchedEffect(Unit) {
        state.indicatorOffsetFlow.collect {
            val currentOffset = with(density) { state.indicatorOffset + it.toDp() }
            // 重点：解决触摸滑动展示header||footer的时候反向滑动另一边的布局被展示出一个白边出来
            // 当footer显示的情况下，希望父布局的偏移量最大只有0dp，防止尾布局会被偏移几个像素
            // 当header显示的情况下，希望父布局的偏移量最小只有0dp，防止头布局会被偏移几个像素
            // 其余的情况下，直接偏移
            state.snapToOffset(
                when {
                    state.footerIsShow ->
                        currentOffset.coerceAtMost(0.dp).coerceAtLeast(-(footerThreshold ?: Dp.Infinity))
                    state.headerIsShow ->
                        currentOffset.coerceAtLeast(0.dp).coerceAtMost(headerThreshold ?: Dp.Infinity)
                    else -> currentOffset
                }
            )
        }
    }
    LaunchedEffect(state.refreshFlag) {
        when (state.refreshFlag) {
            SmartSwipeStateFlag.REFRESHING -> {
                onRefresh?.invoke()
                state.smartSwipeRefreshAnimateFinishing = state.smartSwipeRefreshAnimateFinishing.copy(isFinishing = false, isRefresh = true)
            }
            SmartSwipeStateFlag.SUCCESS, SmartSwipeStateFlag.ERROR -> {
                delay(1000)
                state.animateToOffset(0.dp)
            }
            else -> {}
        }
    }
    LaunchedEffect(state.loadMoreFlag) {
        when (state.loadMoreFlag) {
            SmartSwipeStateFlag.REFRESHING -> {
                onLoadMore?.invoke()
                state.smartSwipeRefreshAnimateFinishing = state.smartSwipeRefreshAnimateFinishing.copy(isFinishing = false, isRefresh = false)
            }
            SmartSwipeStateFlag.SUCCESS, SmartSwipeStateFlag.ERROR -> {
                delay(1000)
                state.animateToOffset(0.dp)
            }
            else -> {}
        }
    }
    Box(modifier = modifier.zIndex(-1f)) {
        SubComposeSmartSwipeRefresh(
            headerIndicator = headerIndicator,
            footerIndicator = footerIndicator,
            isNeedRefresh,
            isNeedLoadMore
        ) { header, footer ->
            val smartSwipeRefreshNestedScrollConnection = remember(state, header, footer) {
                RefreshNestedScrollConnection(state, header, footer)
            }
            Box(
                modifier.nestedScroll(smartSwipeRefreshNestedScrollConnection),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isNeedRefresh) {
                    Box(Modifier.offset(y = -header + state.indicatorOffset)) {
                        headerIndicator()
                    }
                }
                // 因为无法测量出content的高度 所以footer偏移到content布局之下
                Box(modifier = Modifier.offset(y = state.indicatorOffset)) {
                    content()
                    if (isNeedLoadMore) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = footer)
                        ) {
                            footerIndicator()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun rememberSmartSwipeRefreshState(): SmartSwipeRefreshState {
    return remember {
        SmartSwipeRefreshState()
    }
}

data class SmartSwipeRefreshAnimateFinishing(
    val isFinishing: Boolean = true,
    val isRefresh: Boolean = true
)



@Composable
private fun SubComposeSmartSwipeRefresh(
    headerIndicator: @Composable () -> Unit,
    footerIndicator: @Composable () -> Unit,
    isNeedRefresh: Boolean,
    isNeedLoadMore: Boolean,
    content: @Composable (header: Dp, footer: Dp) -> Unit
) {
    SubcomposeLayout { constraints: Constraints ->
        val headerIndicatorPlaceable = subcompose("headerIndicator", headerIndicator).first().measure(constraints)
        val footerIndicatorPlaceable = subcompose("footerIndicator", footerIndicator).first().measure(constraints)
        val contentPlaceable = subcompose("content") {
            content(
                if (isNeedRefresh) headerIndicatorPlaceable.height.toDp() else 0.dp,
                if (isNeedLoadMore) footerIndicatorPlaceable.height.toDp() else 0.dp
            )
        }.map {
            it.measure(constraints)
        }.first()
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.placeRelative(0, 0)
        }
    }
}

