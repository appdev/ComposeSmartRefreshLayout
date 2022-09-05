package com.appdev.compose.composesmartrefreshlayout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * compose 下拉刷新/加载更多/二楼
 *create by Aracoix
 *2022/4/8
 */
@Composable
fun RefreshLayout(
    modifier: Modifier = Modifier,
    state: RefreshLayoutState,
    onRefresh: suspend () -> Unit = {},
    onLoadMore: suspend () -> Unit = {},
    refreshHeader: @Composable (RefreshEnum) -> Unit = {
        CommonRefreshHeader(it)
    },
    refreshFooter: @Composable (RefreshEnum) -> Unit = {
        CommonRefreshFooter(it)
    },
    twoLevel: @Composable BoxScope.() -> Unit = {
    },
    content: @Composable () -> Unit,
) {
    val mState by rememberUpdatedState(newValue = state)
    RefreshLayout(
        modifier = modifier,
        autoRefresh = mState._autoRefresh,
        onRefresh = onRefresh,
        enableRefresh = mState._enableRefresh,
        isTwoLevel = mState._isTwoLevel,
        enableTwoLevel = mState._enableTwoLevel,
        refreshHeader = refreshHeader,
        twoLevel = twoLevel,
        refreshFooter = refreshFooter,
        onTwoLevelCall = {
            if (!it) {
                mState._isTwoLevel = null
            } else {
                mState._isTwoLevel = it
            }
        },
        offsetTop = mState.topOffset,
        offsetBottom = mState.bottomOffset,
        refreshMinTime = mState.refreshMinTime,
        onLoadMore = onLoadMore,
        enableLoadMore = mState._enableLoadMore,
        enableAutoLoadMore = mState._enableAutoLoadMore,
//        enableOffset = mState._enableOffset,
        enableFastScrollTopOver = mState._enableFastScrollTopOver,
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RefreshLayout(
    modifier: Modifier = Modifier,
    autoRefresh: Int? = null,
    onRefresh: suspend () -> Unit,
    enableRefresh: Boolean = true,
    isTwoLevel: Boolean? = null,
    enableTwoLevel: Boolean = false,
//    enableOffset: Boolean = false,
    enableAutoLoadMore: Boolean = true,
    refreshMinTime: Long = 300L,
    refreshHeader: @Composable (RefreshEnum) -> Unit,
    refreshFooter: @Composable (RefreshEnum) -> Unit,
    twoLevel: @Composable BoxScope.() -> Unit,
    onTwoLevelCall: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
    enableLoadMore: Boolean = false,
    enableFastScrollTopOver: Boolean = true,
    offsetTop: Animatable<Float, AnimationVector1D>,
    offsetBottom: Animatable<Float, AnimationVector1D>,
    onLoadMore: suspend () -> Unit,
) {
    val TAG = "RefreshLayout"
    val density = LocalDensity.current
    // 拖拽阻尼系数
    val dragMultiplier = 0.5f
    //加载更多比例 footerHeight / loadMoreRate
    val loadMoreRate = 0.75f
    // 二楼出现 高度 headerHeight * twoLevelRate
    val twoLevelRate = 4f
    // 下拉最大距离 headerHeight * (1f + twoLevelRate)
    val maxDrag = 1f + twoLevelRate

    val scope = rememberCoroutineScope()

    //下拉刷新状态
    var innerRefreshState by remember {
        mutableStateOf(RefreshEnum.None)
    }
    //加载更多状态
    var innerLoadState by remember {
        mutableStateOf(RefreshEnum.None)
    }
    //最大距离
    var maxHeight by remember {
        mutableStateOf(0f)
    }
    //刷新头部高度
    var headerHeight by remember {
        mutableStateOf(0f)
    }
    //加载更多高度
    var footerHeight by remember {
        mutableStateOf(0f)
    }
    //顶部偏移量
    val topIndicatorOffset by rememberUpdatedState(newValue = offsetTop)
    //底部偏移量
    val bottomIndicatorOffset by rememberUpdatedState(newValue = offsetBottom)

    //自动刷新
    val innerAutoRefresh by rememberUpdatedState(newValue = autoRefresh)
//    val innerEnableOffset by rememberUpdatedState(newValue = false)
    //刷新方法
    val innerDoRefresh by rememberUpdatedState(newValue = onRefresh)
    //是否能加载更多
    val innerEnableRefresh by rememberUpdatedState(newValue = enableRefresh)
    // 二楼显示中
    val innerIsTwoLevel by rememberUpdatedState(newValue = isTwoLevel)
    //是否能显示二楼
    val innerEnableTwoLevel by rememberUpdatedState(newValue = enableTwoLevel)
    //快速滚动顶部漏出一点
    val innerEnableFastScrollTopOver by rememberUpdatedState(newValue = enableFastScrollTopOver)

    // 是否能自动加载更多
    val innerEnableAutoLoadMore by rememberUpdatedState(newValue = enableAutoLoadMore)
    // 加载更多方法
    val innerDoLoadMore by rememberUpdatedState(newValue = onLoadMore)
    // 是否能加载更多
    val innerEnableLoadMore by rememberUpdatedState(newValue = enableLoadMore)
    //是否正在加载中
    var innerIsLoadingMore by remember {
        mutableStateOf(false)
    }
    //是否正在刷新
    var innerIsRefresh by remember {
        mutableStateOf(false)
    }

    //<editor-fold desc="设置刷新状态">
    fun setInnerRefreshState(refreshEnum: RefreshEnum) {
        innerRefreshState = refreshEnum
    }
    //</editor-fold>

    //<editor-fold desc="设置加载更多状态">
    fun setInnerLoadState(refreshEnum: RefreshEnum) {
        innerLoadState = refreshEnum
    }
    //</editor-fold>

    //<editor-fold desc="加载更多">
    LaunchedEffect(key1 = innerIsLoadingMore, block = {
        if (innerIsLoadingMore) {
            innerDoLoadMore()
            setInnerLoadState(RefreshEnum.LoadFinish)
            innerIsLoadingMore = false
        }
    })
    //</editor-fold>

    //<editor-fold desc="刷新方法">
    LaunchedEffect(key1 = innerIsRefresh, block = {
        if (innerIsRefresh) {
            val startTime = System.currentTimeMillis()
            innerIsLoadingMore = false
            innerDoRefresh()
            val refreshTime = System.currentTimeMillis() - startTime
            if (refreshTime < refreshMinTime) {
                delay(refreshMinTime - refreshTime)
            }
            setInnerRefreshState(RefreshEnum.RefreshFinish)
            innerIsRefresh = false
        }
    })
    //</editor-fold>

    //<editor-fold desc="自动刷新">
    LaunchedEffect(key1 = innerAutoRefresh, block = {
        if (innerAutoRefresh != null && !innerIsRefresh) {
            bottomIndicatorOffset.snapTo(0f)
            if (innerRefreshState.isTwoLevel) {
                onTwoLevelCall(false)
            }
            setInnerRefreshState(RefreshEnum.ReleaseToRefresh)
        }
    })
    //</editor-fold>

    //<editor-fold desc="二楼">
    LaunchedEffect(key1 = innerIsTwoLevel, block = {
        if (innerIsTwoLevel != null && !innerIsRefresh) {
            if (innerIsTwoLevel as Boolean) {
                setInnerRefreshState(RefreshEnum.TwoLevelReleased)
            } else {
                if (topIndicatorOffset.value != 0f) {
                    setInnerRefreshState(RefreshEnum.TwoLevelFinish)
                }
            }
        } else {
            if (innerIsTwoLevel is Boolean) {
                onTwoLevelCall(false)
            }
        }
    })
    //</editor-fold>

    //<editor-fold desc="刷新状态">
    LaunchedEffect(key1 = innerRefreshState, block = {
        when (innerRefreshState) {
            RefreshEnum.None -> {
                if (!innerIsRefresh) {
                    topIndicatorOffset.snapTo(0f)
                }
            }
            //<editor-fold desc="refresh ">
            RefreshEnum.PullDownToRefresh -> {
            }
            RefreshEnum.PullDownCanceled -> {
                if (!innerIsRefresh) {
                    topIndicatorOffset.animateTo(0f)
                    setInnerRefreshState(RefreshEnum.None)
                }
            }
            RefreshEnum.ReleaseToRefresh -> {

                setInnerRefreshState(RefreshEnum.Refreshing)

            }
            RefreshEnum.Refreshing -> {
                if (!innerIsRefresh) {
                    innerIsRefresh = true
                }
                topIndicatorOffset.animateTo(headerHeight)
            }
            RefreshEnum.RefreshFinish -> {
                if (innerIsTwoLevel != true) {
                    topIndicatorOffset.animateTo(0f)
                    setInnerRefreshState(RefreshEnum.None)
                } else {
                    setInnerRefreshState(RefreshEnum.TwoLevel)
                }
            }
            //</editor-fold>

            //<editor-fold desc="twolevel">
            RefreshEnum.TwoLeveling -> {
            }
            RefreshEnum.TwoLevelReleased -> {
                onTwoLevelCall(true)
                topIndicatorOffset.animateTo(maxHeight + headerHeight)
                setInnerRefreshState(RefreshEnum.TwoLevel)
            }
            RefreshEnum.TwoLevel -> {
            }
            RefreshEnum.TwoLevelFinish -> {
                onTwoLevelCall(false)
                topIndicatorOffset.animateTo(0f)
                setInnerRefreshState(RefreshEnum.None)
            }
            //</editor-fold>

            else -> {}
        }
    })
    //</editor-fold>

    //<editor-fold desc="加载更多状态">
    LaunchedEffect(key1 = innerLoadState, block = {
        when (innerLoadState) {
            RefreshEnum.None -> {
                if (!innerIsLoadingMore) {
                    bottomIndicatorOffset.snapTo(0f)
                }
            }

            RefreshEnum.PullUpToLoad -> {
            }
            RefreshEnum.LoadFinish -> {
                bottomIndicatorOffset.animateTo(0f)
                setInnerLoadState(RefreshEnum.None)
            }
            RefreshEnum.Loading -> {
                if (!innerIsLoadingMore) {
                    innerIsLoadingMore = true
                }
                bottomIndicatorOffset.animateTo(-footerHeight)
            }
            RefreshEnum.ReleaseToLoad -> {
                setInnerLoadState(RefreshEnum.Loading)
            }
            RefreshEnum.PullUpCanceled -> {
                if (!innerIsLoadingMore) {
                    bottomIndicatorOffset.animateTo(0f)
                }
            }

            else -> {}
        }
    })
    //</editor-fold>

    //<editor-fold desc="嵌套滚动 ScrollConnection">
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {

                //<editor-fold desc="刷新出来 上推">
                if (available.y < 0f && topIndicatorOffset.value > 0f /*&& source == NestedScrollSource.Drag*/ && innerEnableRefresh) {
                    if (!topIndicatorOffset.isRunning) {
                        val fl = (topIndicatorOffset.value + available.y).coerceAtLeast(0f)
                        if (!innerIsRefresh) {
                            if (fl > headerHeight * twoLevelRate && innerEnableTwoLevel) {
                                setInnerRefreshState(RefreshEnum.TwoLeveling)
                            } else {
                                setInnerRefreshState(RefreshEnum.PullDownToRefresh)
                            }
                        }

                        scope.launch {
                            topIndicatorOffset.snapTo(fl)
                        }
                    }
                    return Offset.Zero
                }
                //</editor-fold>

                //<editor-fold desc="加载view出来 下推">
                if (available.y > 0 && bottomIndicatorOffset.value < 0 /*&& innerEnableLoadMore*/ && !bottomIndicatorOffset.isRunning) {
                    val fl = (bottomIndicatorOffset.value + available.y)
                        .coerceAtLeast(-footerHeight).coerceAtMost(0f)
                    if (!innerIsLoadingMore) {
                        setInnerLoadState(RefreshEnum.PullUpToLoad)
                    }
                    scope.launch {
                        bottomIndicatorOffset.snapTo(fl)
                    }
//                    if (innerEnableOffset) {
//                        return Offset(0f, available.y)
//                    } else {
                    return Offset.Zero
//                    }
                }
                //</editor-fold>

                return super.onPreScroll(available, source)
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                //<editor-fold desc="快速滚动显示刷新头部">
                if (available.y > 0
                    && consumed.y > 0
                    && available.y > consumed.y
                    && innerEnableFastScrollTopOver
                    && enableRefresh
                    && !innerIsRefresh
                ) {
                    val S = headerHeight / 6f
                    val v = available.y - consumed.y
                    val rate = v / maxHeight
                    if (rate > 1) {
                        topIndicatorOffset.animateTo(
                            (rate * S * dragMultiplier).coerceAtMost(
                                headerHeight * maxDrag
                            )
                        )
                        topIndicatorOffset.animateTo(0f)
                    }
                }
                //</editor-fold>

                //<editor-fold desc="上拉 或者上拉取消">
                if (innerEnableLoadMore && !innerIsLoadingMore) {
                    if (bottomIndicatorOffset.value <= -footerHeight * loadMoreRate) {
                        setInnerLoadState(RefreshEnum.ReleaseToLoad)
                    }

                    if (bottomIndicatorOffset.value < 0f && bottomIndicatorOffset.value > -footerHeight * loadMoreRate) {
                        setInnerLoadState(RefreshEnum.PullUpCanceled)
                    }
                }
                //</editor-fold>

                //<editor-fold desc="下拉加载 取消 二楼">
                if (innerEnableRefresh) {
                    if (topIndicatorOffset.value > headerHeight) {
                        if (innerEnableTwoLevel && !innerIsRefresh) {
                            if (topIndicatorOffset.value > headerHeight * twoLevelRate) {
                                setInnerRefreshState(RefreshEnum.TwoLevelReleased)
                            } else {
                                setInnerRefreshState(RefreshEnum.ReleaseToRefresh)
                            }
                        } else {
                            setInnerRefreshState(RefreshEnum.ReleaseToRefresh)
                        }
                    } else {
                        if (!innerIsRefresh && topIndicatorOffset.value > 0f) {
                            setInnerRefreshState(RefreshEnum.PullDownCanceled)
                        }
                    }
                }
                //</editor-fold>

                return super.onPostFling(consumed, available)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                //<editor-fold desc="下拉">
                val needMultiplier = if (innerIsRefresh) {
                    source == NestedScrollSource.Drag || source == NestedScrollSource.Fling
                } else {
                    source == NestedScrollSource.Drag
                }

                if (needMultiplier
                    && !innerRefreshState.isFooter
                    && consumed.y >= 0f
                    && available.y > 0
                    && innerEnableRefresh
                ) {
                    val newOffset =
                        (topIndicatorOffset.value + if (innerIsRefresh) consumed.y + available.y else (available.y * dragMultiplier))
                            .coerceAtLeast(0f)
                            .coerceAtMost(if (innerIsRefresh) headerHeight else headerHeight * maxDrag)
                    if (newOffset > headerHeight * twoLevelRate && innerEnableTwoLevel && !innerIsRefresh) {
                        setInnerRefreshState(RefreshEnum.TwoLeveling)
                        scope.launch {
                            topIndicatorOffset.snapTo(newOffset)
                        }
                        return Offset(
                            0f,
                            if (innerIsRefresh) consumed.y + available.y else available.y / dragMultiplier
                        )
                    }

                    if (!topIndicatorOffset.isRunning) {
                        if (!innerIsRefresh) {
                            setInnerRefreshState(RefreshEnum.PullDownToRefresh)
                        }
                        scope.launch {
                            topIndicatorOffset.snapTo(newOffset)
                        }
                    }
//                    if (innerEnableOffset) {
                    return Offset(
                        0f,
                        if (innerIsRefresh) available.y else available.y / dragMultiplier
                    )
//                    } else {
//                        return Offset.Zero
//                    }
                }
                //</editor-fold>

                //<editor-fold desc="上拉加载">
                val auto = if (innerEnableAutoLoadMore) true else source == NestedScrollSource.Drag
                if (available.y < 0
                    && innerEnableLoadMore
                    && auto
                    && !bottomIndicatorOffset.isRunning
                ) {
                    val y = available.y + consumed.y
                    val newOffset =
                        (bottomIndicatorOffset.value + if (source == NestedScrollSource.Drag) y * dragMultiplier else y)
                            .coerceAtLeast(if (source == NestedScrollSource.Drag) (-footerHeight * maxDrag) else -footerHeight)
                            .coerceAtMost(0f)

                    scope.launch {
                        bottomIndicatorOffset.snapTo(newOffset)
                    }
                    if (!innerIsLoadingMore) {
                        setInnerLoadState(RefreshEnum.PullUpToLoad)
                    }
//                    if (innerEnableOffset) {
//                        return Offset(0f,
//                            if (innerIsRefresh) available.y else available.y / dragMultiplier)
//                    } else {
                    return Offset.Zero
//                    }
                }
                //</editor-fold>

                return super.onPostScroll(consumed, available, source)
            }

        }
    }
    //</editor-fold>

    //overScroll -:> never UI
    val refresh: @Composable () -> Unit = { InnerRefreshHeader(refreshHeader, innerRefreshState) }
    val footer: @Composable () -> Unit = { InnerRefreshHeader(refreshFooter, innerLoadState) }
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        SubComposeSmartSwipeRefresh(
            headerIndicator = refresh,
            footerIndicator = footer,
            innerEnableRefresh,
            innerEnableLoadMore
        ) { header, footer ->
            headerHeight = header
            footerHeight = footer
            Box(
                modifier = modifier
                    .zIndex(-1f)
                    .nestedScroll(connection)
                    .onGloballyPositioned {
                        if (maxHeight == 0f) {
                            maxHeight = it.size.height.toFloat()
                        }
                    }
            ) {
                //<editor-fold desc="二楼UI">
                if (enableTwoLevel && innerRefreshState.isTwoLevel) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .offset {
                                IntOffset(
                                    0,
                                    (topIndicatorOffset.value - maxHeight - headerHeight).toInt()
                                )
                            }
                    ) {
                        twoLevel()
                    }
                }
                //</editor-fold>

                //<editor-fold desc="headerUI">
                if (innerEnableRefresh) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            IntOffset(
                                0,
                                (topIndicatorOffset.value - headerHeight).toInt()
                            )
                        }
                    ) {
                        refresh.invoke()
                    }
                }
                //</editor-fold>

                //<editor-fold desc="content UI">
                Box(modifier = Modifier
                    .offset {
                        IntOffset(
                            0,
                            (topIndicatorOffset.value + bottomIndicatorOffset.value).toInt()
                        )
                    }) {
                    content()
                }
                //</editor-fold>

                //<editor-fold desc="加载更多UI">
                if (innerEnableLoadMore) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .offset {
                            IntOffset(
                                0,
                                (bottomIndicatorOffset.value + footerHeight + if (topIndicatorOffset.value > 0) topIndicatorOffset.value else 0f).toInt()
                            )
                        }
                    ) {
                        refreshFooter(innerLoadState)
                    }
                }
                //</editor-fold>
            }
        }

    }
}

@Composable
private fun SubComposeSmartSwipeRefresh(
    headerIndicator: @Composable () -> Unit,
    footerIndicator: @Composable () -> Unit,
    isNeedRefresh: Boolean,
    isNeedLoadMore: Boolean,
    content: @Composable (header: Float, footer: Float) -> Unit
) {
    SubcomposeLayout { constraints: Constraints ->
        val headerIndicatorPlaceable =
            subcompose("headerIndicator", headerIndicator).first().measure(constraints)
        val footerIndicatorPlaceable =
            subcompose("footerIndicator", footerIndicator).first().measure(constraints)
        val contentPlaceable = subcompose("content") {
            content(
                if (isNeedRefresh) headerIndicatorPlaceable.height.toFloat() else 0f,
                if (isNeedLoadMore) footerIndicatorPlaceable.height.toFloat() else 0f
            )
        }.map {
            it.measure(constraints)
        }.first()
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.placeRelative(0, 0)
        }
    }
}

@Composable
fun CommonRefreshFooter(state: RefreshEnum) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (state != RefreshEnum.None) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun CommonRefreshHeader(state: RefreshEnum) {
    val mState by rememberUpdatedState(newValue = state)
    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(mState.toString(), modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun InnerRefreshHeader(
    refreshHeader: @Composable() (RefreshEnum) -> Unit,
    state: RefreshEnum
) {
    refreshHeader.invoke(state)
}

@Composable
fun InnerRefreshFooter(
    refreshHeader: @Composable() (RefreshEnum) -> Unit,
    state: RefreshEnum
) {
    refreshHeader.invoke(state)
}


class RefreshLayoutState {
    internal var refreshMinTime: Long by mutableStateOf(300L)
    internal var _autoRefresh: Int? by mutableStateOf(null)
    internal var _enableRefresh: Boolean by mutableStateOf(false)

    //    internal var _enableOffset: Boolean by mutableStateOf(true)
    internal var _isTwoLevel: Boolean? by mutableStateOf(null)
    internal var _enableTwoLevel: Boolean by mutableStateOf(false)
    internal var _enableLoadMore: Boolean by mutableStateOf(false)
    internal var _enableAutoLoadMore: Boolean by mutableStateOf(true)
    internal var _enableFastScrollTopOver: Boolean by mutableStateOf(true)

    //顶部偏移量
    val topOffset by mutableStateOf(Animatable(0f))

    //底部偏移量
    val bottomOffset by mutableStateOf(Animatable(0f))

    fun enableAutoLoadMore() {
        _enableAutoLoadMore = true
    }

    fun disAbleAutoLoadMore() {
        _enableAutoLoadMore = false
    }

    fun enableLoadMore() {
        _enableLoadMore = true
    }

    fun disAbleLoadMore() {
        _enableLoadMore = false
    }

    fun isTwoLevelOpen() = _isTwoLevel != null && _isTwoLevel == true

    fun toggleTwoLevel() {
        if (_enableTwoLevel) {
            _isTwoLevel = !(_isTwoLevel ?: false)
        }
    }

    fun autoRefresh() {
        if (_enableRefresh) {
            _isTwoLevel = null
            _autoRefresh = (_autoRefresh ?: 0) + 1
        }
    }
}


@Composable
fun rememberRefreshLayoutState(
    enableRefresh: Boolean = true,
    enableLoadMore: Boolean = false,
    enableTwoLevel: Boolean = false,
    enableAutoLoadMore: Boolean = true,
    enableFastScrollTopOver: Boolean = true,
//    enableOffset: Boolean = true,
    refreshMinTime: Long = 800L,
): RefreshLayoutState {
    return remember {
        RefreshLayoutState().apply {
            this._enableRefresh = enableRefresh
            this._enableTwoLevel = enableTwoLevel
            this._enableLoadMore = enableLoadMore
            this._enableAutoLoadMore = enableAutoLoadMore
            this._enableFastScrollTopOver = enableFastScrollTopOver
//            this._enableOffset = enableOffset
            this.refreshMinTime = refreshMinTime
        }
    }
}

/**
 * 刷新状态
 */
enum class RefreshEnum(role: Int, twoLevel: Boolean) {
    None(0, false),
    PullDownToRefresh(1, false),
    PullUpToLoad(2, false),
    PullDownCanceled(1, false),
    PullUpCanceled(2, false),
    ReleaseToRefresh(1, false),
    ReleaseToLoad(2, false),
    TwoLevelReleased(1, true),
    Refreshing(1, false),
    Loading(2, false),
    TwoLeveling(1, true),
    TwoLevel(1, true),
    RefreshFinish(1, false),
    LoadFinish(2, false),
    TwoLevelFinish(1, true);

    val isHeader: Boolean = role == 1
    val isFooter: Boolean = role == 2
    val isTwoLevel // 二级刷新  TwoLevelReleased TwoLevel
            : Boolean = twoLevel

}

