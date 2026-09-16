package com.gregorypina.delamain.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SpeechVoicePreference(
    val engineId: String,
    val voiceId: String,
    val version: Int = CURRENT_VERSION,
) {
    companion object { const val CURRENT_VERSION = 1 }
}

sealed interface SpeechVoicePreferenceReadResult {
    data class Found(val preference: SpeechVoicePreference) : SpeechVoicePreferenceReadResult
    data object Empty : SpeechVoicePreferenceReadResult
    data object Failed : SpeechVoicePreferenceReadResult
}

interface SpeechVoicePreferenceStore {
    suspend fun read(): SpeechVoicePreferenceReadResult
    suspend fun write(preference: SpeechVoicePreference): Boolean
    suspend fun clear(): Boolean
}

sealed interface SpeechVoicePreferenceEvent {
    data class Restore(val preference: SpeechVoicePreference) : SpeechVoicePreferenceEvent
    data object Unavailable : SpeechVoicePreferenceEvent
    data object ReadFailed : SpeechVoicePreferenceEvent
    data object Saved : SpeechVoicePreferenceEvent
    data object Cleared : SpeechVoicePreferenceEvent
    data object SaveFailed : SpeechVoicePreferenceEvent
    data object ClearFailed : SpeechVoicePreferenceEvent
}

/** Coordinates persistence only; TTS application remains in the adapter. */
class SpeechVoicePreferenceCoordinator(
    private val store: SpeechVoicePreferenceStore,
    private val onEvent: (SpeechVoicePreferenceEvent) -> Unit = {},
) {
    private var generation = 0L
    private val writeMutex = Mutex()

    /** Call synchronously before applying any explicit user choice. */
    fun beginExplicitChange(): Long {
        generation += 1
        return generation
    }

    suspend fun restore(engineId: String, eligibleVoiceIds: Set<String>) {
        val startedAt = generation
        when (val result = store.read()) {
            is SpeechVoicePreferenceReadResult.Found -> {
                if (startedAt != generation) return
                val preference = result.preference
                if (preference.version == SpeechVoicePreference.CURRENT_VERSION &&
                    preference.engineId == engineId && preference.voiceId in eligibleVoiceIds) {
                    onEvent(SpeechVoicePreferenceEvent.Restore(preference))
                } else onEvent(SpeechVoicePreferenceEvent.Unavailable)
            }
            SpeechVoicePreferenceReadResult.Empty -> Unit
            SpeechVoicePreferenceReadResult.Failed -> if (startedAt == generation) {
                onEvent(SpeechVoicePreferenceEvent.ReadFailed)
            }
        }
    }

    suspend fun saveConfirmed(change: Long, engineId: String, voiceId: String) {
        val saved = writeMutex.withLock { store.write(SpeechVoicePreference(engineId, voiceId)) }
        if (change == generation) onEvent(
            if (saved) SpeechVoicePreferenceEvent.Saved else SpeechVoicePreferenceEvent.SaveFailed,
        )
    }

    suspend fun clearPreference(change: Long) {
        val cleared = writeMutex.withLock { store.clear() }
        if (change == generation) onEvent(
            if (cleared) SpeechVoicePreferenceEvent.Cleared else SpeechVoicePreferenceEvent.ClearFailed,
        )
    }
}
