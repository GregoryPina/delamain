package com.gregorypina.delamain.domain

import com.gregorypina.delamain.domain.PersonalityPreference
import com.gregorypina.delamain.domain.PersonalityTone
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SpeechVoicePreferenceCoordinatorTest {
    @Test fun `late mute read cannot undo explicit choice`() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val store = object : SpeechVoicePreferenceStore {
            override suspend fun read() = SpeechVoicePreferenceReadResult.Empty
            override suspend fun write(preference: SpeechVoicePreference) = true
            override suspend fun clear() = true
            override suspend fun readMute(): Boolean { gate.await(); return false }
        }
        val coordinator = SpeechVoicePreferenceCoordinator(store) { _, event -> events += event }
        val token = coordinator.openSession()
        val read = async(start = CoroutineStart.UNDISPATCHED) { coordinator.restoreMute(token) }
        coordinator.beginMuteChange()
        gate.complete(Unit)
        read.await()
        assertTrue(events.isEmpty())
    }

    @Test fun `late personality read cannot undo explicit choice`() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val store = object : SpeechVoicePreferenceStore {
            override suspend fun read() = SpeechVoicePreferenceReadResult.Empty
            override suspend fun write(preference: SpeechVoicePreference) = true
            override suspend fun clear() = true
            override suspend fun readPersonality(): PersonalityPreferenceReadResult {
                gate.await()
                return PersonalityPreferenceReadResult.Empty
            }
        }
        val coordinator = SpeechVoicePreferenceCoordinator(store) { _, event -> events += event }
        val token = coordinator.openSession()
        val read = async(start = CoroutineStart.UNDISPATCHED) { coordinator.restorePersonality(token) }
        coordinator.beginPersonalityChange()
        gate.complete(Unit)
        read.await()
        assertTrue(events.isEmpty())
    }

    @Test fun `valid preference restores only in current session`() = runBlocking {
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val c = SpeechVoicePreferenceCoordinator(FakeStore(SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("e","v")))) { _, e -> events += e }
        val session = c.openSession(); c.restore(session, "e", setOf("v"))
        assertTrue(events.single() is SpeechVoicePreferenceEvent.Restore)
    }
    @Test fun `restore from closed session cannot reach reopened session`() = runBlocking {
        val gate = CompletableDeferred<Unit>(); val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val store = object : SpeechVoicePreferenceStore {
            override suspend fun read(): SpeechVoicePreferenceReadResult { gate.await(); return SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("e","old")) }
            override suspend fun write(preference: SpeechVoicePreference)=true; override suspend fun clear()=true
        }
        val c = SpeechVoicePreferenceCoordinator(store) { _, e -> events += e }
        val a=c.openSession(); val job=async(start=CoroutineStart.UNDISPATCHED){c.restore(a,"e",setOf("old"))}
        c.closeSession(a); c.openSession(); gate.complete(Unit); job.await(); assertTrue(events.isEmpty())
    }
    @Test fun `explicit save invalidates delayed restore`() = runBlocking {
        val gate=CompletableDeferred<Unit>(); val events=mutableListOf<SpeechVoicePreferenceEvent>()
        val store=object:SpeechVoicePreferenceStore{
            override suspend fun read():SpeechVoicePreferenceReadResult{gate.await();return SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("e","old"))}
            override suspend fun write(preference:SpeechVoicePreference)=true; override suspend fun clear()=true }
        val c=SpeechVoicePreferenceCoordinator(store){_,e->events+=e}; val s=c.openSession()
        val restore=async(start=CoroutineStart.UNDISPATCHED){c.restore(s,"e",setOf("old","new"))}
        val change=c.beginExplicitChange(); c.saveConfirmed(change,s,"e","new"); gate.complete(Unit); restore.await()
        assertEquals(listOf(SpeechVoicePreferenceEvent.Saved),events)
    }
    @Test fun `obsolete save arriving after newer intent never writes disk`() = runBlocking {
        val store=FakeStore(SpeechVoicePreferenceReadResult.Empty); val c=SpeechVoicePreferenceCoordinator(store); val s=c.openSession()
        val old=c.beginExplicitChange(); val newest=c.beginExplicitChange()
        c.saveConfirmed(newest,s,"e","new"); c.saveConfirmed(old,s,"e","old")
        assertEquals(listOf("new"),store.writes.map{it.voiceId})
    }
    @Test fun `obsolete clear cannot erase newer save`() = runBlocking {
        val store=FakeStore(SpeechVoicePreferenceReadResult.Empty); val c=SpeechVoicePreferenceCoordinator(store); val s=c.openSession()
        val oldClear=c.beginExplicitChange(); val save=c.beginExplicitChange(); c.saveConfirmed(save,s,"e","new"); c.clearPreference(oldClear,s)
        assertEquals(0,store.clears); assertEquals("new",store.writes.single().voiceId)
    }
    @Test fun `obsolete save cannot replace newer clear`() = runBlocking {
        val store=FakeStore(SpeechVoicePreferenceReadResult.Empty); val c=SpeechVoicePreferenceCoordinator(store); val s=c.openSession()
        val oldSave=c.beginExplicitChange(); val clear=c.beginExplicitChange(); c.clearPreference(clear,s); c.saveConfirmed(oldSave,s,"e","old")
        assertEquals(1,store.clears); assertTrue(store.writes.isEmpty())
    }
    @Test fun `mute restore and persist respect session token`() = runBlocking {
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val store = object : SpeechVoicePreferenceStore {
            var muted = false
            override suspend fun read(): SpeechVoicePreferenceReadResult = SpeechVoicePreferenceReadResult.Empty
            override suspend fun write(preference: SpeechVoicePreference) = true
            override suspend fun clear() = true
            override suspend fun readMute(): Boolean = muted
            override suspend fun writeMute(muted: Boolean): Boolean {
                this.muted = muted
                return true
            }
        }
        val c = SpeechVoicePreferenceCoordinator(store) { _, e -> events += e }
        val session = c.openSession()
        c.restoreMute(session)
        assertEquals(SpeechVoicePreferenceEvent.MuteRestored(false), events.single())
        c.persistMute(session, true)
        assertTrue(store.muted)
        c.closeSession(session)
        c.persistMute(session, false)
        assertTrue(store.muted)
    }

    @Test fun `personality clear does not touch voice preference`() = runBlocking {
        val store = object : SpeechVoicePreferenceStore {
            var personality = PersonalityPreference("Ana", PersonalityTone.DIRECT)
            override suspend fun read(): SpeechVoicePreferenceReadResult =
                SpeechVoicePreferenceReadResult.Found(SpeechVoicePreference("e", "v"))
            override suspend fun write(preference: SpeechVoicePreference) = true
            override suspend fun clear() = true
            override suspend fun readPersonality(): PersonalityPreferenceReadResult =
                PersonalityPreferenceReadResult.Found(personality)
            override suspend fun writePersonality(preference: PersonalityPreference): Boolean {
                personality = preference
                return true
            }
            override suspend fun clearPersonality(): Boolean {
                personality = PersonalityPreference()
                return true
            }
        }
        val events = mutableListOf<SpeechVoicePreferenceEvent>()
        val c = SpeechVoicePreferenceCoordinator(store) { _, e -> events += e }
        val session = c.openSession()
        c.clearPersonality(session)
        assertEquals(listOf(SpeechVoicePreferenceEvent.PersonalityCleared), events)
        assertEquals(PersonalityPreference(), store.personality)
    }

    @Test fun `read and write failures remain distinct`() = runBlocking {
        val events=mutableListOf<SpeechVoicePreferenceEvent>(); val c=SpeechVoicePreferenceCoordinator(FakeStore(SpeechVoicePreferenceReadResult.Failed,false)){_,e->events+=e}; val s=c.openSession()
        c.restore(s,"e",setOf("v")); val ch=c.beginExplicitChange(); c.saveConfirmed(ch,s,"e","v")
        assertEquals(listOf(SpeechVoicePreferenceEvent.ReadFailed,SpeechVoicePreferenceEvent.SaveFailed),events)
    }
    private class FakeStore(private val result:SpeechVoicePreferenceReadResult,private val writeResult:Boolean=true):SpeechVoicePreferenceStore{
        val writes=mutableListOf<SpeechVoicePreference>(); var clears=0
        override suspend fun read()=result
        override suspend fun write(preference:SpeechVoicePreference):Boolean{writes+=preference;return writeResult}
        override suspend fun clear():Boolean{clears++;return true}
    }
}
