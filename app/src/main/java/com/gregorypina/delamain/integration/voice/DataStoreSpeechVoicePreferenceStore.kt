package com.gregorypina.delamain.integration.voice

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gregorypina.delamain.domain.SpeechVoicePreference
import com.gregorypina.delamain.domain.SpeechVoicePreferenceReadResult
import com.gregorypina.delamain.domain.SpeechVoicePreferenceStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.io.IOException

private val Context.voicePreferencesDataStore by preferencesDataStore(name = "vexa_preferences")
internal val voicePreferenceVersionKey = intPreferencesKey("voice_preference_version")
internal val voiceEngineIdKey = stringPreferencesKey("voice_engine_id")
internal val voiceIdKey = stringPreferencesKey("voice_id")
internal fun clearVoicePreferenceKeys(values: MutablePreferences) {
    values.remove(voicePreferenceVersionKey); values.remove(voiceEngineIdKey); values.remove(voiceIdKey)
}
class DataStoreSpeechVoicePreferenceStore(context: Context) : SpeechVoicePreferenceStore {
    private val dataStore = context.applicationContext.voicePreferencesDataStore
    override suspend fun read(): SpeechVoicePreferenceReadResult = try {
        val values = dataStore.data.first(); val version = values[voicePreferenceVersionKey]
        val engine = values[voiceEngineIdKey]; val voice = values[voiceIdKey]
        if (version == null && engine == null && voice == null) SpeechVoicePreferenceReadResult.Empty
        else if (version == null || engine.isNullOrBlank() || voice.isNullOrBlank()) SpeechVoicePreferenceReadResult.Failed
        else SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference(engine, voice, version))
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { SpeechVoicePreferenceReadResult.Failed }
      catch (_: RuntimeException) { SpeechVoicePreferenceReadResult.Failed }
    override suspend fun write(preference: SpeechVoicePreference): Boolean = try {
        dataStore.edit { it[voicePreferenceVersionKey] = preference.version; it[voiceEngineIdKey] = preference.engineId; it[voiceIdKey] = preference.voiceId }; true
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { false }
      catch (_: RuntimeException) { false }
    override suspend fun clear(): Boolean = try {
        dataStore.edit(::clearVoicePreferenceKeys); true
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { false }
      catch (_: RuntimeException) { false }
}
