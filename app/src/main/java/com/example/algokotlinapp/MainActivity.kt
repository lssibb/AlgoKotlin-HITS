package com.example.algokotlinapp
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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

data class LocationInfo(val name: String, val type: String, val description: String, val color: Long)

val CampusDictionary = mapOf(
    Pair(41, 14) to LocationInfo("Общежитие университета", "Остановка", "Общежитие рядом с остановкой транспорта.", 0xFF1E88E5),
    Pair(19, 15) to LocationInfo("Абрикос", "Магазин", "Продуктовый магазин.", 0xFF9C27B0),
    Pair(26, 41) to LocationInfo("Юридический институт ТГУ", "Остановка", "Остановка рядом с институтом.", 0xFF1E88E5),
    Pair(37, 48) to LocationInfo("Юридический институт ТГУ", "Вход", "Вход в Юридический институт.", 0xFF795548),
    Pair(18, 68) to LocationInfo("Юридический институт ТГУ", "Остановка", "Остановка рядом с институтом.", 0xFF1E88E5),
    Pair(13, 93) to LocationInfo("Белая соборная мечеть", "Остановка", "Мечеть рядом с остановкой.", 0xFF1E88E5),
    Pair(51, 98) to LocationInfo("Граффити", "Достопримечательность", "Яркое граффити на территории кампуса.", 0xFFFFEB3B),
    Pair(44, 89) to LocationInfo("Пирс", "Достопримечательность", "Пирс на территории кампуса.", 0xFFFFEB3B),
    Pair(60, 64) to LocationInfo("Лицей ТГУ, ФФ", "Учебный корпус", "Лицей и факультет фундаментальной медицины.", 0xFF43A047),
    Pair(77, 122) to LocationInfo("Ботанический сад ТГУ", "Достопримечательность", "Ботанический сад университета.", 0xFFFFEB3B),
    Pair(73, 68) to LocationInfo("Вендинговый автомат", "Еда", "Вендинговый автомат во втором корпусе.", 0xFFFF9800),
    Pair(73, 71) to LocationInfo("Буфет второй корпус", "Еда", "Буфет для студентов и сотрудников.", 0xFFFF9800),
    Pair(73, 73) to LocationInfo("Коворкинг ВК", "Коворкинг", "Коворкинг во втором корпусе.", 0xFF00BCD4),
    Pair(73, 75) to LocationInfo("Второй корпус", "Вход", "Вход во второй корпус.", 0xFF795548),
    Pair(102, 115) to LocationInfo("Крылов и Сергиевская", "Памятник", "Памятник Крылову и Сергиевской.", 0xFF900B09),
    Pair(92, 92) to LocationInfo("Староанатомический корпус", "Учебный корпус", "Староанатомический корпус ТГУ.", 0xFF43A047),
    Pair(87, 82) to LocationInfo("Спорткорпус ТГУ", "Спорт", "Спортивный корпус университета.", 0xFF4CAF50),
    Pair(97, 56) to LocationInfo("Приемная комиссия ТГУ", "Администрация", "Приемная комиссия для абитуриентов.", 0xFF607D8B),
    Pair(91, 55) to LocationInfo("Старбукс", "Еда", "Кофейня Starbucks.", 0xFFFF9800),
    Pair(93, 55) to LocationInfo("Столовая Сырбор", "Еда", "Столовая Сырбор.", 0xFFFF9800),
    Pair(93, 59) to LocationInfo("Сибирские блины", "Еда", "Кафе с сибирскими блюдами.", 0xFFFF9800),
    Pair(114, 57) to LocationInfo("Главный корпус ТГУ", "Учебный корпус", "Главный корпус Томского государственного университета.", 0xFF43A047),
    Pair(137, 92) to LocationInfo("Библиотека ТГУ", "Коворкинг", "Библиотека и коворкинг с Wi-Fi и розетками.", 0xFF00BCD4),
    Pair(128, 74) to LocationInfo("Памятник павшим", "Памятник", "Памятник павшим сотрудникам и студентам ТГУ.", 0xFF900B09),
    Pair(126, 57) to LocationInfo("Центр кампуса", "Ориентир", "Центральная точка кампуса.", 0xFFE91E63),
    Pair(142, 70) to LocationInfo("Г.Н. Потанин", "Памятник", "Памятник Григорию Потанину.", 0xFF900B09),
    Pair(151, 84) to LocationInfo("Библиотека ТГУ", "Остановка", "Остановка рядом с библиотекой.", 0xFF1E88E5),
    Pair(155, 55) to LocationInfo("Остановка ТГУ", "Остановка", "Главная остановка кампуса.", 0xFF1E88E5),
    Pair(161, 65) to LocationInfo("РОСТИКС", "Еда", "Ресторан быстрого питания Rostic's.", 0xFFFF9800),
    Pair(196, 58) to LocationInfo("Наш Гастроном", "Магазин", "Продуктовый магазин.", 0xFF9C27B0),
    Pair(217, 123) to LocationInfo("Ярче на Кирова", "Магазин", "Супермаркет Ярче.", 0xFF9C27B0),
    Pair(196, 17) to LocationInfo("Ярче на Советской", "Магазин", "Супермаркет Ярче.", 0xFF9C27B0),
    Pair(196, 13) to LocationInfo("Кафедра ИЗО", "Вход", "Вход в кафедру изобразительного искусства.", 0xFF795548),
    Pair(166, 9) to LocationInfo("ИЭМ", "Вход", "Вход в Институт экономики и менеджмента.", 0xFF795548),
    Pair(139, 9) to LocationInfo("Стрит-арт", "Достопримечательность", "Яркий стрит-арт на стене здания.", 0xFFFFEB3B),
    Pair(131, 24) to LocationInfo("Мост", "Сооружение", "Мост через реку или овраг.", 0xFF607D8B)
)

val LegendItems = listOf(
    Pair(Color(0xFF1E88E5), "Остановка"),
    Pair(Color(0xFFFF9800), "Еда"),
    Pair(Color(0xFF9C27B0), "Магазин"),
    Pair(Color(0xFF43A047), "Учебный корпус"),
    Pair(Color(0xFF00BCD4), "Коворкинг"),
    Pair(Color(0xFF900B09), "Памятник"),
    Pair(Color(0xFFFFEB3B), "Достопримечательность"),
    Pair(Color(0xFF795548), "Вход"),
    Pair(Color(0xFF607D8B), "Администрация")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
                            "Tree"      -> DecisionTreeScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
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
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(0.dp))
        Image(painter = painterResource(id = R.drawable.tsulogo11), contentDescription = null, modifier = Modifier.size(350.dp))
        Text("TSU.MyMap", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary, modifier = Modifier.padding(bottom = 1.dp))
        Button(onClick = { onNavigate("Map") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("\uD83D\uDDFA\uFE0F Карта кампуса", fontSize = 16.sp) }
        Button(onClick = { onNavigate("Route") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("\uD83E\uDDED Навигация (A*)", fontSize = 16.sp) }
        Button(onClick = { onNavigate("Food") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("\uD83C\uDF54 Где поесть? (Генетика)", fontSize = 16.sp) }
        Button(onClick = { onNavigate("KMeans") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("\uD83D\uDCCA Кластеризация (K-Means)", fontSize = 16.sp) }
        Button(onClick = { onNavigate("Coworking") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("\uD83D\uDC1C Коворкинги (Муравьи)", fontSize = 16.sp) }
        Button(onClick = { onNavigate("Tree") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("\uD83E\uDD57 Куда пойти поесть? (Дерево)", fontSize = 16.sp) }
        Button(onClick = { onNavigate("NeuralNet") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text("\uD83E\uDDE0 Оценка: Нейросеть", fontSize = 16.sp) }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CampusMapScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val mapData = remember {
        val lines = context.assets.open("tsu_campus_matrix.txt").bufferedReader().use { it.readLines() }
        val gridRows = lines.size
        val gridCols = lines.maxOfOrNull { it.length } ?: 0
        Pair(Triple(gridCols, gridRows, lines), lines)
    }

    val gridCols = mapData.first.first
    val gridRows = mapData.first.second
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
                            CampusDictionary.forEach { (pos, info) ->
                                val (c, r) = pos
                                if (c !in 0 until gridCols || r !in 0 until gridRows) return@forEach
                                val center = Offset(c * cellW + cellW / 2f, r * cellH + cellH / 2f)
                                val radius = minOf(cellW, cellH) * 2.0f
                                val isSel = selectedLocation?.first == c && selectedLocation?.second == r
                                val pointColor = Color(info.color)
                                if (isSel) {
                                    drawCircle(pointColor.copy(alpha = 0.3f), radius * 2.5f, center)
                                    drawCircle(Color.White, radius * 1.8f, center)
                                }
                                drawCircle(Color.Black.copy(alpha = 0.2f), radius + 1.5f, Offset(center.x + 1f, center.y + 1.5f))
                                drawCircle(pointColor, radius, center)
                                drawCircle(Color.White, radius * 0.4f, center)
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

        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendItems.forEach { (color, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(5.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text(label, fontSize = 11.sp, color = Color(0xFF666666))
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
                    "Добавь информацию об этой точке в CampusDictionary.",
                    0xFF607D8B
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
                                    val nearest = findNearestWalkable(grid, loc.second, loc.first)
                                    if (nearest != null) routeEnd = nearest else routeEnd = Pair(loc.second, loc.first)
                                    selectedLocation = null
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
                            ) { Text("Маршрут сюда", fontWeight = FontWeight.SemiBold) }
                            OutlinedButton(
                                onClick = {
                                    val nearest = findNearestWalkable(grid, loc.second, loc.first)
                                    if (nearest != null) routeStart = nearest else routeStart = Pair(loc.second, loc.first)
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
            Text("Где поесть?", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
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
                Text("Нет точек питания с выбранными типами еды", fontSize = 12.sp, color = Color(0xFFC62828), modifier = Modifier.padding(8.dp))
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
                Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Коворкинги", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
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
                        "Сброс", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
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
                    Text("Тапните на бордовые точки (достопримечательности), чтобы выбрать маршрут",
                        fontSize = 13.sp, color = Color.Gray)
                } else {
                    Text("Выбрано локаций: ${selectedIdx.size}", fontSize = 14.sp, color = Color(0xFF900B09), fontWeight = FontWeight.Medium)
                    if (antResult == null) {
                        if (selectedIdx.size >= 2) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val chosen = selectedIdx.toList().map { allLandmarks[it] }
                                    antResult = antColonyTSP(chosen, grid)
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TsuBluePrimary)
                            ) { Text("Построить маршрут (Муравьи)", fontWeight = FontWeight.SemiBold) }
                        } else {
                            Text("Выберите минимум 2 локации", fontSize = 12.sp, color = Color.Red)
                        }
                    } else {
                        val res = antResult!!
                        if (res.distance > 0) {
                            Text("Маршрут найден!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
                            Text("Дистанция: ${res.distance} шагов", fontSize = 13.sp, color = Color(0xFF666666))
                            Text("Итераций: ${res.iterations}", fontSize = 12.sp, color = Color.Gray)
                            val selList = selectedIdx.toList()
                            res.route.forEachIndexed { ord, idxInSel ->
                                val lm = allLandmarks[selList[idxInSel]]
                                Text("${ord + 1}. [${lm.row}, ${lm.col}]  комфорт: ${"%.2f".format(lm.comfort)}",
                                    fontSize = 12.sp, color = Color(0xFF900B09))
                            }
                        } else {
                            Text("Маршрут не найден (точки недостижимы)", fontSize = 14.sp, color = Color.Red)
                        }
                    }
                }
            }
        }
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

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(TsuBluePrimary, RoundedCornerShape(12.dp))) {
                Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Куда пойти на обед", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            }
            Spacer(Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEEF3FF),
                modifier = Modifier.clickable { showTree = !showTree }
            ) {
                Text(
                    if (showTree) "Скрыть дерево" else "Показать дерево",
                    fontSize = 12.sp, color = TsuBluePrimary, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text("Выборка: ${rows.size} записей", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))

            attributes.forEach { attr ->
                Text(
                    attrLabels[attr] ?: attr,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E),
                    modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    attrValues[attr]?.forEach { value ->
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
                    if (selections.size == attributes.size) "Получить рекомендацию"
                    else "Заполни все поля (${selections.size}/${attributes.size})",
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
                        Text("Рекомендация", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            path.result,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Путь по дереву:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
                        Spacer(Modifier.height(6.dp))
                        path.steps.forEachIndexed { i, step ->
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

            if (showTree) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF7F9FC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Структура дерева (ID3)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
                        Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(24.dp))
        }
    }
}