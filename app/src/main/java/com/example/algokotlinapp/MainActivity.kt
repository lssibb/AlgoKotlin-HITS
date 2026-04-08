package com.example.algokotlinapp
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
import com.example.algokotlinapp.algorithms.*
import com.example.algokotlinapp.ui.theme.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class LocationInfo(val name: String, val type: String, val description: String)

val CampusDictionary = mapOf(
    Pair(57, 73) to LocationInfo("Библиотека ТГУ", "Коворкинг", "Отличное место для учебы. Есть розетки и Wi-Fi."),
    Pair(88, 72) to LocationInfo("Главный Корпус (ГК)", "Учебный корпус", "Здесь находится приемная комиссия и актовый зал."),
    Pair(32, 14) to LocationInfo("Остановка 'Университет'", "Транспорт", "Можно уехать в любую точку города."),
    Pair(98, 72) to LocationInfo("Центр кампуса", "Ориентир", "Центральная точка университетской рощи."),
    Pair(20, 41) to LocationInfo("Остановка 'ТГУ'", "Транспорт", "Остановка на ул. Ленина."),
    Pair(47, 64) to LocationInfo("Вход в ФТФ", "Вход", "Физико-технический факультет."),
    Pair(29, 48) to LocationInfo("Вход в ГК", "Вход", "Главный корпус ТГУ."),
    Pair(71, 55) to LocationInfo("Столовая", "Еда", "Столовая для студентов и сотрудников."),
    Pair(15, 15) to LocationInfo("Продуктовый магазин", "Магазин", "Ближайший магазин продуктов.")
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
                            "KMeans"    -> KMeansScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
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
        Button(onClick = { onNavigate("KMeans") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Кластеризация (K-Means)", fontSize = 18.sp) }
        Button(onClick = { onNavigate("Coworking") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Коворкинги (Муравьи)", fontSize = 18.sp) }
        Button(onClick = { onNavigate("NeuralNet") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("Оценка: Нейросеть", fontSize = 18.sp) }
    }
}

@Composable
fun CampusMapScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val mapData = remember {
        val lines = context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { it.readLines() }
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
                    '8' -> Color(0x660011FF)
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

    val grid = remember {
        rawLines.map { line -> line.map { it.toString().toIntOrNull() ?: 0 }.toIntArray() }.toTypedArray()
    }

    var scale by remember { mutableFloatStateOf(3f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedLocation by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var displayedLocation by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var routeStart by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var routeEnd by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val routePath = remember(routeStart, routeEnd) {
        if (routeStart != null && routeEnd != null)
            astar(grid, routeStart!!.first, routeStart!!.second, routeEnd!!.first, routeEnd!!.second)
        else null
    }

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
            if (routeStart != null || routeEnd != null) {
                Button(
                    onClick = { routeStart = null; routeEnd = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF3FF), contentColor = TsuBluePrimary)
                ) { Text("Сброс", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                Spacer(Modifier.width(8.dp))
            }
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
                            val maxOffsetX = ((mW * newScale) - bW).coerceAtLeast(0f) / 2f
                            val maxOffsetY = ((mH * newScale) - bH).coerceAtLeast(0f) / 2f
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

                            routePath?.forEach { (r, c) ->
                                drawRoundRect(
                                    Color(0xCCFF6D00),
                                    topLeft = Offset(c * cellW, r * cellH),
                                    size = Size(cellW, cellH),
                                    cornerRadius = CornerRadius(1f, 1f)
                                )
                            }

                            routeStart?.let { (r, c) ->
                                val center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                val radius = minOf(cellW, cellH) * 2.5f
                                drawCircle(Color.White, radius * 1.3f, center)
                                drawCircle(Color(0xFF00C853), radius, center)
                                drawCircle(Color.White, radius * 0.35f, center)
                            }
                            routeEnd?.let { (r, c) ->
                                val center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                val radius = minOf(cellW, cellH) * 2.5f
                                drawCircle(Color.White, radius * 1.3f, center)
                                drawCircle(Color(0xFFE53935), radius, center)
                                drawCircle(Color.White, radius * 0.35f, center)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        "×${"%.1f".format(scale)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TsuBluePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (selectedLocation == null && (routeStart != null || routeEnd != null)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (routeStart != null) Text(
                        "Откуда: [${routeStart!!.second}, ${routeStart!!.first}]",
                        fontSize = 14.sp, color = Color(0xFF00C853), fontWeight = FontWeight.Medium
                    )
                    if (routeEnd != null) Text(
                        "Куда: [${routeEnd!!.second}, ${routeEnd!!.first}]",
                        fontSize = 14.sp, color = Color(0xFFE53935), fontWeight = FontWeight.Medium
                    )
                    if (routePath != null) Text(
                        "Маршрут найден! ${routePath!!.size} шагов",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary
                    ) else if (routeStart != null && routeEnd != null) Text(
                        "Маршрут не найден",
                        fontSize = 14.sp, color = Color.Red
                    ) else Text(
                        if (routeStart == null) "Выберите начальную точку" else "Выберите конечную точку",
                        fontSize = 13.sp, color = Color.Gray
                    )
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
                                onClick = {
                                    routeEnd = Pair(loc.second, loc.first)
                                    selectedLocation = null
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
                            ) { Text("Маршрут сюда", fontWeight = FontWeight.SemiBold) }
                            OutlinedButton(
                                onClick = {
                                    routeStart = Pair(loc.second, loc.first)
                                    selectedLocation = null
                                },
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
@Composable
fun RouteScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val grid = remember {
        context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { reader ->
            reader.readLines().map { line ->
                line.map { it.toString().toInt() }.toIntArray()
            }.toTypedArray()
        }
    }

    var start by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var end by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val path = remember(start, end) {
        if (start != null && end != null)
            astar(grid, start!!.first, start!!.second, end!!.first, end!!.second)
        else null
    }

    var scale by remember { mutableFloatStateOf(3f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

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
            Text("Навигация (A*)", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { start = null; end = null; scale = 3f; offsetX = 0f; offsetY = 0f },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF3FF), contentColor = TsuBluePrimary)
            ) { Text("Сбросить", fontWeight = FontWeight.SemiBold) }
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
            val gridRows = grid.size
            val gridCols = grid[0].size
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
                            val maxOffsetX = ((mW * newScale) - bW).coerceAtLeast(0f) / 2f
                            val maxOffsetY = ((mH * newScale) - bH).coerceAtLeast(0f) / 2f
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
                            val col = (worldX / cellW).toInt()
                            val row = (worldY / cellH).toInt()
                            if (row in grid.indices && col in grid[0].indices && grid[row][col] != 0) {
                                if (start == null) {
                                    start = Pair(row, col)
                                } else if (end == null) {
                                    end = Pair(row, col)
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
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (row in 0 until gridRows) {
                                for (col in 0 until gridCols) {
                                    val color = when (grid[row][col]) {
                                        0 -> Color(0xFF808080)
                                        1 -> Color(0xFFFFFF00)
                                        2 -> Color(0xFFFF00E6)
                                        3 -> Color(0xFF00EEFF)
                                        4 -> Color(0xFF1EFF00)
                                        5 -> Color(0xFFFF0400)
                                        6 -> Color(0xFF900B09)
                                        7 -> Color(0xFF532C00)
                                        8 -> Color(0xFF0011FF)
                                        else -> Color.LightGray
                                    }
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(col * cellW, row * cellH),
                                        size = Size(cellW, cellH)
                                    )
                                }
                            }
                            start?.let { (r, c) ->
                                drawCircle(
                                    color = Color.Cyan,
                                    radius = cellW * 2,
                                    center = Offset(c * cellW + cellW / 2, r * cellH + cellH / 2)
                                )
                            }
                            end?.let { (r, c) ->
                                drawCircle(
                                    color = Color.Red,
                                    radius = cellW * 2,
                                    center = Offset(c * cellW + cellW / 2, r * cellH + cellH / 2)
                                )
                            }
                            path?.forEach { (r, c) ->
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(c * cellW, r * cellH),
                                    size = Size(cellW, cellH)
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        "×${"%.1f".format(scale)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TsuBluePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (start != null || end != null || path != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (start != null) Text("Старт: [${start!!.second}, ${start!!.first}]", fontSize = 14.sp, color = Color(0xFF00897B))
                    if (end != null) Text("Финиш: [${end!!.second}, ${end!!.first}]", fontSize = 14.sp, color = Color(0xFFE53935))
                    if (path != null) Text("Маршрут найден! ${path!!.size} шагов", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                    else if (start != null && end != null) Text("Маршрут не найден", fontSize = 14.sp, color = Color.Red)
                    else if (start != null && end == null) Text("Выберите конечную точку", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
    }
}

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
            Text("Где поесть? (ГА)", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            Spacer(Modifier.weight(1f))
            if (gaResult != null) {
                Button(
                    onClick = { gaResult = null; routePaths = emptyList(); startPoint = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF3FF), contentColor = TsuBluePrimary)
                ) { Text("Сброс", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            foodTypes.forEach { food ->
                val label = FOOD_LABELS[food] ?: food
                val isSel = food in selectedFoods
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSel) TsuBluePrimary else Color(0xFFEEF3FF),
                    modifier = Modifier.height(36.dp).clickable {
                        selectedFoods = if (isSel) selectedFoods - food else selectedFoods + food
                        gaResult = null; routePaths = emptyList()
                    }
                ) {
                    Text(
                        label, fontSize = 12.sp,
                        color = if (isSel) Color.White else Color(0xFF666666),
                        fontWeight = FontWeight.Medium,
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
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (row in 0 until gridRows) {
                                for (col in 0 until gridCols) {
                                    val color = when (grid[row][col]) {
                                        0 -> Color(0xFF808080); 8 -> Color(0xFF0011FF).copy(alpha = 0.3f)
                                        else -> Color(0xFFB0BEC5)
                                    }
                                    drawRect(color, topLeft = Offset(col * cellW, row * cellH), size = Size(cellW, cellH))
                                }
                            }

                            routePaths.forEach { (r, c) ->
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
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White, shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (startPoint == null) {
                    Text("Тапните на карту, чтобы задать стартовую точку", fontSize = 13.sp, color = Color.Gray)
                } else if (gaResult == null) {
                    Text("Старт: [${startPoint!!.second}, ${startPoint!!.first}]", fontSize = 14.sp, color = Color(0xFF00C853))
                    if (selectedFoods.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isRunning = true
                                gaResult = foodGeneticAlgorithm(places, selectedFoods, startPoint!!.first, startPoint!!.second, grid)
                                isRunning = false
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
                        ) { Text("Построить маршрут (ГА)", fontWeight = FontWeight.SemiBold) }
                    } else {
                        Text("Выберите хотя бы один тип еды", fontSize = 13.sp, color = Color.Red)
                    }
                } else {
                    val res = gaResult!!
                    if (res.distance > 0) {
                        Text("Маршрут найден!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                        Text("Дистанция: ${res.distance} шагов (~${"%.0f".format(res.distance * 0.3)} м)", fontSize = 14.sp, color = Color(0xFF666666))
                        Text("Поколений ГА: ${res.generations}, лучший фитнес: ${res.bestPerGeneration.lastOrNull()}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        res.route.forEachIndexed { idx, placeIdx ->
                            val p = places[placeIdx]
                            Text("${idx + 1}. ${p.name} (${p.menu.filter { it in selectedFoods }.joinToString { FOOD_LABELS[it] ?: it }})",
                                fontSize = 13.sp, color = placeColors[placeIdx % placeColors.size], fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text("Маршрут не найден", fontSize = 14.sp, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun KMeansScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current

    val mapInfo = remember {
        val lines = context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { it.readLines() }
        val grid = lines.map { line -> line.map { it.toString().toIntOrNull() ?: 0 }.toIntArray() }.toTypedArray()
        val foodPts = mutableListOf<Pair<Int, Int>>()
        for (r in lines.indices) for (c in lines[r].indices) {
            if (lines[r][c] == '4') foodPts.add(c to r)
        }
        grid to foodPts
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
            Text("Кластеризация (K-Means)", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
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
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (row in 0 until gridRows) {
                                for (col in 0 until gridCols) {
                                    val color = when (grid[row][col]) {
                                        0 -> Color(0xFF808080); 8 -> Color(0xFF0011FF).copy(alpha = 0.3f)
                                        else -> Color(0xFFB0BEC5)
                                    }
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
