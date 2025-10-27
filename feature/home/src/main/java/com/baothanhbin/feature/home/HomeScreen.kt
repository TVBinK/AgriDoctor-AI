package com.baothanhbin.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.nio.file.WatchEvent

@Composable
fun HomeRoute() {
    HomeScreen(
    )
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // ✅ căn giữa nội dung trong Box
    ) {
        Text(text = "Home")
    }
}
