package com.appdev.sample.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appdev.compose.composesmartrefreshlayout.SwipeRefreshStyle
import com.appdev.sample.MainViewModel
import com.appdev.sample.R
import com.appdev.sample.RefreshLayout
import com.appdev.sample.ext.Color
import com.appdev.sample.rememberRefreshLayoutState
import com.appdev.sample.widget.TitleBar
import kotlinx.coroutines.delay

@Composable
fun fixedFrontHeaderSample(viewModel: MainViewModel = viewModel()) {
    var refreshing by remember { mutableStateOf(false) }
    val mainUiState = viewModel.mainUiState.observeAsState()
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(2000)
            refreshing = false
        }
    }
    val refreshState = rememberRefreshLayoutState(
        enableLoadMore = true,
        enableTwoLevel = true
    )

    Column() {
        TitleBar(title = "FixedFront Header", true) {
//            activity?.finish()
        }
        RefreshLayout(
            state = refreshState,
            onRefresh = {
                viewModel.fillData(true)
                delay(1000)
            },
            onLoadMore = {
                viewModel.fillData(false)
                delay(1000)
            }, refreshStyle = SwipeRefreshStyle.FixedContent
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(0.dp, 16.dp, 0.dp, 0.dp)
                    .background(Color.White)
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