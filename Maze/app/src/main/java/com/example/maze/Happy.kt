// Happy.kt
package com.example.maze

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun HappyDialog(
    onDismissRequest: () -> Unit,
    onPlayAgainClick: () -> Unit,
    elapsedTime: Int // Thêm tham số thời gian
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(Color(0xAA1E1E2F), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hình ảnh chúc mừng
            Image(
                painter = painterResource(id = R.drawable.win_image), // Thay thế bằng ID ảnh chúc mừng của bạn
                contentDescription = "Chúc mừng",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            // Tiêu đề chúc mừng
            Text(
                text = "🎉 Hoàn thành!",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Cyan,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Thông báo thời gian
            Text(
                text = "Bạn đã giải mê cung trong",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "${elapsedTime} giây",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Yellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Nút chơi lại
            Button(
                onClick = onPlayAgainClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Chơi lại", fontSize = 18.sp)
            }

            // Nút đóng (tùy chọn)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Đóng", fontSize = 18.sp)
            }
        }
    }
}