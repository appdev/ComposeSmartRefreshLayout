package com.appdev.sample.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.appdev.compose.composesmartrefreshlayout.SmartSwipeRefreshState
import com.appdev.compose.composesmartrefreshlayout.SmartSwipeStateFlag

@Composable
fun LottieRefreshHeader(state: SmartSwipeRefreshState) {
    var isPlaying by remember {
        mutableStateOf(false)
    }

    isPlaying = state.refreshFlag != SmartSwipeStateFlag.IDLE
//    isPlaying = state.refreshFlag == SmartSwipeStateFlag.REFRESHING
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.Asset("green_loading.json"))

    val lottieAnimationState by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isPlaying,
//        speed = speed, // 动画速度状态
        restartOnPlay = true // 暂停后重新播放是否从头开始
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            lottieComposition,
            lottieAnimationState,
            modifier = Modifier.size(if (state.indicatorOffset in 0.dp..45.dp) state.indicatorOffset else 40.dp)
        )

    }

}
