package com.example.maze

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SettingMenu(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit,
    onToggleSound: () -> Unit,
    onViewHistoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.4f)
                .wrapContentHeight() // Thay fillMaxHeight bằng wrapContentHeight để tự điều chỉnh
                .background(Color(0xAA001F3D), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Thêm khoảng cách đều giữa các nút
        ) {
            // Nút Hỗ Trợ
            TextButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Hỗ Trợ", color = Color.White)
            }

            // Nút Âm Thanh
            TextButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Âm thanh", color = Color.White)
            }

            // Nút Hồ Sơ
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Hồ Sơ", color = Color.White)
            }

            // Nút Xem Lịch Sử
            TextButton(
                onClick = onViewHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Xem Lịch Sử", color = Color.White)
            }

            // Nút Thoát (sửa từ "Đóng" thành "Thoát" và gán đúng callback)
            TextButton(
                onClick = onLogoutClick, // Giả sử "Thoát" liên quan đến logout
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Thoát", color = Color.White)
            }

            // Thêm Spacer để tạo không gian nếu cần
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}