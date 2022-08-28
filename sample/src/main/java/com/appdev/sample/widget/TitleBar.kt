package com.appdev.sample.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TitleBar(title: String, showArrow: Boolean = true, callback: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(Color(0xff2299ee))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xff33aaff))
                .padding(16.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showArrow) {
                IconButton(
                    onClick = callback,
                    Modifier
                        .size(24.dp)
                        .padding(0.dp, 0.dp, 16.dp, 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "")
                }

            }
            Text(text = title, color = Color.White, style = MaterialTheme.typography.h6)
        }
    }
}