package com.gregorypina.delamain.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SpeechVoicePreference(val engineId: String, val voiceId: String, val version: Int = CURRENT_VERSION) {
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
    suspend fun readMute(): Boolean = false
    suspend fun writeMute(muted: Boolean): Boolean = true
}
sealed interface SpeechVoicePreferenceEvent {
    data class Restore(val preference: SpeechVoicePreference) : SpeechVoicePreferenceEvent
    data object Unavailable : SpeechVoicePreferenceEvent
    data object ReadFailed : SpeechVoicePreferenceEvent
    data object Saved : SpeechVoicePreferenceEvent
    data object Cleared : SpeechVoicePreferenceEvent
    data object SaveFailed : SpeechVoicePreferenceEvent
    data object ClearFailed : SpeechVoicePreferenceEvent
    data class MuteRestored(val muted: Boolean) : SpeechVoicePreferenceEvent
}

/** Persistence coordinator. Session tokens prevent a restore from escaping the panel lifetime. */
class SpeechVoicePreferenceCoordinator(
    private val store: SpeechVoicePreferenceStore,
    private val onEvent: (Long, SpeechVoicePreferenceEvent) -> Unit = { _, _ -> },
) {
    private var generation = 0L
    private var session = 0L
    private val writeMutex = Mutex()

    fun openSession(): Long { session += 1; return session }
    fun closeSession(token: Long) { if (token == session) session += 1 }
    fun beginExplicitChange(): Long { generation += 1; return generation }

    suspend fun restore(token: Long, engineId: String, eligibleVoiceIds: Set<String>) {
        val startedAt = generation
        val result = store.read()
        if (token != session || startedAt != generation) return
        when (result) {
            is SpeechVoicePreferenceReadResult.Found -> {
                val p = result.preference
                if (p.version == SpeechVoicePreference.CURRENT_VERSION && p.engineId == engineId && p.voiceId in eligibleVoiceIds)
                    onEvent(token, SpeechVoicePreferenceEvent.Restore(p))
                else onEvent(token, SpeechVoicePreferenceEvent.Unavailable)
            }
            SpeechVoicePreferenceReadResult.Empty -> Unit
            SpeechVoicePreferenceReadResult.Failed -> onEvent(token, SpeechVoicePreferenceEvent.ReadFailed)
        }
    }

    suspend fun saveConfirmed(change: Long, token: Long, engineId: String, voiceId: String) {
        val result = writeMutex.withLock {
            if (change != generation) return@withLock null
            store.write(SpeechVoicePreference(engineId, voiceId))
        } ?: return
        if (change == generation && token == session)
            onEvent(token, if (result) SpeechVoicePreferenceEvent.Saved else SpeechVoicePreferenceEvent.SaveFailed)
    }

    suspend fun clearPreference(change: Long, token: Long) {
        val result = writeMutex.withLock {
            if (change != generation) return@withLock null
            store.clear()
        } ?: return
        if (change == generation && token == session)
            onEvent(token, if (result) SpeechVoicePreferenceEvent.Cleared else SpeechVoicePreferenceEvent.ClearFailed)
    }

    suspend fun restoreMute(token: Long) {
        if (token != session) return
        onEvent(token, SpeechVoicePreferenceEvent.MuteRestored(store.readMute()))
    }

    suspend fun persistMute(token: Long, muted: Boolean) {
        writeMutex.withLock {
            if (token == session) store.writeMute(muted)
        }
    }
}
