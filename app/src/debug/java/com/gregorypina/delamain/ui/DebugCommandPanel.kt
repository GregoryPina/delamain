package com.gregorypina.delamain.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gregorypina.delamain.domain.InteractionCoordinator
import com.gregorypina.delamain.domain.SpeechOutputState

private val DebugPanelBackground = Color(0xEE101820)
private val DebugPanelAccent = Color(0xFF2E8BFF)

@Composable
internal fun DebugCommandPanel(
    interactionCoordinator: InteractionCoordinator,
    voiceSession: VoiceInteractionSession,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
    ) {
        if (expanded) {
            DebugCommandPanelContent(
                voiceSession = voiceSession,
                onDismiss = { expanded = false },
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
    voiceSession: VoiceInteractionSession,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("Digite um comando local para testar.") }
    var problem by remember { mutableStateOf<String?>(null) }

    fun submit(text: String = input) {
        when (val result = voiceSession.submitText(text)) {
            is VoiceInteractionSession.SubmitResult.Spoken -> {
                output = result.display
                problem = null
            }
            is VoiceInteractionSession.SubmitResult.TextOnly -> {
                output = result.display
                problem = "Resposta em texto."
            }
            VoiceInteractionSession.SubmitResult.EmptyInput -> Unit
        }
        focus.clearFocus()
    }

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
                voiceSession.stopSpeech()
            }) {
                Text("PARAR VOZ")
            }
            TextButton(onClick = onDismiss) {
                Text("FECHAR")
            }
        }
        Text(
            text = problem ?: debugSpeechLabel(voiceSession.speechState),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
        )
        voiceSession.preferenceMessage?.let {
            Text(
                text = "Preferência: $it",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
            )
        }
        if (voiceSession.voiceSelection.ids.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (voiceSession.preferenceSaved) "Voz salva" else "Voz da sessão",
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
                TextButton(onClick = {
                    voiceSession.voiceSelection.nextId()?.let { id ->
                        problem = if (voiceSession.selectVoice(id)) null else "Falha ao trocar voz."
                    }
                }) {
                    Text("TROCAR VOZ")
                }
                TextButton(onClick = {
                    problem = if (voiceSession.selectDefaultVoice()) {
                        null
                    } else {
                        "Padrão indisponível; preferência mantida."
                    }
                }) {
                    Text("USAR PADRÃO")
                }
                TextButton(onClick = {
                    problem = if (voiceSession.speakSample()) null else "Falha no exemplo."
                }) {
                    Text("TESTAR VOZ")
                }
            }
        }
        Text(
            text = voiceSession.inputState.toString(),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
        )
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
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

private fun debugSpeechLabel(state: SpeechOutputState): String = when (state) {
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
