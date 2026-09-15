package com.gregorypina.delamain.ui

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gregorypina.delamain.domain.LocalCommandEngine
import com.gregorypina.delamain.domain.LocalCommandResult
import com.gregorypina.delamain.integration.CompositeLocalActionPort
import com.gregorypina.delamain.integration.apps.AndroidLaunchAppActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaVolumeActionPort

private val DebugPanelBackground = Color(0xEE101820)
private val DebugPanelAccent = Color(0xFF2E8BFF)

@Composable
internal fun DebugCommandPanel() {
    val applicationContext = LocalContext.current.applicationContext
    val engine = remember(applicationContext) {
        LocalCommandEngine(
            actionPort = CompositeLocalActionPort(
                volumePort = AndroidMediaVolumeActionPort.from(applicationContext),
                launchAppPort = AndroidLaunchAppActionPort.from(applicationContext),
            ),
        )
    }
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
    ) {
        if (expanded) {
            DebugCommandPanelContent(
                engine = engine,
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
    engine: LocalCommandEngine,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("Digite um comando local para testar.") }

    fun submit() {
        output = when (val result = engine.process(input)) {
            is LocalCommandResult.Recognized -> "${result.intent}: ${result.response}"
            LocalCommandResult.Unknown -> "Comando local não reconhecido."
        }
        focusManager.clearFocus()
    }

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
            TextButton(onClick = onDismiss) {
                Text("FECHAR")
            }
        }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
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
