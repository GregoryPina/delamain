package com.gregorypina.delamain.domain

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SpeechVoicePreferenceCoordinatorTest {
    @Test fun `restores only matching engine and eligible stable id`() = runBlocking {
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val store = FakeStore(SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("engine.a", "voice.b")))
        val coordinator = SpeechVoicePreferenceCoordinator(store, events::add)
        coordinator.restore("engine.a", setOf("voice.a", "voice.b"))
        assertEquals(SpeechVoicePreferenceEvent.Restore(SpeechVoicePreference("engine.a", "voice.b")), events.single())
    }

    @Test fun `same voice id from another engine is unavailable`() = runBlocking {
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val coordinator = SpeechVoicePreferenceCoordinator(
            FakeStore(SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("old.engine", "same"))), events::add)
        coordinator.restore("new.engine", setOf("same"))
        assertEquals(listOf(SpeechVoicePreferenceEvent.Unavailable), events)
    }

    @Test fun `removed or now ineligible voice is unavailable without overwriting store`() = runBlocking {
        val store = FakeStore(SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("engine", "network-now")))
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        SpeechVoicePreferenceCoordinator(store, events::add).restore("engine", setOf("local"))
        assertEquals(listOf(SpeechVoicePreferenceEvent.Unavailable), events)
        assertTrue(store.writes.isEmpty())
    }

    @Test fun `read and write failures are distinct`() = runBlocking {
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val store = FakeStore(SpeechVoicePreferenceReadResult.Failed, writeResult = false)
        val coordinator = SpeechVoicePreferenceCoordinator(store, events::add)
        coordinator.restore("engine", setOf("voice"))
        val change = coordinator.beginExplicitChange()
        coordinator.saveConfirmed(change, "engine", "voice")
        assertEquals(listOf(SpeechVoicePreferenceEvent.ReadFailed, SpeechVoicePreferenceEvent.SaveFailed), events)
    }

    @Test fun `explicit selection wins over delayed startup read`() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val store = object : SpeechVoicePreferenceStore {
            override suspend fun read(): SpeechVoicePreferenceReadResult {
                gate.await()
                return SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("engine", "old"))
            }
            override suspend fun write(preference: SpeechVoicePreference) = true
            override suspend fun clear() = true
        }
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val coordinator = SpeechVoicePreferenceCoordinator(store, events::add)
        val restore = async(start = CoroutineStart.UNDISPATCHED) { coordinator.restore("engine", setOf("old", "new")) }
        val change = coordinator.beginExplicitChange()
        coordinator.saveConfirmed(change, "engine", "new")
        gate.complete(Unit)
        restore.await()
        assertEquals(listOf(SpeechVoicePreferenceEvent.Saved), events)
    }

    @Test fun `serialized writes keep latest explicit preference last`() = runBlocking {
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val writes = mutableListOf<String>()
        val store = object : SpeechVoicePreferenceStore {
            override suspend fun read() = SpeechVoicePreferenceReadResult.Empty
            override suspend fun write(preference: SpeechVoicePreference): Boolean {
                if (preference.voiceId == "one") { firstEntered.complete(Unit); releaseFirst.await() }
                writes += preference.voiceId
                return true
            }
            override suspend fun clear() = true
        }
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val coordinator = SpeechVoicePreferenceCoordinator(store, events::add)
        val first = coordinator.beginExplicitChange()
        val writeOne = async(start = CoroutineStart.UNDISPATCHED) { coordinator.saveConfirmed(first, "engine", "one") }
        firstEntered.await()
        val second = coordinator.beginExplicitChange()
        val writeTwo = async { coordinator.saveConfirmed(second, "engine", "two") }
        releaseFirst.complete(Unit)
        writeOne.await(); writeTwo.await()
        assertEquals(listOf("one", "two"), writes)
        assertEquals(listOf(SpeechVoicePreferenceEvent.Saved), events)
    }

    @Test fun `use default clears voice preference through dedicated operation`() = runBlocking {
        val store = FakeStore(SpeechVoicePreferenceReadResult.Empty)
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val coordinator = SpeechVoicePreferenceCoordinator(store, events::add)
        val change = coordinator.beginExplicitChange()
        coordinator.clearPreference(change)
        assertEquals(1, store.clears)
        assertEquals(listOf(SpeechVoicePreferenceEvent.Cleared), events)
    }

    @Test fun `unsupported preference version is unavailable`() = runBlocking {
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val old = SpeechVoicePreference("engine", "voice", version = 99)
        SpeechVoicePreferenceCoordinator(FakeStore(SpeechVoicePreferenceReadResult.Found(old)), events::add)
            .restore("engine", setOf("voice"))
        assertEquals(listOf(SpeechVoicePreferenceEvent.Unavailable), events)
    }

    private class FakeStore(
        private val readResult: SpeechVoicePreferenceReadResult,
        private val writeResult: Boolean = true,
    ) : SpeechVoicePreferenceStore {
        val writes = mutableListOf<SpeechVoicePreference>()
        var clears = 0
        override suspend fun read() = readResult
        override suspend fun write(preference: SpeechVoicePreference): Boolean { writes += preference; return writeResult }
        override suspend fun clear(): Boolean { clears += 1; return true }
    }
}
