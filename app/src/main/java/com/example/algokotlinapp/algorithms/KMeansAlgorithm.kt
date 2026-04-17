package com.example.algokotlinapp.algorithms

import kotlin.math.abs
import kotlin.math.max

enum class DistanceMetric { EUCLIDEAN, MANHATTAN, CHEBYSHEV }

private fun distance(
    px: Double, py: Double,
    cx: Double, cy: Double,
    metric: DistanceMetric
): Double {
    val dx = px - cx
    val dy = py - cy
    return when (metric) {
        DistanceMetric.EUCLIDEAN -> dx * dx + dy * dy
        DistanceMetric.MANHATTAN -> abs(dx) + abs(dy)
        DistanceMetric.CHEBYSHEV -> max(abs(dx), abs(dy))
    }
}

data class KMeansResult(
    val centroids: List<Pair<Double, Double>>,
    val assignments: IntArray,
    val iterations: Int
)

fun kmeans(
    points: List<Pair<Int, Int>>,
    k: Int,
    maxIterations: Int = 100,
    metric: DistanceMetric = DistanceMetric.EUCLIDEAN
): KMeansResult {
    if (points.isEmpty() || k <= 0) return KMeansResult(emptyList(), intArrayOf(), 0)
    val actualK = minOf(k, points.size)

    var centroids = points.shuffled().take(actualK)
        .map { it.first.toDouble() to it.second.toDouble() }
    var assignments = IntArray(points.size)
    var iter = 0

    repeat(maxIterations) { iteration ->
        iter = iteration + 1

        val newAssignments = IntArray(points.size) { i ->
            val (px, py) = points[i]
            centroids.indices.minByOrNull { j ->
                distance(px.toDouble(), py.toDouble(), centroids[j].first, centroids[j].second, metric)
            } ?: 0
        }

        if (iteration > 0 && newAssignments.contentEquals(assignments)) {
            assignments = newAssignments
            return KMeansResult(centroids, assignments, iter)
        }
        assignments = newAssignments

        centroids = (0 until actualK).map { c ->
            val pts = points.filterIndexed { i, _ -> assignments[i] == c }
            if (pts.isEmpty()) centroids[c]
            else pts.map { it.first.toDouble() }.average() to
                    pts.map { it.second.toDouble() }.average()
        }
    }

    return KMeansResult(centroids, assignments, iter)
}
