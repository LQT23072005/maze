package com.example.maze

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.math.BigInteger

fun hmacSHA512(key: String, data: String): String {
    val hmacSha512 = Mac.getInstance("HmacSHA512")
    val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA512")
    hmacSha512.init(secretKey)
    val hash = hmacSha512.doFinal(data.toByteArray())
    return String.format("%0128x", BigInteger(1, hash)).lowercase()
}

class MainActivity : AppCompatActivity() {
    private var purchasedIcons: MutableList<Int> = mutableListOf()
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = intent.getStringExtra("username") ?: ""
        val sharedPreferences = getSharedPreferences("MazeGamePrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("currentUsername", username).apply()
        loadPurchasedIcons()
        handlePaymentCallback(intent)
        setContent {
            MazeAppGame(username)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePaymentCallback(intent)
    }

    private fun loadPurchasedIcons() {
        val sharedPreferences = getSharedPreferences("MazeGamePrefs", MODE_PRIVATE)
        val purchasedIconsJson = sharedPreferences.getString("purchasedIcons_$username", "[]")
        val type = object : TypeToken<List<Int>>() {}.type
        purchasedIcons = Gson().fromJson(purchasedIconsJson, type) ?: mutableListOf()
    }

    private fun handlePaymentCallback(intent: Intent) {
        intent.data?.let { uri ->
            if (uri.scheme == "mazeapp" && uri.host == "payment-callback") {
                val vnp_ResponseCode = uri.getQueryParameter("vnp_ResponseCode")
                val iconId = uri.getQueryParameter("iconId")?.toIntOrNull() ?: 0
                if (vnp_ResponseCode == "00") {
                    val sharedPreferences = getSharedPreferences("MazeGamePrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    if (!purchasedIcons.contains(iconId)) {
                        purchasedIcons.add(iconId)
                        editor.putString("purchasedIcons_$username", Gson().toJson(purchasedIcons))
                        editor.apply()
                    }
                } else {
                    android.widget.Toast.makeText(this, "Thanh toán thất bại: $vnp_ResponseCode", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MazeAppGame(username: String) {
    val context = LocalContext.current
    val purchasedIconsState = remember {
        val sharedPreferences = context.getSharedPreferences("MazeGamePrefs", Context.MODE_PRIVATE)
        val purchasedIconsJson = sharedPreferences.getString("purchasedIcons_$username", "[]")
        val type = object : TypeToken<List<Int>>() {}.type
        mutableStateOf(Gson().fromJson<List<Int>>(purchasedIconsJson, type) ?: emptyList())
    }
    var gameState by remember { mutableStateOf(GameState()) }
    var levelSelected by remember { mutableStateOf(false) }
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

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.background_music)?.apply {
            isLooping = true
        } ?: run {
            android.widget.Toast.makeText(context, "Không thể tải nhạc nền", android.widget.Toast.LENGTH_SHORT).show()
            null
        }
    }
    DisposableEffect(mediaPlayer) {
        if (isSoundEnabledGlobally) {
            mediaPlayer?.start()
        }
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

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

    fun generateVnpayPaymentUrl(context: Context, iconId: Int): String {
        val vnp_TmnCode = "6E03FFCJ"
        val vnp_HashSecret = "8NIZ0VS17CGLAY964LR1YPF80B5XZXGM"
        val vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
        val vnp_ReturnUrl = "mazeapp://payment-callback?iconId=$iconId"
        val vnp_Amount = 22000 * 100
        val vnp_OrderInfo = "Mua icon $iconId"
        val vnp_CreateDate = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val vnp_IpAddr = "127.0.0.1"

        val vnp_Params = hashMapOf(
            "vnp_Amount" to vnp_Amount.toString(),
            "vnp_Command" to "pay",
            "vnp_CreateDate" to vnp_CreateDate,
            "vnp_CurrCode" to "VND",
            "vnp_IpAddr" to vnp_IpAddr,
            "vnp_Locale" to "vn",
            "vnp_OrderInfo" to vnp_OrderInfo,
            "vnp_OrderType" to "other",
            "vnp_ReturnUrl" to vnp_ReturnUrl,
            "vnp_TmnCode" to vnp_TmnCode,
            "vnp_TxnRef" to "TXN${System.currentTimeMillis()}",
            "vnp_Version" to "2.1.0"
        )

        val sortedParams = vnp_Params.toList().sortedBy { it.first }
            .joinToString("&") { "${it.first}=${java.net.URLEncoder.encode(it.second, "UTF-8")}" }
        val secureHash = hmacSHA512(vnp_HashSecret, sortedParams)
        return "$vnp_Url?$sortedParams&vnp_SecureHash=$secureHash"
    }

    fun openVnpayUrlForIcon(iconId: Int) {
        val url = generateVnpayPaymentUrl(context, iconId)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Không thể mở URL thanh toán", android.widget.Toast.LENGTH_SHORT).show()
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
                    if (!purchasedIconsState.value.contains(iconId)) {
                        openVnpayUrlForIcon(iconId)
                    }
                },
                onCloseClick = { isChoosingIcon = false },
                purchasedIcons = purchasedIconsState.value
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
                    History.saveGameRecord(context, record, username)
                    showWinDialog = false
                    levelSelected = false
                    isPlayingGame = false
                    isGameWon = false
                    elapsedTime = 0
                },
                elapsedTime = elapsedTime,
                level = gameState.level
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