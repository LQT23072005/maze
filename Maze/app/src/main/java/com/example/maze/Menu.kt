package com.example.maze

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(
    onPlayGameClick: () -> Unit,
    onChangeIconClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.trangchu),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Box Chơi Game
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xAA1E1E2F), shape = RoundedCornerShape(12.dp))
                        .clickable(onClick = onPlayGameClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Chơi Game",
                        color = Color.Cyan,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Khoảng cách giữa hai box
                Spacer(modifier = Modifier.width(140.dp))

                // Box Thay Đổi Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xAA332255), shape = RoundedCornerShape(12.dp))
                        .clickable(onClick = onChangeIconClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Đổi Icon",
                        color = Color.Yellow,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}