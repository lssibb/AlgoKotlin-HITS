package com.example.algokotlinapp.algorithms

import kotlin.math.ln

sealed class TreeNode {
    data class Leaf(val label: String) : TreeNode()
    data class Branch(
        val attribute: String,
        val children: Map<String, TreeNode>,
        val fallback: String
    ) : TreeNode()
}

data class DecisionPath(
    val steps: List<Triple<String, String, String>>,
    val result: String
)

fun parseCsv(text: String): Pair<List<String>, List<Map<String, String>>> {
    val lines = text.trim().lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList<String>() to emptyList()
    val headers = lines[0].split(",").map { it.trim() }
    val rows = lines.drop(1).map { line ->
        val values = line.split(",").map { it.trim() }
        headers.zip(values).toMap()
    }
    return headers to rows
}

private fun log2(x: Double): Double = if (x <= 0.0) 0.0 else ln(x) / ln(2.0)

private fun entropy(rows: List<Map<String, String>>, target: String): Double {
    if (rows.isEmpty()) return 0.0
    val total = rows.size.toDouble()
    val counts = rows.groupingBy { it[target]!! }.eachCount()
    return counts.values.sumOf { c ->
        val p = c / total
        -p * log2(p)
    }
}

private fun infoGain(
    rows: List<Map<String, String>>,
    attribute: String,
    target: String
): Double {
    val base = entropy(rows, target)
    val total = rows.size.toDouble()
    val subsets = rows.groupBy { it[attribute]!! }
    val weighted = subsets.values.sumOf { subset ->
        (subset.size / total) * entropy(subset, target)
    }
    return base - weighted
}

private fun majorityLabel(rows: List<Map<String, String>>, target: String): String {
    return rows.groupingBy { it[target]!! }.eachCount()
        .maxByOrNull { it.value }?.key ?: "unknown"
}

fun buildDecisionTree(
    rows: List<Map<String, String>>,
    attributes: List<String>,
    target: String
): TreeNode {
    if (rows.isEmpty()) return TreeNode.Leaf("unknown")

    val labels = rows.map { it[target]!! }.toSet()
    if (labels.size == 1) return TreeNode.Leaf(labels.first())
    if (attributes.isEmpty()) return TreeNode.Leaf(majorityLabel(rows, target))

    val best = attributes.maxByOrNull { infoGain(rows, it, target) } ?: attributes.first()
    val subsets = rows.groupBy { it[best]!! }
    val fallback = majorityLabel(rows, target)

    val children = mutableMapOf<String, TreeNode>()
    for ((value, subset) in subsets) {
        children[value] = buildDecisionTree(subset, attributes - best, target)
    }

    return TreeNode.Branch(best, children, fallback)
}

fun classify(tree: TreeNode, sample: Map<String, String>): DecisionPath {
    val steps = mutableListOf<Triple<String, String, String>>()
    var node: TreeNode = tree
    while (node is TreeNode.Branch) {
        val attr = node.attribute
        val value = sample[attr]
        if (value == null) {
            steps.add(Triple(attr, "?", node.fallback))
            return DecisionPath(steps, node.fallback)
        }
        val next = node.children[value]
        if (next == null) {
            steps.add(Triple(attr, value, node.fallback))
            return DecisionPath(steps, node.fallback)
        }
        val outcomeLabel = when (next) {
            is TreeNode.Leaf -> next.label
            is TreeNode.Branch -> "→ ${next.attribute}?"
        }
        steps.add(Triple(attr, value, outcomeLabel))
        node = next
    }
    return DecisionPath(steps, (node as TreeNode.Leaf).label)
}

fun treeToText(tree: TreeNode, indent: Int = 0): String {
    val pad = "  ".repeat(indent)
    return when (tree) {
        is TreeNode.Leaf -> "$pad→ ${tree.label}\n"
        is TreeNode.Branch -> {
            val sb = StringBuilder()
            sb.append("$pad[${tree.attribute}]\n")
            for ((value, child) in tree.children) {
                sb.append("$pad  = $value:\n")
                sb.append(treeToText(child, indent + 2))
            }
            sb.toString()
        }
    }
}
