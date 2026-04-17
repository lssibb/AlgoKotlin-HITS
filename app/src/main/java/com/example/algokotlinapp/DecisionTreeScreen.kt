package com.example.algokotlinapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.algorithms.DecisionPath
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
                        Text(stringResource(R.string.tree_structure_title), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
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
