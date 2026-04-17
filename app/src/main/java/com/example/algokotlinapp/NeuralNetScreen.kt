package com.example.algokotlinapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.ui.theme.PixelBorder
import com.example.algokotlinapp.ui.theme.PixelEmpty
import com.example.algokotlinapp.ui.theme.PixelFilled
import com.example.algokotlinapp.ui.theme.TsuBluePrimary
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
