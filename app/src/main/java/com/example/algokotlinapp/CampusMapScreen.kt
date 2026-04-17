package com.example.algokotlinapp

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.algokotlinapp.algorithms.findNearestWalkable
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

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
