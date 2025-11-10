package com.baothanhbin.core.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Body3
import com.baothanhbin.core.theme.TitleLarge3

@Composable
fun LocationDialog(
    onAllowClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Dialog(onDismissRequest = onCancelClick) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .width(400.dp),
            shape = RoundedCornerShape(16.dp), // Bo góc cho Card
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Thêm đổ bóng
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // Padding tổng thể cho nội dung bên trong dialog
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Image(
                    painter = painterResource(id = R.drawable.ic_permission), // Thay bằng icon thực tế của bạn
                    contentDescription = "Location Permission Icon",
                    modifier = Modifier.size(96.dp) // Kích thước icon như trong UI
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tiêu đề
                Text(
                    text = "Allow “PlanQ” to use your location?",
                    style = MaterialTheme.typography.TitleLarge3,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mô tả
                Text(
                    text = "Your location will be used to determine your\nhardiness zone and provide you with relevant\nweather data",
                    style = MaterialTheme.typography.Body3,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Nút "Allow"
                Button(
                    onClick = onAllowClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp), // Chiều cao nút
                    shape = RoundedCornerShape(12.dp), // Bo góc nút
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Màu xanh lá cây như trong UI
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Allow",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nút "No, Thanks"
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, // Nền trắng
                        contentColor = Color.Gray // Màu chữ xám
                    ),
                    // Thêm border để giống UI
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color.LightGray)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp) // Không đổ bóng
                ) {
                    Text(
                        text = "No, Thanks",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationDialogPreview() {
    LocationDialog(
        onAllowClick = {},
        onCancelClick = {}
    )
}