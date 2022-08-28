package com.appdev.sample

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.appdev.compose.composesmartrefreshlayout.RefreshState
import com.appdev.compose.composesmartrefreshlayout.SwipeRefreshStyle
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
    refreshHeader: @Composable BoxScope.(RefreshState) -> Unit = {
        CommonRefreshHeader(it)
    },
    refreshFooter: @Composable BoxScope.(RefreshState) -> Unit = {
        CommonRefreshFooter(it)
    },
    twoLevel: @Composable BoxScope.() -> Unit = {
    },
    refreshStyle: SwipeRefreshStyle = SwipeRefreshStyle.Translate,
    content: @Composable BoxScope.() -> Unit,
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
        enableOffset = mState._enableOffset,
        enableFastScrollTopOver = mState._enableFastScrollTopOver,
        refreshStyle = refreshStyle,
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
    enableOffset: Boolean = false,
    enableAutoLoadMore: Boolean = true,
    refreshMinTime: Long = 800L,
    refreshHeader: @Composable BoxScope.(RefreshState) -> Unit,
    refreshFooter: @Composable BoxScope.(RefreshState) -> Unit,
    twoLevel: @Composable BoxScope.() -> Unit,
    onTwoLevelCall: (Boolean) -> Unit = {},
    refreshStyle: SwipeRefreshStyle = SwipeRefreshStyle.Translate,
    content: @Composable BoxScope.() -> Unit,
    enableLoadMore: Boolean = false,
    enableFastScrollTopOver: Boolean = true,
    offsetTop: Animatable<Float, AnimationVector1D>,
    offsetBottom: Animatable<Float, AnimationVector1D>,
    onLoadMore: suspend () -> Unit,
) {
    val TAG = "RefreshLayout"
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
        mutableStateOf(RefreshState.None)
    }
    //加载更多状态
    var innerLoadState by remember {
        mutableStateOf(RefreshState.None)
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

    var refreshHeaderStyle by remember {
        mutableStateOf(refreshStyle)
    }


    //顶部偏移量
    val topOffset by rememberUpdatedState(newValue = offsetTop)
    //底部偏移量
    val bottomOffset by rememberUpdatedState(newValue = offsetBottom)

    //自动刷新
    val innerAutoRefresh by rememberUpdatedState(newValue = autoRefresh)
    val innerEnableOffset by rememberUpdatedState(newValue = enableOffset)
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
    fun setInnerRefreshState(refreshEnum: RefreshState) {
        innerRefreshState = refreshEnum
    }
    //</editor-fold>

    //<editor-fold desc="设置加载更多状态">
    fun setInnerLoadState(refreshEnum: RefreshState) {
        innerLoadState = refreshEnum
    }
    //</editor-fold>

    //<editor-fold desc="加载更多">
    LaunchedEffect(key1 = innerIsLoadingMore, block = {
        if (innerIsLoadingMore) {
            innerDoLoadMore()
            setInnerLoadState(RefreshState.LoadFinish)
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
            setInnerRefreshState(RefreshState.RefreshFinish)
            innerIsRefresh = false
        }
    })
    //</editor-fold>

    //<editor-fold desc="自动刷新">
    LaunchedEffect(key1 = innerAutoRefresh, block = {
        if (innerAutoRefresh != null && !innerIsRefresh) {
            bottomOffset.snapTo(0f)
            if (innerRefreshState.isTwoLevel) {
                onTwoLevelCall(false)
            }
            setInnerRefreshState(RefreshState.ReleaseToRefresh)
        }
    })
    //</editor-fold>

    //<editor-fold desc="二楼">
    LaunchedEffect(key1 = innerIsTwoLevel, block = {
        if (innerIsTwoLevel != null && !innerIsRefresh) {
            if (innerIsTwoLevel as Boolean) {
                setInnerRefreshState(RefreshState.TwoLevelReleased)
            } else {
                if (topOffset.value != 0f) {
                    setInnerRefreshState(RefreshState.TwoLevelFinish)
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
            RefreshState.None -> {
                if (!innerIsRefresh) {
                    topOffset.snapTo(0f)
                }
            }
            //<editor-fold desc="refresh ">
            RefreshState.PullDownToRefresh -> {
            }
            RefreshState.PullDownCanceled -> {
                if (!innerIsRefresh) {
                    topOffset.animateTo(0f)
                    setInnerRefreshState(RefreshState.None)
                }
            }
            RefreshState.ReleaseToRefresh -> {

                setInnerRefreshState(RefreshState.Refreshing)

            }
            RefreshState.Refreshing -> {
                if (!innerIsRefresh) {
                    innerIsRefresh = true
                }
                topOffset.animateTo(headerHeight)
            }
            RefreshState.RefreshFinish -> {
                if (innerIsTwoLevel != true) {
                    topOffset.animateTo(0f)
                    setInnerRefreshState(RefreshState.None)
                } else {
                    setInnerRefreshState(RefreshState.TwoLevel)
                }
            }
            //</editor-fold>

            //<editor-fold desc="twolevel">
            RefreshState.TwoLeveling -> {
            }
            RefreshState.TwoLevelReleased -> {
                onTwoLevelCall(true)
                topOffset.animateTo(maxHeight + headerHeight)
                setInnerRefreshState(RefreshState.TwoLevel)
            }
            RefreshState.TwoLevel -> {
            }
            RefreshState.TwoLevelFinish -> {
                onTwoLevelCall(false)
                topOffset.animateTo(0f)
                setInnerRefreshState(RefreshState.None)
            }
            //</editor-fold>
            else -> {}
        }
    })
    //</editor-fold>

    //<editor-fold desc="加载更多状态">
    LaunchedEffect(key1 = innerLoadState, block = {
        when (innerLoadState) {
            RefreshState.None -> {
                if (!innerIsLoadingMore) {
                    bottomOffset.snapTo(0f)
                }
            }

            RefreshState.PullUpToLoad -> {
            }
            RefreshState.LoadFinish -> {
                bottomOffset.animateTo(0f)
                setInnerLoadState(RefreshState.None)
            }
            RefreshState.Loading -> {
                if (!innerIsLoadingMore) {
                    innerIsLoadingMore = true
                }
                bottomOffset.animateTo(-footerHeight)
            }
            RefreshState.ReleaseToLoad -> {
                setInnerLoadState(RefreshState.Loading)
            }
            RefreshState.PullUpCanceled -> {
                if (!innerIsLoadingMore) {
                    bottomOffset.animateTo(0f)
                }
            }

            else -> {}
        }
    })
    //</editor-fold>
    fun getHeaderZIndex(style: SwipeRefreshStyle): Float {
        return if (style == SwipeRefreshStyle.FixedFront || style == SwipeRefreshStyle.FixedContent) {
            1f
        } else {
            0f
        }
    }

    fun isHeaderNeedClip(indicatorHeight: Int): Boolean {
        return topOffset.value.toInt() < indicatorHeight
    }

    fun getHeaderOffset(
        style: SwipeRefreshStyle,
        indicatorHeight: Int
    ): IntOffset {
        return when (style) {
            SwipeRefreshStyle.Translate -> {
                IntOffset(0, topOffset.value.toInt() - indicatorHeight)
            }
            SwipeRefreshStyle.FixedBehind, SwipeRefreshStyle.FixedFront -> {
                IntOffset(0, 0)
            }
            else -> {
                IntOffset(0, topOffset.value.toInt() - indicatorHeight)
            }
        }
    }

    //<editor-fold desc="嵌套滚动 ScrollConnection">
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {

                //<editor-fold desc="刷新出来 上推">
                if (available.y < 0f && topOffset.value > 0f /*&& source == NestedScrollSource.Drag*/ && innerEnableRefresh) {
                    if (!topOffset.isRunning) {
                        val fl = (topOffset.value + available.y).coerceAtLeast(0f)
                        if (!innerIsRefresh) {
                            if (fl > headerHeight * twoLevelRate && innerEnableTwoLevel) {
                                setInnerRefreshState(RefreshState.TwoLeveling)
                            } else {
                                setInnerRefreshState(RefreshState.PullDownToRefresh)
                            }
                        }

                        scope.launch {
                            topOffset.snapTo(fl)
                        }
                    }
                    if (innerEnableOffset) {
                        return Offset(0f, available.y)
                    } else {
                        return Offset.Zero
                    }
                }
                //</editor-fold>

                //<editor-fold desc="加载view出来 下推">
                if (available.y > 0 && bottomOffset.value < 0 /*&& innerEnableLoadMore*/ && !bottomOffset.isRunning) {
                    val fl = (bottomOffset.value + available.y)
                        .coerceAtLeast(-footerHeight).coerceAtMost(0f)
                    if (!innerIsLoadingMore) {
                        setInnerLoadState(RefreshState.PullUpToLoad)
                    }
                    scope.launch {
                        bottomOffset.snapTo(fl)
                    }
                    if (innerEnableOffset) {
                        return Offset(0f, available.y)
                    } else {
                        return Offset.Zero
                    }
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
                        topOffset.animateTo((rate * S * dragMultiplier).coerceAtMost(headerHeight * maxDrag))
                        topOffset.animateTo(0f)
                    }
                }
                //</editor-fold>

                //<editor-fold desc="上拉 或者上拉取消">
                if (innerEnableLoadMore && !innerIsLoadingMore) {
                    if (bottomOffset.value <= -footerHeight * loadMoreRate) {
                        setInnerLoadState(RefreshState.ReleaseToLoad)
                    }

                    if (bottomOffset.value < 0f && bottomOffset.value > -footerHeight * loadMoreRate) {
                        setInnerLoadState(RefreshState.PullUpCanceled)
                    }
                }
                //</editor-fold>

                //<editor-fold desc="下拉加载 取消 二楼">
                if (innerEnableRefresh) {
                    if (topOffset.value > headerHeight) {
                        if (innerEnableTwoLevel && !innerIsRefresh) {
                            if (topOffset.value > headerHeight * twoLevelRate) {
                                setInnerRefreshState(RefreshState.TwoLevelReleased)
                            } else {
                                setInnerRefreshState(RefreshState.ReleaseToRefresh)
                            }
                        } else {
                            setInnerRefreshState(RefreshState.ReleaseToRefresh)
                        }
                    } else {
                        if (!innerIsRefresh && topOffset.value > 0f) {
                            setInnerRefreshState(RefreshState.PullDownCanceled)
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
                        (topOffset.value + if (innerIsRefresh) consumed.y + available.y else (available.y * dragMultiplier))
                            .coerceAtLeast(0f)
                            .coerceAtMost(if (innerIsRefresh) headerHeight else headerHeight * maxDrag)
                    if (newOffset > headerHeight * twoLevelRate && innerEnableTwoLevel && !innerIsRefresh) {
                        setInnerRefreshState(RefreshState.TwoLeveling)
                        scope.launch {
                            topOffset.snapTo(newOffset)
                        }
                        return Offset(
                            0f,
                            if (innerIsRefresh) consumed.y + available.y else available.y / dragMultiplier
                        )
                    }

                    if (!topOffset.isRunning) {
                        if (!innerIsRefresh) {
                            setInnerRefreshState(RefreshState.PullDownToRefresh)
                        }
                        scope.launch {
                            topOffset.snapTo(newOffset)
                        }
                    }
                    if (innerEnableOffset) {
                        return Offset(
                            0f,
                            if (innerIsRefresh) available.y else available.y / dragMultiplier
                        )
                    } else {
                        return Offset.Zero
                    }
                }
                //</editor-fold>

                //<editor-fold desc="上拉加载">
                val auto = if (innerEnableAutoLoadMore) true else source == NestedScrollSource.Drag
                if (available.y < 0
                    && innerEnableLoadMore
                    && auto
                    && !bottomOffset.isRunning
                ) {
                    val y = available.y + consumed.y
                    val newOffset =
                        (bottomOffset.value + if (source == NestedScrollSource.Drag) y * dragMultiplier else y)
                            .coerceAtLeast(if (source == NestedScrollSource.Drag) (-footerHeight * maxDrag) else -footerHeight)
                            .coerceAtMost(0f)

                    scope.launch {
                        bottomOffset.snapTo(newOffset)
                    }
                    if (!innerIsLoadingMore) {
                        setInnerLoadState(RefreshState.PullUpToLoad)
                    }
                    if (innerEnableOffset) {
                        return Offset(
                            0f,
                            if (innerIsRefresh) available.y else available.y / dragMultiplier
                        )
                    } else {
                        return Offset.Zero
                    }
                }
                //</editor-fold>

                return super.onPostScroll(consumed, available, source)
            }

        }
    }
    //</editor-fold>

    //overScroll -:> never UI
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
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
                                (topOffset.value - maxHeight - headerHeight).toInt()
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
                    .onGloballyPositioned {
                        if (headerHeight == 0f) {
                            headerHeight = it.size.height.toFloat()
                        }
                    }
                    .let { if (isHeaderNeedClip(headerHeight.toInt())) it.clipToBounds() else it }
                    .offset {
                        getHeaderOffset(refreshHeaderStyle, headerHeight.toInt())
                    }
                    .zIndex(getHeaderZIndex(refreshStyle))
                ) {
                    refreshHeader(innerRefreshState)
                }
            }
            //</editor-fold>

            //<editor-fold desc="content UI">
            Box(modifier = Modifier
                .offset {
                    IntOffset(0, (topOffset.value + bottomOffset.value).toInt())
                }) {
                content()
            }
            //</editor-fold>

            //<editor-fold desc="加载更多UI">
            if (innerEnableLoadMore) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned {
                        if (footerHeight == 0f) {
                            footerHeight = it.size.height.toFloat()
                        }
                    }
                    .offset {
                        IntOffset(
                            0,
                            (bottomOffset.value + footerHeight + if (topOffset.value > 0) topOffset.value else 0f).toInt()
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


@Composable
fun CommonRefreshFooter(state: RefreshState) {

    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (state != RefreshState.None) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

    }
}

@Composable
fun CommonRefreshHeader(state: RefreshState) {
    val mState by rememberUpdatedState(newValue = state)
    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(mState.toString(), modifier = Modifier.align(Alignment.Center))
    }

}


class RefreshLayoutState {
    internal var refreshMinTime: Long by mutableStateOf(800L)
    internal var _autoRefresh: Int? by mutableStateOf(null)
    internal var _enableRefresh: Boolean by mutableStateOf(false)
    internal var _enableOffset: Boolean by mutableStateOf(true)
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
    enableOffset: Boolean = true,
    refreshMinTime: Long = 800L,
): RefreshLayoutState {
    return remember {
        RefreshLayoutState().apply {
            this._enableRefresh = enableRefresh
            this._enableTwoLevel = enableTwoLevel
            this._enableLoadMore = enableLoadMore
            this._enableAutoLoadMore = enableAutoLoadMore
            this._enableFastScrollTopOver = enableFastScrollTopOver
            this._enableOffset = enableOffset
            this.refreshMinTime = refreshMinTime
        }
    }
}


/**
 *刷新状态测试
 */
@OptIn(ExperimentalFoundationApi::class)
@Preview(backgroundColor = 0xffffff)
@Composable
fun TestRefreshLayout() {
    val refreshState = rememberRefreshLayoutState(
        enableLoadMore = true,
        enableTwoLevel = true
    )
    var size by remember {
        mutableStateOf(20)
    }
    val state = rememberLazyListState()
    Box {
        RefreshLayout(state = refreshState, onRefresh = {
            delay(3000)
            size = 20
        },
            onLoadMore = {
                delay(3000)
                size += 20
            },
            twoLevel = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                ) {
                    Text(text = "twoLevel", modifier = Modifier.align(Alignment.Center))
                }
            }
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
                    state.scrollToItem(0)
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