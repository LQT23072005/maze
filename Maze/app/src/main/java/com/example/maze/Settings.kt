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
    onViewHistoryClick: () -> Unit,
    onViewLeaderboardClick: () -> Unit // Thêm callback cho bảng xếp hạng
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
                .wrapContentHeight()
                .background(Color(0xAA001F3D), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

            // Nút Xếp Hạng
            TextButton(
                onClick = onViewLeaderboardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Xếp Hạng", color = Color.White)
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

            // Nút Thoát
            TextButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Thoát", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}