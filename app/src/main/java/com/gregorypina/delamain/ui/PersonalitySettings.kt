package com.gregorypina.delamain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gregorypina.delamain.R
import com.gregorypina.delamain.domain.PersonalityTone

private val Accent = Color(0xFF2E8BFF)

@Composable
fun PersonalitySettings(
    session: VoiceInteractionSession,
    nameDraft: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        session.personalityMessage?.let { message ->
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
            )
        }
        val nameDescription = stringResource(R.string.cd_personality_name)
        OutlinedTextField(
            value = nameDraft,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = nameDescription },
            label = { Text(stringResource(R.string.label_personality_name)) },
            placeholder = { Text(stringResource(R.string.hint_personality_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = personalityFieldColors(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val warmDescription = stringResource(R.string.cd_tone_warm)
            FilterChip(
                selected = session.personalityTone == PersonalityTone.WARM,
                onClick = { session.selectPersonalityTone(PersonalityTone.WARM) },
                label = { Text(stringResource(R.string.tone_warm)) },
                modifier = Modifier.semantics { contentDescription = warmDescription },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Accent.copy(alpha = 0.35f),
                    labelColor = Color.White,
                    selectedLabelColor = Color.White,
                ),
            )
            val directDescription = stringResource(R.string.cd_tone_direct)
            FilterChip(
                selected = session.personalityTone == PersonalityTone.DIRECT,
                onClick = { session.selectPersonalityTone(PersonalityTone.DIRECT) },
                label = { Text(stringResource(R.string.tone_direct)) },
                modifier = Modifier.semantics { contentDescription = directDescription },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Accent.copy(alpha = 0.35f),
                    labelColor = Color.White,
                    selectedLabelColor = Color.White,
                ),
            )
        }
        session.personalityPreview?.let { preview ->
            Text(
                text = stringResource(R.string.personality_preview, preview),
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val saveDescription = stringResource(R.string.cd_save_personality)
            Button(
                onClick = { session.savePersonality(nameDraft) },
                modifier = Modifier.semantics { contentDescription = saveDescription },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(stringResource(R.string.action_save_personality))
            }
            val previewDescription = stringResource(R.string.cd_preview_personality)
            TextButton(
                onClick = { session.previewPersonality(nameDraft) },
                modifier = Modifier.semantics { contentDescription = previewDescription },
            ) {
                Text(stringResource(R.string.action_preview_personality), color = Accent, fontSize = 12.sp)
            }
            val resetDescription = stringResource(R.string.cd_reset_personality)
            TextButton(
                onClick = {
                    onNameChange("")
                    session.resetPersonality()
                },
                modifier = Modifier.semantics { contentDescription = resetDescription },
            ) {
                Text(stringResource(R.string.action_reset_personality), color = Accent, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun personalityFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Accent,
    focusedBorderColor = Accent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.55f),
    focusedLabelColor = Accent,
    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
)
