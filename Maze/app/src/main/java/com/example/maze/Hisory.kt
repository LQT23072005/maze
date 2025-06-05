package com.example.maze

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList // Import SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class GameRecord(
    val level: Int,
    val elapsedTime: Int, // Thời gian chơi (giây)
    val timestamp: Long = System.currentTimeMillis() // Thời gian hoàn thành
)

object HistoryManager {
    private const val HISTORY_KEY = "game_history"
    private val gson = Gson()

    // Lưu lịch sử vào SharedPreferences
    fun saveGameRecord(context: Context, record: GameRecord) {
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Lấy danh sách hiện tại
        val history = getGameHistory(context).toMutableList()
        history.add(record)

        // Lưu lại danh sách mới
        val json = gson.toJson(history)
        editor.putString(HISTORY_KEY, json)
        editor.apply()
    }

    // Lấy danh sách lịch sử
    fun getGameHistory(context: Context): List<GameRecord> {
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val json = prefs.getString(HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<GameRecord>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Xóa lịch sử (nếu cần)
    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(HISTORY_KEY)
        editor.apply()
    }
}

@Composable
fun rememberGameHistory(context: Context): SnapshotStateList<GameRecord> {
    val history = remember { mutableStateListOf<GameRecord>() }
    LaunchedEffect(Unit) {
        history.addAll(HistoryManager.getGameHistory(context))
    }
    return history
}

@Composable
fun HistoryDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val gameHistory = rememberGameHistory(context)

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(Color(0xAA1E1E2F), RoundedCornerShape(16.dp))
                .padding(16.dp)
                .fillMaxWidth()
                .heightIn(max = 400.dp), // Giới hạn chiều cao tối đa
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tiêu đề
            Text(
                text = "Lịch Sử Chơi",
                fontSize = 24.sp,
                color = Color.Cyan,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (gameHistory.isEmpty()) {
                Text(
                    text = "Chưa có lịch sử nào!",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(gameHistory) { record ->
                        GameRecordItem(record)
                    }
                }
            }

            // Nút đóng
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Đóng", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun GameRecordItem(record: GameRecord) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = dateFormat.format(Date(record.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E3F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Cấp độ ${record.level}",
                    fontSize = 16.sp,
                    color = Color.Cyan
                )
                Text(
                    text = "Thời gian: ${record.elapsedTime}s",
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}