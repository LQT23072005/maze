package com.example.maze

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SoundDialog(
    isSoundEnabled: Boolean,
    onSoundEnabled: () -> Unit,
    onSoundDisabled: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss, enabled = true)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.4f)
                .wrapContentHeight()
                .background(Color(0xAA001F3D), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cài đặt âm thanh",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextButton(
                onClick = {
                    onSoundEnabled()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSoundEnabled) Color(0xFF4CAF50) else Color(0xFF00BCD4),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Bật âm thanh", color = Color.White)
            }

            TextButton(
                onClick = {
                    onSoundDisabled()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (!isSoundEnabled) Color(0xFFF44336) else Color(0xFF00BCD4),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Tắt âm thanh", color = Color.White)
            }
        }
    }
}