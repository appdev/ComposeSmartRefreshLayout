package com.appdev.sample

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

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
        startDestination = "main",
        modifier = modifier,
    ) {
        //主页面
        composable(
            route = "main"
        ) {
            //系统颜色的状态栏
            swipeRefresh()

            //点击两次返回才关闭app
            BackHandler {
//                TwoBackFinish().execute(context, onFinish)
            }
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

/**
 * 页面跳转关键类
 */
enum class KeyNavigationRoute(
    val route: String
) {
    //主页面
    MAIN("main"),

    //H5
    WEBVIEW("webview"),

}










