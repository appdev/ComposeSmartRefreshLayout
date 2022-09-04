package com.appdev.sample

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.appdev.sample.compose.*
import com.appdev.sample.utils.RouteName

/**
 * 内容 导航
 */
@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    onBackClick: () -> Unit,
    navController: NavHostController
) {
    NavHost(
        navController,
        startDestination = RouteName.HOME,
        modifier = modifier,
    ) {
        //主页面
        composable(
            route = RouteName.HOME
        ) {
            //系统颜色的状态栏
            RefreshLayoutDemo(navController)
            //点击两次返回才关闭app
            BackHandler {
//                TwoBackFinish().execute(context, onFinish)
            }
        }
        composable(
            route = RouteName.REFRESH_FIXED_HEADER
        ) {
            SmartRefresh2()
        }
        composable(
            route = RouteName.REFRESH_LOTTIE_HEADER
        ) {
            LottieRefreshBox()
        }
        composable(
            route = RouteName.REFRESH_LAYOUT
        ) {
            TestRefreshLayout()
        }
        //H5页面
        composable(
            route = "webview?url={url}", arguments = listOf(
                navArgument("url") {
                    defaultValue = "https://www.wanandroid.com/"
                })
        ) { backStackEntry ->

            //系统颜色的状态栏

            BackHandler { navController.navigateUp() }
        }
    }
}



