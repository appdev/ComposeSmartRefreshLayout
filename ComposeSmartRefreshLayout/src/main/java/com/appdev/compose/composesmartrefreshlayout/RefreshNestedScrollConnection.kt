package com.appdev.compose.composesmartrefreshlayout

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp


class RefreshNestedScrollConnection(
    val density: Density,
    val state: SmartSwipeRefreshState,
    val dragMultiplier: Float,
    val headerHeight: Dp,
    val footerHeight: Dp,
    val onLoadMore: (Offset:Float) -> Unit
) : NestedScrollConnection {

    /**
     * 预先劫持滑动事件，消费后再交由子布局
     *
     * header展示如果反向滑动优先父布局处理 做动画并拦截滑动事件
     * footer展示如果反向滑动优先父布局处理 做动画并拦截滑动事件
     */
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        return if (source == NestedScrollSource.Drag && !state.isRefreshing()) {
            if (state.headerIsShow || state.footerIsShow) {
                when {
                    state.headerIsShow -> {
                        // header已经在展示
                        state.refreshFlag =
                            if (state.indicatorOffset > headerHeight) SmartSwipeStateFlag.TIPS_RELEASE else SmartSwipeStateFlag.TIPS_DOWN
                        if (available.y < 0f) {
                            // 头部已经展示并且上滑动
                            state.updateOffsetDelta(available.y * dragMultiplier)
                            Offset(x = 0f, y = available.y)
                        } else {
                            Offset.Zero
                        }
                    }
                    state.footerIsShow -> {
                        // footer已经在展示
                        state.loadMoreFlag =
                            if (state.indicatorOffset < -footerHeight) SmartSwipeStateFlag.TIPS_RELEASE else SmartSwipeStateFlag.TIPS_DOWN
                        if (available.y > 0f) {
                            // 尾部已经展示并且上滑动
                            state.updateOffsetDelta(available.y * dragMultiplier)
                            Offset(x = 0f, y = available.y)
                        } else {
                            Offset.Zero
                        }
                    }
                    else -> Offset.Zero
                }
            } else Offset.Zero
        } else {
            if (state.isRefreshing()) {
                Offset(x = 0f, y = available.y)
            } else {
                Offset.Zero
            }
        }
    }

    /**
     * 获取子布局处理后的滑动事件
     *
     * consumed==0代表子布局没有消费事件 即列表没有被滚动
     * 此时事件在available中 把其中的事件传递给header||footer
     * 调用state.updateOffsetDelta(available.y *dragMultiplier)做父布局动画
     * 并且消费掉滑动事件
     *
     * 刷新中不消费事件 拦截子布局即列表的滚动
     */
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (available.y < 0
            && footerHeight != 0.dp
            && !state.refreshAnimateIsRunning()
        ) {
            val y = available.y + consumed.y
            val newOffset =
                (getPx(state.indicatorOffset) + if (source == NestedScrollSource.Drag) y * dragMultiplier else y)
                    .coerceAtLeast(if (source == NestedScrollSource.Drag) -getPx(footerHeight) else -getPx(footerHeight) )
                    .coerceAtMost(0f)
            onLoadMore.invoke(newOffset)
            Log.d(TAG, "onPostScroll: enable auto load more")
        }
        return if (source == NestedScrollSource.Drag && !state.isRefreshing()) {
            if (available.y != 0f) {
                if (headerHeight != 0.dp && available.y > 0f) {
                    state.updateOffsetDelta(available.y * dragMultiplier)
                }

                if (footerHeight != 0.dp && available.y < 0f) {
                    state.updateOffsetDelta(available.y * dragMultiplier)
                }
                Offset(x = 0f, y = available.y)
            } else {
                Offset.Zero
            }
        } else {
            Offset.Zero
        }
    }

    private val TAG = "RefreshNestedScrollConn"

    /**
     * indicatorOffset>=0 header显示 indicatorOffset<=0 footer显示
     * 拖动到头部快速滑动时 如果indicatorOffset>headerHeight则
     */
    override suspend fun onPreFling(available: Velocity): Velocity {

        if (!state.isRefreshing()) {
            if (state.indicatorOffset >= headerHeight) {
                state.animateToOffset(headerHeight)
                state.refreshFlag = SmartSwipeStateFlag.REFRESHING
            } else if (state.indicatorOffset <= -footerHeight) {
                state.animateToOffset(-footerHeight)
                state.loadMoreFlag = SmartSwipeStateFlag.REFRESHING
            } else {
                if (state.indicatorOffset != 0.dp) {
                    state.animateToOffset(0.dp)
                }
            }
        }
        return super.onPreFling(available)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        return super.onPostFling(consumed, available)
    }

    fun getPx(dp: Dp):Float {
        return with(density) { dp.toPx() }
    }
}
