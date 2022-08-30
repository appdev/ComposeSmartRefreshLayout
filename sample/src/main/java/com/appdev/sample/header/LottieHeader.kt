package com.appdev.sample.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.appdev.compose.composesmartrefreshlayout.SmartSwipeStateFlag

@Composable
fun LottieRefreshHeader(flag: SmartSwipeStateFlag) {
    var isPlaying by remember {
        mutableStateOf(false)
    }
//    val speed by remember {
//        mutableStateOf(1f)
//    }

    isPlaying = flag == SmartSwipeStateFlag.REFRESHING
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.Asset("green_loading.json"))

    val lottieAnimationState by animateLottieCompositionAsState(
        composition = lottieComposition, // 动画资源句柄
        iterations = LottieConstants.IterateForever, // 迭代次数
        isPlaying = isPlaying, // 动画播放状态
//        speed = speed, // 动画速度状态
        restartOnPlay = false // 暂停后重新播放是否从头开始
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight().background(Color.LightGray), contentAlignment = Alignment.TopCenter
    ) {
        LottieAnimation(
            lottieComposition,
            lottieAnimationState,
            modifier = Modifier.size(45.dp)
        )

    }

}
