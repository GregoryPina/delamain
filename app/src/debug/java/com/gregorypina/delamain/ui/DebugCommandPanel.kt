package com.gregorypina.delamain.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.gregorypina.delamain.domain.InteractionCoordinator
import com.gregorypina.delamain.domain.LocalCommandEngine
import com.gregorypina.delamain.domain.LocalCommandResult
import com.gregorypina.delamain.domain.LocalUnknownResponses
import com.gregorypina.delamain.domain.SpeechInputPort
import com.gregorypina.delamain.domain.SpeechInputStartResult
import com.gregorypina.delamain.domain.SpeechInputState
import com.gregorypina.delamain.domain.SpeechOutputPort
import com.gregorypina.delamain.domain.SpeechOutputResult
import com.gregorypina.delamain.domain.SpeechOutputState
import com.gregorypina.delamain.domain.SpeechVoicePreferenceCoordinator
import com.gregorypina.delamain.domain.SpeechVoicePreferenceEvent
import com.gregorypina.delamain.domain.SpeechVoiceSelection
import com.gregorypina.delamain.integration.CompositeLocalActionPort
import com.gregorypina.delamain.integration.apps.AndroidLaunchAppActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaKeyActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaVolumeActionPort
import com.gregorypina.delamain.integration.system.AndroidBatteryStatusPort
import com.gregorypina.delamain.integration.voice.AndroidSpeechInputPort
import com.gregorypina.delamain.integration.voice.AndroidTextToSpeechPort
import com.gregorypina.delamain.integration.voice.DataStoreSpeechVoicePreferenceStore
import kotlinx.coroutines.launch

private val DebugPanelBackground = Color(0xEE101820)
private val DebugPanelAccent = Color(0xFF2E8BFF)

@Composable
internal fun DebugCommandPanel(interactionCoordinator: InteractionCoordinator) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val engine = remember(context) {
        LocalCommandEngine(
            actionPort = CompositeLocalActionPort(
                volumePort = AndroidMediaVolumeActionPort.from(context),
                mediaKeyPort = AndroidMediaKeyActionPort.from(context),
                launchAppPort = AndroidLaunchAppActionPort.from(context),
            ),
            batteryStatusPort = AndroidBatteryStatusPort.from(context),
        )
    }
    var outputPort by remember { mutableStateOf<AndroidTextToSpeechPort?>(null) }
    var selection by remember { mutableStateOf(SpeechVoiceSelection()) }
    var speechState by remember { mutableStateOf(SpeechOutputState.Preparing) }
    var prefMessage by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    var activeSession by remember { mutableLongStateOf(0L) }
    val store = remember(context) { DataStoreSpeechVoicePreferenceStore(context) }
    val pref = remember(store) {
        SpeechVoicePreferenceCoordinator(store) { token, event ->
            if (token != activeSession) return@SpeechVoicePreferenceCoordinator
            when (event) {
                is SpeechVoicePreferenceEvent.Restore -> {
                    val ok = outputPort?.restoreVoice(event.preference.engineId, event.preference.voiceId) == true
                    saved = ok
                    prefMessage = if (ok) "Voz salva restaurada." else "Preferência indisponível."
                }
                SpeechVoicePreferenceEvent.Saved -> {
                    saved = true
                    prefMessage = "Preferência salva."
                }
                SpeechVoicePreferenceEvent.Cleared -> {
                    saved = false
                    prefMessage = "Preferência removida."
                }
                SpeechVoicePreferenceEvent.SaveFailed -> {
                    saved = false
                    prefMessage = "Usando nesta sessão; não foi possível salvar."
                }
                SpeechVoicePreferenceEvent.ClearFailed -> {
                    prefMessage = "Não foi possível remover a preferência."
                }
                SpeechVoicePreferenceEvent.ReadFailed -> {
                    prefMessage = "Não foi possível ler a preferência."
                }
                SpeechVoicePreferenceEvent.Unavailable -> {
                    saved = false
                    prefMessage = "Preferência indisponível; usando padrão local."
                }
            }
        }
    }
    val owner = LocalView.current.findViewTreeLifecycleOwner()
    DisposableEffect(context, expanded, owner) {
        if (expanded) {
            speechState = SpeechOutputState.Preparing
            selection = SpeechVoiceSelection()
            prefMessage = null
            saved = false
            val token = pref.openSession()
            activeSession = token
            var restored = false
            val port = AndroidTextToSpeechPort(
                context,
                onState = { state, interactionId ->
                    speechState = state
                    interactionCoordinator.output(state, interactionId)
                },
                onVoices = { voices ->
                    selection = voices
                    if (!restored && voices.engineId != null && voices.ids.isNotEmpty()) {
                        restored = true
                        scope.launch {
                            pref.restore(token, voices.engineId!!, voices.ids.toSet())
                        }
                    }
                },
            )
            outputPort = port
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    port.stop()
                    interactionCoordinator.stop()
                }
            }
            owner?.lifecycle?.addObserver(observer)
            onDispose {
                owner?.lifecycle?.removeObserver(observer)
                pref.closeSession(token)
                port.shutdown()
                interactionCoordinator.stop()
                if (activeSession == token) outputPort = null
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
                speechOutputPort = outputPort,
                speechState = speechState,
                selection = selection,
                prefMessage = prefMessage,
                saved = saved,
                interactionCoordinator = interactionCoordinator,
                onSelect = { id ->
                    val change = pref.beginExplicitChange()
                    val ok = outputPort?.selectVoice(id) == true
                    val engineId = selection.engineId
                    saved = false
                    if (ok && engineId != null) {
                        scope.launch { pref.saveConfirmed(change, activeSession, engineId, id) }
                    }
                    ok
                },
                onDefault = {
                    val change = pref.beginExplicitChange()
                    val ok = outputPort?.selectDefaultVoice() == true
                    saved = false
                    if (ok) scope.launch { pref.clearPreference(change, activeSession) }
                    ok
                },
                onDismiss = {
                    outputPort?.stop()
                    interactionCoordinator.stop()
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
    selection: SpeechVoiceSelection,
    prefMessage: String?,
    saved: Boolean,
    interactionCoordinator: InteractionCoordinator,
    onSelect: (String) -> Boolean,
    onDefault: () -> Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("Digite um comando local para testar.") }
    var problem by remember { mutableStateOf<String?>(null) }
    var inputPort by remember { mutableStateOf<SpeechInputPort?>(null) }
    var inputState by remember { mutableStateOf(SpeechInputState.Idle) }
    var listenInteractionId by remember { mutableLongStateOf(0L) }
    val currentOutput by rememberUpdatedState(speechOutputPort)

    fun speak(text: String, interactionId: Long): SpeechOutputResult? {
        return speechOutputPort?.speak(text, interactionId)
    }

    fun submit(text: String = input) {
        inputPort?.cancel()
        val interactionId = interactionCoordinator.begin()
        val pair = when (val result = engine.process(text)) {
            is LocalCommandResult.Recognized -> "${result.intent}: ${result.response}" to result.response
            LocalCommandResult.Unknown -> LocalUnknownResponses.next().let { it to it }
        }
        output = pair.first
        problem = when (speak(pair.second, interactionId)) {
            SpeechOutputResult.Queued -> null
            SpeechOutputResult.Failed -> {
                interactionCoordinator.error(interactionId)
                "Não foi possível iniciar a fala."
            }
            else -> {
                interactionCoordinator.error(interactionId)
                "Voz indisponível; resposta em texto."
            }
        }
        focus.clearFocus()
    }

    val recognized by rememberUpdatedState<(String) -> Unit> {
        { text: String ->
            input = text
            submit(text)
        }
    }
    val context = LocalContext.current.applicationContext
    val owner = LocalView.current.findViewTreeLifecycleOwner()
    DisposableEffect(context, owner) {
        val port = AndroidSpeechInputPort(
            context,
            stopOutput = { currentOutput?.stop() ?: false },
            onState = { state ->
                inputState = state
                interactionCoordinator.input(state, listenInteractionId)
            },
            onText = recognized,
        )
        inputPort = port
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                port.cancel()
                interactionCoordinator.stop()
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose {
            owner?.lifecycle?.removeObserver(observer)
            port.shutdown()
            interactionCoordinator.stop()
            inputPort = null
        }
    }
    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        inputState = if (granted) SpeechInputState.Idle else SpeechInputState.PermissionRequired
    }
    val listening = inputState in setOf(
        SpeechInputState.Starting,
        SpeechInputState.Listening,
        SpeechInputState.Processing,
    )

    Column(
        modifier = modifier
            .padding(12.dp)
            .widthIn(max = 620.dp)
            .fillMaxWidth(0.9f)
            .heightIn(max = 270.dp)
            .background(DebugPanelBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "DESENVOLVIMENTO // COMANDO LOCAL",
                color = DebugPanelAccent,
                fontSize = 12.sp,
            )
            TextButton(onClick = {
                speechOutputPort?.stop()
                interactionCoordinator.stop()
            }) {
                Text("PARAR VOZ")
            }
            TextButton(onClick = {
                inputPort?.cancel()
                onDismiss()
            }) {
                Text("FECHAR")
            }
        }
        Text(
            text = problem ?: speechStatusLabel(speechState),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
        )
        prefMessage?.let {
            Text(
                text = "Preferência: $it",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
            )
        }
        if (selection.ids.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (saved) "Voz salva" else "Voz da sessão",
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
                TextButton(onClick = {
                    inputPort?.cancel()
                    selection.nextId()?.let { id ->
                        problem = if (onSelect(id)) null else "Falha ao trocar voz."
                    }
                }) {
                    Text("TROCAR VOZ")
                }
                TextButton(onClick = {
                    inputPort?.cancel()
                    problem = if (onDefault()) null else "Padrão indisponível; preferência mantida."
                }) {
                    Text("USAR PADRÃO")
                }
                TextButton(onClick = {
                    inputPort?.cancel()
                    val interactionId = interactionCoordinator.begin()
                    problem = when (
                        speak("Olá. Sou a Vexa. Pronta para acompanhar sua viagem.", interactionId)
                    ) {
                        SpeechOutputResult.Queued -> null
                        else -> {
                            interactionCoordinator.error(interactionId)
                            "Falha no exemplo."
                        }
                    }
                }) {
                    Text("TESTAR VOZ")
                }
            }
        }
        Row {
            TextButton(
                enabled = inputPort != null && speechOutputPort != null,
                onClick = {
                    focus.clearFocus()
                    if (listening) {
                        inputPort?.cancel()
                        interactionCoordinator.stop()
                    } else if (currentOutput?.stop() != false) {
                        listenInteractionId = interactionCoordinator.begin()
                        when (inputPort?.start()) {
                            SpeechInputStartResult.PermissionRequired ->
                                permission.launch(Manifest.permission.RECORD_AUDIO)
                            else -> Unit
                        }
                        interactionCoordinator.input(SpeechInputState.Starting, listenInteractionId)
                    } else {
                        problem = "Não foi possível interromper a fala."
                    }
                },
            ) {
                Text(if (listening) "CANCELAR ESCUTA" else "OUVIR")
            }
            Text(
                text = inputState.toString(),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
            )
        }
        OutlinedTextField(
            value = input,
            onValueChange = {
                inputPort?.cancel()
                input = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Entrada") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
        )
        Row {
            Button(onClick = { submit() }) {
                Text("ENVIAR")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = output,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

private fun speechStatusLabel(state: SpeechOutputState): String = when (state) {
    SpeechOutputState.Preparing -> "Voz: preparando…"
    SpeechOutputState.Ready -> "Voz: pronta"
    SpeechOutputState.Unavailable -> "Voz local pt-BR indisponível"
    SpeechOutputState.Queued -> "Voz: aguardando início"
    SpeechOutputState.Speaking -> "Voz: falando"
    SpeechOutputState.Completed -> "Voz: fala concluída"
    SpeechOutputState.Stopped -> "Voz: interrupção solicitada"
    SpeechOutputState.Failed -> "Voz: falha; resposta em texto."
    SpeechOutputState.Closed -> "Voz: encerrada"
}
