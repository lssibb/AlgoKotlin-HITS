package com.example.algokotlinapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.algorithms.DecisionPath
import com.example.algokotlinapp.algorithms.TreeNode
import com.example.algokotlinapp.algorithms.buildDecisionTree
import com.example.algokotlinapp.algorithms.classify
import com.example.algokotlinapp.algorithms.parseCsv
import com.example.algokotlinapp.algorithms.treeToText
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

@Composable
fun DecisionTreeScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current

    val parsed = remember {
        val text = context.assets.open("food_decisions.csv").bufferedReader().use { it.readText() }
        parseCsv(text)
    }
    val headers = parsed.first
    val rows = parsed.second
    val target = "recommended_place"
    val attributes = remember(headers) { headers.filter { it != target } }

    val tree = remember(rows) { buildDecisionTree(rows, attributes, target) }

    val attrValues = remember(rows) {
        attributes.associateWith { attr -> rows.map { it[attr]!! }.toSet().toList().sorted() }
    }
    val attrLabels = mapOf(
        "location" to "Где находишься",
        "budget" to "Бюджет",
        "time_available" to "Время",
        "food_type" to "Что хочешь",
        "queue_tolerance" to "Очередь",
        "weather" to "Погода"
    )
    val valueLabels = mapOf(
        "main_building" to "Главный корпус",
        "second_building" to "2 корпус",
        "bus_stop" to "Остановка",
        "campus_center" to "Центр кампуса",
        "low" to "Низкий",
        "medium" to "Средний",
        "high" to "Высокий",
        "very_short" to "Очень мало",
        "short" to "Мало",
        "coffee" to "Кофе",
        "pancakes" to "Блины",
        "full_meal" to "Обед",
        "snack" to "Снэк",
        "good" to "Хорошая",
        "bad" to "Плохая"
    )

    val selections = remember { mutableStateMapOf<String, String>() }
    var result by remember { mutableStateOf<DecisionPath?>(null) }
    var showTree by remember { mutableStateOf(false) }
    var graphMode by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(TsuBluePrimary, RoundedCornerShape(12.dp))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(stringResource(R.string.title_tree), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            }
            Spacer(Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEEF3FF),
                modifier = Modifier.clickable { showTree = !showTree }
            ) {
                Text(
                    if (showTree) stringResource(R.string.tree_hide) else stringResource(R.string.tree_show),
                    fontSize = 12.sp, color = TsuBluePrimary, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text(stringResource(R.string.tree_sample_size, rows.size), fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))

            attributes.forEach { attr ->
                Text(
                    attrLabels[attr] ?: attr,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E),
                    modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = attrValues[attr].orEmpty(),
                        key = { value -> "$attr:$value" }
                    ) { value ->
                        val sel = selections[attr] == value
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (sel) TsuBluePrimary else Color.White,
                            shadowElevation = if (sel) 4.dp else 1.dp,
                            modifier = Modifier.clickable {
                                selections[attr] = value
                                result = null
                            }
                        ) {
                            Text(
                                valueLabels[value] ?: value,
                                fontSize = 12.sp,
                                color = if (sel) Color.White else Color(0xFF666666),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (selections.size == attributes.size) {
                        result = classify(tree, selections.toMap())
                    }
                },
                enabled = selections.size == attributes.size,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
            ) {
                Text(
                    if (selections.size == attributes.size) stringResource(R.string.tree_btn_classify)
                    else stringResource(R.string.tree_btn_fill, selections.size, attributes.size),
                    fontWeight = FontWeight.SemiBold
                )
            }

            result?.let { path ->
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.tree_recommendation), fontSize = 12.sp, color = Color.Gray)
                        Text(
                            path.result,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.tree_path_title), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
                        Spacer(Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                itemsIndexed(
                                    items = path.steps,
                                    key = { i, step -> "${step.first}:${step.second}:$i" }
                                ) { i, step ->
                                    val attr = step.first
                                    val value = step.second
                                    val outcome = step.third
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEEF3FF),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text("${i + 1}", fontSize = 11.sp, color = TsuBluePrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "${attrLabels[attr] ?: attr} = ${valueLabels[value] ?: value}  →  $outcome",
                                            fontSize = 12.sp, color = Color(0xFF444444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showTree) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF7F9FC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.tree_structure_title), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
                            Spacer(Modifier.weight(1f))
                            TreeModeToggle(
                                graphMode = graphMode,
                                onModeChange = { graphMode = it }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (graphMode) {
                            TreeCanvas(tree = tree, attrLabels = attrLabels, valueLabels = valueLabels)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                                Text(
                                    treeToText(tree),
                                    fontSize = 10.sp,
                                    color = Color(0xFF444444),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TreeModeToggle(graphMode: Boolean, onModeChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0xFFEEF3FF), RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        ModePill(
            label = stringResource(R.string.tree_mode_text),
            selected = !graphMode,
            onClick = { onModeChange(false) }
        )
        ModePill(
            label = stringResource(R.string.tree_mode_graph),
            selected = graphMode,
            onClick = { onModeChange(true) }
        )
    }
}

@Composable
private fun ModePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) TsuBluePrimary else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = if (selected) Color.White else TsuBluePrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun TreeCanvas(
    tree: TreeNode,
    attrLabels: Map<String, String>,
    valueLabels: Map<String, String>
) {
    val density = LocalDensity.current
    val leafW = with(density) { 110.dp.toPx() }
    val nodeH = with(density) { 40.dp.toPx() }
    val yStep = with(density) { 90.dp.toPx() }
    val hPad = with(density) { 16.dp.toPx() }
    val vPad = with(density) { 16.dp.toPx() }
    val nodeTextPx = with(density) { 11.sp.toPx() }
    val edgeTextPx = with(density) { 9.sp.toPx() }

    val branchTextPaint = remember(nodeTextPx) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = nodeTextPx
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    val leafTextPaint = remember(nodeTextPx) {
        android.graphics.Paint().apply {
            color = 0xFF1A1A2E.toInt()
            textSize = nodeTextPx
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    val edgeTextPaint = remember(edgeTextPx) {
        android.graphics.Paint().apply {
            color = 0xFF5A6B7A.toInt()
            textSize = edgeTextPx
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val treeW = subtreeWidth(tree, leafW) + hPad * 2f
    val canvasWidthDp = with(density) { treeW.toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Canvas(
            modifier = Modifier
                .width(canvasWidthDp)
                .fillMaxHeight()
        ) {
            drawTreeNode(
                node = tree,
                centerX = hPad + subtreeWidth(tree, leafW) / 2f,
                centerY = vPad + nodeH / 2f,
                yStep = yStep,
                leafW = leafW,
                nodeH = nodeH,
                branchTextPaint = branchTextPaint,
                leafTextPaint = leafTextPaint,
                edgeTextPaint = edgeTextPaint,
                attrLabels = attrLabels,
                valueLabels = valueLabels
            )
        }
    }
}

private fun subtreeWidth(node: TreeNode, leafW: Float): Float = when (node) {
    is TreeNode.Leaf -> leafW
    is TreeNode.Branch -> node.children.values
        .sumOf { subtreeWidth(it, leafW).toDouble() }
        .toFloat()
        .coerceAtLeast(leafW)
}

private fun DrawScope.drawTreeNode(
    node: TreeNode,
    centerX: Float,
    centerY: Float,
    yStep: Float,
    leafW: Float,
    nodeH: Float,
    branchTextPaint: android.graphics.Paint,
    leafTextPaint: android.graphics.Paint,
    edgeTextPaint: android.graphics.Paint,
    attrLabels: Map<String, String>,
    valueLabels: Map<String, String>
) {
    val boxW = leafW - 20f
    val corner = CornerRadius(14f, 14f)
    val topLeft = Offset(centerX - boxW / 2f, centerY - nodeH / 2f)
    val boxSize = Size(boxW, nodeH)

    when (node) {
        is TreeNode.Leaf -> {
            drawRoundRect(
                color = Color(0xFFD5E7FF),
                topLeft = topLeft,
                size = boxSize,
                cornerRadius = corner
            )
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(
                    node.label,
                    centerX,
                    centerY + leafTextPaint.textSize / 3f,
                    leafTextPaint
                )
            }
        }
        is TreeNode.Branch -> {
            val childWidths = node.children.mapValues { (_, child) -> subtreeWidth(child, leafW) }
            val totalChildrenW = childWidths.values.sum().coerceAtLeast(leafW)

            drawRoundRect(
                color = Color(0xFF4A90E2),
                topLeft = topLeft,
                size = boxSize,
                cornerRadius = corner
            )
            drawIntoCanvas { c ->
                val label = attrLabels[node.attribute] ?: node.attribute
                c.nativeCanvas.drawText(
                    label,
                    centerX,
                    centerY + branchTextPaint.textSize / 3f,
                    branchTextPaint
                )
            }

            var x = centerX - totalChildrenW / 2f
            for ((value, child) in node.children) {
                val w = childWidths[value] ?: leafW
                val cx = x + w / 2f
                val cy = centerY + yStep

                drawLine(
                    color = Color(0xFF9AA6B2),
                    start = Offset(centerX, centerY + nodeH / 2f),
                    end = Offset(cx, cy - nodeH / 2f),
                    strokeWidth = 2f
                )
                drawIntoCanvas { c ->
                    val midX = (centerX + cx) / 2f
                    val midY = (centerY + nodeH / 2f + cy - nodeH / 2f) / 2f
                    val lbl = valueLabels[value] ?: value
                    c.nativeCanvas.drawText(lbl, midX, midY, edgeTextPaint)
                }

                drawTreeNode(
                    node = child,
                    centerX = cx,
                    centerY = cy,
                    yStep = yStep,
                    leafW = leafW,
                    nodeH = nodeH,
                    branchTextPaint = branchTextPaint,
                    leafTextPaint = leafTextPaint,
                    edgeTextPaint = edgeTextPaint,
                    attrLabels = attrLabels,
                    valueLabels = valueLabels
                )
                x += w
            }
        }
    }
}
