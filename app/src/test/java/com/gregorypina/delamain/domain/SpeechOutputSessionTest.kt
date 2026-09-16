package com.gregorypina.delamain.domain

import org.junit.Assert.*
import org.junit.Test

class SpeechOutputSessionTest {
    private class FakeEngine : SpeechOutputEngine {
        val requests = mutableListOf<Pair<String, String>>()
        var accepts = true
        var stops = true
        var throwOnStop = false
        var throwOnEnqueue = false
        var shutdownCalls = 0
        var stopCalls = 0
        override fun enqueue(text: String, utteranceId: String): Boolean {
            if (throwOnEnqueue) throw IllegalStateException("engine disconnected")
            requests += text to utteranceId
            return accepts
        }
        override fun stop(): Boolean {
            stopCalls++
            if (throwOnStop) throw IllegalStateException("engine disconnected")
            return stops
        }
        override fun shutdown() { shutdownCalls++ }
    }

    @Test fun `queue acceptance is not speech start or completion`() {
        val engine = FakeEngine()
        val states = mutableListOf<SpeechOutputState>()
        val session = SpeechOutputSession(engine, onState = { state, _ -> states += state })
        session.initialized(true)
        assertEquals(SpeechOutputResult.Queued, session.speak("Olá"))
        assertEquals(SpeechOutputState.Queued, session.state)
        val id = engine.requests.single().second
        session.started(id)
        session.completed(id)
        assertEquals(listOf(SpeechOutputState.Ready, SpeechOutputState.Queued,
            SpeechOutputState.Speaking, SpeechOutputState.Completed), states)
        session.failed(id)
        session.started(id)
        assertEquals(SpeechOutputState.Completed, session.state)
    }

    @Test fun `unavailable and preparing never enqueue or replay text on ready`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        assertEquals(SpeechOutputResult.Unavailable, session.speak("antes"))
        session.initialized(false)
        assertEquals(SpeechOutputResult.Unavailable, session.speak("depois"))
        session.initialized(true)
        assertEquals(SpeechOutputState.Unavailable, session.state)
        assertTrue(engine.requests.isEmpty())
    }

    @Test fun `replacement ignores old start done error and stop`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        session.initialized(true)
        session.speak("primeira")
        val old = engine.requests.last().second
        session.speak("segunda")
        val current = engine.requests.last().second
        assertNotEquals(old, current)
        session.started(old)
        session.completed(old)
        session.failed(old)
        session.stopped(old)
        session.completed(null)
        assertEquals(SpeechOutputState.Queued, session.state)
        session.started(current)
        assertEquals(SpeechOutputState.Speaking, session.state)
    }

    @Test fun `stop invalidates callbacks and a new request still works`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        session.initialized(true)
        session.speak("primeira")
        val id = engine.requests.last().second
        assertTrue(session.stop())
        session.started(id)
        session.completed(id)
        session.failed(id)
        assertEquals(SpeechOutputState.Stopped, session.state)
        assertEquals(SpeechOutputResult.Queued, session.speak("segunda"))
    }

    @Test fun `shutdown before initialization cannot resurrect or notify UI`() {
        val engine = FakeEngine()
        val states = mutableListOf<SpeechOutputState>()
        val session = SpeechOutputSession(engine, onState = { state, _ -> states += state })
        session.shutdown()
        session.initialized(true)
        session.shutdown()
        assertTrue(session.stop())
        assertEquals(SpeechOutputResult.Unavailable, session.speak("tardia"))
        assertEquals(SpeechOutputState.Closed, session.state)
        assertEquals(1, engine.shutdownCalls)
        assertTrue(states.isEmpty())
        assertTrue(engine.requests.isEmpty())
    }

    @Test fun `shutdown during speech drops late callbacks even if stop throws`() {
        val engine = FakeEngine()
        val states = mutableListOf<SpeechOutputState>()
        val session = SpeechOutputSession(engine, onState = { state, _ -> states += state })
        session.initialized(true)
        session.speak("olá")
        val id = engine.requests.last().second
        states.clear()
        engine.throwOnStop = true
        session.shutdown()
        session.started(id)
        session.completed(id)
        session.failed(id)
        session.stopped(id)
        assertTrue(states.isEmpty())
        assertEquals(1, engine.shutdownCalls)
        assertEquals(SpeechOutputState.Closed, session.state)
    }

    @Test fun `enqueue rejection and exception report failure and ignore callbacks`() {
        for (throws in listOf(false, true)) {
            val engine = FakeEngine().apply { accepts = false; throwOnEnqueue = throws }
            val session = SpeechOutputSession(engine)
            session.initialized(true)
            assertEquals(SpeechOutputResult.Failed, session.speak("olá"))
            engine.requests.lastOrNull()?.second?.let { session.completed(it) }
            assertEquals(SpeechOutputState.Failed, session.state)
        }
    }

    @Test fun `async failure permits explicit retry but no automatic retry`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        session.initialized(true)
        session.speak("olá")
        session.failed(engine.requests.last().second)
        assertEquals(SpeechOutputState.Failed, session.state)
        assertEquals(1, engine.requests.size)
        assertEquals(SpeechOutputResult.Queued, session.speak("nova tentativa"))
    }

    @Test fun `engine stop event clears active utterance`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        session.initialized(true)
        session.speak("olá")
        val id = engine.requests.last().second
        session.stopped(id)
        session.completed(id)
        assertEquals(SpeechOutputState.Stopped, session.state)
    }

    @Test fun `failed stop is not success and blocks replacement enqueue`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        session.initialized(true)
        session.speak("primeira")
        engine.stops = false
        assertFalse(session.stop())
        assertEquals(SpeechOutputState.Failed, session.state)
        assertEquals(SpeechOutputResult.Failed, session.speak("segunda"))
        assertEquals(1, engine.requests.size)
    }

    @Test fun `blank input cancels preceding speech without enqueue`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        session.initialized(true)
        session.speak("olá")
        val id = engine.requests.last().second
        assertEquals(SpeechOutputResult.Failed, session.speak("  "))
        session.completed(id)
        assertEquals(SpeechOutputState.Failed, session.state)
        assertEquals(1, engine.requests.size)
        assertEquals(2, engine.stopCalls)
    }

    @Test fun `closing while preparing does not report ready on stop`() {
        val session = SpeechOutputSession(FakeEngine())
        assertTrue(session.stop())
        assertEquals(SpeechOutputState.Preparing, session.state)
        session.shutdown()
        session.initialized(true)
        assertEquals(SpeechOutputState.Closed, session.state)
    }
    @Test fun `failed voice selection disables speech until explicit successful selection`() {
        val engine = FakeEngine()
        val session = SpeechOutputSession(engine)
        session.initialized(true)
        session.speak("antiga")
        val id = engine.requests.last().second
        session.stop()
        session.voiceAvailabilityChanged(false)
        session.completed(id)
        assertEquals(SpeechOutputResult.Unavailable, session.speak("bloqueada"))
        assertEquals(1, engine.requests.size)
        session.voiceAvailabilityChanged(true)
        assertEquals(SpeechOutputResult.Queued, session.speak("nova"))
    }

    @Test fun `voice selection cannot resurrect disposed session`() {
        val states = mutableListOf<SpeechOutputState>()
        val session = SpeechOutputSession(FakeEngine(), onState = { state, _ -> states += state })
        session.shutdown()
        session.voiceAvailabilityChanged(true)
        assertTrue(states.isEmpty())
        assertEquals(SpeechOutputState.Closed, session.state)
        assertEquals(SpeechOutputResult.Unavailable, session.speak("tardia"))
    }

    @Test fun `callbacks keep the interaction id from speak`() {
        val engine = FakeEngine()
        val events = mutableListOf<Pair<SpeechOutputState, Long>>()
        val session = SpeechOutputSession(engine, onState = { state, id -> events += state to id })
        session.initialized(true)
        session.speak("olá", 7L)
        val utteranceId = engine.requests.last().second
        session.started(utteranceId)
        session.completed(utteranceId)
        assertEquals(
            listOf(
                SpeechOutputState.Ready to 0L,
                SpeechOutputState.Queued to 7L,
                SpeechOutputState.Speaking to 7L,
                SpeechOutputState.Completed to 7L,
            ),
            events,
        )
    }

    @Test fun `queued watchdog reports failure without success`() {
        val scheduler = object : SpeechOutputWatchdogScheduler {
            val tasks = mutableListOf<Pair<Long, () -> Unit>>()
            override fun schedule(delayMs: Long, action: () -> Unit): () -> Unit {
                tasks += delayMs to action
                return {}
            }
        }
        val events = mutableListOf<SpeechOutputState>()
        val session = SpeechOutputSession(
            FakeEngine(),
            onState = { state, _ -> events += state },
            watchdog = SpeechOutputWatchdog(scheduler),
        )
        session.initialized(true)
        session.speak("olá", 3L)
        scheduler.tasks.single { it.first == SpeechOutputWatchdog.DEFAULT_START_TIMEOUT_MS }.second.invoke()
        assertEquals(SpeechOutputState.Failed, session.state)
        assertFalse(events.contains(SpeechOutputState.Completed))
    }
}
