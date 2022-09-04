package com.appdev.sample.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.appdev.sample.utils.RouteName
import com.appdev.sample.utils.RouteUtils
import com.appdev.sample.widget.LottieView
import com.appdev.sample.widget.RefreshColumnItem

@Composable
fun RefreshLayoutDemo(navController: NavHostController) {
    val context = LocalContext.current
    Column {
        LottieView()
        LazyColumn {
            item {
//            HeaderTitle(title = "使用示例", false)
            }
            item {
                RefreshColumnItem("Basic", "基本使用") {
//                BasicUsageActivity.navigate(context)
                    RouteUtils.navTo(
                        navController,
                        RouteName.REFRESH_LAYOUT
                    )
                }
            }
            item {
                RefreshColumnItem("Custom", "自定义Header") {
                    RouteUtils.navTo(
                        navController,
                        RouteName.REFRESH_FIXED_HEADER
                    )
                }
            }
            item {
                RefreshColumnItem("LottieRefresh", "自定义Header使用Lottie") {
                    RouteUtils.navTo(
                        navController,
                        RouteName.REFRESH_LOTTIE_HEADER
                    )
                }
            }
            item {
                RefreshColumnItem("FixedBehind", "下拉的时候Header固定在背后") {
//                FixedBehindHeaderActivity.navigate(context)
                }
            }
            item {
                RefreshColumnItem("FixedFront", "下拉的时候Header固定在前面") {
//                FixedFrontHeaderActivity.navigate(context)
                }
            }
            item {
                RefreshColumnItem("FixedContent", "下拉的时候内容不动,Header向下滑动") {
//                FixedContentHeaderActivity.navigate(context)
                }
            }
        }
    }

}
