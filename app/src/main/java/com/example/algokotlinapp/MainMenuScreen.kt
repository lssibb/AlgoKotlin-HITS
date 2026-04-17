package com.example.algokotlinapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    var showAboutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(0.dp))
        Image(painter = painterResource(id = R.drawable.tsulogo11), contentDescription = null, modifier = Modifier.size(350.dp))
        Text(stringResource(R.string.app_title), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary, modifier = Modifier.padding(bottom = 1.dp))
        Button(onClick = { onNavigate("Map") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_map), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Route") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_route), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Food") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_food), fontSize = 16.sp) }
        Button(onClick = { onNavigate("KMeans") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_kmeans), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Coworking") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_coworking), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Tree") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_tree), fontSize = 16.sp) }
        Button(onClick = { onNavigate("NeuralNet") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_neuralnet), fontSize = 16.sp) }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showAboutSheet = true },
            modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)
        ) { Text("ℹ\uFE0F О приложении", fontSize = 14.sp) }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAboutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAboutSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "TSU.MyMap",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TsuBluePrimary
                )
                Text(
                    "Приложение для студентов ТГУ",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Реализованные алгоритмы:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                AboutItem("A*", "Кратчайший маршрут по кампусу")
                AboutItem("K-Means", "Кластеризация зон")
                AboutItem("Генетический", "Маршрут для покупки еды")
                AboutItem("Муравьиный", "Обход коворкингов")
                AboutItem("Дерево решений (ID3)", "Рекомендация заведения")
                AboutItem("Нейросеть", "Распознавание цифр 50×50")
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AboutItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(TsuBluePrimary, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A2E))
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
