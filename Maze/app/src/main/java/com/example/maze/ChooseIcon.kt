package com.example.maze

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ChooseIconScreen(
    selectedIconId: Int,
    onIconSelected: (Int) -> Unit,
    onBuyIcon: (Int) -> Unit,
    onCloseClick: () -> Unit,
    purchasedIcons: List<Int>
) {
    val iconList = listOf(
        R.drawable.iconone,
        R.drawable.icontwo,
        R.drawable.iconthree,
        R.drawable.iconfour,
        R.drawable.iconfive,
        R.drawable.iconsix
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thay thế Text "Chọn Nhân Vật" bằng nút "Đóng" ở trên cùng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End // Đặt nút "Đóng" ở cuối hàng (bên phải)
        ) {
            Button(onClick = onCloseClick) {
                Text("Đóng")
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(iconList) { iconId ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                if (iconId == selectedIconId) Color.Cyan else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable {
                                val isFree = iconId == R.drawable.iconone || iconId == R.drawable.icontwo
                                if (isFree || purchasedIcons.contains(iconId)) {
                                    onIconSelected(iconId)
                                } else {
                                    onBuyIcon(iconId)
                                }
                            }
                    ) {
                        Image(
                            painter = painterResource(id = iconId),
                            contentDescription = "Icon",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Hiển thị giá hoặc "Đã mua"
                    if (iconId != R.drawable.iconone && iconId != R.drawable.icontwo) {
                        if (purchasedIcons.contains(iconId)) {
                            Text("Đã mua", color = Color.Green)
                        } else {
                            Text("22.000đ", color = Color.Red)
                        }
                    } else {
                        Text("Miễn phí", color = Color.Gray)
                    }
                }
            }
        }

        // Loại bỏ nút "Đóng" ở cuối vì đã di chuyển lên trên
    }
}