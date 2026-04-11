package com.example.algokotlinapp.algorithms

import kotlin.math.pow
import kotlin.random.Random

data class Landmark(
    val row: Int,
    val col: Int,
    val comfort: Double
)

data class AntResult(
    val route: List<Int>,
    val distance: Int,
    val iterations: Int,
    val bestPerIteration: List<Double>
)

fun antColonyTSP(
    landmarks: List<Landmark>,
    grid: Array<IntArray>,
    iterations: Int = 80,
    antCount: Int = 20,
    alpha: Double = 1.0,
    beta: Double = 3.0,
    evaporation: Double = 0.5,
    q: Double = 100.0
): AntResult {
    val n = landmarks.size
    if (n == 0) return AntResult(emptyList(), 0, 0, emptyList())
    if (n == 1) return AntResult(listOf(0), 0, 0, emptyList())

    val bfsMaps = landmarks.map { bfsDistanceMap(grid, it.row, it.col) }
    val dist = Array(n) { i ->
        DoubleArray(n) { j ->
            val d = bfsMaps[i][landmarks[j].row][landmarks[j].col]
            if (d == Int.MAX_VALUE) 1e9 else d.toDouble()
        }
    }

    val pheromone = Array(n) { DoubleArray(n) { 1.0 } }
    var bestRoute: List<Int> = (0 until n).toList()
    var bestLen = Double.MAX_VALUE
    val bestPerIter = mutableListOf<Double>()

    fun routeLength(route: List<Int>): Double {
        var s = 0.0
        for (i in 0 until route.size - 1) s += dist[route[i]][route[i + 1]]
        return s
    }

    repeat(iterations) {
        val tours = mutableListOf<List<Int>>()
        repeat(antCount) {
            val visited = mutableSetOf<Int>()
            val tour = mutableListOf<Int>()
            var cur = Random.nextInt(n)
            tour.add(cur); visited.add(cur)
            while (visited.size < n) {
                val unvisited = (0 until n).filter { it !in visited }
                val probs = unvisited.map { j ->
                    val tau = pheromone[cur][j].pow(alpha)
                    val eta = (landmarks[j].comfort / (dist[cur][j] + 1e-6)).pow(beta)
                    tau * eta
                }
                val sum = probs.sum()
                var pick = unvisited.last()
                if (sum > 0.0) {
                    val r = Random.nextDouble() * sum
                    var acc = 0.0
                    for (k in unvisited.indices) {
                        acc += probs[k]
                        if (acc >= r) { pick = unvisited[k]; break }
                    }
                }
                tour.add(pick); visited.add(pick); cur = pick
            }
            tours.add(tour)
        }

        for (i in 0 until n) for (j in 0 until n) pheromone[i][j] *= (1 - evaporation)

        for (tour in tours) {
            val len = routeLength(tour)
            if (len < bestLen) { bestLen = len; bestRoute = tour }
            val deposit = q / (len + 1e-6)
            for (k in 0 until tour.size - 1) {
                pheromone[tour[k]][tour[k + 1]] += deposit
                pheromone[tour[k + 1]][tour[k]] += deposit
            }
        }
        bestPerIter.add(bestLen)
    }

    return AntResult(bestRoute, if (bestLen >= 1e8) -1 else bestLen.toInt(), iterations, bestPerIter)
}
