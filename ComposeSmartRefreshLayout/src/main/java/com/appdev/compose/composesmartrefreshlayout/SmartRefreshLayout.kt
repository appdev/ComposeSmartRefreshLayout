package com.appdev.compose.composesmartrefreshlayout

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
fun SmartRefreshLayout(
    modifier: Modifier = Modifier,
    onRefresh: (suspend () -> Unit)? = null,
    onLoadMore: (suspend () -> Unit)? = null,
    state: SmartSwipeRefreshState,
    isNeedRefresh: Boolean = true,
    isNeedLoadMore: Boolean = true,
    headerThreshold: Dp? = null,
    footerThreshold: Dp? = null,
    swipeStyle: SwipeRefreshStyle = SwipeRefreshStyle.Translate,
    headerIndicator: @Composable (SmartSwipeRefreshState) -> Unit = { TestRefresh(state) },
    footerIndicator: @Composable () -> Unit = { MyRefreshHeader(flag = state.loadMoreFlag) },
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        state.indicatorOffsetFlow.collect {
            val currentOffset = with(density) { state.indicatorOffset + it.toDp() }
            Log.d(TAG, "currentOffset: $currentOffset")

            // 重点：解决触摸滑动展示header||footer的时候反向滑动另一边的布局被展示出一个白边出来
            // 当footer显示的情况下，希望父布局的偏移量最大只有0dp，防止尾布局会被偏移几个像素
            // 当header显示的情况下，希望父布局的偏移量最小只有0dp，防止头布局会被偏移几个像素
            // 其余的情况下，直接偏移
            state.snapToOffset(
                when {
                    state.footerIsShow ->
                        currentOffset.coerceAtMost(0.dp)
                            .coerceAtLeast(-(footerThreshold ?: Dp.Infinity))
                    state.headerIsShow ->
                        currentOffset.coerceAtLeast(0.dp)
                            .coerceAtMost(headerThreshold ?: Dp.Infinity)
                    else -> currentOffset
                }
            )
        }
    }
    LaunchedEffect(state.refreshFlag) {
        when (state.refreshFlag) {
            SmartSwipeStateFlag.REFRESHING -> {
                onRefresh?.invoke()
                state.smartSwipeRefreshAnimateFinishing =
                    state.smartSwipeRefreshAnimateFinishing.copy(
                        isFinishing = false,
                        isRefresh = true
                    )
            }
            SmartSwipeStateFlag.SUCCESS, SmartSwipeStateFlag.ERROR -> {
                delay(400)
                state.animateToOffset(0.dp)
            }
            else -> {}
        }
    }
    LaunchedEffect(state.loadMoreFlag) {
        when (state.loadMoreFlag) {
            SmartSwipeStateFlag.REFRESHING -> {
                onLoadMore?.invoke()
                state.smartSwipeRefreshAnimateFinishing =
                    state.smartSwipeRefreshAnimateFinishing.copy(
                        isFinishing = false,
                        isRefresh = false
                    )
            }
            SmartSwipeStateFlag.SUCCESS, SmartSwipeStateFlag.ERROR -> {
                delay(400)
                state.animateToOffset(0.dp)
            }
            else -> {}
        }
    }

    val refresh: @Composable () -> Unit = { BuildRefreshHeader(state) }
    Box(modifier = modifier.zIndex(-1f)) {
        SubComposeSmartSwipeRefresh(
            headerIndicator = refresh,
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
                    Box(
                        Modifier
//                            .onGloballyPositioned {
//                                Log.d(TAG, "SmartRefreshLayoutSmartRefreshLayoutSmartRefreshLayoutSmartRefreshLayoutSmartRefreshLayout() ${ with(density){it.size.height.toDp()}}")
//                                refreshHeight =  with(density){it.size.height.toDp()}
//                            }
                            .offset(y = getOffSetY(header, swipeStyle, state))
                            .zIndex(getHeaderZIndex(swipeStyle))
//                        .background(color = Color.White)
                    ) {
                        refresh.invoke()
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


private const val TAG = "SmartRefreshLayout"

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
        val headerIndicatorPlaceable =
            subcompose("headerIndicator", headerIndicator).first().measure(constraints)
        val footerIndicatorPlaceable =
            subcompose("footerIndicator", footerIndicator).first().measure(constraints)
        Log.d(TAG, "SubComposeSmartSwipeRefresh() called with: constraints = ${headerIndicatorPlaceable.height}")
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


private fun isHeaderNeedClip(state: SmartSwipeRefreshState, indicatorHeight: Float): Boolean {
    return state.indicatorOffset.value < indicatorHeight
}

private fun getHeaderZIndex(style: SwipeRefreshStyle): Float {
    return if (style == SwipeRefreshStyle.FixedFront || style == SwipeRefreshStyle.FixedContent) {
        1f
    } else {
        0f
    }
}

private fun getHeaderHeight(
    indicatorHeight: Float,
    style: SwipeRefreshStyle,
    state: SmartSwipeRefreshState
): Float {
    return when (style) {
        SwipeRefreshStyle.Center -> state.indicatorOffset.value
        else -> indicatorHeight
    }
}

@Composable
private fun getOffSetY(
    header: Dp,
    style: SwipeRefreshStyle,
    state: SmartSwipeRefreshState
): Dp {
    return when (style) {
        SwipeRefreshStyle.Translate -> {
            -header + state.indicatorOffset
        }
        SwipeRefreshStyle.FixedBehind, SwipeRefreshStyle.FixedFront -> {
            header
        }
        SwipeRefreshStyle.Center -> {
            if (state.indicatorOffset == 0.dp) -header else (-header + state.indicatorOffset) / 2

        }
        else -> {
            -header + state.indicatorOffset
        }
    }
}


private fun getContentOffset(
    header: Dp,
    style: SwipeRefreshStyle,
    state: SmartSwipeRefreshState
): IntOffset {
    val minSize = (-header + state.indicatorOffset).value.toInt()
    return when (style) {
        SwipeRefreshStyle.Translate, SwipeRefreshStyle.Center -> {
            IntOffset(0, state.indicatorOffset.value.toInt())
        }
        SwipeRefreshStyle.FixedBehind -> {
            IntOffset(0, state.indicatorOffset.value.toInt())
        }
        else -> {
            IntOffset(0, state.indicatorOffset.value.toInt())
        }
    }
}


@Composable
private fun BuildRefreshHeader(
    state: SmartSwipeRefreshState
) {
    TestRefresh(state)

}