package com.appdev.compose.composesmartrefreshlayout

import androidx.compose.runtime.*

@Stable
class RefreshHeaderData(
    stateFlag: SmartSwipeStateFlag, isRefreshing: Boolean,
    currentOffset: Float
) {

    var stateFlag: SmartSwipeStateFlag by mutableStateOf(stateFlag)
        internal set

    /**
     */
    var isRefreshing: Boolean by mutableStateOf(isRefreshing)
        internal set

    /**
     * 刷新生效距离
     */
    var currentOffset: Float by mutableStateOf(currentOffset)
        internal set
}

@Composable
internal fun rememberRefreshHeaderData(
    stateFlag: SmartSwipeStateFlag = SmartSwipeStateFlag.IDLE,
    isRefreshing: Boolean = false,
    currentOffset: Float = 0f
): RefreshHeaderData {
    return remember {
        RefreshHeaderData(
            stateFlag = stateFlag,
            isRefreshing = isRefreshing,
            currentOffset = currentOffset,
        )
    }
}