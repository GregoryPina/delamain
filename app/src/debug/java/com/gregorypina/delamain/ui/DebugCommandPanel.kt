package com.gregorypina.delamain.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.gregorypina.delamain.domain.*
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
    val scope = rememberCoroutineScope()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var speechOutputPort by remember { mutableStateOf<AndroidTextToSpeechPort?>(null) }
    var voiceSelection by remember { mutableStateOf(SpeechVoiceSelection()) }
    var speechState by remember { mutableStateOf(SpeechOutputState.Preparing) }
    var preferenceMessage by remember { mutableStateOf<String?>(null) }
    var preferenceSaved by remember { mutableStateOf(false) }
    var restoredCatalogKey by remember { mutableStateOf<String?>(null) }
    val preferenceStore = remember(applicationContext) { DataStoreSpeechVoicePreferenceStore(applicationContext) }
    val preferenceCoordinator = remember(preferenceStore) {
        SpeechVoicePreferenceCoordinator(preferenceStore) { event ->
            when (event) {
                is SpeechVoicePreferenceEvent.Restore -> {
                    val restored = speechOutputPort?.restoreVoice(event.preference.engineId, event.preference.voiceId) == true
                    preferenceSaved = restored
                    preferenceMessage = if (restored) "Voz salva restaurada." else "Preferência de voz indisponível; usando voz local padrão."
                }
                SpeechVoicePreferenceEvent.Unavailable -> {
                    preferenceSaved = false
                    preferenceMessage = "Preferência de voz indisponível; usando voz local padrão."
                }
                SpeechVoicePreferenceEvent.ReadFailed -> {
                    preferenceSaved = false
                    preferenceMessage = "Não foi possível ler a preferência; usando voz local nesta sessão."
                }
                SpeechVoicePreferenceEvent.Saved -> {
                    preferenceSaved = true
                    preferenceMessage = "Preferência de voz salva."
                }
                SpeechVoicePreferenceEvent.SaveFailed -> {
                    preferenceSaved = false
                    preferenceMessage = "Usando nesta sessão; não foi possível salvar."
                }
                SpeechVoicePreferenceEvent.Cleared -> {
                    preferenceSaved = false
                    preferenceMessage = "Preferência removida; usando voz local padrão."
                }
                SpeechVoicePreferenceEvent.ClearFailed -> {
                    preferenceMessage = "Não foi possível remover a preferência salva."
                }
            }
        }
    }
    val lifecycleOwner = LocalView.current.findViewTreeLifecycleOwner()
    DisposableEffect(applicationContext, expanded, lifecycleOwner) {
        if (expanded) {
            speechState = SpeechOutputState.Preparing
            voiceSelection = SpeechVoiceSelection()
            preferenceMessage = null
            preferenceSaved = false
            restoredCatalogKey = null
            val port = AndroidTextToSpeechPort(
                applicationContext,
                onState = { speechState = it },
                onVoices = { selection ->
                    voiceSelection = selection
                    val engineId = selection.engineId
                    if (engineId != null && selection.ids.isNotEmpty()) {
                        val key = engineId + "\u0000" + selection.ids.joinToString("\u0000")
                        if (restoredCatalogKey == null) {
                            restoredCatalogKey = key
                            scope.launch { preferenceCoordinator.restore(engineId, selection.ids.toSet()) }
                        }
                    }
                },
            )
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
        } else onDispose { }
    }

    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
        if (expanded) {
            DebugCommandPanelContent(
                engine = engine,
                speechOutputPort = speechOutputPort,
                speechState = speechState,
                voiceSelection = voiceSelection,
                preferenceMessage = preferenceMessage,
                preferenceSaved = preferenceSaved,
                onSelectVoice = { id ->
                    val change = preferenceCoordinator.beginExplicitChange()
                    val port = speechOutputPort
                    val engineId = voiceSelection.engineId
                    val applied = port?.selectVoice(id) == true
                    preferenceSaved = false
                    if (applied && engineId != null) {
                        scope.launch { preferenceCoordinator.saveConfirmed(change, engineId, id) }
                    }
                    applied
                },
                onUseDefault = {
                    val change = preferenceCoordinator.beginExplicitChange()
                    val applied = speechOutputPort?.selectDefaultVoice() == true
                    preferenceSaved = false
                    scope.launch { preferenceCoordinator.clearPreference(change) }
                    applied
                },
                onDismiss = { speechOutputPort?.stop(); expanded = false },
                modifier = Modifier.align(Alignment.TopCenter),
            )
        } else TextButton(onClick = { expanded = true }, modifier = Modifier.align(Alignment.TopStart)) {
            Text("DEV", color = DebugPanelAccent, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun DebugCommandPanelContent(
    engine: LocalCommandEngine,
    speechOutputPort: SpeechOutputPort?,
    speechState: SpeechOutputState,
    voiceSelection: SpeechVoiceSelection,
    preferenceMessage: String?,
    preferenceSaved: Boolean,
    onSelectVoice: (String) -> Boolean,
    onUseDefault: () -> Boolean,
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
            is LocalCommandResult.Recognized -> "${result.intent}: ${result.response}" to result.response
            LocalCommandResult.Unknown -> LocalUnknownResponses.next().let { it to it }
        }
        output = display
        submissionProblem = when (speechOutputPort?.speak(speechText)) {
            SpeechOutputResult.Queued -> null
            SpeechOutputResult.Failed -> "Não foi possível iniciar a fala."
            SpeechOutputResult.Unavailable, null -> "Voz ainda não disponível; resposta em texto."
        }
        focusManager.clearFocus()
    }

    val onRecognized by rememberUpdatedState<(String) -> Unit> { text -> input = text; submit(text) }
    val context = LocalContext.current.applicationContext
    val owner = LocalView.current.findViewTreeLifecycleOwner()
    DisposableEffect(context, owner) {
        val port = AndroidSpeechInputPort(context, stopOutput = { currentOutput?.stop() ?: false },
            onState = { inputState = it }, onText = { onRecognized(it) })
        speechInputPort = port
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) port.cancel() }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer); port.shutdown(); speechInputPort = null }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        inputState = if (granted) SpeechInputState.Idle else SpeechInputState.PermissionRequired
    }
    val listening = inputState in setOf(SpeechInputState.Starting, SpeechInputState.Listening, SpeechInputState.Processing)

    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp).widthIn(max = 620.dp)
            .fillMaxWidth(0.9f).heightIn(max = 250.dp).background(DebugPanelBackground)
            .verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("DESENVOLVIMENTO // COMANDO LOCAL", color = DebugPanelAccent, fontFamily = FontFamily.Monospace,
                fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            TextButton(onClick = { submissionProblem = null; speechOutputPort?.stop() }) { Text("PARAR VOZ") }
            TextButton(onClick = { speechInputPort?.cancel(); onDismiss() }) { Text("FECHAR") }
        }
        Text(
            text = submissionProblem ?: preferenceMessage ?: when (speechState) {
                SpeechOutputState.Preparing -> "Voz: preparando…"
                SpeechOutputState.Ready -> "Voz: pronta"
                SpeechOutputState.Unavailable -> "Voz local pt-BR indisponível; resposta em texto."
                SpeechOutputState.Queued -> "Voz: aguardando início"
                SpeechOutputState.Speaking -> "Voz: falando"
                SpeechOutputState.Completed -> "Voz: fala concluída"
                SpeechOutputState.Stopped -> "Voz: interrupção solicitada"
                SpeechOutputState.Failed -> "Voz: falha; resposta em texto."
                SpeechOutputState.Closed -> "Voz: encerrada"
            }, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
        )
        if (voiceSelection.ids.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (voiceSelection.selectedId == null) "Voz não selecionada" else
                        "Voz ${voiceSelection.ids.indexOf(voiceSelection.selectedId) + 1} de ${voiceSelection.ids.size}" +
                            if (preferenceSaved) " (salva)" else " (sessão)",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {
                    speechInputPort?.cancel()
                    voiceSelection.nextId()?.let { id ->
                        submissionProblem = if (onSelectVoice(id)) null else "Não foi possível trocar a voz."
                    }
                }) { Text("TROCAR VOZ") }
                TextButton(onClick = {
                    speechInputPort?.cancel()
                    submissionProblem = if (onUseDefault()) null else "Preferência removida; voz local indisponível."
                }) { Text("USAR PADRÃO") }
                TextButton(enabled = voiceSelection.selectedId != null, onClick = {
                    speechInputPort?.cancel()
                    submissionProblem = when (speechOutputPort?.speak("Olá. Sou a Vexa. Pronta para acompanhar sua viagem.")) {
                        SpeechOutputResult.Queued -> null
                        else -> "Não foi possível reproduzir o exemplo."
                    }
                }) { Text("TESTAR VOZ") }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(enabled = speechInputPort != null && speechOutputPort != null, onClick = {
                focusManager.clearFocus()
                if (listening) speechInputPort?.cancel()
                else if (speechInputPort?.start() == SpeechInputStartResult.PermissionRequired) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }) { Text(if (listening) "CANCELAR ESCUTA" else "OUVIR") }
            Text(text = when (inputState) {
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
            }, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(
            value = input, onValueChange = { speechInputPort?.cancel(); input = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Entrada") }, placeholder = { Text("Ex.: que horas são?") }, singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = KeyboardActions(onSend = { submit() }),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                cursorColor = DebugPanelAccent, focusedBorderColor = DebugPanelAccent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.55f), focusedLabelColor = DebugPanelAccent,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f), focusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.45f)),
        )
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
            Button(onClick = { submit() }) { Text("ENVIAR") }
            Text(output, color = Color.White, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
        }
    }
}
