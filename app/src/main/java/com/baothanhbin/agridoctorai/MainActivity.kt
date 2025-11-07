package com.baothanhbin.agridoctorai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.baothanhbin.agridoctorai.navigation.rememberAppState
import com.baothanhbin.agridoctorai.security.SecurityManager
import com.baothanhbin.agridoctorai.ui.MainApp
import com.baothanhbin.agridoctorai.ui.SecurityWarningDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var securityCheckResult by remember { mutableStateOf<com.baothanhbin.agridoctorai.security.SecurityCheckResult?>(null) }
            var showSecurityWarning by remember { mutableStateOf(false) }

            // Thực hiện kiểm tra bảo mật khi app khởi động
            LaunchedEffect(Unit) {
                val result = SecurityManager.performSecurityCheck(this@MainActivity)
                securityCheckResult = result
                
                // Hiển thị cảnh báo nếu có vấn đề nghiêm trọng
                if (!result.isSecure && result.hasCriticalIssues()) {
                    showSecurityWarning = true
                }
            }

            // Hiển thị dialog cảnh báo bảo mật
            if (showSecurityWarning && securityCheckResult != null) {
                SecurityWarningDialog(
                    result = securityCheckResult!!,
                    onExit = {
                        // Đóng ứng dụng
                        finish()
                    }
                )
            }

            val appState = rememberAppState() // Tạo navigation state
            MainApp(
                modifier = Modifier.fillMaxSize(),
                appState = appState //Truyền state xuống
            )
        }
    }
}