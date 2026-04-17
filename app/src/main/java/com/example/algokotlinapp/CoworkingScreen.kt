package com.example.algokotlinapp

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.algokotlinapp.algorithms.AntResult
import com.example.algokotlinapp.algorithms.Landmark
import com.example.algokotlinapp.algorithms.antColonyTSP
import com.example.algokotlinapp.algorithms.astar
import com.example.algokotlinapp.algorithms.findNearestWalkable
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CoworkingScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current

    val coworkingTypes = setOf("Коворкинг", "Учебный корпус", "Спорт")

    val mapInfo = remember {
        val lines = context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { it.readLines() }
        val grid = lines.map { line -> line.map { it.toString().toIntOrNull() ?: 0 }.toIntArray() }.toTypedArray()
        val landmarks = CampusDictionary.filter { (pos, info) ->
            info.type in coworkingTypes && pos.second in grid.indices && pos.first < grid.getOrNull(pos.second)?.size ?: 0
        }.map { (pos, _) -> Landmark(pos.second, pos.first, 1.0) }
        Triple(grid, lines, landmarks)
    }
    val grid = mapInfo.first
    val allLandmarks = mapInfo.third

    var selectedIdx by remember { mutableStateOf(setOf<Int>()) }
    var antResult by remember { mutableStateOf<AntResult?>(null) }
    var routePaths by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(3f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(antResult) {
        val res = antResult
        if (res != null && res.route.size >= 2) {
            val paths = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until res.route.size - 1) {
                val fromIdx = selectedIdx.toList()[res.route[i]]
                val toIdx = selectedIdx.toList()[res.route[i + 1]]
                val from = allLandmarks.getOrNull(fromIdx) ?: continue
                val to = allLandmarks.getOrNull(toIdx) ?: continue
                val startRow = findNearestWalkable(grid, from.row, from.col)?.first ?: from.row
                val startCol = findNearestWalkable(grid, from.row, from.col)?.second ?: from.col
                val endRow = findNearestWalkable(grid, to.row, to.col)?.first ?: to.row
                val endCol = findNearestWalkable(grid, to.row, to.col)?.second ?: to.col
                val seg = astar(grid, startRow, startCol, endRow, endCol)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(stringResource(R.string.title_coworking), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            }
            Spacer(Modifier.weight(1f))
            if (selectedIdx.isNotEmpty() || antResult != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEEEE),
                    modifier = Modifier.clickable {
                        selectedIdx = emptySet(); antResult = null; routePaths = emptyList()
                    }
                ) {
                    Text(
                        stringResource(R.string.btn_reset), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCC3333),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
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
            val gridRows = grid.size; val gridCols = grid[0].size
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
                            val hit = allLandmarks.indexOfFirst { it.row == row && it.col == col }
                            if (hit >= 0) {
                                selectedIdx = if (hit in selectedIdx) selectedIdx - hit else selectedIdx + hit
                                antResult = null; routePaths = emptyList()
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

                            allLandmarks.forEachIndexed { i, lm ->
                                val center = Offset(lm.col * cellW + cellW / 2f, lm.row * cellH + cellH / 2f)
                                val isSel = i in selectedIdx
                                val rad = minOf(cellW, cellH) * if (isSel) 2.8f else 2f
                                drawCircle(Color.White, rad * 1.3f, center)
                                drawCircle(if (isSel) Color(0xFF900B09) else Color(0xFF78909C), rad, center)
                                drawCircle(Color.White, rad * 0.3f, center)
                            }

                            val res = antResult
                            if (res != null && res.route.size >= 2) {
                                val selList = selectedIdx.toList()
                                res.route.forEachIndexed { ord, idxInSel ->
                                    val lm = allLandmarks[selList[idxInSel]]
                                    val center = Offset(lm.col * cellW + cellW / 2f, lm.row * cellH + cellH / 2f)
                                    val rad = minOf(cellW, cellH) * 3.5f
                                    drawCircle(Color.White, rad, center)
                                    val color = if (ord == 0) Color(0xFF00C853)
                                    else if (ord == res.route.size - 1) Color(0xFFE53935)
                                    else Color(0xFF900B09)
                                    drawCircle(color, rad * 0.85f, center)
                                }
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
            modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (selectedIdx.isEmpty()) {
                    Text(stringResource(R.string.coworking_hint),
                        fontSize = 13.sp, color = Color.Gray)
                } else {
                    Text(stringResource(R.string.coworking_selected, selectedIdx.size), fontSize = 14.sp, color = Color(0xFF900B09), fontWeight = FontWeight.Medium)
                    if (antResult == null) {
                        if (selectedIdx.size >= 2) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (isRunning) return@Button
                                    val chosen = selectedIdx.toList().map { allLandmarks[it] }
                                    isRunning = true
                                    scope.launch {
                                        val res = withContext(Dispatchers.Default) { antColonyTSP(chosen, grid) }
                                        antResult = res
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
                                    Text(stringResource(R.string.coworking_btn_build), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Text(stringResource(R.string.coworking_need_two), fontSize = 12.sp, color = Color.Red)
                        }
                    } else {
                        val res = antResult!!
                        if (res.distance > 0) {
                            Text(stringResource(R.string.common_route_found), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                            Text(stringResource(R.string.coworking_distance, res.distance), fontSize = 13.sp, color = Color(0xFF666666))
                            Text(stringResource(R.string.coworking_iterations, res.iterations), fontSize = 12.sp, color = Color.Gray)
                            val selList = selectedIdx.toList()
                            res.route.forEachIndexed { ord, idxInSel ->
                                val lm = allLandmarks[selList[idxInSel]]
                                Text("${ord + 1}. [${lm.row}, ${lm.col}]  комфорт: ${"%.2f".format(lm.comfort)}",
                                    fontSize = 12.sp, color = Color(0xFF900B09))
                            }
                        } else {
                            Text(stringResource(R.string.coworking_not_found), fontSize = 14.sp, color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
