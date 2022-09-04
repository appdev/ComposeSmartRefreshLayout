package com.appdev.compose.composesmartrefreshlayout

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "RefreshView"

@Composable
fun TestRefresh(state: SmartSwipeRefreshState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White), contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = when (state.refreshFlag) {
                    SmartSwipeStateFlag.REFRESHING -> "刷新中..."
                    SmartSwipeStateFlag.SUCCESS -> "刷新成功"
                    SmartSwipeStateFlag.ERROR -> "刷新失败"
                    SmartSwipeStateFlag.IDLE, SmartSwipeStateFlag.TIPS_DOWN -> "下拉刷新"
                    SmartSwipeStateFlag.TIPS_RELEASE -> "释放刷新"
                }, fontSize = 18.sp
            )
        }
    }
}

@Composable
fun MyRefreshHeader(state: SmartSwipeRefreshState) {
    var lastRecordTime by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
    ) {
        val refreshAnimate by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing))
        )
        val transitionState = remember { MutableTransitionState(0) }
        val transition = updateTransition(transitionState, label = "arrowTransition")
        val arrowDegrees by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 500) }, label = "arrowDegrees"
        ) {
            if (it == 0) 0f else 180f
        }
        transitionState.targetState = if (state.refreshFlag == SmartSwipeStateFlag.TIPS_RELEASE) 1 else 0
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.rotate(if (state.refreshFlag == SmartSwipeStateFlag.REFRESHING) refreshAnimate else arrowDegrees),
                imageVector = when (state.refreshFlag) {
                    SmartSwipeStateFlag.IDLE -> Icons.Default.KeyboardArrowDown
                    SmartSwipeStateFlag.REFRESHING -> Icons.Default.Refresh
                    SmartSwipeStateFlag.SUCCESS -> {
                        lastRecordTime = System.currentTimeMillis()
                        Icons.Default.Done
                    }
                    SmartSwipeStateFlag.ERROR -> {
                        lastRecordTime = System.currentTimeMillis()
                        Icons.Default.Warning
                    }
                    SmartSwipeStateFlag.TIPS_DOWN -> Icons.Default.KeyboardArrowDown
                    SmartSwipeStateFlag.TIPS_RELEASE -> Icons.Default.KeyboardArrowDown
                },
                contentDescription = null
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = when (state.refreshFlag) {
                        SmartSwipeStateFlag.REFRESHING -> "刷新中..."
                        SmartSwipeStateFlag.SUCCESS -> "刷新成功"
                        SmartSwipeStateFlag.ERROR -> "刷新失败"
                        SmartSwipeStateFlag.IDLE, SmartSwipeStateFlag.TIPS_DOWN -> "下拉可以刷新"
                        SmartSwipeStateFlag.TIPS_RELEASE -> "释放立即刷新"
                    }, fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "上次刷新：${
                        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(
                            lastRecordTime
                        )
                    }", fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MyRefreshFooter(flag: SmartSwipeStateFlag, isNeedTimestamp: Boolean = true) {
    var lastRecordTime by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
    ) {
        val refreshAnimate by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing))
        )
        val transitionState = remember { MutableTransitionState(0) }
        val transition = updateTransition(transitionState, label = "arrowTransition")
        val arrowDegrees by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 500) }, label = "arrowDegrees"
        ) {
            if (it == 0) 0f else 180f
        }
        transitionState.targetState = if (flag == SmartSwipeStateFlag.TIPS_RELEASE) 1 else 0
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.rotate(if (flag == SmartSwipeStateFlag.REFRESHING) refreshAnimate else arrowDegrees),
                imageVector = when (flag) {
                    SmartSwipeStateFlag.IDLE -> Icons.Default.KeyboardArrowUp
                    SmartSwipeStateFlag.REFRESHING -> Icons.Default.Refresh
                    SmartSwipeStateFlag.SUCCESS -> {
                        lastRecordTime = System.currentTimeMillis()
                        Icons.Default.Done
                    }
                    SmartSwipeStateFlag.ERROR -> {
                        lastRecordTime = System.currentTimeMillis()
                        Icons.Default.Warning
                    }
                    SmartSwipeStateFlag.TIPS_DOWN -> Icons.Default.KeyboardArrowUp
                    SmartSwipeStateFlag.TIPS_RELEASE -> Icons.Default.KeyboardArrowUp
                },
                contentDescription = null
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = when (flag) {
                        SmartSwipeStateFlag.REFRESHING -> "正在加载..."
                        SmartSwipeStateFlag.SUCCESS -> "加载成功"
                        SmartSwipeStateFlag.ERROR -> "加载失败"
                        SmartSwipeStateFlag.IDLE, SmartSwipeStateFlag.TIPS_DOWN -> "上拉加载更多"
                        SmartSwipeStateFlag.TIPS_RELEASE -> "释放立即加载"
                    }, fontSize = 18.sp
                )
                if (isNeedTimestamp) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "上次加载：${
                            SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(
                                lastRecordTime
                            )
                        }", fontSize = 14.sp
                    )
                }
            }
        }
    }
}
