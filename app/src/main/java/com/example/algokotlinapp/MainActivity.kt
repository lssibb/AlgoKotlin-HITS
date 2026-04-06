package com.example.algokotlinapp
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
import com.example.algokotlinapp.algorithms.astar
import com.example.algokotlinapp.ui.theme.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class LocationInfo(val name: String, val type: String, val description: String)

val CampusDictionary = mapOf(
    Pair(50, 40) to LocationInfo("Библиотека ТГУ", "Коворкинг", "Отличное место для учебы. Есть розетки и Wi-Fi."),
    Pair(35, 60) to LocationInfo("Главный Корпус (ГК)", "Учебный корпус", "Здесь находится приемная комиссия и актовый зал."),
    Pair(60, 50) to LocationInfo("Остановка 'Университет'", "Транспорт", "Можно уехать в любую точку города.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlgoKotlinAppTheme {
                var currentScreen by remember { mutableStateOf("MainMenu") }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        label = "screen_transition",
                        transitionSpec = {
                            if (targetState != "MainMenu" && initialState == "MainMenu") {
                                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(tween(400)) togetherWith
                                        slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it / 2 }) + fadeOut(tween(400))
                            } else {
                                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it / 2 }) + fadeIn(tween(400)) togetherWith
                                        slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(tween(400))
                            }
                        }
                    ) { targetScreen ->
                        when (targetScreen) {
                            "MainMenu"  -> MainMenuScreen(modifier = Modifier.padding(innerPadding), onNavigate = { currentScreen = it })
                            "Map"       -> CampusMapScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "Route"     -> RouteScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "Food"      -> FoodScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "Coworking" -> CoworkingScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "NeuralNet" -> NeuralNetScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.tsulogo1), contentDescription = null, modifier = Modifier.size(160.dp).padding(bottom = 8.dp))
        Text("TSU.MyMap", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { onNavigate("Map") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Карта кампуса", fontSize = 18.sp) }
        Button(onClick = { onNavigate("Route") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Навигация (A*)", fontSize = 18.sp) }
        Button(onClick = { onNavigate("Food") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Где поесть? (Генетика)", fontSize = 18.sp) }
        Button(onClick = { onNavigate("Coworking") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Коворкинги (Муравьи)", fontSize = 18.sp) }
        Button(onClick = { onNavigate("NeuralNet") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Оценка: Нейросеть", fontSize = 18.sp) }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CampusMapScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val mapData = remember {
        val lines = CAMPUS_MAP_DATA.trim().lines()
        val gridRows = lines.size
        val gridCols = lines.maxOfOrNull { it.length } ?: 0
        val tiles = mutableListOf<Triple<Int, Int, Color>>()
        for (r in lines.indices) {
            for (c in lines[r].indices) {
                val color = when (lines[r][c]) {
                    '1' -> Color(0xFFFFFF00)
                    '2' -> Color(0xFFFF00E6)
                    '3' -> Color(0xFF00EEFF)
                    '4' -> Color(0xFF1EFF00)
                    '5' -> Color(0xFFFF0400)
                    '6' -> Color(0xFF900B09)
                    '7' -> Color(0xFF532C00)

                    else -> Color.Transparent
                }
                if (color != Color.Transparent) tiles.add(Triple(c, r, color))
            }
        }
        Pair(Triple(gridCols, gridRows, tiles), lines)
    }

    val gridCols = mapData.first.first
    val gridRows = mapData.first.second
    val activeTiles = mapData.first.third
    val rawLines = mapData.second

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedLocation by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var displayedLocation by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(selectedLocation) {
        if (selectedLocation != null) displayedLocation = selectedLocation
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
                    "Карта кампуса",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A2E)
                )
                Text("ТГУ · Томск", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEEF3FF)) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TsuBluePrimary,
                    modifier = Modifier.padding(8.dp).size(22.dp)
                )
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
        ){
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
                            val newScale = (scale * zoom).coerceIn(1f, 8f)
                            scale = newScale
                            val maxOffsetX = ((mW * scale) - bW).coerceAtLeast(0f) / 2f
                            val maxOffsetY = ((mH * scale) - bH).coerceAtLeast(0f) / 2f
                            offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            val pivX = bW / 2f
                            val pivY = bH / 2f
                            val worldX = (tap.x - pivX - offsetX) / scale + pivX - (bW - mW) / 2f
                            val worldY = (tap.y - pivY - offsetY) / scale + pivY - (bH - mH) / 2f
                            val gridX = (worldX / cellW).toInt()
                            val gridY = (worldY / cellH).toInt()
                            if (gridY in rawLines.indices && gridX >= 0 && gridX < (rawLines.getOrNull(gridY)?.length ?: 0)) {
                                val ch = rawLines[gridY][gridX]
                                selectedLocation = if (ch != '0' && ch != '8') Pair(gridX, gridY) else null
                            } else {
                                selectedLocation = null
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
                            activeTiles.forEach { (c, r, color) ->
                                val cx = c * cellW
                                val cy = r * cellH
                                val center = Offset(cx + cellW / 2f, cy + cellH / 2f)
                                val isSel = selectedLocation?.first == c && selectedLocation?.second == r
                                if (color == Color(0x660011FF)) {
                                    drawRoundRect(
                                        Color(0xBB1A6FFF),
                                        topLeft = Offset(cx, cy),
                                        size = Size(cellW, cellH),
                                        cornerRadius = CornerRadius(2f, 2f)
                                    )
                                } else {
                                    val radius = minOf(cellW, cellH) * 1.8f
                                    if (isSel) {
                                        drawCircle(TsuBluePrimary.copy(alpha = 0.25f), radius * 2.8f, center)
                                        drawCircle(Color.White, radius * 1.6f, center)
                                    }
                                    drawCircle(Color.Black.copy(alpha = 0.15f), radius + 1.5f, Offset(center.x + 1f, center.y + 1.5f))
                                    drawCircle(color, radius, center)
                                    drawCircle(Color.White, radius * 0.38f, center)
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
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 6.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                scale = (scale * 1.4f).coerceIn(1f, 8f)
                            },
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

        AnimatedVisibility(
            visible = selectedLocation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(250)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200))
        ) {
            displayedLocation?.let { loc ->
                val info = CampusDictionary[loc] ?: LocationInfo(
                    "Точка [${loc.first}, ${loc.second}]",
                    "Неизвестный объект",
                    "Добавь информацию об этой точке в CampusDictionary."
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 28.dp)) {
                        Box(
                            modifier = Modifier
                                .width(40.dp).height(4.dp)
                                .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFEEF3FF),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TsuBluePrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(info.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                                Text(info.type, fontSize = 13.sp, color = TsuBluePrimary, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = { selectedLocation = null }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFBBBBBB))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(info.description, fontSize = 14.sp, color = Color(0xFF666666), lineHeight = 20.sp)
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
                            ) { Text("Маршрут сюда", fontWeight = FontWeight.SemiBold) }
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, TsuBluePrimary)
                            ) { Text("Отсюда", fontWeight = FontWeight.SemiBold, color = TsuBluePrimary) }
                        }
                    }
                }
            }
        }
    }
}
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RouteScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current

    val grid = remember {
        val reader = context.assets.open("tsu_campus_matrix.txt").bufferedReader()
        val lines = reader.readLines()
        lines.map { line ->
            line.map { it.toString().toInt() }.toIntArray()
        }.toTypedArray()
    }

    val mapData = remember {
        val lines = CAMPUS_MAP_DATA.trim().lines()
        val gridRows = lines.size
        val gridCols = lines.maxOfOrNull { it.length } ?: 0
        val tiles = mutableListOf<Triple<Int, Int, Color>>()
        for (r in lines.indices) {
            for (c in lines[r].indices) {
                val color = when (lines[r][c]) {
                    '1' -> Color(0xFFFFFF00)
                    '2' -> Color(0xFFFF00E6)
                    '3' -> Color(0xFF00EEFF)
                    '4' -> Color(0xFF1EFF00)
                    '5' -> Color(0xFFFF0400)
                    '6' -> Color(0xFF900B09)
                    '7' -> Color(0xFF532C00)
                    '8' -> Color(0x880044CC)
                    else -> Color.Transparent
                }
                if (color != Color.Transparent) tiles.add(Triple(c, r, color))
            }
        }
        Triple(gridCols, gridRows, tiles)
    }
    val mapCols = mapData.first
    val mapRows = mapData.second
    val allTiles = mapData.third

    var start by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var end by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedStart by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedEnd by remember { mutableStateOf<Pair<Int, Int>?>(null) }

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
            val fit = minOf(bW / mapCols, bH / mapRows)
            val mW = fit * mapCols
            val mH = fit * mapRows
            val cellW = mW / mapCols
            val cellH = mH / mapRows

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
                    .pointerInput(Unit) {
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
                            val pathSet = path?.toSet() ?: emptySet()

                            allTiles.forEach { (c, r, color) ->
                                val cx = c * cellW
                                val cy = r * cellH
                                val center = Offset(cx + cellW / 2f, cy + cellH / 2f)

                                if (color == Color(0x880044CC)) {
                                    drawRoundRect(
                                        Color(0x661A6FFF),
                                        topLeft = Offset(cx, cy),
                                        size = Size(cellW, cellH),
                                        cornerRadius = CornerRadius(1.5f, 1.5f)
                                    )
                                } else {
                                    val radius = minOf(cellW, cellH) * 1.8f
                                    drawCircle(Color.Black.copy(alpha = 0.12f), radius + 1.5f, Offset(center.x + 1f, center.y + 1.5f))
                                    drawCircle(color, radius, center)
                                    drawCircle(Color.White, radius * 0.38f, center)
                                }
                            }

                            pathSet.forEach { (r, c) ->
                                drawCircle(
                                    Color(0xFFFFFFFF).copy(alpha = 0.85f),
                                    radius = minOf(cellW, cellH) * 0.7f,
                                    center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                )
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

@Composable
fun FoodScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Назад") }
        Text("Генетический алгоритм", fontSize = 24.sp)
    }
}

@Composable
fun CoworkingScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Назад") }
        Text("Муравьиный алгоритм", fontSize = 24.sp)
    }
}

class NativeInference(private val context: android.content.Context) {
    private val inputSize = 2500
    private val hiddenSize = 128
    private val outputSize = 10
    private val W1 = loadFloatArray("w1.bin", hiddenSize * inputSize)
    private val b1 = loadFloatArray("b1.bin", hiddenSize)
    private val W2 = loadFloatArray("w2.bin", outputSize * hiddenSize)
    private val b2 = loadFloatArray("b2.bin", outputSize)

    private fun loadFloatArray(fileName: String, expectedSize: Int): FloatArray {
        val stream = context.assets.open(fileName)
        val bytes = stream.readBytes()
        stream.close()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(expectedSize).also { buffer.asFloatBuffer().get(it) }
    }

    fun predict(X: FloatArray): Int {
        val Z1 = FloatArray(hiddenSize) { i ->
            var sum = b1[i]
            for (j in 0 until inputSize) sum += W1[i * inputSize + j] * X[j]
            if (sum > 0) sum else 0f
        }
        val Z2 = FloatArray(outputSize) { i ->
            var sum = b2[i]
            for (j in 0 until hiddenSize) sum += W2[i * hiddenSize + j] * Z1[j]
            sum
        }
        return Z2.indices.maxByOrNull { Z2[it] } ?: 0
    }
}

fun centerAndFlattenImage(pixels: List<Boolean>, gridSize: Int): FloatArray {
    var minX = gridSize; var maxX = -1; var minY = gridSize; var maxY = -1
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            if (pixels[row * gridSize + col]) {
                minX = minOf(minX, col); maxX = maxOf(maxX, col)
                minY = minOf(minY, row); maxY = maxOf(maxY, row)
            }
        }
    }
    val result = FloatArray(gridSize * gridSize)
    if (maxX < 0) return result
    val offsetX = (gridSize - (maxX - minX + 1)) / 2 - minX
    val offsetY = (gridSize - (maxY - minY + 1)) / 2 - minY
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            if (pixels[row * gridSize + col]) {
                val nr = row + offsetY; val nc = col + offsetX
                if (nr in 0 until gridSize && nc in 0 until gridSize) result[nr * gridSize + nc] = 1.0f
            }
        }
    }
    return result
}

@Composable
fun NeuralNetScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val gridSize = 50
    val pixels = remember { mutableStateListOf(*Array(gridSize * gridSize) { false }) }
    val context = LocalContext.current
    var prediction by remember { mutableStateOf<Int?>(null) }
    val nativeNet = remember { try { NativeInference(context) } catch (e: Exception) { null } }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("Назад") }
        Text("Рисование цифры", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
        Text(if (prediction != null) "Результат: $prediction" else "Нарисуйте цифру", fontSize = 18.sp)
        Box(
            modifier = Modifier.size(300.dp).border(2.dp, PixelBorder).background(PixelEmpty)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    val col = (change.position.x / (size.width / gridSize)).toInt()
                                    val row = (change.position.y / (size.height / gridSize)).toInt()
                                    if (col in 0 until gridSize && row in 0 until gridSize) {
                                        for (i in -1..1) for (j in -1..1) {
                                            val nr = row + i; val nc = col + j
                                            if (nr in 0 until gridSize && nc in 0 until gridSize) pixels[nr * gridSize + nc] = true
                                        }
                                    }
                                    change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cw = size.width / gridSize; val ch = size.height / gridSize
                for (i in pixels.indices) if (pixels[i]) drawRect(PixelFilled, topLeft = Offset((i % gridSize) * cw, (i / gridSize) * ch), size = Size(cw, ch))
            }
        }
        Button(onClick = { prediction = nativeNet?.predict(centerAndFlattenImage(pixels.toList(), gridSize)) }, modifier = Modifier.padding(top = 16.dp)) { Text("Распознать") }
        OutlinedButton(onClick = { pixels.fill(false); prediction = null }) { Text("Очистить") }
    }
}


const val CAMPUS_MAP_DATA = """
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000888800000008888888888886888888800008888888887888888888888888888888888888888888888888800000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000800888888888000008000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000008800000080008000008000000000000800008888888888888888888888888888888800000000000000000800000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000088808000008000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000008880000000000000000000000000000000088000000000888000006000000000000800008000000000000000000000000000000870000000000000000800000
00000000000000000000000000000000300000000000000000080000000000000000000000000000000080000000000008000000000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000058800000000000000800000000000000000080000000000000000000000000000000880000000000008000000000000000000800008000000000000000000000000000000800000888888888000800000
00000000000000000800000000008888800000000000000000080000000000000000000000000000000800000000000008000000000000000000800008000000000000000000000000000000800000800000008000800000
00000000000000000800000000008000000000000000000000080000000000000888888880000000008800000000000008000000000000000000800008000000000000000000000000000000850000800000008000800000
00000000000000000800000000008000000000000000000000088888888888888800000088888000008000000000000008000000000000000000800008000000000000000000000000000000800000800000008000800000
00000000000000000800000088888000000000000000000000008000000000000000000000008888888000000000000008000000000000000000800008000000000000000000000000000000800000800000008888800000
00000000000000000800000080000000000000000000000000008000000000000000000000000000080000000000000000000000000000000000800008000000000000000000000000000000800000800000000000800000
00000000000000000800000080000000000000000000000000088000000000000000000000000000080000000000000000000000000000000000800008000000000000000000000000000000800000800000000000800000
00000000000000008800000080000000000000000000000000080000000000000000000000000000880000000000000000000000000000000000800008000000000000000000000000000000800000800000000000800000
00000000000000008000088880000000000800000000000000880000000000000000000000000000800000000000000000000000000000000000800008000000000000000000000000000000888888800000000000800000
00000000000000008000080000000000000800000000000000800000000000000000000000000000800000000000000000000860000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000888888888888888800000000000000000000000000008800000000000000000000800000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000088888888888880800000000000000000000000000000000000000000008000000000000000000000800000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000088800000000000000000000000000000000000000000008000000000000000000088800000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000888880000000000000000000000000000000000000088000000000000000088880800000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000000088880000000000000000000000000000000000880000000000000008880000800000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000000000088888000000000000000000000000000008800000000000000888000000888000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000000000000008880000000000000000000000000008000000000000088800000000808000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000000000000000088880000000000000000000000008000000000008880000000000808000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000000000000000000088888800000000000000000088000000008888000000000000808000000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000000000000000000008000888888000000000000080000000088000000000000000808800000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000000000000000000000000000008000000008888888000000080000000880000000000000000800800000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000700000000888888000000000088000000000080008888888888888888800000000000000000800800000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000800000000808008000000000080000000000080000000008008000000880000000000000000800880000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000800000000808088000000000080000000000080000008888008000000080000000000000000800080000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000800000000808080000000000080000000000880000088008008000000088000000000000000800080000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000800000000808080000000000080000000088800000880008008000000008000000000000000800088000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000380000800000000808080000000000880000000080800008800008008000000008000000000000000800008000000000800008000000000000000000000000000000800000000000000000800000
00000000000000008000080000800000000808880000000000800000000080800008000008008000000008800000000000008800008800000000800008000000000000000000000000000000888888880000000000800000
00000000000000008000080000800000000808000000000000800000008880800088000088008000000000888000000000008000000800000000800008000000000000000000000000000000800000080000000000800000
00000000000000008000080000800000000808000000000000800000000088888880000080008000000000088800000000008000000888000000800008000000000000000000000000000000800000080000000000800000
00000000000000008000080000800000000808000000000008800000000000000088880880008000000000080880000000088000000008000000800008000000000000000000000000000000800000080000000000800000
00000000000000008000080000800000000888000000000008000000000000000080088800088000000000088088000000080000000008000000800008000000000000000000000000000000800000080000000000800000
00000000000000008880080000800000000808000000000008000000000000000880070800888000000000008008800000080000000008000000800008000000000000000000000000000000800000080000000000800000
00000000000000000088888888888700000808000000000008000000000000000800000888808888000000008000888000080000000008800000800008000000000000000000000000000000800000080000000000800000
00000000000000000080000000000000000808000000000008000000000000000800000080000008888000008000008888888000000000800000800008000000000000000000000000000000800000080000000000800000
00000000000000000080000000000000000888000000000008888800000088888800000080000000008000008800000080808000000000880000800008000000000000000000000000000000800000088888888888800000
00000000000000000080000000000000000800000088888888000888800080000000000080000000008000000800000880808880000000088000800008000000000000000000000000000000800000000000000000800000
00000000000000000080000000000000000888888880000000000000888880000000000080000000008000000880008800800088000000008000800008000000000000000000000000000000800000000000000000800000
00000000000000000080000000000000008800000000000000000000800000000000000080000000008000000080088088888008800000008800800008000000000000000000000000000000800000000000000000800000
00000000000000000080000000000000008000000000000000000000800000000000000080000000008000000080880080808000880000000880800008000000000000000000000000000000800000000000000000800000
00000000000000000088888888888888888000000000000000000008800000000000000484000000008000000088800880808880088000000080800003000000000000000000888888888888800000000000000000800000
00000000000000000080000000000000000000000000000000000008000000000000000080000000008000000008800800800080008000000080800008000000000000000000800000000000800000000000000000800000
00000000000000000080000000000000000000000000000000000008000000000000000080000000008000000008888888288888888888888888880008000000000000000000800000000000800000000000000000800000
00000000000000000080000000000000000000000000000000000008000000000000000080000000008000000078888888888888888888888888880008000000000000000000800000000000850000000000000000800000
00000000000000008880000000000000000000000000000000000008000000000000000084000000008000000008000800800080008000000080880008000000000000000000800000000000800000000000000000800000
00000000000000008000000000000000000000000000000008888888000000000000000080000000008000000008000880800880088600000080888888888000000000000000800000000000800000000000000000800000
00000000000000008000000000000000000000000000000008000000000000000000000080000000008000000008800080800800880000000080880008008888888888888888800000000000888888888888888888800000
00000000000000008000000000000000000000000000000008000000000000000000888880000000008000000088880088888808800000000080880008808000000000000000000000000000800000000000000000800000
00000000000000008000000000000000000000000000000008000000000000000000800000000000008000000080088000800088000000000080880000808000000000000000000000000000800000000000000000800000
00000000000000008000000000000000000000000000000788888888888888880088800000000000008000000880008880808880000000000080880000808000000000000000000000000000800000000000000000800000
00000000000000008000000000000000000000000000000000000000000008088080000000000008888000000800000088888000000000008880880000808840000000000000000000000000800000000000000000800000
00000000000000008000000000000000000000000000000000000000000008008880000000000088000000000800000088000000000000008000880000800000000000000000000000000000800000000000000000800000
00000000000000008000000000000000000000000000000000000000000008000080000888888880000000008800000888800000000000008000880000800000000000000000000000000000800000000000000000800000
00000000000000388000000000000000000000000000000000000000048808000088088800008000000000008000088800880000000000008000880000800000000000000000000000000000800000000000000000800000
00000000000000080000000000000000000000000000000000000000000808000008880000008000000000088000880000080000000000008000880000800000000000000000000000000000800000000000000000800000
00000000000000080000000000000000000000000000000000000000000808000008088000008800000000080008800000088000000000068000880000800000000000000000000000000000800000000000000000800000
00000000000000880000000000000000000000000000000000000000048808000088008880000800000000080088000000008800000000000000880000800000000000000000000000000000800000000000000000800000
00000000000000800000000000000000000000000000000000000000000808000080000088888800000000088880000000000880000000000000880000800000000000000000000000000000800000000000000000800000
00000000000000800000000000000000000000000000000000000000018888888888888880000800000000888000000000000080000000000000880000800000000000000000000000000000800000000000000000800000
00000000000000800000000000000000000000000000000000000000000808000000000000000800000000888000000000006888000000000000880000800000000000000000000000000000800000000000000000800000
00000000000000800000000000000000000000000000000000000000078808000000000000000800000008800000000000000008000000000000880000800000000000000000000000000000800000000000000000800000
00000000000000800000000000000000000000000000000000000000000008000000000000000800000088000000000000000008800000000000880000800000000000000000000000000000800000000000000000800000
00000000000008800000000000000000000000000000000000000000000008000000000000000800000080000000000000000000800000000000880000800000000000000000000000000000800000000000000000800000
00000000000008000000000000000000000000000000000000000000000008000000000000000800000880000000000000000000888000000000880000800000000000000000000000000000800000000000000000800000
00000000000008000000000000000000000000000000000000000000000008000000000000000800000800000000000000000000808000000000880000800000000000000000000000000000800000000000000000800000
00000000000008000000000000000000000000000000000000000000000008888888888888888888888800000000000000000000808888888000880000800000000000000000000000000000800000000000000000800000
00000000000008000000000000000000000000000000000000000000000008008000800000000000888000000000000000000088808000008888880000800000000000000000000000000000800000000000000000800000
00000000000008000000000000000000000888888000000000000000000088008000700000000000088880000000000000000088008000008000080000800000000000000000000000000000800000000000000000800000
00000000000088000000000000000000000800008888888888888888888880008000000000000000080088000000000000000088008008888000080000800000000000000000000000000000800000000000000000800000
00000000000080000000000000000000000800000000000000000000000000008000000000000000080008880000000000000088008888000000083000800000000000000000000000000000800000000000000000800000
00000000000080000000000000000000000800000000000000000000000000008000000000000000080000088888000000000088000000000000080000800000000000000000000000000000800000000000000000800000
00000000000080000000000000000000000800000000000000000000000000008000000000000000880000000088800000000088000000000000080000800000000000000000000000000000800000000000000000800000
00000000000080000000000000000000000800000000000000000000000800008000000000000000800000000000888800000088000000000000080000800000000000000000000000000000800000000000000000800000
00000000000080000000000000000000000800000000000000000000000888808000000000000000800000000000000888000088000000000000080000800000000000000000000000000000800000000000000000800000
00000000000080000000000000000000006800000000000000000000000000888000000000000000800000000000000008888888000000000000080000800000000000000000000000000000800000000000000000800000
00000000000080000000000000000000000800000000000000000000000000008888800000000008800000000000000000000888000000000000080000800000000000000000000000000000800000000000000000800000
00000000000880000000000000000000000800000000000000000000000000000000888888888888888888888888888888888888888888888888880000800000000000000000000000000000800000000000000000800000
00000000000800000000000000000000000800000000000000000000000000000008800060000008000000000000000000000000000100000000000000800000000000000000000000000000800000000000000000800000
00000000003800000000000000000000000800000000000000000000000000000008000000000008000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000800000000000000000000000800000000000000000000000000000008000000000008000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000800000000000000000000000800000000000000000000000000000000000000000008000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000008800000000000000000000000800000000000000000000000000000000000000000008000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000008000000000000000000000000800000000000000000000000000000000000000000008000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000008888888888888888888888888800086000000000000000000000000000000000000088000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000080000000000000000000000000000000000000080000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000080000000000000000000000000000000000000880000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000088888888888888888888880000000000000000800000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000088000000000000008800000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000008800000000000008000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000000800000000000008000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000000888000000000088000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000000008000000000080000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000000008000000000080000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000000008000000000080000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000888800000888800000000080000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000888808800880000000080000000000000000000000000000000000000000000000800000000000000000000000000000800000000000000000800000
00000000000000000000000000000000000000000000000000000000000888000088000000080000000000000000000000000000000000000000000000888888888888888888888888888888888888888888888888880000
00000000000000000000000000000000000000000000000000000000000080000008800000080000000000000000000000000000000000000000000000800000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000008880000000880000080000000000000000000000000000000000000000000000800000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000088000000000088008888800000000000000000000000000000000000000000000888888888888888888888888888888888888888888888888880000
00000000000000000000000000000000000000000000000000000000080000000000008888000888600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000880000000000000880000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000800000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000800000000000008800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000800000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000800000000000088000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000888000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000008868888888880000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000580000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
"""