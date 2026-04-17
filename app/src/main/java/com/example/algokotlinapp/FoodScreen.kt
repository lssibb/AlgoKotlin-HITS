package com.example.algokotlinapp

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.algorithms.FOOD_LABELS
import com.example.algokotlinapp.algorithms.FOOD_PLACES
import com.example.algokotlinapp.algorithms.GAResult
import com.example.algokotlinapp.algorithms.astar
import com.example.algokotlinapp.algorithms.foodGeneticAlgorithm
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FoodScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val grid = remember {
        context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { reader ->
            reader.readLines().map { line -> line.map { it.toString().toIntOrNull() ?: 0 }.toIntArray() }.toTypedArray()
        }
    }
    val places = FOOD_PLACES
    val foodTypes = listOf("coffee", "pancakes", "full_meal", "snack")

    var selectedFoods by remember { mutableStateOf(setOf("coffee", "full_meal")) }
    var startPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var gaResult by remember { mutableStateOf<GAResult?>(null) }
    var routePaths by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(3f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val placeColors = listOf(
        Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
        Color(0xFFFF9800), Color(0xFF8E24AA), Color(0xFF00ACC1)
    )

    LaunchedEffect(gaResult) {
        if (gaResult != null && gaResult!!.route.isNotEmpty() && startPoint != null) {
            val stops = listOf(startPoint!!) + gaResult!!.route.map { places[it].row to places[it].col }
            val paths = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until stops.size - 1) {
                val seg = astar(grid, stops[i].first, stops[i].second, stops[i + 1].first, stops[i + 1].second)
                if (seg != null) paths.addAll(seg)
            }
            routePaths = paths
        } else {
            routePaths = emptyList()
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(TsuBluePrimary, RoundedCornerShape(12.dp))) {
                Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Text(stringResource(R.string.title_food), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            Spacer(Modifier.weight(1f))
            if (gaResult != null) {
                Button(
                    onClick = { gaResult = null; routePaths = emptyList(); startPoint = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF3FF), contentColor = TsuBluePrimary)
                ) { Text(stringResource(R.string.btn_reset), fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            foodTypes.forEach { food ->
                val label = FOOD_LABELS[food] ?: food
                val isSel = food in selectedFoods
                val available = places.filter { it.menu.contains(food) }.size
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSel) TsuBluePrimary else Color(0xFFEEF3FF),
                    modifier = Modifier.height(40.dp).clickable {
                        selectedFoods = if (isSel) selectedFoods - food else selectedFoods + food
                        gaResult = null; routePaths = emptyList()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            label, fontSize = 11.sp,
                            color = if (isSel) Color.White else Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(4.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = if (isSel) Color.White.copy(alpha = 0.3f) else Color(0xFFE0E0E0)) {
                            Text("$available", fontSize = 10.sp, color = if (isSel) Color.White else Color(0xFF666666), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        val filteredPlaces = places.filter { it.menu.any { m -> m in selectedFoods } }
        if (filteredPlaces.isEmpty() && selectedFoods.isNotEmpty()) {
            Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.food_no_places), fontSize = 12.sp, color = Color(0xFFC62828), modifier = Modifier.padding(8.dp))
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)).background(Color.White)
        ) {
            val bW = constraints.maxWidth.toFloat()
            val bH = constraints.maxHeight.toFloat()
            val gridRows = grid.size
            val gridCols = grid[0].size
            val fit = minOf(bW / gridCols, bH / gridRows)
            val mW = fit * gridCols; val mH = fit * gridRows
            val cellW = mW / gridCols; val cellH = mH / gridRows
            val density = androidx.compose.ui.platform.LocalDensity.current
            val mapWDp = with(density) { mW.toDp() }
            val mapHDp = with(density) { mH.toDp() }
            val mapStartXDp = with(density) { ((bW - mW) / 2f).toDp() }
            val mapStartYDp = with(density) { ((bH - mH) / 2f).toDp() }

            Box(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val ns = (scale * zoom).coerceIn(1f, 8f); scale = ns
                            val mx = ((mW * ns) - bW).coerceAtLeast(0f) / 2f
                            val my = ((mH * ns) - bH).coerceAtLeast(0f) / 2f
                            offsetX = (offsetX + pan.x).coerceIn(-mx, mx)
                            offsetY = (offsetY + pan.y).coerceIn(-my, my)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            val pivX = bW / 2f; val pivY = bH / 2f
                            val wx = (tap.x - pivX - offsetX) / scale + pivX - (bW - mW) / 2f
                            val wy = (tap.y - pivY - offsetY) / scale + pivY - (bH - mH) / 2f
                            val col = (wx / cellW).toInt(); val row = (wy / cellH).toInt()
                            if (row in grid.indices && col in grid[0].indices && grid[row][col] != 0) {
                                startPoint = row to col
                                gaResult = null; routePaths = emptyList()
                            }
                        }
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY)) {
                    Box(modifier = Modifier.offset(x = mapStartXDp, y = mapStartYDp).size(width = mapWDp, height = mapHDp)) {
                        Image(painter = painterResource(id = R.drawable.campus_map), contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (row in 0 until gridRows) {
                                for (col in 0 until gridCols) {
                                    if (col >= grid[row].size) continue
                                    val value = grid[row][col]
                                    if (value == 0 || value == 8) continue
                                    val color = Color.White.copy(alpha = 0.7f)
                                    drawRect(color, topLeft = Offset(col * cellW, row * cellH), size = Size(cellW, cellH))
                                }
                            }

                            routePaths.forEach { (r, c) ->
                                if (r in grid.indices && c in grid[r].indices)
                                drawRect(Color(0xCCFF6D00), topLeft = Offset(c * cellW, r * cellH), size = Size(cellW, cellH))
                            }

                            places.forEachIndexed { i, place ->
                                val center = Offset(place.col * cellW + cellW / 2f, place.row * cellH + cellH / 2f)
                                val rad = minOf(cellW, cellH) * 2.5f
                                val isOnRoute = gaResult?.route?.contains(i) == true
                                drawCircle(Color.White, rad * 1.3f, center)
                                drawCircle(if (isOnRoute) placeColors[i % placeColors.size] else Color(0xFF78909C), rad, center)
                                drawCircle(Color.White, rad * 0.3f, center)
                            }

                            startPoint?.let { (r, c) ->
                                val center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                val rad = minOf(cellW, cellH) * 3f
                                drawCircle(Color.White, rad * 1.3f, center)
                                drawCircle(Color(0xFF00C853), rad, center)
                                drawCircle(Color.White, rad * 0.3f, center)
                            }

                            CampusDictionary.forEach { (pos, info) ->
                                val (c, r) = pos
                                if (c !in 0 until gridCols || r !in 0 until gridRows) return@forEach
                                val center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                val radius = minOf(cellW, cellH) * 1.5f
                                drawCircle(Color(info.color).copy(alpha = 0.6f), radius, center)
                                drawCircle(Color.White, radius * 0.4f, center)
                            }

                            places.forEachIndexed { i, place ->
                                val center = Offset(place.col * cellW + cellW / 2f, place.row * cellH + cellH / 2f)
                                val rad = minOf(cellW, cellH) * 2.5f
                                val isOnRoute = gaResult?.route?.contains(i) == true
                                drawCircle(Color.White, rad * 1.3f, center)
                                drawCircle(if (isOnRoute) placeColors[i % placeColors.size] else Color(0xFF78909C), rad, center)
                                drawCircle(Color.White, rad * 0.3f, center)
                            }

                            gaResult?.route?.forEachIndexed { idx, placeIdx ->
                                val p = places[placeIdx]
                                val center = Offset(p.col * cellW + cellW / 2f, p.row * cellH + cellH / 2f)
                                val rad = minOf(cellW, cellH) * 3.5f
                                drawCircle(Color.White, rad, center)
                                drawCircle(placeColors[placeIdx % placeColors.size], rad * 0.85f, center)
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
                            onClick = { scale = (scale * 1.4f).coerceIn(1f, 8f) },
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
                                val newScale = (scale / 1.4f).coerceIn(1f, 8f)
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
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White, shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (startPoint == null) {
                    Text(stringResource(R.string.food_hint_start), fontSize = 13.sp, color = Color.Gray)
                } else if (gaResult == null) {
                    Text(stringResource(R.string.food_start, startPoint!!.second, startPoint!!.first), fontSize = 14.sp, color = Color(0xFF00C853))
                    if (selectedFoods.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (isRunning) return@Button
                                isRunning = true
                                scope.launch {
                                    val result = withContext(Dispatchers.Default) {
                                        foodGeneticAlgorithm(places, selectedFoods, startPoint!!.first, startPoint!!.second, grid)
                                    }
                                    gaResult = result
                                    isRunning = false
                                }
                            },
                            enabled = !isRunning,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.common_computing), fontWeight = FontWeight.SemiBold)
                            } else {
                                Text(stringResource(R.string.food_btn_build), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(stringResource(R.string.food_select_food), fontSize = 13.sp, color = Color.Red)
                    }
                } else {
                    val res = gaResult!!
                    if (res.distance > 0) {
                        Text(stringResource(R.string.food_route_found), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                        Text(stringResource(R.string.food_distance, res.distance, "%.0f".format(res.distance * 0.3)), fontSize = 14.sp, color = Color(0xFF666666))
                        Text(stringResource(R.string.food_generations, res.generations, res.bestPerGeneration.lastOrNull().toString()), fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        res.route.forEachIndexed { idx, placeIdx ->
                            val p = places[placeIdx]
                            Text("${idx + 1}. ${p.name} (${p.menu.filter { it in selectedFoods }.joinToString { FOOD_LABELS[it] ?: it }})",
                                fontSize = 13.sp, color = placeColors[placeIdx % placeColors.size], fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text(stringResource(R.string.common_route_not_found), fontSize = 14.sp, color = Color.Red)
                    }
                }
            }
        }
    }
}
