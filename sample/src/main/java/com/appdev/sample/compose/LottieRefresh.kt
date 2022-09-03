@file:OptIn(ExperimentalFoundationApi::class)

package com.appdev.sample.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appdev.compose.composesmartrefreshlayout.SmartSwipeStateFlag
import com.appdev.compose.composesmartrefreshlayout.rememberSmartSwipeRefreshState
import com.appdev.sample.MainViewModel
import com.appdev.sample.R
import com.appdev.sample.ext.Color
import com.appdev.sample.header.LottieRefreshHeader

@Composable
fun LottieRefreshBox(viewModel: MainViewModel = viewModel()) {
    Box {
        Text(text = "", modifier = Modifier
            .height(200.dp)
            .width(20.dp)
            .background(Color.Yellow))
        LottieRefresh()
    }
}


@Composable
fun LottieRefresh(viewModel: MainViewModel = viewModel()) {

    val scrollState = rememberLazyListState()
    val mainUiState = viewModel.mainUiState.observeAsState()
    val refreshState = rememberSmartSwipeRefreshState()
    SmartRefreshBuilder(
        viewModel,
        scrollState = scrollState,
        refreshState = refreshState,
        headerIndicator = {
            LottieRefreshHeader(flag = refreshState.refreshFlag)
        }) {

        LaunchedEffect(mainUiState.value) {
            mainUiState.value?.let {
                if (!it.isLoading) {
                    refreshState.refreshFlag = when (it.refreshSuccess) {
                        true -> SmartSwipeStateFlag.SUCCESS
                        false -> SmartSwipeStateFlag.ERROR
                        else -> SmartSwipeStateFlag.IDLE
                    }
                    refreshState.loadMoreFlag = when (it.loadMoreSuccess) {
                        true -> SmartSwipeStateFlag.SUCCESS
                        false -> SmartSwipeStateFlag.ERROR
                        else -> SmartSwipeStateFlag.IDLE
                    }
                }
            }
        }

        CompositionLocalProvider(LocalOverscrollConfiguration.provides(null)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState
            ) {
                mainUiState.value?.data?.let {
                    itemsIndexed(it) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .background(if (index % 2 == 0) Color.LightGray else Color.White)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(32.dp),
                                painter = painterResource(id = item.icon),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = item.title, color = R.color.text_color.Color())
                        }
                    }
                }
            }
        }
    }
}