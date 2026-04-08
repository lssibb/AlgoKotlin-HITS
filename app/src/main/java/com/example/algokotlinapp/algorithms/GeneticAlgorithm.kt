package com.example.algokotlinapp.algorithms

import kotlin.random.Random

data class FoodPlace(
    val name: String,
    val row: Int,
    val col: Int,
    val menu: Set<String>,
    val openHour: Int,
    val closeHour: Int
)

val FOOD_PLACES = listOf(
    FoodPlace("Старбакс", 55, 71, setOf("coffee", "snack"), 8, 22),
    FoodPlace("Сибирские блины", 55, 73, setOf("pancakes", "full_meal"), 9, 20),
    FoodPlace("Столовая ГК", 59, 73, setOf("full_meal", "coffee", "snack"), 8, 18),
    FoodPlace("Rostic's", 65, 126, setOf("full_meal", "snack"), 10, 22),
    FoodPlace("СырБор", 68, 57, setOf("full_meal", "snack", "coffee"), 9, 21),
    FoodPlace("Вендинг 2 корпус", 71, 57, setOf("coffee", "snack"), 0, 24)
)

val FOOD_LABELS = mapOf(
    "coffee" to "Кофе",
    "pancakes" to "Блины",
    "full_meal" to "Полноценный обед",
    "snack" to "Снэк"
)

data class GAResult(
    val route: List<Int>,
    val distance: Int,
    val generations: Int,
    val bestPerGeneration: List<Int>
)

fun bfsDistanceMap(grid: Array<IntArray>, startRow: Int, startCol: Int): Array<IntArray> {
    val rows = grid.size
    val cols = grid[0].size
    val dist = Array(rows) { IntArray(cols) { Int.MAX_VALUE } }
    if (startRow !in 0 until rows || startCol !in 0 until cols || grid[startRow][startCol] == 0) return dist
    dist[startRow][startCol] = 0
    val queue = ArrayDeque<Pair<Int, Int>>()
    queue.add(startRow to startCol)
    val dirs = arrayOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
    while (queue.isNotEmpty()) {
        val (r, c) = queue.removeFirst()
        for ((dr, dc) in dirs) {
            val nr = r + dr; val nc = c + dc
            if (nr in 0 until rows && nc in 0 until cols && grid[nr][nc] != 0 && dist[nr][nc] == Int.MAX_VALUE) {
                dist[nr][nc] = dist[r][c] + 1
                queue.add(nr to nc)
            }
        }
    }
    return dist
}

fun foodGeneticAlgorithm(
    places: List<FoodPlace>,
    desiredFoods: Set<String>,
    startRow: Int,
    startCol: Int,
    grid: Array<IntArray>,
    populationSize: Int = 80,
    generationCount: Int = 150,
    mutationRate: Double = 0.2
): GAResult {
    val candidates = places.indices.filter { places[it].menu.any { f -> f in desiredFoods } }
    if (candidates.isEmpty()) return GAResult(emptyList(), 0, 0, emptyList())

    val allPoints = listOf(startRow to startCol) + candidates.map { places[it].row to places[it].col }
    val bfsMaps = allPoints.map { (r, c) -> bfsDistanceMap(grid, r, c) }
    val dist = Array(allPoints.size) { i ->
        IntArray(allPoints.size) { j ->
            bfsMaps[i][allPoints[j].first][allPoints[j].second]
        }
    }

    fun routeDistance(route: List<Int>): Int {
        if (route.isEmpty()) return Int.MAX_VALUE
        val idx = route.map { candidates.indexOf(it) + 1 }
        var total = dist[0][idx[0]]
        if (total >= Int.MAX_VALUE / 2) return Int.MAX_VALUE
        for (i in 0 until idx.size - 1) {
            val d = dist[idx[i]][idx[i + 1]]
            if (d >= Int.MAX_VALUE / 2) return Int.MAX_VALUE
            total += d
        }
        return total
    }

    fun covers(route: List<Int>): Boolean =
        route.flatMap { places[it].menu }.toSet().containsAll(desiredFoods)

    fun fitness(route: List<Int>): Int =
        if (covers(route)) routeDistance(route) else Int.MAX_VALUE

    fun generateRoute(): List<Int> {
        val selected = mutableListOf<Int>()
        val remaining = desiredFoods.toMutableSet()
        while (remaining.isNotEmpty()) {
            val viable = candidates.filter { it !in selected && places[it].menu.any { f -> f in remaining } }
            if (viable.isEmpty()) break
            val pick = viable.random()
            selected.add(pick)
            remaining.removeAll(places[pick].menu)
        }
        return selected.shuffled()
    }

    fun crossover(p1: List<Int>, p2: List<Int>): List<Int> {
        val pool = (p1 + p2).distinct().shuffled()
        val selected = mutableListOf<Int>()
        val remaining = desiredFoods.toMutableSet()
        for (gene in pool) {
            val newFoods = places[gene].menu.filter { it in remaining }
            if (newFoods.isNotEmpty()) {
                selected.add(gene)
                remaining.removeAll(newFoods.toSet())
            }
            if (remaining.isEmpty()) break
        }
        for (food in remaining.toList()) {
            val p = candidates.firstOrNull { places[it].menu.contains(food) && it !in selected }
            if (p != null) { selected.add(p); remaining.removeAll(places[p].menu) }
        }
        return selected
    }

    fun mutate(route: List<Int>): List<Int> {
        val r = route.toMutableList()
        if (r.size >= 2 && Random.nextBoolean()) {
            val i = Random.nextInt(r.size); val j = Random.nextInt(r.size)
            r[i] = r[j].also { r[j] = r[i] }
        } else if (r.isNotEmpty()) {
            val idx = Random.nextInt(r.size)
            val served = places[r[idx]].menu.filter { it in desiredFoods }
            val alts = candidates.filter { it != r[idx] && it !in r && places[it].menu.any { f -> f in served } }
            if (alts.isNotEmpty()) {
                r[idx] = alts.random()
                val pruned = r.toMutableList()
                pruned.removeAll { est ->
                    val without = pruned.filter { it != est }
                    without.flatMap { places[it].menu }.toSet().containsAll(desiredFoods)
                }
                if (pruned.isNotEmpty() && pruned.flatMap { places[it].menu }.toSet().containsAll(desiredFoods)) {
                    return pruned
                }
            }
        }
        return r
    }

    var population = (0 until populationSize).map { generateRoute() }
    val bestPerGen = mutableListOf<Int>()

    repeat(generationCount) {
        val fitnesses = population.map { fitness(it) }
        bestPerGen.add(fitnesses.filter { it < Int.MAX_VALUE / 2 }.minOrNull() ?: Int.MAX_VALUE)

        val newPop = mutableListOf<List<Int>>()
        val bestIdx = fitnesses.indexOf(fitnesses.min())
        newPop.add(population[bestIdx])

        while (newPop.size < populationSize) {
            val t1 = (0 until 4).map { Random.nextInt(populationSize) }.minByOrNull { fitnesses[it] }!!
            val t2 = (0 until 4).map { Random.nextInt(populationSize) }.minByOrNull { fitnesses[it] }!!
            var child = crossover(population[t1], population[t2])
            if (Random.nextDouble() < mutationRate) child = mutate(child)
            newPop.add(child)
        }
        population = newPop
    }

    val finalFit = population.map { fitness(it) }
    val bestIdx = finalFit.indexOf(finalFit.min())

    return GAResult(
        route = population[bestIdx],
        distance = if (finalFit[bestIdx] < Int.MAX_VALUE / 2) finalFit[bestIdx] else -1,
        generations = generationCount,
        bestPerGeneration = bestPerGen
    )
}
