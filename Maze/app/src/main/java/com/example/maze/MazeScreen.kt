package com.example.maze

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged // <-- Đã thêm import này
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun CellView(cell: Cell, cellSize: Dp, isEnd: Boolean, selectedIconId: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val baseModifier = Modifier
        .size(cellSize)
        .clip(RoundedCornerShape(2.dp))
        .then(if (cell.type == CellType.WALL) Modifier.shadow(2.dp) else Modifier)
        .then(if (isEnd) Modifier.scale(scale) else Modifier)

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        when (cell.type) {
            CellType.WALL, CellType.PATH -> Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = if (cell.type == CellType.WALL) Color(0xFF00FFFF) else Color.Black)
                    .then(
                        if (cell.type != CellType.PATH) Modifier.border(0.5.dp, Color(0xFFB3B3B3), RoundedCornerShape(2.dp))
                        else Modifier
                    )
            )

            CellType.START -> {
                Image(
                    painter = painterResource(id = selectedIconId),
                    contentDescription = "Player Icon",
                    modifier = Modifier
                        .size(cellSize)
                        .border(0.dp, Color.Transparent),
                    contentScale = ContentScale.Fit
                )
            }

            CellType.END -> {
                Image(
                    painter = painterResource(id = R.drawable.goal), // Đổi thành tên hình bạn có
                    contentDescription = "Goal Icon",
                    modifier = Modifier
                        .size(cellSize * 0.9f)
                        .border(0.dp, Color.Transparent),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}


@Composable
fun MazeGrid(
    modifier: Modifier = Modifier,
    maze: Array<Array<Cell>>,
    cellSize: Dp,
    selectedIconId: Int
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        maze.forEach { row ->
            Row {
                row.forEach { cell ->
                    CellView(
                        cell = cell,
                        cellSize = cellSize,
                        isEnd = cell.type == CellType.END,
                        selectedIconId = selectedIconId
                    )
                }
            }
        }
    }
}

@Composable
fun ImageButton(imageResId: Int, size: Dp, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    Button(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = Modifier
            .size(size)
            .shadow(8.dp, CircleShape)
            .scale(if (isPressed) 0.95f else 1f),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun GameControls(
    buttonSize: Dp,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = buttonSize / 2

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        ImageButton(imageResId = R.drawable.up, size = buttonSize) { onMove(0, -1) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageButton(imageResId = R.drawable.left, size = buttonSize) { onMove(-1, 0) }
            ImageButton(imageResId = R.drawable.right, size = buttonSize) { onMove(1, 0) }
        }
        ImageButton(imageResId = R.drawable.down, size = buttonSize) { onMove(0, 2) }
    }
}

@Composable
fun MazeScreen(
    gameState: GameState,
    level: Int,
    onMove: (Int, Int) -> Unit,
    onTimeChange: (Int) -> Unit,
    selectedIconId: Int,
    onBackClick: () -> Unit
) {
    val maze = gameState.maze
    val mazeWidth = maze[0].size
    val mazeHeight = maze.size
    val density = LocalDensity.current
    // screenWidthDp và screenHeightDp không còn được dùng trực tiếp để tính cellSize
    // val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    // val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    // Kích thước cố định cho nút điều khiển và nút Back
    val controlButtonSize = 70.dp
    val backButtonSize = 80.dp

    val horizontalPadding = 16.dp
    val verticalPadding = 16.dp

    var calculatedCellSize by remember { mutableStateOf(0.dp) }
    var boxWidth by remember { mutableStateOf(0.dp) } // <-- Đã sửa ở đây
    var boxHeight by remember { mutableStateOf(0.dp) } // <-- Đã sửa ở đây

    var showWinDialog by remember { mutableStateOf(false) }
    val onTimeChangeUpdated by rememberUpdatedState(onTimeChange)
    var elapsedTime by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning && isActive) {
            delay(1000)
            elapsedTime++
            onTimeChangeUpdated(elapsedTime)
        }
    }

    LaunchedEffect(gameState.maze, elapsedTime) {
        val startCell = maze.flatten().find { it.type == CellType.START }
        val endCell = maze.flatten().find { it.type == CellType.END }

        if (startCell != null && endCell != null) {
            if (startCell.x == endCell.x && startCell.y == endCell.y) {
                showWinDialog = true
                isRunning = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.maze_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            // Phần Header: Level, Time, Nút Back
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Level $level",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Time: ${elapsedTime}s",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
                ImageButton(
                    imageResId = R.drawable.ic_back,
                    size = backButtonSize,
                    onClick = onBackClick
                )
            }

            // Phần chính: Controls và Maze
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                GameControls(
                    buttonSize = controlButtonSize,
                    onMove = onMove,
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(0.5f)
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.25f)
                        .aspectRatio(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { size ->
                                val width = with(density) { size.width.toDp() }
                                val height = with(density) { size.height.toDp() }
                                boxWidth = width // <-- Đã sửa ở đây
                                boxHeight = height // <-- Đã sửa ở đây
                                // Tính cellSize dựa trên kích thước thực tế của Box
                                // và số lượng hàng/cột của mê cung
                                val maxCellSizeForWidth = if (mazeWidth > 0) width / mazeWidth.toFloat() else 0.dp // <-- Đã sửa ở đây
                                val maxCellSizeForHeight = if (mazeHeight > 0) height / mazeHeight.toFloat() else 0.dp // <-- Đã sửa ở đây
                                calculatedCellSize = minOf(maxCellSizeForWidth, maxCellSizeForHeight)
                            }
                    ) {
                        if (calculatedCellSize > 0.dp) {
                            MazeGrid(
                                modifier = Modifier.align(Alignment.Center),
                                maze = maze,
                                cellSize = calculatedCellSize,
                                selectedIconId = selectedIconId
                            )
                        }
                    }
                }
            }
        }}}