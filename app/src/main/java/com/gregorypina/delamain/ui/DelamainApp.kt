package com.gregorypina.delamain.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gregorypina.delamain.R
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

private enum class UiState {
    BOOT, IDLE, LISTENING, THINKING, SPEAKING, ERROR
}

private val Background = Color(0xFF14181D)
private val Cyan = Color(0xFF2E8BFF)
private val ErrorRed = Color(0xFFFF3B5C)

private fun imageResFor(state: UiState): Int = when (state) {
    UiState.BOOT -> R.drawable.face_idle
    UiState.IDLE -> R.drawable.face_idle
    UiState.LISTENING -> R.drawable.face_listening
    UiState.THINKING -> R.drawable.face_thinking
    UiState.SPEAKING -> R.drawable.face_speaking
    UiState.ERROR -> R.drawable.face_error
}

private fun glitchIntensityFor(state: UiState): Float = when (state) {
    UiState.BOOT -> 0.55f
    UiState.IDLE -> 0.05f
    UiState.LISTENING -> 0.12f
    UiState.THINKING -> 0.55f
    UiState.SPEAKING -> 0.20f
    UiState.ERROR -> 0.90f
}

@Composable
fun DelamainApp() {
    MaterialTheme {
        Surface(color = Background, modifier = Modifier.fillMaxSize()) {
            var state by remember { mutableStateOf(UiState.BOOT) }

            LaunchedEffect(Unit) {
                delay(2200)
                state = UiState.IDLE
            }

            Box(modifier = Modifier.fillMaxSize()) {
                DelamainScreen(
                    state = state,
                    onTap = {
                        state = when (state) {
                            UiState.BOOT -> UiState.IDLE
                            UiState.IDLE -> UiState.LISTENING
                            UiState.LISTENING -> UiState.THINKING
                            UiState.THINKING -> UiState.SPEAKING
                            UiState.SPEAKING -> UiState.ERROR
                            UiState.ERROR -> UiState.IDLE
                        }
                    }
                )
                DebugCommandPanel()
            }
        }
    }
}

@Composable
private fun DelamainScreen(state: UiState, onTap: () -> Unit) {
    val activeColor = if (state == UiState.ERROR) ErrorRed else Cyan
    val label = when (state) {
        UiState.BOOT -> "INITIALIZING"
        UiState.IDLE -> "ONLINE"
        UiState.LISTENING -> "LISTENING"
        UiState.THINKING -> "THINKING"
        UiState.SPEAKING -> "SPEAKING"
        UiState.ERROR -> "ERROR"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) { detectTapGestures { onTap() } }
    ) {
        Crossfade(targetState = state, label = "face-crossfade") { currentState ->
            GlitchFace(
                imageRes = imageResFor(currentState),
                intensity = glitchIntensityFor(currentState),
                tint = if (currentState == UiState.ERROR) ErrorRed else Cyan,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 20.dp),
            style = TextStyle(
                color = activeColor.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        )

        Text(
            text = "${stringResource(R.string.app_name)} // V0.1",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 28.dp, top = 20.dp),
            style = TextStyle(
                color = activeColor.copy(alpha = 0.35f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
        )
    }
}

@Composable
private fun GlitchFace(
    imageRes: Int,
    intensity: Float,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val image = ImageBitmap.imageResource(id = imageRes)
    val transition = rememberInfiniteTransition(label = "glitch")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1800),
            RepeatMode.Restart
        ),
        label = "phase"
    )

    val fastFlicker by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(90),
            RepeatMode.Reverse
        ),
        label = "flicker"
    )

    Canvas(modifier = modifier) {
        val scale = minOf(size.width / image.width, size.height / image.height)
        val dstW = image.width * scale
        val dstH = image.height * scale
        val baseX = (size.width - dstW) / 2f
        val baseY = (size.height - dstH) / 2f

        if (intensity < 0.02f) {
            drawImage(
                image = image,
                dstOffset = IntOffset(baseX.toInt(), baseY.toInt()),
                dstSize = IntSize(dstW.toInt(), dstH.toInt())
            )
        } else {
            val sliceCount = 28
            val sliceH = image.height / sliceCount

            for (i in 0 until sliceCount) {
                val srcY = i * sliceH
                val srcH = if (i == sliceCount - 1) image.height - srcY else sliceH
                val dstSliceY = baseY + srcY.toFloat() / image.height * dstH
                val dstSliceH = srcH.toFloat() / image.height * dstH
                val trigger = pseudoRandom(i, phase)
                val shiftX = if (trigger > 1f - intensity * 0.55f) {
                    (pseudoRandom(i + 500, phase) - 0.5f) * dstW * 0.10f * intensity
                } else {
                    0f
                }

                drawImage(
                    image = image,
                    srcOffset = IntOffset(0, srcY),
                    srcSize = IntSize(image.width, srcH),
                    dstOffset = IntOffset((baseX + shiftX).toInt(), dstSliceY.toInt()),
                    dstSize = IntSize(dstW.toInt(), maxOf(1, dstSliceH.toInt()))
                )
            }

            // Extra tinted ghost pass. Uses the default SrcOver mode so it is
            // compatible across Compose graphics versions; no SrcATop needed.
            for (i in 0 until sliceCount) {
                val srcY = i * sliceH
                val srcH = if (i == sliceCount - 1) image.height - srcY else sliceH
                val trigger = pseudoRandom(i, phase)

                if (trigger > 1f - intensity * 0.55f) {
                    val dstSliceY = baseY + srcY.toFloat() / image.height * dstH
                    val dstSliceH = srcH.toFloat() / image.height * dstH
                    val shift = (pseudoRandom(i + 500, phase) - 0.5f) * dstW * 0.10f * intensity

                    drawImage(
                        image = image,
                        srcOffset = IntOffset(0, srcY),
                        srcSize = IntSize(image.width, srcH),
                        dstOffset = IntOffset((baseX + shift * 1.6f).toInt(), dstSliceY.toInt()),
                        dstSize = IntSize(dstW.toInt(), maxOf(1, dstSliceH.toInt())),
                        alpha = 0.18f * intensity
                    )

                    drawRect(
                        color = tint.copy(alpha = 0.08f * intensity),
                        topLeft = Offset(0f, dstSliceY),
                        size = Size(size.width, maxOf(1f, dstSliceH))
                    )
                }
            }
        }

        drawScanlines(phase, tint, intensity)

        val flickerAlpha = 0.02f +
            0.10f * intensity * abs(
                sin(fastFlicker * Math.PI.toFloat() + phase * 30f)
            )
        drawRect(Color.White.copy(alpha = flickerAlpha))

        if (intensity > 0.6f && pseudoRandom(999, phase) > 0.88f) {
            val y = pseudoRandom(1000, phase) * size.height
            drawRect(
                Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(0f, y),
                size = Size(size.width, size.height * 0.01f + 3f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScanlines(
    phase: Float,
    color: Color,
    intensity: Float
) {
    val spacing = 5f
    var y = (phase * spacing * 3f) % spacing
    val alpha = 0.03f + 0.05f * intensity

    while (y < size.height) {
        drawLine(
            color.copy(alpha = alpha),
            Offset(0f, y),
            Offset(size.width, y),
            1f
        )
        y += spacing
    }
}

private fun pseudoRandom(seed: Int, phase: Float): Float {
    val x = sin(seed * 12.9898f + phase * 78.233f) * 43758.5453f
    return x - floor(x)
}
