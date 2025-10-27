package com.baothanhbin.agridoctorai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.baothanhbin.agridoctorai.navigation.rememberAppState
import com.baothanhbin.agridoctorai.ui.MainApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberAppState() // Tạo navigation state
            MainApp(
                modifier = Modifier.fillMaxSize(),
                appState = appState //Truyền state xuống
            )
        }
    }
}