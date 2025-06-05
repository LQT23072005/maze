package com.example.maze

import java.util.*
import kotlin.math.max

enum class CellType { WALL, PATH, START, END }

data class Cell(val x: Int, val y: Int, var type: CellType)

fun generateMazeBacktracking(width: Int, height: Int): Array<Array<Cell>> {
    val maze = Array(height) { y -> Array(width) { x -> Cell(x, y, CellType.WALL) } }
    val rand = Random()

    fun inBounds(x: Int, y: Int) = x in 1 until width step 2 && y in 1 until height step 2

    fun carvePath(x: Int, y: Int) {
        maze[y][x].type = CellType.PATH

        val directions = listOf(
            0 to -2, // lên
            0 to 2,  // xuống
            -2 to 0, // trái
            2 to 0   // phải
        ).shuffled(rand)

        for ((dx, dy) in directions) {
            val nx = x + dx
            val ny = y + dy
            if (inBounds(nx, ny) && maze[ny][nx].type == CellType.WALL) {
                maze[y + dy / 2][x + dx / 2].type = CellType.PATH
                carvePath(nx, ny)
            }
        }
    }

    val startX = rand.nextInt(width / 2) * 2 + 1
    val startY = rand.nextInt(height / 2) * 2 + 1
    carvePath(startX, startY)

    return maze
}

fun findFarthestPathCells(maze: Array<Array<Cell>>): Pair<Cell, Cell>? {
    val pathCells = mutableListOf<Cell>()
    for (row in maze) {
        for (cell in row) {
            if (cell.type == CellType.PATH) {
                pathCells.add(cell)
            }
        }
    }

    if (pathCells.size < 2) return null

    fun bfs(start: Cell): Pair<Cell, Int> {
        val queue = LinkedList<Cell>()
        val visited = mutableSetOf<Cell>()
        val distances = mutableMapOf<Cell, Int>()
        queue.add(start)
        visited.add(start)
        distances[start] = 0

        var farthestCell = start
        var maxDistance = 0

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            val (x, y) = current

            listOf(x - 1 to y, x + 1 to y, x to y - 1, x to y + 1)
                .filter { (nx, ny) ->
                    nx in 0 until maze[0].size && ny in 0 until maze.size &&
                            maze[ny][nx].type == CellType.PATH && maze[ny][nx] !in visited
                }
                .forEach { (nx, ny) ->
                    val neighbor = maze[ny][nx]
                    queue.add(neighbor)
                    visited.add(neighbor)
                    distances[neighbor] = distances[current]!! + 1
                    if (distances[neighbor]!! > maxDistance) {
                        maxDistance = distances[neighbor]!!
                        farthestCell = neighbor
                    }
                }
        }

        return farthestCell to maxDistance
    }

    val startCell = pathCells.random()
    val (endCell, _) = bfs(startCell)
    val (finalStartCell, _) = bfs(endCell)

    return Pair(finalStartCell, endCell)
}

fun generateMazeByLevel(level: Int): Array<Array<Cell>> {
    val baseSize = 13 + level * 6 // 19, 25, 31, 37 cho level 1-4
    val maze = generateMazeBacktracking(baseSize, baseSize)

    val farthestCells = findFarthestPathCells(maze)
    farthestCells?.let { (startCell, endCell) ->
        maze[startCell.y][startCell.x].type = CellType.START
        maze[endCell.y][endCell.x].type = CellType.END
    }

    return maze
}