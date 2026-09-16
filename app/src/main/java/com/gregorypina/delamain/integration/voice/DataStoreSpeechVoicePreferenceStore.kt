package com.gregorypina.delamain.integration.voice

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gregorypina.delamain.domain.SpeechVoicePreference
import com.gregorypina.delamain.domain.SpeechVoicePreferenceReadResult
import com.gregorypina.delamain.domain.SpeechVoicePreferenceStore
import kotlinx.coroutines.flow.first
import java.io.IOException

private val Context.voicePreferencesDataStore by preferencesDataStore(name = "vexa_preferences")

class DataStoreSpeechVoicePreferenceStore(context: Context) : SpeechVoicePreferenceStore {
    private val dataStore = context.applicationContext.voicePreferencesDataStore

    override suspend fun read(): SpeechVoicePreferenceReadResult = try {
        val values = dataStore.data.first()
        val version = values[VERSION]
        val engine = values[ENGINE_ID]
        val voice = values[VOICE_ID]
        if (version == null && engine == null && voice == null) SpeechVoicePreferenceReadResult.Empty
        else if (version == null || engine.isNullOrBlank() || voice.isNullOrBlank()) {
            SpeechVoicePreferenceReadResult.Failed
        } else SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference(engine, voice, version))
    } catch (_: IOException) {
        SpeechVoicePreferenceReadResult.Failed
    } catch (_: RuntimeException) {
        SpeechVoicePreferenceReadResult.Failed
    }

    override suspend fun write(preference: SpeechVoicePreference): Boolean = try {
        dataStore.edit { values ->
            values[VERSION] = preference.version
            values[ENGINE_ID] = preference.engineId
            values[VOICE_ID] = preference.voiceId
        }
        true
    } catch (_: IOException) {
        false
    } catch (_: RuntimeException) {
        false
    }

    override suspend fun clear(): Boolean = try {
        dataStore.edit { values ->
            // Deliberately remove only voice keys; future personality settings share this store safely.
            values.remove(VERSION)
            values.remove(ENGINE_ID)
            values.remove(VOICE_ID)
        }
        true
    } catch (_: IOException) {
        false
    } catch (_: RuntimeException) {
        false
    }

    private companion object {
        val VERSION = intPreferencesKey("voice_preference_version")
        val ENGINE_ID = stringPreferencesKey("voice_engine_id")
        val VOICE_ID = stringPreferencesKey("voice_id")
    }
}
