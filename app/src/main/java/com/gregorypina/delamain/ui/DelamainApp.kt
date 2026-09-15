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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private enum class UiState {
    BOOT, IDLE, LISTENING, THINKING, SPEAKING, ERROR
}

private val Background = Color(0xFF020608)
private val Shell = Color(0xFFB9D7E3)
private val ShellBright = Color(0xFFE8F7FC)
private val ShellDark = Color(0xFF42616D)
private val Cyan = Color(0xFF64D8FF)
private val ErrorRed = Color(0xFFFF315A)
private val Internal = Color(0xFF071116)

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
        initialValue = 0.985f,
        targetValue = 1.015f,
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
            val cy = size.height * 0.45f
            val scale = minOf(size.width / 900f, size.height / 520f)
            val faceW = 340f * scale * pulse
            val faceH = 395f * scale * pulse
            val energy = when (state) {
                UiState.BOOT -> 0.60f
                UiState.IDLE -> 0.72f
                UiState.LISTENING -> 1.05f
                UiState.THINKING -> 1.20f
                UiState.SPEAKING -> 1.10f
                UiState.ERROR -> 1.35f
            }

            drawScanlines(phase, activeColor)
            drawCyberFace(
                center = Offset(cx, cy),
                width = faceW,
                height = faceH,
                color = activeColor,
                energy = energy,
                state = state
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

private fun DrawScope.drawCyberFace(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    energy: Float,
    state: UiState
) {
    val left = center.x - width / 2f
    val top = center.y - height / 2f
    val right = center.x + width / 2f
    val bottom = center.y + height / 2f
    val line = (1.6f * energy).coerceAtLeast(1f)

    drawCircle(color.copy(alpha = 0.035f), radius = width * 0.64f, center = Offset(center.x, center.y - height * 0.02f))
    drawCircle(color.copy(alpha = 0.12f), radius = width * 0.64f, center = Offset(center.x, center.y - height * 0.02f), style = Stroke(line))

    drawNeck(center, width, height, color, line)

    val head = Path().apply {
        moveTo(center.x, top)
        cubicTo(left + width * 0.20f, top, left + width * 0.08f, top + height * 0.15f, left + width * 0.13f, center.y)
        cubicTo(left + width * 0.15f, center.y + height * 0.32f, left + width * 0.28f, bottom - height * 0.04f, center.x - width * 0.12f, bottom)
        lineTo(center.x, bottom + height * 0.015f)
        lineTo(center.x + width * 0.12f, bottom)
        cubicTo(right - width * 0.28f, bottom - height * 0.04f, right - width * 0.15f, center.y + height * 0.32f, right - width * 0.13f, center.y)
        cubicTo(right - width * 0.08f, top + height * 0.15f, right - width * 0.20f, top, center.x, top)
        close()
    }
    drawPath(head, Shell.copy(alpha = 0.94f))
    drawPath(head, ShellDark.copy(alpha = 0.72f), style = Stroke(line))

    drawPanel(center.x, top + height * 0.08f, width * 0.58f, height * 0.18f, ShellBright, ShellDark, line)
    drawPanel(left + width * 0.18f, top + height * 0.28f, width * 0.23f, height * 0.25f, Shell.copy(alpha = 0.95f), ShellDark, line)
    drawPanel(right - width * 0.18f, top + height * 0.28f, width * 0.23f, height * 0.25f, Shell.copy(alpha = 0.95f), ShellDark, line)

    drawTempleModule(left + width * 0.06f, center.y - height * 0.03f, width * 0.13f, color, energy)
    drawTempleModule(right - width * 0.06f, center.y - height * 0.03f, width * 0.13f, color, energy)

    val eyeY = center.y - height * 0.10f
    val eyeSpacing = width * 0.23f
    drawBrow(center.x - eyeSpacing, eyeY, width * 0.21f, color, line)
    drawBrow(center.x + eyeSpacing, eyeY, width * 0.21f, color, line)
    drawMechanicalEye(center.x - eyeSpacing, eyeY, width * 0.20f, height * 0.07f, color, energy)
    drawMechanicalEye(center.x + eyeSpacing, eyeY, width * 0.20f, height * 0.07f, color, energy)

    val nose = Path().apply {
        moveTo(center.x, eyeY + height * 0.04f)
        lineTo(center.x - width * 0.035f, center.y + height * 0.13f)
        lineTo(center.x - width * 0.065f, center.y + height * 0.17f)
        lineTo(center.x, center.y + height * 0.18f)
        lineTo(center.x + width * 0.065f, center.y + height * 0.17f)
        lineTo(center.x + width * 0.035f, center.y + height * 0.13f)
        close()
    }
    drawPath(nose, ShellDark.copy(alpha = 0.85f), style = Stroke(line, join = StrokeJoin.Round))
    drawLine(color.copy(alpha = 0.55f), Offset(center.x, top + height * 0.09f), Offset(center.x, center.y + height * 0.11f), line * 0.7f)

    drawCheekPlate(left + width * 0.25f, center.y + height * 0.12f, width * 0.21f, height * 0.16f, color, line, mirror = false)
    drawCheekPlate(right - width * 0.25f, center.y + height * 0.12f, width * 0.21f, height * 0.16f, color, line, mirror = true)

    val mouthY = center.y + height * 0.25f
    val mouthOpen = if (state == UiState.SPEAKING) height * 0.020f else height * 0.005f
    drawLine(color.copy(alpha = 0.85f), Offset(center.x - width * 0.13f, mouthY), Offset(center.x + width * 0.13f, mouthY), line)
    drawArc(
        color = ShellDark.copy(alpha = 0.72f),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - width * 0.13f, mouthY - mouthOpen * 0.5f),
        size = Size(width * 0.26f, mouthOpen * 2f + 4f),
        style = Stroke(line * 0.8f)
    )

    drawPanel(center.x, bottom - height * 0.11f, width * 0.22f, height * 0.10f, Shell.copy(alpha = 0.90f), ShellDark, line)

    val seamAlpha = when (state) {
        UiState.THINKING, UiState.ERROR -> 0.72f
        else -> 0.48f
    }
    drawLine(color.copy(alpha = seamAlpha), Offset(left + width * 0.19f, top + height * 0.52f), Offset(left + width * 0.26f, top + height * 0.74f), line * 0.8f)
    drawLine(color.copy(alpha = seamAlpha), Offset(right - width * 0.19f, top + height * 0.52f), Offset(right - width * 0.26f, top + height * 0.74f), line * 0.8f)
}

private fun DrawScope.drawNeck(center: Offset, width: Float, height: Float, color: Color, line: Float) {
    val top = center.y + height * 0.38f
    val neckW = width * 0.34f
    val neckH = height * 0.34f
    drawRoundRect(
        color = Internal,
        topLeft = Offset(center.x - neckW / 2f, top),
        size = Size(neckW, neckH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(neckW * 0.08f)
    )
    for (i in -2..2) {
        val x = center.x + i * neckW * 0.11f
        drawLine(color.copy(alpha = 0.33f), Offset(x, top + neckH * 0.10f), Offset(x, top + neckH * 0.88f), line * 0.55f)
    }
    drawLine(color.copy(alpha = 0.60f), Offset(center.x - neckW * 0.38f, top), Offset(center.x - neckW * 0.48f, top + neckH * 0.82f), line)
    drawLine(color.copy(alpha = 0.60f), Offset(center.x + neckW * 0.38f, top), Offset(center.x + neckW * 0.48f, top + neckH * 0.82f), line)
}

private fun DrawScope.drawPanel(centerX: Float, centerY: Float, width: Float, height: Float, fill: Color, border: Color, line: Float) {
    drawRoundRect(
        color = fill,
        topLeft = Offset(centerX - width / 2f, centerY - height / 2f),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.08f)
    )
    drawRoundRect(
        color = border.copy(alpha = 0.55f),
        topLeft = Offset(centerX - width / 2f, centerY - height / 2f),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.08f),
        style = Stroke(line * 0.75f)
    )
}

private fun DrawScope.drawTempleModule(x: Float, y: Float, radius: Float, color: Color, energy: Float) {
    drawCircle(Internal, radius = radius, center = Offset(x, y))
    drawCircle(color.copy(alpha = 0.18f), radius = radius, center = Offset(x, y), style = Stroke(2f * energy))
    drawCircle(color.copy(alpha = 0.72f), radius = radius * 0.63f, center = Offset(x, y), style = Stroke(1.2f * energy))
    drawCircle(color.copy(alpha = 0.85f), radius = radius * 0.20f, center = Offset(x, y))
}

private fun DrawScope.drawBrow(x: Float, y: Float, width: Float, color: Color, line: Float) {
    drawLine(color.copy(alpha = 0.65f), Offset(x - width / 2f, y - 2f), Offset(x + width / 2f, y - 6f), strokeWidth = line * 1.4f, cap = StrokeCap.Round)
}

private fun DrawScope.drawMechanicalEye(x: Float, y: Float, width: Float, height: Float, color: Color, energy: Float) {
    drawOval(color = Internal, topLeft = Offset(x - width / 2f, y - height / 2f), size = Size(width, height))
    drawOval(color = color.copy(alpha = 0.88f), topLeft = Offset(x - width / 2f, y - height / 2f), size = Size(width, height), style = Stroke(1.6f * energy))
    drawCircle(color.copy(alpha = 0.12f), radius = height * 1.55f, center = Offset(x, y))
    drawCircle(color.copy(alpha = 0.84f), radius = height * 0.40f, center = Offset(x, y))
    drawCircle(Internal, radius = height * 0.19f, center = Offset(x, y))
    for (i in 0 until 8) {
        val a = i * (Math.PI / 4.0)
        val r1 = height * 0.50f
        val r2 = height * 0.78f
        drawLine(color.copy(alpha = 0.72f), Offset(x + cos(a).toFloat() * r1, y + sin(a).toFloat() * r1), Offset(x + cos(a).toFloat() * r2, y + sin(a).toFloat() * r2), strokeWidth = 0.9f * energy)
    }
}

private fun DrawScope.drawCheekPlate(x: Float, y: Float, width: Float, height: Float, color: Color, line: Float, mirror: Boolean) {
    val s = if (mirror) -1f else 1f
    val path = Path().apply {
        moveTo(x - s * width / 2f, y - height / 2f)
        lineTo(x + s * width * 0.25f, y - height * 0.40f)
        lineTo(x + s * width / 2f, y)
        lineTo(x + s * width * 0.18f, y + height / 2f)
        close()
    }
    drawPath(path, color.copy(alpha = 0.09f), style = Stroke(line * 0.75f))
}

private fun DrawScope.drawScanlines(phase: Float, color: Color) {
    val spacing = 5f
    var y = (phase * spacing * 3f) % spacing
    while (y < size.height) {
        drawLine(color.copy(alpha = 0.07f), Offset(0f, y), Offset(size.width, y), 1f)
        y += spacing
    }
}

private fun DrawScope.drawGlitch(phase: Float, cx: Float, cy: Float, faceW: Float, faceH: Float, color: Color, state: UiState) {
    val intensity = when (state) {
        UiState.BOOT -> 0.50f
        UiState.THINKING -> 1.0f
        UiState.ERROR -> 1.5f
        UiState.SPEAKING -> 0.65f
        else -> 0.15f
    }

    for (i in 0 until 7) {
        val t = phase * 6f + i * 1.73f
        val trigger = abs(sin(t * 2.1f))
        if (trigger > 0.90f) {
            val y = cy - faceH / 2f + ((i * 0.17f + phase * 0.13f) % 1f) * faceH
            val shift = sin(t * 3.1f) * faceW * 0.11f * intensity
            val length = faceW * (0.18f + trigger * 0.45f) * intensity
            drawRect(
                color = color.copy(alpha = 0.15f * intensity),
                topLeft = Offset(cx - length / 2f + shift, y),
                size = Size(length, maxOf(2f, 2.0f * intensity))
            )
        }
    }
}
