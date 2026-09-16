package com.gregorypina.delamain.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberUpdatedState
import com.gregorypina.delamain.domain.SpeechInputPort
import com.gregorypina.delamain.domain.SpeechInputState
import com.gregorypina.delamain.domain.SpeechInputStartResult
import com.gregorypina.delamain.integration.voice.AndroidSpeechInputPort
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gregorypina.delamain.domain.LocalCommandEngine
import com.gregorypina.delamain.domain.LocalCommandResult
import com.gregorypina.delamain.domain.LocalUnknownResponses
import com.gregorypina.delamain.domain.SpeechOutputPort
import com.gregorypina.delamain.domain.SpeechOutputResult
import com.gregorypina.delamain.domain.SpeechOutputState
import com.gregorypina.delamain.integration.CompositeLocalActionPort
import com.gregorypina.delamain.integration.apps.AndroidLaunchAppActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaKeyActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaVolumeActionPort
import com.gregorypina.delamain.integration.system.AndroidBatteryStatusPort
import com.gregorypina.delamain.integration.voice.AndroidTextToSpeechPort

private val DebugPanelBackground = Color(0xEE101820)
private val DebugPanelAccent = Color(0xFF2E8BFF)

@Composable
internal fun DebugCommandPanel() {
    val applicationContext = LocalContext.current.applicationContext
    val engine = remember(applicationContext) {
        LocalCommandEngine(
            actionPort = CompositeLocalActionPort(
                volumePort = AndroidMediaVolumeActionPort.from(applicationContext),
                mediaKeyPort = AndroidMediaKeyActionPort.from(applicationContext),
                launchAppPort = AndroidLaunchAppActionPort.from(applicationContext),
            ),
            batteryStatusPort = AndroidBatteryStatusPort.from(applicationContext),
        )
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var speechOutputPort by remember { mutableStateOf<SpeechOutputPort?>(null) }
    var speechState by remember { mutableStateOf(SpeechOutputState.Preparing) }
    val lifecycleOwner = LocalView.current.findViewTreeLifecycleOwner()
    DisposableEffect(applicationContext, expanded, lifecycleOwner) {
        if (expanded) {
            speechState = SpeechOutputState.Preparing
            val port = AndroidTextToSpeechPort(applicationContext) { speechState = it }
            speechOutputPort = port
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) port.stop()
            }
            lifecycleOwner?.lifecycle?.addObserver(observer)
            onDispose {
                lifecycleOwner?.lifecycle?.removeObserver(observer)
                port.shutdown()
                speechOutputPort = null
            }
        } else {
            onDispose { }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
    ) {
        if (expanded) {
            DebugCommandPanelContent(
                engine = engine,
                speechOutputPort = speechOutputPort,
                speechState = speechState,
                onDismiss = {
                    speechOutputPort?.stop()
                    expanded = false
                },
                modifier = Modifier.align(Alignment.TopCenter),
            )
        } else {
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Text("DEV", color = DebugPanelAccent, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun DebugCommandPanelContent(
    engine: LocalCommandEngine,
    speechOutputPort: SpeechOutputPort?,
    speechState: SpeechOutputState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("Digite um comando local para testar.") }

    var submissionProblem by remember(speechState) { mutableStateOf<String?>(null) }

    var speechInputPort by remember { mutableStateOf<SpeechInputPort?>(null) }
    var inputState by remember { mutableStateOf(SpeechInputState.Idle) }
    val currentOutput by rememberUpdatedState(speechOutputPort)

    fun submit(text: String = input) {
        speechInputPort?.cancel()
        val (display, speechText) = when (val result = engine.process(text)) {
            is LocalCommandResult.Recognized -> {
                "${result.intent}: ${result.response}" to result.response
            }
            LocalCommandResult.Unknown -> {
                val unknown = LocalUnknownResponses.next()
                unknown to unknown
            }
        }
        output = display
        submissionProblem = when (speechOutputPort?.speak(speechText)) {
            SpeechOutputResult.Queued -> null
            SpeechOutputResult.Failed -> "Não foi possível iniciar a fala."
            SpeechOutputResult.Unavailable, null -> "Voz ainda não disponível; resposta em texto."
        }
        focusManager.clearFocus()
    }

    val onRecognized by rememberUpdatedState<(String) -> Unit> { text ->
        input = text
        submit(text)
    }
    val context = LocalContext.current.applicationContext
    val owner = LocalView.current.findViewTreeLifecycleOwner()
    DisposableEffect(context, owner) {
        val port = AndroidSpeechInputPort(
            context,
            stopOutput = { currentOutput?.stop() ?: false },
            onState = { inputState = it },
            onText = { onRecognized(it) },
        )
        speechInputPort = port
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) port.cancel()
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose {
            owner?.lifecycle?.removeObserver(observer)
            port.shutdown()
            speechInputPort = null
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        // Permission is not a pending capture request: the user must tap again.
        inputState = if (granted) SpeechInputState.Idle else SpeechInputState.PermissionRequired
    }
    val listening = inputState in setOf(
        SpeechInputState.Starting, SpeechInputState.Listening, SpeechInputState.Processing,
    )

    Column(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .widthIn(max = 620.dp)
            .fillMaxWidth(0.9f)
            .heightIn(max = 220.dp)
            .background(DebugPanelBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "DESENVOLVIMENTO // COMANDO LOCAL",
                color = DebugPanelAccent,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                submissionProblem = null
                speechOutputPort?.stop()
            }) {
                Text("PARAR VOZ")
            }
            TextButton(onClick = { speechInputPort?.cancel(); onDismiss() }) {
                Text("FECHAR")
            }
        }

        Text(
            text = submissionProblem ?: when (speechState) {
                SpeechOutputState.Preparing -> "Voz: preparando…"
                SpeechOutputState.Ready -> "Voz: pronta"
                SpeechOutputState.Unavailable -> "Voz local pt-BR indisponível; resposta em texto."
                SpeechOutputState.Queued -> "Voz: aguardando início"
                SpeechOutputState.Speaking -> "Voz: falando"
                SpeechOutputState.Completed -> "Voz: fala concluída"
                SpeechOutputState.Stopped -> "Voz: interrupção solicitada"
                SpeechOutputState.Failed -> "Voz: falha; resposta em texto."
                SpeechOutputState.Closed -> "Voz: encerrada"
            },
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                enabled = speechInputPort != null && speechOutputPort != null,
                onClick = {
                    focusManager.clearFocus()
                    if (listening) speechInputPort?.cancel()
                    else if (speechInputPort?.start() == SpeechInputStartResult.PermissionRequired) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            ) { Text(if (listening) "CANCELAR ESCUTA" else "OUVIR") }
            Text(
                text = when (inputState) {
                    SpeechInputState.Idle -> "Toque em OUVIR e diga uma frase."
                    SpeechInputState.Starting -> "Preparando microfone…"
                    SpeechInputState.Listening -> "Ouvindo…"
                    SpeechInputState.Processing -> "Reconhecendo…"
                    SpeechInputState.Completed -> "Frase recebida."
                    SpeechInputState.Canceled -> "Escuta cancelada."
                    SpeechInputState.Unavailable -> "Reconhecimento local pt-BR indisponível; use texto."
                    SpeechInputState.PermissionRequired -> "Permita o microfone para ouvir; texto continua disponível."
                    SpeechInputState.Failed -> "Não foi possível ouvir. Toque para tentar novamente."
                    SpeechInputState.NoMatch -> "Não entendi a frase. Toque para tentar novamente."
                    SpeechInputState.TimedOut -> "Tempo de escuta encerrado."
                    SpeechInputState.Closed -> "Microfone encerrado."
                },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedTextField(
            value = input,
            onValueChange = { speechInputPort?.cancel(); input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Entrada") },
            placeholder = { Text("Ex.: que horas são?") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = DebugPanelAccent,
                focusedBorderColor = DebugPanelAccent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.55f),
                focusedLabelColor = DebugPanelAccent,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = { submit() }) {
                Text("ENVIAR")
            }
            Text(
                text = output,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
