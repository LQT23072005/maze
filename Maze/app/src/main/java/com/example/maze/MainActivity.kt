package com.example.maze

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MazeAppGame()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MazeAppGame(username: String = "") {
    var gameState by remember { mutableStateOf(GameState()) }
    var levelSelected by remember { mutableStateOf(false) }
    var purchasedIcons by remember { mutableStateOf(listOf<Int>()) }
    var isPlayingGame by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var isSoundEnabledGlobally by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableStateOf(0) }
    var isGameWon by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    var isChoosingIcon by remember { mutableStateOf(false) }
    var selectedIconId by remember { mutableStateOf(R.drawable.iconone) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Khởi tạo MediaPlayer
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.background_music) ?: run {
            println("Failed to load background music")
            null
        }
    }
    DisposableEffect(mediaPlayer) {
        mediaPlayer?.isLooping = true
        if (isSoundEnabledGlobally) {
            mediaPlayer?.start()
        }
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    // Đồng bộ hóa trạng thái âm thanh
    LaunchedEffect(isSoundEnabledGlobally, mediaPlayer) {
        mediaPlayer?.let {
            if (isSoundEnabledGlobally && !it.isPlaying) {
                it.start()
            } else if (!isSoundEnabledGlobally && it.isPlaying) {
                it.pause()
            }
        }
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (!isGameWon) {
                delay(1000)
                elapsedTime++
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun openVnpayUrlForIcon(iconId: Int) {
        val url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?amount=22000&iconId=$iconId"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.android.chrome")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.trangchu),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = "Settings",
                modifier = Modifier
                    .size(150.dp)
                    .clickable { showSettingsDialog = true }
            )
        }

        if (isChoosingIcon) {
            ChooseIconScreen(
                selectedIconId = selectedIconId,
                onIconSelected = { iconId -> selectedIconId = iconId },
                onBuyIcon = { iconId ->
                    if (!purchasedIcons.contains(iconId)) {
                        openVnpayUrlForIcon(iconId)
                    }
                },
                onCloseClick = { isChoosingIcon = false },
                purchasedIcons = purchasedIcons
            )
        } else if (!levelSelected) {
            MenuScreen(
                onPlayGameClick = { levelSelected = true },
                onChangeIconClick = { isChoosingIcon = true }
            )
        } else if (!isPlayingGame) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(110.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (level in 1..4) {
                        Button(
                            onClick = {
                                gameState = gameState.copy(
                                    level = level,
                                    maze = generateMazeByLevel(level)
                                )
                                isPlayingGame = true
                                elapsedTime = 0
                                isGameWon = false
                                startTimer()
                            },
                            modifier = Modifier
                                .width(90.dp)
                                .height(90.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xAA1E1E2F),
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(2.dp, Color.Cyan),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                text = "Level $level",
                                color = Color.Cyan,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        } else {
            MazeScreen(
                gameState = gameState,
                level = gameState.level,
                onMove = { dx, dy ->
                    if (!isGameWon) {
                        val startCell = gameState.maze.flatten().find { it.type == CellType.START }
                        startCell?.let {
                            val newX = it.x + dx
                            val newY = it.y + dy
                            if (newX in 0 until gameState.maze[0].size && newY in 0 until gameState.maze.size) {
                                val targetCell = gameState.maze[newY][newX]
                                if (targetCell.type != CellType.WALL) {
                                    val updatedMaze = gameState.maze.map { row -> row.map { it.copy() }.toTypedArray() }.toTypedArray()
                                    updatedMaze[it.y][it.x].type = CellType.PATH
                                    updatedMaze[newY][newX].type = CellType.START
                                    gameState = gameState.copy(maze = updatedMaze)
                                    if (targetCell.type == CellType.END) {
                                        isGameWon = true
                                        stopTimer()
                                        showWinDialog = true
                                    }
                                }
                            }
                        }
                    }
                },
                onTimeChange = {},
                selectedIconId = selectedIconId,
                onBackClick = {
                    isPlayingGame = false
                    stopTimer()
                    elapsedTime = 0
                    levelSelected = false
                }
            )
        }

        if (showSettingsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                SettingMenu(
                    onDismiss = { showSettingsDialog = false },
                    onLogoutClick = {
                        println("Support clicked")
                        showSettingsDialog = false
                        showHelpDialog = true
                    },
                    onToggleSound = {
                        showSettingsDialog = false
                        showSoundDialog = true
                    },
                    onViewHistoryClick = {
                        showSettingsDialog = false
                        showHistoryDialog = true
                    },
                    onViewLeaderboardClick = {
                        showSettingsDialog = false
                        showLeaderboardDialog = true
                    }
                )
            }
        }

        if (showWinDialog) {
            HappyDialog(
                onDismissRequest = { showWinDialog = false },
                onPlayAgainClick = {
                    val record = GameRecord(level = gameState.level, elapsedTime = elapsedTime)
                    HistoryManager.saveGameRecord(context, record, username)
                    showWinDialog = false
                    levelSelected = false
                    isPlayingGame = false
                    isGameWon = false
                    elapsedTime = 0
                },
                elapsedTime = elapsedTime,
                level = gameState.level // Truyền level
            )
        }

        if (showHistoryDialog) {
            HistoryDialog(
                onDismissRequest = { showHistoryDialog = false },
                username = username
            )
        }

        if (showSoundDialog) {
            SoundDialog(
                isSoundEnabled = isSoundEnabledGlobally,
                onSoundEnabled = { isSoundEnabledGlobally = true },
                onSoundDisabled = { isSoundEnabledGlobally = false },
                onDismiss = { showSoundDialog = false }
            )
        }

        if (showHelpDialog) {
            HelpDialog(
                onDismiss = { showHelpDialog = false }
            )
        }

        if (showLeaderboardDialog) {
            LeaderboardDialog(
                onDismissRequest = { showLeaderboardDialog = false }
            )
        }
    }
}