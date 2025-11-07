package com.baothanhbin.agridoctorai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.baothanhbin.agridoctorai.security.SecurityCheckResult

/**
 * Dialog cảnh báo bảo mật khi phát hiện app bị chỉnh sửa
 * - Nền mờ che toàn màn hình
 * - Không thể dismiss bằng cách click bên ngoài hoặc back button
 * - Chỉ có thể thoát app
 */
@Composable
fun SecurityWarningDialog(
    result: SecurityCheckResult,
    onExit: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Không cho phép dismiss */ },
        properties = DialogProperties(
            dismissOnBackPress = false,      // Không đóng khi nhấn back
            dismissOnClickOutside = false,   // Không đóng khi click bên ngoài
            usePlatformDefaultWidth = false  // Chiếm toàn bộ width
        )
    ) {
        // Nền mờ đen che toàn màn hình
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)), // Nền đen 85% opacity
            contentAlignment = Alignment.Center
        ) {
            // Card cảnh báo
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // 90% chiều rộng màn hình
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon cảnh báo
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Security Warning",
                        modifier = Modifier.size(72.dp),
                        tint = Color(0xFFFF6B6B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tiêu đề
                    Text(
                        text = "CẢNH BÁO BẢO MẬT",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nội dung cảnh báo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3F3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = result.getUserMessage(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Start,
                            lineHeight = 22.sp,
                            color = Color(0xFF2C2C2C)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nút thoát ứng dụng (duy nhất)
                    Button(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B6B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Thoát ứng dụng",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text thông báo
                    Text(
                        text = "Vui lòng tải ứng dụng chính thức",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

