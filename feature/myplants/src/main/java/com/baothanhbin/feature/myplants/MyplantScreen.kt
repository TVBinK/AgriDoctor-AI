package com.baothanhbin.feature.myplants

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.baothanhbin.agridoctorai.resources.R

@Composable
fun MyplantRoute() {
    MyplantScreen(
    )
}

@Composable
fun MyplantScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // ✅ căn giữa nội dung trong Box
    ) {
        Text(text = "My Plants")
    }
}