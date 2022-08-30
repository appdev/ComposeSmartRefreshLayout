package com.appdev.sample.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieView() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("green_loading.json"))
    LottieAnimation(composition,iterations = Int.MAX_VALUE)

}