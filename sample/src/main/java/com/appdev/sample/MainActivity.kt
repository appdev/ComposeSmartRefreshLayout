@file:OptIn(ExperimentalLayoutApi::class)

package com.appdev.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavigationHost(
                onBackClick = {
                    finish()
                }, navController = rememberNavController()
            )
        }

        lifecycleScope.launch {
            recommendKey.collect {
                Log.d(TAG, "recommendKey  $it")
            }
        }

        for (i in 0..20) {
            lifecycleScope.launch {
            recommendKey.tryEmit(i)

            }
        }

    }

    // 需要推荐数据
    val recommendKey = MutableSharedFlow<Int>(replay = 0)
    private val TAG = "MainActivity"
}
