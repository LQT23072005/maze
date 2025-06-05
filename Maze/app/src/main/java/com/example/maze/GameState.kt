package com.example.maze

data class GameState(
    val level: Int = 1,
    val score: Int = 0,
    val maze: Array<Array<Cell>> = generateMazeByLevel(1)

)