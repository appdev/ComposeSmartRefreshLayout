package com.appdev.compose.composesmartrefreshlayout

/**
 * 刷新状态
 */
enum class RefreshState(role: Int, twoLevel: Boolean) {
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