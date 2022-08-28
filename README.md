## ComposeSmartRefreshLayout
参照SmartRefreshLayout仿写，基于compose实现。有下拉刷新&上拉加载功能，并且可设置拖动阈值以及自定义头尾布局。

## 如何使用

```gradle

```

```kotlin
val refreshState = rememberSmartSwipeRefreshState()
SmartSwipeRefresh(
    onRefresh = {
        // refresh
    },
    onLoadMore = {
        // loadMore
    },
    state = refreshState,
    isNeedRefresh = true,
    isNeedLoadMore = true,
    headerIndicator = {
        MyRefreshHeader(refreshState.refreshFlag, true)
    },
    footerIndicator = {
        MyRefreshFooter(refreshState.loadMoreFlag, true)
    }) {
    
}
```
