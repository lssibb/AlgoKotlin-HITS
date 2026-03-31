package com.example.algokotlinapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.algorithms.runAstar
import com.example.algokotlinapp.ui.theme.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

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
        Image(
            painter=painterResource(id=R.drawable.tsulogo1),
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
    val gridSize=50
    val pixels=remember { mutableStateListOf(*Array(gridSize * gridSize) { false }) }
    val context=LocalContext.current
    var prediction by remember { mutableStateOf<Int?>(null) }

    val interpreter=remember {
        try {
            val fileDescriptor=context.assets.openFd("model_50x50.tflite")
            val inputStream=FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel=inputStream.channel
            val startOffset=fileDescriptor.startOffset
            val declaredLength=fileDescriptor.declaredLength
            val buffer=fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            Interpreter(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Column(
        modifier=modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment=Alignment.CenterHorizontally
    ) {
        Button(onClick=onBack, modifier=Modifier.align(Alignment.Start)) { Text("<- Назад") }

        Spacer(modifier=Modifier.height(16.dp))
        Text(
            text="Нарисуйте цифру (0-9)",
            fontSize=24.sp,
            fontWeight=FontWeight.Bold,
            color=TsuBluePrimary
        )

        Spacer(modifier=Modifier.height(8.dp))
        Text(
            text=if (prediction != null) "Результат: $prediction" else "Нарисуйте цифру и нажмите Распознать",
            fontSize=18.sp,
            fontWeight=FontWeight.Medium,
            color=if (prediction != null) TsuBluePrimary else Color.Gray
        )

        Spacer(modifier=Modifier.height(16.dp))

        Box(
            modifier=Modifier
                .size(300.dp)
                .border(2.dp, PixelBorder)
                .background(PixelEmpty)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event=awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    val cellWidth=size.width.toFloat() / gridSize
                                    val cellHeight=size.height.toFloat() / gridSize

                                    val col=(change.position.x / cellWidth).toInt()
                                    val row=(change.position.y / cellHeight).toInt()

                                    if (col in 0 until gridSize && row in 0 until gridSize) {
                                        for (i in -2..2) {
                                            for (j in -2..2) {
                                                val newRow=row + i
                                                val newCol=col + j
                                                if (newRow in 0 until gridSize && newCol in 0 until gridSize) {
                                                    val newIndex=newRow * gridSize + newCol
                                                    pixels[newIndex]=true
                                                }
                                            }
                                        }
                                    }
                                    change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier=Modifier.fillMaxSize()) {
                val cellWidth=size.width / gridSize
                val cellHeight=size.height / gridSize

                for (row in 0 until gridSize) {
                    for (col in 0 until gridSize) {
                        val index=row * gridSize + col
                        if (pixels[index]) {
                            drawRect(
                                color=PixelFilled,
                                topLeft=Offset(col * cellWidth, row * cellHeight),
                                size=Size(cellWidth, cellHeight)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier=Modifier.height(32.dp))

        Button(
            onClick={
                if (interpreter == null) {
                    Toast.makeText(context, "Файл модели не найден в assets!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val inputBuffer=ByteBuffer.allocateDirect(1 * gridSize * gridSize * 1 * 4)
                inputBuffer.order(ByteOrder.nativeOrder())

                pixels.forEach { isFilled ->
                    inputBuffer.putFloat(if (isFilled) 1.0f else 0.0f)
                }

                val outputBuffer=Array(1) { FloatArray(10) }

                interpreter.run(inputBuffer, outputBuffer)

                val probabilities=outputBuffer[0]
                var maxIdx=0
                var maxProb=probabilities[0]
                for (i in 1 until probabilities.size) {
                    if (probabilities[i] > maxProb) {
                        maxProb=probabilities[i]
                        maxIdx=i
                    }
                }

                prediction=maxIdx
            },
            modifier=Modifier.fillMaxWidth(0.6f).height(50.dp)
        ) {
            Text("Распознать", fontSize=18.sp)
        }

        Spacer(modifier=Modifier.height(16.dp))
        OutlinedButton(
            onClick={
                for (i in 0 until gridSize * gridSize) { pixels[i]=false }
                prediction=null
            },
            modifier=Modifier.fillMaxWidth(0.6f).height(50.dp),
            border=BorderStroke(1.dp, Color.Gray)
        ) {
            Text("Очистить поле", color=Color.Gray, fontSize=18.sp)
        }
    }
}