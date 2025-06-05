package com.example.maze

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class GameRecord(
    val level: Int,
    val elapsedTime: Int, // Thời gian chơi (giây)
    val timestamp: Long = System.currentTimeMillis() // Thời gian hoàn thành
)

data class LeaderboardEntry(
    val username: String,
    val score: Int,
    val bestLevel: Int,
    val bestTime: Int
)

object HistoryManager {
    private const val HISTORY_KEY_PREFIX = "game_history_"
    private val gson = Gson()

    // Lưu lịch sử vào SharedPreferences
    fun saveGameRecord(context: Context, record: GameRecord, username: String) {
        val key = "$HISTORY_KEY_PREFIX$username"
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val history = getGameHistory(context, username).toMutableList()
        history.add(record)

        val json = gson.toJson(history)
        editor.putString(key, json)
        editor.apply()
    }

    // Lấy danh sách lịch sử
    fun getGameHistory(context: Context, username: String): List<GameRecord> {
        val key = "$HISTORY_KEY_PREFIX$username"
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<GameRecord>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Xóa lịch sử
    fun clearHistory(context: Context, username: String) {
        val key = "$HISTORY_KEY_PREFIX$username"
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()
    }

    // Lấy danh sách người chơi từ Firebase và tính điểm
    @Composable
    fun getLeaderboard(context: Context): List<LeaderboardEntry> {
        val leaderboard = remember { mutableStateListOf<LeaderboardEntry>() }
        val database = FirebaseDatabase.getInstance().reference.child("users")

        LaunchedEffect(Unit) {
            database.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val usernames = snapshot.children.map { it.key.toString() }
                    val entries = mutableListOf<LeaderboardEntry>()

                    usernames.forEach { username ->
                        val history = getGameHistory(context, username)
                        if (history.isNotEmpty()) {
                            // Tính điểm: (level * 1000) - elapsedTime
                            val bestRecord = history.maxByOrNull { (it.level * 1000) - it.elapsedTime }
                            bestRecord?.let {
                                entries.add(
                                    LeaderboardEntry(
                                        username = username,
                                        score = (it.level * 1000) - it.elapsedTime,
                                        bestLevel = it.level,
                                        bestTime = it.elapsedTime
                                    )
                                )
                            }
                        }
                    }

                    // Sắp xếp theo điểm giảm dần
                    leaderboard.clear()
                    leaderboard.addAll(entries.sortedByDescending { it.score })
                }
            }
        }

        return leaderboard
    }
}

@Composable
fun rememberGameHistory(context: Context, username: String): SnapshotStateList<GameRecord> {
    val history = remember { mutableStateListOf<GameRecord>() }
    LaunchedEffect(Unit) {
        history.addAll(HistoryManager.getGameHistory(context, username))
    }
    return history
}

@Composable
fun HistoryDialog(
    onDismissRequest: () -> Unit,
    username: String
) {
    val context = LocalContext.current
    val gameHistory = rememberGameHistory(context, username)

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(Color(0xAA1E1E2F), RoundedCornerShape(16.dp))
                .padding(16.dp)
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lịch Sử Chơi - $username",
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
    val score = (record.level * 1000) - record.elapsedTime // Tính điểm

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
                    text = "Điểm: $score",
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

@Composable
fun LeaderboardDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val leaderboard = HistoryManager.getLeaderboard(context)

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(Color(0xAA1E1E2F), RoundedCornerShape(16.dp))
                .padding(16.dp)
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bảng Xếp Hạng",
                fontSize = 24.sp,
                color = Color.Cyan,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (leaderboard.isEmpty()) {
                Text(
                    text = "Chưa có dữ liệu xếp hạng!",
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
                    items(leaderboard) { entry ->
                        LeaderboardItem(
                            rank = leaderboard.indexOf(entry) + 1,
                            entry = entry
                        )
                    }
                }
            }

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
fun LeaderboardItem(
    rank: Int,
    entry: LeaderboardEntry
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$rank.",
                    fontSize = 16.sp,
                    color = when (rank) {
                        1 -> Color(0xFFFFD700) // Vàng cho hạng 1
                        2 -> Color(0xFFC0C0C0) // Bạc cho hạng 2
                        3 -> Color(0xFFCD7F32) // Đồng cho hạng 3
                        else -> Color.White
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = entry.username,
                        fontSize = 16.sp,
                        color = Color.Cyan
                    )
                    Text(
                        text = "Điểm: ${entry.score}",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Cấp độ ${entry.bestLevel} - ${entry.bestTime}s",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}