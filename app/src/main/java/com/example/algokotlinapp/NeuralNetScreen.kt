package com.example.algokotlinapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    private val hiddenSize1 = 256
    private val hiddenSize2 = 64
    private val outputSize = 10
    private val W1 = loadFloatArray("w1.bin", hiddenSize1 * inputSize)
    private val b1 = loadFloatArray("b1.bin", hiddenSize1)
    private val W2 = loadFloatArray("w2.bin", hiddenSize2 * hiddenSize1)
    private val b2 = loadFloatArray("b2.bin", hiddenSize2)
    private val W3 = loadFloatArray("w3.bin", outputSize * hiddenSize2)
    private val b3 = loadFloatArray("b3.bin", outputSize)

    private fun loadFloatArray(fileName: String, expectedSize: Int): FloatArray {
        val stream = context.assets.open(fileName)
        val bytes = stream.readBytes()
        stream.close()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(expectedSize).also { buffer.asFloatBuffer().get(it) }
    }

    fun predict(X: FloatArray): Int {
        val h1 = FloatArray(hiddenSize1) { i ->
            var sum = b1[i]
            for (j in 0 until inputSize) sum += W1[i * inputSize + j] * X[j]
            if (sum > 0) sum else 0f
        }
        val h2 = FloatArray(hiddenSize2) { i ->
            var sum = b2[i]
            for (j in 0 until hiddenSize1) sum += W2[i * hiddenSize1 + j] * h1[j]
            if (sum > 0) sum else 0f
        }
        val out = FloatArray(outputSize) { i ->
            var sum = b3[i]
            for (j in 0 until hiddenSize2) sum += W3[i * hiddenSize2 + j] * h2[j]
            sum
        }
        return out.indices.maxByOrNull { out[it] } ?: 0
    }
}

fun centerAndFlattenImage(pixels: List<Boolean>, gridSize: Int): FloatArray {
    val inputGridSize = 50
    var sumX = 0f; var sumY = 0f; var count = 0
    for (row in 0 until inputGridSize) {
        for (col in 0 until inputGridSize) {
            if (pixels[row * inputGridSize + col]) {
                sumX += col; sumY += row; count++
            }
        }
    }
    val result = FloatArray(gridSize * gridSize)
    if (count == 0) return result
    
    val centerX = sumX / count
    val centerY = sumY / count
    val scale = minOf(gridSize.toFloat() / 30f, 1.5f)
    val targetCenter = gridSize / 2f
    val offsetX = targetCenter - centerX * scale
    val offsetY = targetCenter - centerY * scale
    
    for (row in 0 until inputGridSize) {
        for (col in 0 until inputGridSize) {
            if (pixels[row * inputGridSize + col]) {
                val srcX = (col - centerX) * scale + targetCenter
                val srcY = (row - centerY) * scale + targetCenter
                val nr = srcY.toInt(); val nc = srcX.toInt()
                if (nr in 0 until gridSize && nc in 0 until gridSize) {
                    result[nr * gridSize + nc] = 1.0f
                    if (nr + 1 < gridSize) result[(nr + 1) * gridSize + nc] = 0.7f
                    if (nc + 1 < gridSize) result[nr * gridSize + nc + 1] = 0.7f
                }
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
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val nativeNet = remember { try { NativeInference(context) } catch (e: Exception) { null } }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text(stringResource(R.string.btn_back)) }
        Text(stringResource(R.string.title_neuralnet), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary)
        Text(if (prediction != null) stringResource(R.string.nn_result, prediction!!) else stringResource(R.string.nn_draw_prompt), fontSize = 18.sp)
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
        Button(
            onClick = {
                if (isRunning) return@Button
                val snapshot = pixels.toList()
                isRunning = true
                scope.launch {
                    val result = withContext(Dispatchers.Default) {
                        nativeNet?.predict(centerAndFlattenImage(snapshot, gridSize))
                    }
                    prediction = result
                    isRunning = false
                }
            },
            enabled = !isRunning,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            if (isRunning) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_recognizing))
            } else {
                Text(stringResource(R.string.nn_btn_recognize))
            }
        }
        OutlinedButton(onClick = { pixels.fill(false); prediction = null }) { Text(stringResource(R.string.btn_clear)) }
    }
}
