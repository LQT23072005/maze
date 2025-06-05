package com.example.maze

import android.content.Context
import android.widget.Toast
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class GameRecord(
    val level: Int = 0,
    val elapsedTime: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this(0, 0, System.currentTimeMillis())
}

data class LeaderboardEntry(
    val username: String = "",
    val score: Int = 0,
    val bestLevel: Int = 0,
    val bestTime: Int = 0
) {
    constructor() : this("", 0, 0, 0)
}

object History {
    private const val HISTORY_KEY_PREFIX = "game_history_"
    private val gson = Gson()
    private val database = FirebaseDatabase.getInstance().reference

    fun saveGameRecord(context: Context, record: GameRecord, username: String) {
        if (username.isEmpty()) {
            Toast.makeText(context, "Tên người dùng không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val key = "$HISTORY_KEY_PREFIX$username"
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Lưu cục bộ
        val history = getLocalGameHistory(context, username).toMutableList()
        history.add(record)
        val json = gson.toJson(history)
        editor.putString(key, json)
        editor.apply()

        // Lưu lên Firebase
        database.child("gameHistory").child(username).setValue(history)
            .addOnSuccessListener {
                Toast.makeText(context, "Đã lưu lịch sử", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi khi lưu lịch sử: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Cập nhật bảng xếp hạng
        updateLeaderboard(context, username)
    }

    private fun getLocalGameHistory(context: Context, username: String): List<GameRecord> {
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

    fun clearHistory(context: Context, username: String) {
        val key = "$HISTORY_KEY_PREFIX$username"
        val prefs = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()

        database.child("gameHistory").child(username).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Đã xóa lịch sử", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi khi xóa lịch sử: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @Composable
    fun getLeaderboard(context: Context): SnapshotStateList<LeaderboardEntry> {
        val leaderboard = remember { mutableStateListOf<LeaderboardEntry>() }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            database.child("leaderboard").limitToLast(50).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val entries = mutableListOf<LeaderboardEntry>()
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.hasChild("username") && userSnapshot.hasChild("score")) {
                            val entry = userSnapshot.getValue(LeaderboardEntry::class.java)
                            entry?.let { entries.add(it) }
                        }
                    }
                    leaderboard.clear()
                    leaderboard.addAll(entries.sortedByDescending { it.score })
                    errorMessage = null
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage = "Lỗi khi tải bảng xếp hạng: ${error.message}"
                }
            })
        }

        // Hiển thị lỗi trong giao diện nếu có
        errorMessage?.let {
            LaunchedEffect(it) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return leaderboard
    }

    private fun updateLeaderboard(context: Context, username: String) {
        val history = getLocalGameHistory(context, username)
        if (history.isNotEmpty()) {
            val bestRecord = history.maxByOrNull { (it.level * 1000) - it.elapsedTime }
            bestRecord?.let {
                val entry = LeaderboardEntry(
                    username = username,
                    score = (it.level * 1000) - it.elapsedTime,
                    bestLevel = it.level,
                    bestTime = it.elapsedTime
                )
                database.child("leaderboard").child(username).setValue(entry)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Đã cập nhật bảng xếp hạng", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Lỗi khi cập nhật bảng xếp hạng: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

@Composable
fun rememberGameHistory(context: Context, username: String): SnapshotStateList<GameRecord> {
    val history = remember { mutableStateListOf<GameRecord>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        if (username.isNotEmpty()) {
            FirebaseDatabase.getInstance().reference.child("gameHistory").child(username)
                .limitToLast(50)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val records = snapshot.children.mapNotNull { snap ->
                            if (snap.hasChild("level") && snap.hasChild("elapsedTime")) {
                                snap.getValue(GameRecord::class.java)
                            } else {
                                null
                            }
                        }
                        history.clear()
                        history.addAll(records)
                        errorMessage = null
                    }

                    override fun onCancelled(error: DatabaseError) {
                        errorMessage = "Lỗi khi tải lịch sử: ${error.message}"
                    }
                })
        }
    }

    // Hiển thị lỗi trong giao diện nếu có
    errorMessage?.let {
        LaunchedEffect(it) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
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
                    items(gameHistory, key = { it.timestamp }) { record ->
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
    val score = (record.level * 1000) - record.elapsedTime

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
                .padding(8.dp), // Đã sửa lỗi cú pháp
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
    val leaderboard = History.getLeaderboard(context)

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
                    items(leaderboard, key = { it.username }) { entry ->
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
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
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