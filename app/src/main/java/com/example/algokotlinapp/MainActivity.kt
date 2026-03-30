package com.example.algokotlinapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.algorithms.runAstar
import com.example.algokotlinapp.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runAstar()
        enableEdgeToEdge()
        setContent {
            AlgoKotlinAppTheme {
                var currentScreen by remember { mutableStateOf("MainMenu") }
                Scaffold(modifier=Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedContent(
                        targetState=currentScreen,
                        label="screen_transition",
                        transitionSpec={
                            if (targetState != "MainMenu" && initialState == "MainMenu") {
                                slideInHorizontally(
                                    animationSpec=tween(400),
                                    initialOffsetX={ fullWidth -> fullWidth }
                                ) + fadeIn(tween(400)) togetherWith slideOutHorizontally(
                                    animationSpec=tween(400),
                                    targetOffsetX={ fullWidth -> -fullWidth / 2 }
                                ) + fadeOut(tween(400))
                            } else {
                                slideInHorizontally(
                                    animationSpec=tween(400),
                                    initialOffsetX={ fullWidth -> -fullWidth / 2 }
                                ) + fadeIn(tween(400)) togetherWith slideOutHorizontally(
                                    animationSpec=tween(400),
                                    targetOffsetX={ fullWidth -> fullWidth }
                                ) + fadeOut(tween(400))
                            }
                        }
                    ) { targetScreen ->
                        when (targetScreen) {
                            "MainMenu" -> MainMenuScreen(
                                modifier=Modifier.padding(innerPadding),
                                onNavigate={ screenName -> currentScreen=screenName }
                            )
                            "Route" -> RouteScreen(Modifier.padding(innerPadding)) { currentScreen="MainMenu" }
                            "Food" -> FoodScreen(Modifier.padding(innerPadding)) { currentScreen="MainMenu" }
                            "Coworking" -> CoworkingScreen(Modifier.padding(innerPadding)) { currentScreen="MainMenu" }
                            "NeuralNet" -> NeuralNetScreen(Modifier.padding(innerPadding)) { currentScreen="MainMenu" }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(modifier: Modifier=Modifier, onNavigate: (String) -> Unit) {
    Column(
        modifier=modifier.fillMaxSize(),
        verticalArrangement=Arrangement.Center,
        horizontalAlignment=Alignment.CenterHorizontally

    ) {
        //логотгу
        androidx.compose.foundation.Image(
            painter=androidx.compose.ui.res.painterResource(id=R.drawable.tsulogo1),
            contentDescription="Логотип ТГУ",
            modifier=Modifier
                .size(250.dp)
                .padding(bottom=16.dp)
        )

        Text(
            text="TSU.MyMap",
            fontSize=55.sp,
            fontWeight=FontWeight.Bold,
            color=TsuBluePrimary,
            modifier=Modifier.padding(bottom=32.dp)
        )

        Button(onClick={ onNavigate("Route") }, modifier=Modifier.fillMaxWidth(0.8f).padding(8.dp)) {
            Text("Навигация", fontSize=18.sp)
        }

        Button(onClick={ onNavigate("Food") }, modifier=Modifier.fillMaxWidth(0.8f).padding(8.dp)) {
            Text("Где поесть?", fontSize=18.sp)
        }

        Button(onClick={ onNavigate("Coworking") }, modifier=Modifier.fillMaxWidth(0.8f).padding(8.dp)) {
            Text("Коворкинги", fontSize=18.sp)
        }

        Button(onClick={ onNavigate("NeuralNet") }, modifier=Modifier.fillMaxWidth(0.8f).padding(8.dp)) {
            Text("Оценка: Нейросеть (Тест)", fontSize=18.sp)
        }
    }
}

@Composable
fun RouteScreen(modifier: Modifier=Modifier, onBack: () -> Unit) {
    Column(modifier=modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick=onBack) { Text("<- Назад") }
        Spacer(modifier=Modifier.height(16.dp))
        Text("Здесь будет карта рощи и маршруты", fontSize=24.sp)
    }
}


@Composable
fun FoodScreen(modifier: Modifier=Modifier, onBack: () -> Unit) {
    Column(modifier=modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick=onBack) { Text("<- Назад") }
        Spacer(modifier=Modifier.height(16.dp))
        Text("Здесь будет выбор предпочтений по еде", fontSize=24.sp)
    }
}

@Composable
fun CoworkingScreen(modifier: Modifier=Modifier, onBack: () -> Unit) {
    Column(modifier=modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick=onBack) { Text("<- Назад") }
        Spacer(modifier=Modifier.height(16.dp))
        Text("Здесь будет муравьиный алгоритм", fontSize=24.sp)
    }
}

@Composable
fun NeuralNetScreen(modifier: Modifier=Modifier, onBack: () -> Unit) {
    val pixels=remember { mutableStateListOf(*Array(25) { false }) }

    Column(
        modifier=modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment=Alignment.CenterHorizontally
    ) {
        Button(onClick=onBack, modifier=Modifier.align(Alignment.Start)) { Text("<- Назад") }

        Spacer(modifier=Modifier.height(32.dp))
        Text(
            text="Нарисуйте оценку (0-9)",
            fontSize=24.sp,
            fontWeight=FontWeight.Bold,
            color=TsuBluePrimary
        )
        Spacer(modifier=Modifier.height(32.dp))
        Column(
            modifier=Modifier
                .padding(16.dp)
                .border(2.dp, PixelBorder)
        ) {
            for (row in 0 until 5) {
                Row {
                    for (col in 0 until 5) {
                        val index=row * 5 + col
                        Box(
                            modifier=Modifier
                                .size(60.dp)
                                .background(if (pixels[index]) PixelFilled else PixelEmpty)
                                .border(1.dp, PixelBorder)
                                .clickable {
                                    pixels[index]=!pixels[index]
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier=Modifier.height(32.dp))

        Button(
            onClick={
                val inputForNeuralNet=pixels.map { if (it) 1 else 0 }
                println("Отправляем в нейросеть массив: $inputForNeuralNet")
            },
            modifier=Modifier.fillMaxWidth(0.6f).height(50.dp)
        ) {
            Text("Распознать", fontSize=18.sp)
        }

        Spacer(modifier=Modifier.height(16.dp))
        androidx.compose.material3.OutlinedButton(
            onClick={
                for (i in 0 until 25) { pixels[i]=false }
            },
            modifier=Modifier.fillMaxWidth(0.6f).height(50.dp),
            border=androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
        ) {
            Text("Очистить поле", color=Color.Gray, fontSize=18.sp)
        }
    }
}