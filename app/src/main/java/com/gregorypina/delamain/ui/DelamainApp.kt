package com.gregorypina.delamain.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.gregorypina.delamain.R
import com.gregorypina.delamain.domain.InteractionCoordinator
import com.gregorypina.delamain.domain.InteractionFace
import com.gregorypina.delamain.domain.InteractionSnapshot
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

private val Background = Color(0xFF14181D)
private val Cyan = Color(0xFF2E8BFF)
private val ErrorRed = Color(0xFFFF3B5C)

@Composable
fun DelamainApp() {
    MaterialTheme {
        Surface(color = Background, modifier = Modifier.fillMaxSize()) {
            var snapshot by remember { mutableStateOf(InteractionSnapshot()) }
            val coordinator = remember { InteractionCoordinator { snapshot = it } }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current.applicationContext
            val voiceSession = remember(context, coordinator) {
                VoiceInteractionSession(context, coordinator, scope)
            }
            val owner = LocalView.current.findViewTreeLifecycleOwner()

            LaunchedEffect(coordinator) {
                delay(2200)
                coordinator.bootFinished()
            }
            LaunchedEffect(snapshot.face, snapshot.interactionId) {
                if (snapshot.face == InteractionFace.ERROR) {
                    val failedInteraction = snapshot.interactionId
                    delay(3000)
                    coordinator.expireError(failedInteraction)
                }
            }
            DisposableEffect(voiceSession, coordinator, owner) {
                voiceSession.start()
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) voiceSession.onStop()
                }
                owner?.lifecycle?.addObserver(observer)
                onDispose {
                    owner?.lifecycle?.removeObserver(observer)
                    voiceSession.shutdown()
                    coordinator.shutdown()
                }
            }
            Box(Modifier.fillMaxSize()) {
                DelamainScreen(snapshot)
                UserInteractionControls(
                    session = voiceSession,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                )
                DebugCommandPanel(
                    interactionCoordinator = coordinator,
                    voiceSession = voiceSession,
                )
            }
        }
    }
}

@Composable
private fun DelamainScreen(snapshot: InteractionSnapshot) {
    val state = snapshot.face
    val color = if (state == InteractionFace.ERROR) ErrorRed else Cyan
    Box(Modifier.fillMaxSize().background(Background)) {
        Crossfade(state, label = "face-crossfade") {
            GlitchFace(
                imageRes = imageResFor(it),
                intensity = intensity(it),
                tint = if (it == InteractionFace.ERROR) ErrorRed else Cyan,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = snapshot.hud,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 20.dp),
            style = TextStyle(
                color = color.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
            ),
        )
        Text(
            text = "${stringResource(R.string.app_name)} // V0.1",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 28.dp, top = 20.dp),
            style = TextStyle(
                color = color.copy(alpha = 0.35f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
            ),
        )
    }
}

private fun imageResFor(state: InteractionFace) = when (state) {
    InteractionFace.BOOT, InteractionFace.IDLE -> R.drawable.face_idle
    InteractionFace.LISTENING -> R.drawable.face_listening
    InteractionFace.THINKING -> R.drawable.face_thinking
    InteractionFace.SPEAKING -> R.drawable.face_speaking
    InteractionFace.ERROR -> R.drawable.face_error
}

private fun intensity(state: InteractionFace) = when (state) {
    InteractionFace.BOOT -> 0.55f
    InteractionFace.IDLE -> 0.05f
    InteractionFace.LISTENING -> 0.12f
    InteractionFace.THINKING -> 0.55f
    InteractionFace.SPEAKING -> 0.2f
    InteractionFace.ERROR -> 0.9f
}

@Composable
private fun GlitchFace(
    imageRes: Int,
    intensity: Float,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val image = ImageBitmap.imageResource(imageRes)
    val transition = rememberInfiniteTransition(label = "glitch")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "phase",
    )
    val flicker by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(90), RepeatMode.Reverse),
        label = "flicker",
    )
    Canvas(modifier) {
        val scale = kotlin.math.min(size.width / image.width, size.height / image.height)
        val w = image.width * scale
        val h = image.height * scale
        val x = (size.width - w) / 2
        val y = (size.height - h) / 2
        val count = 28
        val sh = image.height / count
        for (i in 0 until count) {
            val sy = i * sh
            val srcH = if (i == count - 1) image.height - sy else sh
            val dy = y + sy.toFloat() / image.height * h
            val dh = srcH.toFloat() / image.height * h
            val trigger = pseudoRandom(i, phase)
            val shift = if (trigger > 1f - intensity * 0.55f) {
                (pseudoRandom(i + 500, phase) - 0.5f) * w * 0.10f * intensity
            } else {
                0f
            }
            drawImage(
                image,
                srcOffset = IntOffset(0, sy),
                srcSize = IntSize(image.width, srcH),
                dstOffset = IntOffset((x + shift).toInt(), dy.toInt()),
                dstSize = IntSize(w.toInt(), maxOf(1, dh.toInt())),
            )
            if (shift != 0f) {
                drawRect(
                    tint.copy(alpha = 0.08f * intensity),
                    Offset(0f, dy),
                    Size(size.width, maxOf(1f, dh)),
                )
            }
        }
        var line = (phase * 15f) % 5f
        while (line < size.height) {
            drawLine(
                tint.copy(alpha = 0.03f + 0.05f * intensity),
                Offset(0f, line),
                Offset(size.width, line),
                strokeWidth = 1f,
            )
            line += 5f
        }
        drawRect(
            Color.White.copy(
                alpha = 0.02f + 0.10f * intensity * abs(sin(flicker * Math.PI.toFloat() + phase * 30f)),
            ),
        )
        if (intensity > 0.6f && pseudoRandom(999, phase) > 0.88f) {
            val yy = pseudoRandom(1000, phase) * size.height
            drawRect(Color.Black.copy(alpha = 0.5f), Offset(0f, yy), Size(size.width, size.height * 0.01f + 3f))
        }
    }
}

private fun pseudoRandom(seed: Int, phase: Float): Float {
    val x = sin(seed * 12.9898f + phase * 78.233f) * 43758.5453f
    return x - floor(x)
}
