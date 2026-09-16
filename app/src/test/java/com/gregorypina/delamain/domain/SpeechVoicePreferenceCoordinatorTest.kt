package com.gregorypina.delamain.domain

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SpeechVoicePreferenceCoordinatorTest {
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
