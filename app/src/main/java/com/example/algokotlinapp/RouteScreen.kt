package com.example.algokotlinapp

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.algorithms.astar
import com.example.algokotlinapp.algorithms.astarWithSteps
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RouteScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current

    val rawLines = remember {
        context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { it.readLines() }
    }

    val grid = remember {
        rawLines.map { line -> line.map { it.toString().toIntOrNull() ?: 0 }.toIntArray() }.toTypedArray()
    }

    val gridCols = rawLines.maxOfOrNull { it.length } ?: 0
    val gridRows = rawLines.size

    var start by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var end by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedStart by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedEnd by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var animatedPath by remember { mutableStateOf<List<Pair<Int, Int>>?>(null) }
    var visitedCells by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }

    LaunchedEffect(start, end) {
        if (start != null && end != null) {
            animatedPath = null
            visitedCells = emptySet()
            val (pathResult, visitedResult) = astarWithSteps(grid, start!!.first, start!!.second, end!!.first, end!!.second)
            if (pathResult != null) {
                var currentVisited = emptySet<Pair<Int, Int>>()
                for (cell in visitedResult) {
                    currentVisited = currentVisited + cell
                    visitedCells = currentVisited
                    kotlinx.coroutines.delay(5)
                }
                var currentPath = emptyList<Pair<Int, Int>>()
                for (cell in pathResult) {
                    currentPath = currentPath + cell
                    animatedPath = currentPath
                    kotlinx.coroutines.delay(15)
                }
            }
        } else {
            animatedPath = null
            visitedCells = emptySet()
        }
    }

    val path = remember(start, end) {
        if (start != null && end != null)
            astar(grid, start!!.first, start!!.second, end!!.first, end!!.second)
        else null
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }


    val step = when {
        start == null -> 0
        end == null -> 1
        else -> 2
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(TsuBluePrimary, RoundedCornerShape(12.dp))
            ) {
                Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Навигация A*",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A2E)
                )
                Text("ТГУ · Томск", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            if (start != null || end != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEEEE),
                    modifier = Modifier.clickable { start = null; end = null; selectedStart = null; selectedEnd = null }
                ) {
                    Text(
                        "Сброс",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCC3333),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (start != null) Color(0xFF00AA55) else if (step == 0) TsuBluePrimary else Color(0xFFE0E0E0)
                ) {
                    Text(
                        if (start != null) "✓ Старт" else "① Старт",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (start != null || step == 0) Color.White else Color(0xFF888888),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Text("→", color = Color(0xFFCCCCCC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (end != null) Color(0xFFCC3333) else if (step == 1) TsuBluePrimary else Color(0xFFE0E0E0)
                ) {
                    Text(
                        if (end != null) "✓ Финиш" else "② Финиш",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (end != null || step == 1) Color.White else Color(0xFF888888),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                if (path != null) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${path.size} шагов",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TsuBluePrimary
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
        ) {
            val bW = constraints.maxWidth.toFloat()
            val bH = constraints.maxHeight.toFloat()
            val fit = minOf(bW / gridCols, bH / gridRows)
            val mW = fit * gridCols
            val mH = fit * gridRows
            val cellW = mW / gridCols
            val cellH = mH / gridRows

            val density = androidx.compose.ui.platform.LocalDensity.current
            val mapWDp = with(density) { mW.toDp() }
            val mapHDp = with(density) { mH.toDp() }
            val mapStartXDp = with(density) { ((bW - mW) / 2f).toDp() }
            val mapStartYDp = with(density) { ((bH - mH) / 2f).toDp() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 10f)
                            scale = newScale
                            val maxOffsetX = ((mW * scale) - bW).coerceAtLeast(0f) / 2f
                            val maxOffsetY = ((mH * scale) - bH).coerceAtLeast(0f) / 2f
                            offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                        }
                    }
                    .pointerInput(step) {
                        detectTapGestures { tap ->
                            val pivX = bW / 2f
                            val pivY = bH / 2f
                            val worldX = (tap.x - pivX - offsetX) / scale + pivX - (bW - mW) / 2f
                            val worldY = (tap.y - pivY - offsetY) / scale + pivY - (bH - mH) / 2f
                            val gridX = (worldX / cellW).toInt()
                            val gridY = (worldY / cellH).toInt()
                            if (gridY in grid.indices && gridX >= 0 && gridX < grid[0].size) {
                                if (grid[gridY][gridX] != 0) {
                                    when (step) {
                                        0 -> { start = Pair(gridY, gridX); selectedStart = Pair(gridX, gridY) }
                                        1 -> { end = Pair(gridY, gridX); selectedEnd = Pair(gridX, gridY) }
                                    }
                                }
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = mapStartXDp, y = mapStartYDp)
                            .size(width = mapWDp, height = mapHDp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.campus_map),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            visitedCells.forEach { (r, c) ->
                                if (r in 0 until gridRows && c in 0 until gridCols) {
                                    drawCircle(
                                        Color(0xFF90CAF9).copy(alpha = 0.4f),
                                        radius = minOf(cellW, cellH) * 0.5f,
                                        center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                    )
                                }
                            }

                            CampusDictionary.forEach { (pos, info) ->
                                val (c, r) = pos
                                if (c !in 0 until gridCols || r !in 0 until gridRows) return@forEach
                                val center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                val radius = minOf(cellW, cellH) * 2.0f
                                drawCircle(Color.Black.copy(alpha = 0.2f), radius + 1.5f, Offset(center.x + 1f, center.y + 1.5f))
                                drawCircle(Color(info.color), radius, center)
                                drawCircle(Color.White, radius * 0.4f, center)
                            }

                            animatedPath?.let { ap ->
                                ap.forEach { (r, c) ->
                                    if (r in 0 until gridRows && c in 0 until gridCols) {
                                        drawCircle(
                                            Color(0xFFFF6D00),
                                            radius = minOf(cellW, cellH) * 0.7f,
                                            center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                        )
                                    }
                                }
                            }

                            selectedStart?.let { (c, r) ->
                                val cx = c * cellW + cellW / 2f
                                val cy = r * cellH + cellH / 2f
                                val radius = minOf(cellW, cellH) * 2.5f
                                drawCircle(Color(0xFF00AA55).copy(alpha = 0.3f), radius * 2f, Offset(cx, cy))
                                drawCircle(Color(0xFF00AA55), radius, Offset(cx, cy))
                                drawCircle(Color.White, radius * 0.5f, Offset(cx, cy))
                            }

                            selectedEnd?.let { (c, r) ->
                                val cx = c * cellW + cellW / 2f
                                val cy = r * cellH + cellH / 2f
                                val radius = minOf(cellW, cellH) * 2.5f
                                drawCircle(Color(0xFFCC3333).copy(alpha = 0.3f), radius * 2f, Offset(cx, cy))
                                drawCircle(Color(0xFFCC3333), radius, Offset(cx, cy))
                                drawCircle(Color.White, radius * 0.5f, Offset(cx, cy))
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 6.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { scale = (scale * 1.4f).coerceIn(1f, 10f) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                        }
                        Box(modifier = Modifier.height(1.dp).width(34.dp).background(Color(0xFFE8E8E8)))
                        Text(
                            "×${"%.1f".format(scale)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                        Box(modifier = Modifier.height(1.dp).width(34.dp).background(Color(0xFFE8E8E8)))
                        IconButton(
                            onClick = {
                                val newScale = (scale / 1.4f).coerceIn(1f, 10f)
                                val maxOffX = ((mW * newScale) - bW).coerceAtLeast(0f) / 2f
                                val maxOffY = ((mH * newScale) - bH).coerceAtLeast(0f) / 2f
                                scale = newScale
                                offsetX = offsetX.coerceIn(-maxOffX, maxOffX)
                                offsetY = offsetY.coerceIn(-maxOffY, maxOffY)
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                        }
                    }
                }
            }

            if (start == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TsuBluePrimary.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            "Нажмите на карту, чтобы выбрать стартовую точку",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            } else if (end == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TsuBluePrimary.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            "Теперь выберите конечную точку маршрута",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = path != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(250)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 20.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)) {
                    Box(
                        modifier = Modifier
                            .width(40.dp).height(4.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEEF9EE), modifier = Modifier.size(44.dp)) {
                            Text("🗺️", fontSize = 20.sp, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Маршрут построен", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                            Text("Длина: ${path?.size ?: 0} шагов", fontSize = 13.sp, color = TsuBluePrimary, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { start = null; end = null; selectedStart = null; selectedEnd = null }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFBBBBBB))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF00AA55), RoundedCornerShape(6.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text("Старт", fontSize = 12.sp, color = Color(0xFF666666))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFFCC3333), RoundedCornerShape(6.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text("Финиш", fontSize = 12.sp, color = Color(0xFF666666))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color.White, RoundedCornerShape(6.dp)).border(1.dp, Color(0xFFAAAAAA), RoundedCornerShape(6.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text("Путь", fontSize = 12.sp, color = Color(0xFF666666))
                        }
                    }
                }
            }
        }
    }
}
