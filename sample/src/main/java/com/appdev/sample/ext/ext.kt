package com.appdev.sample.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

@Composable
fun Int.Color(): Color {
    return colorResource(id = this)
}