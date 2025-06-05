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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpDialog(
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
                .fillMaxWidth(0.6f)
                .wrapContentHeight()
                .background(Color(0xAA001F3D), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hướng Dẫn Chơi Mê Cung",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = """
                    1. **Mục tiêu**: Di chuyển từ điểm BẮT ĐẦU (START) đến điểm KẾT THÚC (END) trong mê cung.
                    2. **Cách di chuyển**: Sử dụng các nút điều khiển (Lên, Xuống, Trái, Phải) để di chuyển biểu tượng người chơi. Chỉ di chuyển được trên các ô đường (PATH), không đi qua tường (WALL).
                    3. **Cấp độ**: Chọn một trong 4 cấp độ, với mê cung ngày càng lớn và phức tạp.
                    4. **Thời gian**: Hoàn thành mê cung nhanh nhất có thể! Thời gian sẽ được lưu vào lịch sử.
                    5. **Mẹo**:
                       - Lập kế hoạch đường đi để tránh bị kẹt.
                       - Sử dụng nút Back để quay lại chọn cấp độ.
                       - Thay đổi biểu tượng trong menu 'Đổi Icon'.
                    6. **Âm thanh**: Bật/tắt nhạc nền trong mục 'Âm thanh' để có trải nghiệm tốt hơn.
                """.trimIndent(),
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00BCD4), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color(0xFF80DEEA), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text("Đóng", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}