package com.gregorypina.delamain.integration.voice

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gregorypina.delamain.domain.PersonalityPreference
import com.gregorypina.delamain.domain.PersonalityPreferenceReadResult
import com.gregorypina.delamain.domain.PersonalityTone
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
internal val voiceMutedKey = booleanPreferencesKey("voice_muted")
internal val personalityDisplayNameKey = stringPreferencesKey("personality_display_name")
internal val personalityToneKey = intPreferencesKey("personality_tone")
internal fun clearVoicePreferenceKeys(values: MutablePreferences) {
    values.remove(voicePreferenceVersionKey); values.remove(voiceEngineIdKey); values.remove(voiceIdKey)
}
internal fun clearPersonalityKeys(values: MutablePreferences) {
    values.remove(personalityDisplayNameKey); values.remove(personalityToneKey)
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
    override suspend fun readMute(): Boolean = try {
        dataStore.data.first()[voiceMutedKey] ?: false
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { false }
      catch (_: RuntimeException) { false }
    override suspend fun writeMute(muted: Boolean): Boolean = try {
        dataStore.edit { it[voiceMutedKey] = muted }; true
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { false }
      catch (_: RuntimeException) { false }
    override suspend fun readPersonality(): PersonalityPreferenceReadResult = try {
        val values = dataStore.data.first()
        val name = values[personalityDisplayNameKey]
        val toneRaw = values[personalityToneKey]
        if (name == null && toneRaw == null) PersonalityPreferenceReadResult.Empty
        else PersonalityPreferenceReadResult.Found(
            PersonalityPreference(
                displayName = name?.takeIf { it.isNotBlank() },
                tone = toneRaw?.toPersonalityTone() ?: PersonalityTone.WARM,
            ),
        )
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { PersonalityPreferenceReadResult.Failed }
      catch (_: RuntimeException) { PersonalityPreferenceReadResult.Failed }
    override suspend fun writePersonality(preference: PersonalityPreference): Boolean = try {
        dataStore.edit {
            val name = preference.displayName?.trim()?.takeIf { it.isNotEmpty() }
            if (name == null) it.remove(personalityDisplayNameKey) else it[personalityDisplayNameKey] = name
            it[personalityToneKey] = preference.tone.storageValue()
        }
        true
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { false }
      catch (_: RuntimeException) { false }
    override suspend fun clearPersonality(): Boolean = try {
        dataStore.edit(::clearPersonalityKeys); true
    } catch (e: CancellationException) { throw e }
      catch (_: IOException) { false }
      catch (_: RuntimeException) { false }
}

private fun PersonalityTone.storageValue(): Int = when (this) {
    PersonalityTone.WARM -> 0
    PersonalityTone.DIRECT -> 1
}

private fun Int.toPersonalityTone(): PersonalityTone = when (this) {
    1 -> PersonalityTone.DIRECT
    else -> PersonalityTone.WARM
}
