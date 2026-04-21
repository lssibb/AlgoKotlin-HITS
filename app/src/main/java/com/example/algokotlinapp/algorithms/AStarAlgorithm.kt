package com.example.algokotlinapp.algorithms

import com.example.algokotlinapp.model.Node

fun findNearestWalkable(grid: Array<IntArray>, targetRow: Int, targetCol: Int): Pair<Int, Int>? {
    val rows = grid.size
    val cols = grid[0].size
    for (dist in 0..maxOf(rows, cols)) {
        for (dr in -dist..dist) {
            for (dc in -dist..dist) {
                if (kotlin.math.abs(dr) + kotlin.math.abs(dc) != dist) continue
                val r = targetRow + dr
                val c = targetCol + dc
                if (r in 0 until rows && c in 0 until cols && r in grid.indices && c < grid[r].size && grid[r][c] != 0) {
                    return r to c
                }
            }
        }
    }
    return null
}

fun astar(
    grid: Array<IntArray>,
    startX: Int, startY: Int,
    endX: Int, endY: Int
): List<Pair<Int, Int>>? {

    val rows = grid.size
    val cols = grid[0].size

    val open = mutableListOf(Node(startX, startY))
    val closed = mutableSetOf<Pair<Int, Int>>()

    val directions = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

    while (open.isNotEmpty()) {
        val current = open.minByOrNull { it.f }!!
        open.remove(current)

        if (current.x == endX && current.y == endY) {
            val path = mutableListOf<Pair<Int, Int>>()
            var node: Node? = current
            while (node != null) {
                path.add(node.x to node.y)
                node = node.parent
            }
            return path.reversed()
        }

        closed.add(current.x to current.y)

        for ((dx, dy) in directions) {
            val nx = current.x + dx
            val ny = current.y + dy

            if (nx < 0 || ny < 0 || nx >= rows || ny >= cols) continue
            if (grid[nx][ny] == 0) continue
            if ((nx to ny) in closed) continue

            val neighbor = Node(
                x = nx,
                y = ny,
                g = current.g + 1,
                h = Math.abs(nx - endX) + Math.abs(ny - endY),
                parent = current
            )

            val existing = open.find { it.x == nx && it.y == ny }
            if (existing == null || neighbor.g < existing.g) {
                if (existing != null) open.remove(existing)
                open.add(neighbor)
            }
        }
    }

    return null
}

fun astarWithSteps(
    grid: Array<IntArray>,
    startX: Int, startY: Int,
    endX: Int, endY: Int
): Pair<List<Pair<Int, Int>>?, List<Pair<Int, Int>>> {

    val rows = grid.size
    val cols = grid[0].size

    val open = mutableListOf(Node(startX, startY))
    val closed = mutableSetOf<Pair<Int, Int>>()
    val visited = mutableListOf<Pair<Int, Int>>()

    val directions = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

    while (open.isNotEmpty()) {
        val current = open.minByOrNull { it.f }!!
        open.remove(current)

        visited.add(current.x to current.y)

        if (current.x == endX && current.y == endY) {
            val path = mutableListOf<Pair<Int, Int>>()
            var node: Node? = current
            while (node != null) {
                path.add(node.x to node.y)
                node = node.parent
            }
            return path.reversed() to visited
        }

        closed.add(current.x to current.y)

        for ((dx, dy) in directions) {
            val nx = current.x + dx
            val ny = current.y + dy

            if (nx < 0 || ny < 0 || nx >= rows || ny >= cols) continue
            if (grid[nx][ny] == 0) continue
            if ((nx to ny) in closed) continue

            val neighbor = Node(
                x = nx,
                y = ny,
                g = current.g + 1,
                h = Math.abs(nx - endX) + Math.abs(ny - endY),
                parent = current
            )

            val existing = open.find { it.x == nx && it.y == ny }
            if (existing == null || neighbor.g < existing.g) {
                if (existing != null) open.remove(existing)
                open.add(neighbor)
            }
        }
    }

    return null to visited
}

fun printGrid(grid: Array<IntArray>, path: List<Pair<Int, Int>>?) {
    val pathSet = path?.toSet() ?: emptySet()
    val start = path?.firstOrNull()
    val end   = path?.lastOrNull()

    val sb = StringBuilder()
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            val cell = i to j
            sb.append(when {
                cell == start      -> "S "
                cell == end        -> "E "
                cell in pathSet    -> "* "
                grid[i][j] == 1   -> "# "
                else               -> ". "
            })
        }
        sb.append("\n")
    }
    println(sb)
}

//fun runAstar() {
//    val grid = arrayOf(
//        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
//        intArrayOf(0, 1, 1, 0, 0, 1, 1, 0),
//        intArrayOf(0, 1, 1, 0, 0, 1, 1, 0),
//        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
//        intArrayOf(0, 0, 1, 1, 1, 1, 0, 0),
//        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
//        intArrayOf(0, 1, 1, 0, 0, 1, 1, 0),
//        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
//    )
//
//    println("=== A* — поиск маршрута до кафе ===")
//    println("S = вы,  E = кафе,  * = путь,  # = здание,  . = дорожка")
//    println()
//
//    val path = astar(grid, startX = 0, startY = 0, endX = 7, endY = 7)
//
//    if (path != null) {
//        println("Маршрут найден! Длина: ${path.size} шагов")
//        printGrid(grid, path)
//    } else {
//        println("Маршрут не найден — путь заблокирован")
//    }
//}
