package com.gregorypina.delamain.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gregorypina.delamain.R

private val PanelBackground = Color(0xCC101820)
private val Accent = Color(0xFF2E8BFF)

@Composable
fun UserInteractionControls(
    session: VoiceInteractionSession,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var textExpanded by rememberSaveable { mutableStateOf(false) }
    var personalityExpanded by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }
    var nameDraft by rememberSaveable { mutableStateOf(session.personalityDisplayName.orEmpty()) }
    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> session.onMicrophonePermissionResult(granted) }

    val listenLabel = if (session.listening) {
        stringResource(R.string.action_cancel_listen)
    } else {
        stringResource(R.string.action_listen)
    }
    val listenDescription = if (session.listening) {
        stringResource(R.string.cd_cancel_listen)
    } else {
        stringResource(R.string.cd_listen)
    }

    Column(
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth(0.92f)
            .imePadding()
            .background(PanelBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        session.statusMessage?.let { key ->
            val message = stringResource(userMessageRes(key))
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 12.sp,
                modifier = Modifier.semantics { contentDescription = message },
            )
        }
        session.lastResponse?.let { response ->
            Text(
                text = response,
                color = Color.White.copy(alpha = 0.72f),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                maxLines = 2,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    session.toggleListen { permission.launch(Manifest.permission.RECORD_AUDIO) }
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = listenDescription },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(listenLabel)
            }
            if (session.speaking) {
                val stopDescription = stringResource(R.string.cd_stop_speech)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        session.stopSpeech()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = stopDescription },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A4450)),
                ) {
                    Text(stringResource(R.string.action_stop_speech))
                }
            }
            val muteDescription = stringResource(
                if (session.voiceMuted) R.string.cd_disable_mute else R.string.cd_enable_mute,
            )
            Button(
                onClick = {
                    focusManager.clearFocus()
                    session.toggleMute()
                },
                modifier = Modifier.semantics { contentDescription = muteDescription },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (session.voiceMuted) Color(0xFF5A3A3A) else Color(0xFF3A4450),
                ),
            ) {
                Text(
                    stringResource(
                        if (session.voiceMuted) R.string.action_disable_mute else R.string.action_enable_mute,
                    ),
                )
            }
            val textToggleDescription = stringResource(
                if (textExpanded) R.string.cd_hide_text else R.string.cd_show_text,
            )
            TextButton(
                onClick = { textExpanded = !textExpanded },
                modifier = Modifier.semantics { contentDescription = textToggleDescription },
            ) {
                Text(
                    text = stringResource(
                        if (textExpanded) R.string.action_hide_text else R.string.action_show_text,
                    ),
                    color = Accent,
                    fontSize = 12.sp,
                )
            }
            val personalityToggleDescription = stringResource(
                if (personalityExpanded) R.string.cd_hide_personality else R.string.cd_show_personality,
            )
            TextButton(
                onClick = { personalityExpanded = !personalityExpanded },
                modifier = Modifier.semantics { contentDescription = personalityToggleDescription },
            ) {
                Text(
                    text = stringResource(
                        if (personalityExpanded) {
                            R.string.action_hide_personality
                        } else {
                            R.string.action_show_personality
                        },
                    ),
                    color = Accent,
                    fontSize = 12.sp,
                )
            }
        }
        AnimatedVisibility(visible = personalityExpanded) {
            PersonalitySettings(
                session = session,
                nameDraft = nameDraft,
                onNameChange = { nameDraft = it },
            )
        }
        AnimatedVisibility(visible = textExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val textInputDescription = stringResource(R.string.cd_text_input)
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = textInputDescription },
                    label = { Text(stringResource(R.string.label_command_input)) },
                    placeholder = { Text(stringResource(R.string.hint_command_input)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            when (session.submitText(draft)) {
                                is VoiceInteractionSession.SubmitResult.EmptyInput -> Unit
                                else -> {
                                    draft = ""
                                }
                            }
                            focusManager.clearFocus()
                        },
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Accent,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.55f),
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    ),
                )
                val sendDescription = stringResource(R.string.cd_send_command)
                Button(
                    onClick = {
                        when (session.submitText(draft)) {
                            is VoiceInteractionSession.SubmitResult.EmptyInput -> Unit
                            else -> draft = ""
                        }
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.semantics { contentDescription = sendDescription },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text(stringResource(R.string.action_send))
                }
            }
        }
    }
}

private fun userMessageRes(key: VoiceInteractionSession.UserMessageKey): Int = when (key) {
    VoiceInteractionSession.UserMessageKey.MicrophoneDenied -> R.string.msg_microphone_denied
    VoiceInteractionSession.UserMessageKey.ListenUnavailable -> R.string.msg_listen_unavailable
    VoiceInteractionSession.UserMessageKey.ListenFailed -> R.string.msg_listen_failed
    VoiceInteractionSession.UserMessageKey.ListenTimedOut -> R.string.msg_listen_timed_out
    VoiceInteractionSession.UserMessageKey.NoSpeechHeard -> R.string.msg_no_speech
    VoiceInteractionSession.UserMessageKey.VoiceUnavailable -> R.string.msg_voice_unavailable
    VoiceInteractionSession.UserMessageKey.SpeechFailed -> R.string.msg_speech_failed
    VoiceInteractionSession.UserMessageKey.CannotStopSpeech -> R.string.msg_cannot_stop_speech
    VoiceInteractionSession.UserMessageKey.InteractionCancelled -> R.string.msg_interaction_cancelled
}
