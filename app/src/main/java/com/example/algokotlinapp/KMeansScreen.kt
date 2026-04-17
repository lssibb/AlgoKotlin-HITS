package com.example.algokotlinapp

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.algokotlinapp.algorithms.KMeansResult
import com.example.algokotlinapp.algorithms.kmeans
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun KMeansScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current

    val mapInfo = remember {
        val lines = context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { it.readLines() }
        val grid = lines.map { line -> line.map { it.toString().toIntOrNull() ?: 0 }.toIntArray() }.toTypedArray()
        val points = CampusDictionary.keys.toList()
        grid to points
    }
    val grid = mapInfo.first
    val defaultPoints = mapInfo.second

    var userPoints by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var k by remember { mutableStateOf(3) }
    var result by remember { mutableStateOf<KMeansResult?>(null) }

    var scale by remember { mutableFloatStateOf(3f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val allPoints = defaultPoints + userPoints

    val clusterColors = listOf(
        Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
        Color(0xFFFF9800), Color(0xFF8E24AA), Color(0xFF00ACC1),
        Color(0xFFD81B60), Color(0xFF6D4C41)
    )

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(TsuBluePrimary, RoundedCornerShape(12.dp))) {
                Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Text("Кластеризация", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("K:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = { if (k > 2) { k--; result = null } },
                modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF3FF), contentColor = TsuBluePrimary)
            ) { Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Text("$k", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
            Button(
                onClick = { if (k < minOf(8, allPoints.size)) { k++; result = null } },
                modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF3FF), contentColor = TsuBluePrimary)
            ) { Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { if (allPoints.size >= k) result = kmeans(allPoints, k) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
            ) { Text("Кластеризовать", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
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
                            if (row in grid.indices && col in grid[0].indices && grid[row][col] != 0) {
                                val pt = col to row
                                userPoints = if (pt in userPoints) userPoints - pt else userPoints + pt
                                result = null
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

                            result?.let { res ->
                                res.centroids.forEachIndexed { ci, (cx, cy) ->
                                    val clusterPts = allPoints.filterIndexed { i, _ -> res.assignments[i] == ci }
                                    if (clusterPts.isNotEmpty()) {
                                        val maxDist = clusterPts.maxOf { (px, py) ->
                                            kotlin.math.sqrt(((px - cx) * (px - cx) + (py - cy) * (py - cy)).toFloat())
                                        }
                                        val radius = (maxDist + 5) * minOf(cellW, cellH)
                                        drawCircle(
                                            clusterColors[ci % clusterColors.size].copy(alpha = 0.15f),
                                            radius, Offset(cx.toFloat() * cellW + cellW / 2f, cy.toFloat() * cellH + cellH / 2f)
                                        )
                                    }
                                }
                            }

                            allPoints.forEachIndexed { i, (col, row) ->
                                val center = Offset(col * cellW + cellW / 2f, row * cellH + cellH / 2f)
                                val rad = minOf(cellW, cellH) * 2f
                                val color = if (result != null) clusterColors[result!!.assignments[i] % clusterColors.size]
                                else if (i < defaultPoints.size) Color(0xFF1EFF00) else Color(0xFFFF6D00)
                                drawCircle(Color.White, rad * 1.3f, center)
                                drawCircle(color, rad, center)
                                drawCircle(Color.White, rad * 0.3f, center)
                            }

                            result?.centroids?.forEachIndexed { ci, (cx, cy) ->
                                val center = Offset(cx.toFloat() * cellW + cellW / 2f, cy.toFloat() * cellH + cellH / 2f)
                                val rad = minOf(cellW, cellH) * 3f
                                drawCircle(clusterColors[ci % clusterColors.size], rad, center)
                                drawLine(Color.White, Offset(center.x - rad * 0.6f, center.y), Offset(center.x + rad * 0.6f, center.y), strokeWidth = 2f)
                                drawLine(Color.White, Offset(center.x, center.y - rad * 0.6f), Offset(center.x, center.y + rad * 0.6f), strokeWidth = 2f)
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
                Text(
                    "Точек: ${allPoints.size} (еда: ${defaultPoints.size}, свои: ${userPoints.size})",
                    fontSize = 13.sp, color = Color.Gray
                )
                Text("Тапните на карту, чтобы добавить/убрать точку", fontSize = 12.sp, color = Color.Gray)
                if (result != null) {
                    Spacer(Modifier.height(4.dp))
                    Text("Кластеризация за ${result!!.iterations} итераций", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                    result!!.centroids.forEachIndexed { i, (cx, cy) ->
                        val count = result!!.assignments.count { it == i }
                        Text("Кластер ${i + 1}: $count точек, центроид (${"%.1f".format(cx)}, ${"%.1f".format(cy)})",
                            fontSize = 12.sp, color = clusterColors[i % clusterColors.size], fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
