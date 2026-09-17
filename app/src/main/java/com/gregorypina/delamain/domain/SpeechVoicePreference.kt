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
    suspend fun readPersonality(): PersonalityPreferenceReadResult = PersonalityPreferenceReadResult.Empty
    suspend fun writePersonality(preference: PersonalityPreference): Boolean = true
    suspend fun clearPersonality(): Boolean = true
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
    data class PersonalityRestored(val preference: PersonalityPreference) : SpeechVoicePreferenceEvent
    data object PersonalitySaved : SpeechVoicePreferenceEvent
    data object PersonalityCleared : SpeechVoicePreferenceEvent
    data object PersonalitySaveFailed : SpeechVoicePreferenceEvent
    data object PersonalityClearFailed : SpeechVoicePreferenceEvent
}

/** Persistence coordinator. Session tokens prevent a restore from escaping the panel lifetime. */
class SpeechVoicePreferenceCoordinator(
    private val store: SpeechVoicePreferenceStore,
    private val onEvent: (Long, SpeechVoicePreferenceEvent) -> Unit = { _, _ -> },
) {
    private var generation = 0L
    private var session = 0L
    private var muteGeneration = 0L
    private var personalityGeneration = 0L
    private val writeMutex = Mutex()

    fun openSession(): Long { session += 1; return session }
    fun closeSession(token: Long) { if (token == session) session += 1 }
    fun beginExplicitChange(): Long { generation += 1; return generation }
    fun beginMuteChange(): Long = ++muteGeneration
    fun beginPersonalityChange(): Long = ++personalityGeneration

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
        val revision = muteGeneration
        if (revision != 0L) return // An explicit choice in this coordinator already owns session state.
        val muted = store.readMute()
        if (token == session && revision == muteGeneration)
            onEvent(token, SpeechVoicePreferenceEvent.MuteRestored(muted))
    }

    suspend fun persistMute(token: Long, muted: Boolean, change: Long = beginMuteChange()) {
        writeMutex.withLock {
            if (token == session && change == muteGeneration) store.writeMute(muted)
        }
    }

    suspend fun restorePersonality(token: Long) {
        if (token != session) return
        val revision = personalityGeneration
        if (revision != 0L) return
        val result = store.readPersonality()
        if (token != session || revision != personalityGeneration) return
        when (result) {
            is PersonalityPreferenceReadResult.Found ->
                onEvent(token, SpeechVoicePreferenceEvent.PersonalityRestored(result.preference))
            PersonalityPreferenceReadResult.Empty ->
                onEvent(token, SpeechVoicePreferenceEvent.PersonalityRestored(PersonalityPreference()))
            PersonalityPreferenceReadResult.Failed -> Unit
        }
    }

    suspend fun persistPersonality(token: Long, preference: PersonalityPreference, change: Long = beginPersonalityChange()): Boolean {
        val saved = writeMutex.withLock {
            if (token != session || change != personalityGeneration) return@withLock false
            store.writePersonality(preference)
        }
        if (token == session && change == personalityGeneration) {
            onEvent(
                token,
                if (saved) SpeechVoicePreferenceEvent.PersonalitySaved
                else SpeechVoicePreferenceEvent.PersonalitySaveFailed,
            )
        }
        return saved
    }

    suspend fun clearPersonality(token: Long, change: Long = beginPersonalityChange()): Boolean {
        val cleared = writeMutex.withLock {
            if (token != session || change != personalityGeneration) return@withLock false
            store.clearPersonality()
        }
        if (token == session && change == personalityGeneration) {
            onEvent(
                token,
                if (cleared) SpeechVoicePreferenceEvent.PersonalityCleared
                else SpeechVoicePreferenceEvent.PersonalityClearFailed,
            )
        }
        return cleared
    }
}
