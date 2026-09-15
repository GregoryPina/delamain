package com.gregorypina.delamain.ui

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

private enum class UiState {
    BOOT, IDLE, LISTENING, THINKING, SPEAKING, ERROR
}

private val Background = Color(0xFF020608)
private val Cyan = Color(0xFF64D8FF)
private val ErrorRed = Color(0xFFFF315A)

@Composable
fun DelamainApp() {
    MaterialTheme {
        Surface(color = Background, modifier = Modifier.fillMaxSize()) {
            var state by remember { mutableStateOf(UiState.BOOT) }

            LaunchedEffect(Unit) {
                delay(3200)
                state = UiState.IDLE
            }

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
        }
    }
}

@Composable
private fun DelamainScreen(state: UiState, onTap: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "delamain")
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.47f
            val scale = minOf(size.width / 900f, size.height / 520f)
            val faceW = 270f * scale
            val faceH = 350f * scale
            val energy = when (state) {
                UiState.BOOT -> 0.55f
                UiState.IDLE -> 0.72f
                UiState.LISTENING -> 1.0f
                UiState.THINKING -> 1.15f
                UiState.SPEAKING -> 1.08f
                UiState.ERROR -> 1.25f
            }

            drawScanlines(phase, activeColor)
            drawFace(
                center = Offset(cx, cy),
                width = faceW * pulse,
                height = faceH * pulse,
                color = activeColor,
                state = state,
                energy = energy
            )
            drawGlitch(phase, cx, cy, faceW, faceH, activeColor, state)
        }

        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 20.dp),
            style = TextStyle(
                color = activeColor.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        )

        Text(
            text = "DELAMAIN // V0.1",
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFace(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    state: UiState,
    energy: Float
) {
    val stroke = (1.7f * energy).coerceAtLeast(1f)
    val left = center.x - width / 2f
    val top = center.y - height / 2f

    drawOval(
        color = color.copy(alpha = 0.08f),
        topLeft = Offset(left, top),
        size = Size(width, height)
    )
    drawOval(
        color = color.copy(alpha = 0.68f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(stroke)
    )

    val eyeY = center.y - height * 0.10f
    val eyeSpacing = width * 0.23f
    val eyeW = width * 0.20f
    val eyeH = height * 0.045f
    val eyeAlpha = if (state == UiState.LISTENING) 0.98f else 0.82f

    drawEye(center.x - eyeSpacing, eyeY, eyeW, eyeH, color, eyeAlpha, stroke)
    drawEye(center.x + eyeSpacing, eyeY, eyeW, eyeH, color, eyeAlpha, stroke)

    val nose = Path().apply {
        moveTo(center.x, eyeY + eyeH)
        lineTo(center.x - width * 0.035f, center.y + height * 0.10f)
        lineTo(center.x + width * 0.035f, center.y + height * 0.10f)
    }
    drawPath(nose, color.copy(alpha = 0.68f), style = Stroke(stroke, join = StrokeJoin.Round))

    val mouthY = center.y + height * 0.25f
    val mouthOpen = if (state == UiState.SPEAKING) height * 0.018f else height * 0.004f
    val mouth = Path().apply {
        moveTo(center.x - width * 0.15f, mouthY)
        cubicTo(
            center.x - width * 0.07f, mouthY + mouthOpen,
            center.x + width * 0.07f, mouthY + mouthOpen,
            center.x + width * 0.15f, mouthY
        )
    }
    drawPath(mouth, color.copy(alpha = 0.9f), style = Stroke(stroke, cap = StrokeCap.Round))

    val contourAlpha = when (state) {
        UiState.THINKING, UiState.ERROR -> 0.55f
        else -> 0.32f
    }
    drawLine(color.copy(alpha = contourAlpha), Offset(left + width * 0.15f, center.y - height * 0.30f), Offset(left + width * 0.07f, center.y), strokeWidth = stroke)
    drawLine(color.copy(alpha = contourAlpha), Offset(left + width * 0.07f, center.y), Offset(left + width * 0.15f, center.y + height * 0.30f), strokeWidth = stroke)
    drawLine(color.copy(alpha = contourAlpha), Offset(left + width * 0.85f, center.y - height * 0.30f), Offset(left + width * 0.93f, center.y), strokeWidth = stroke)
    drawLine(color.copy(alpha = contourAlpha), Offset(left + width * 0.93f, center.y), Offset(left + width * 0.85f, center.y + height * 0.30f), strokeWidth = stroke)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEye(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Color,
    alpha: Float,
    stroke: Float
) {
    drawOval(
        color = color.copy(alpha = alpha),
        topLeft = Offset(x - width / 2f, y - height / 2f),
        size = Size(width, height),
        style = Stroke(stroke)
    )
    drawCircle(color.copy(alpha = alpha), radius = height * 0.34f, center = Offset(x, y))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScanlines(
    phase: Float,
    color: Color
) {
    val spacing = 5f
    var y = (phase * spacing * 3f) % spacing
    while (y < size.height) {
        drawLine(color.copy(alpha = 0.09f), Offset(0f, y), Offset(size.width, y), 1f)
        y += spacing
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlitch(
    phase: Float,
    cx: Float,
    cy: Float,
    faceW: Float,
    faceH: Float,
    color: Color,
    state: UiState
) {
    val intensity = when (state) {
        UiState.BOOT -> 0.35f
        UiState.THINKING -> 1f
        UiState.ERROR -> 1.4f
        UiState.SPEAKING -> 0.55f
        else -> 0.18f
    }

    for (i in 0 until 5) {
        val t = phase * 6f + i * 1.73f
        val trigger = abs(sin(t * 2.1f))
        if (trigger > 0.88f) {
            val y = cy - faceH / 2f + ((i * 0.19f + phase * 0.13f) % 1f) * faceH
            val shift = sin(t * 3.2f) * faceW * 0.10f * intensity
            val length = faceW * (0.25f + trigger * 0.55f) * intensity
            drawRect(
                color = color.copy(alpha = 0.20f * intensity),
                topLeft = Offset(cx - length / 2f + shift, y),
                size = Size(length, maxOf(2f, 2.5f * intensity))
            )
        }
    }
}
