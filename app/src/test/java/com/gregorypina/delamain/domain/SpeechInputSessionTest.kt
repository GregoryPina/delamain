package com.gregorypina.delamain.domain

import org.junit.Assert.*
import org.junit.Test

class SpeechInputSessionTest {
    private class FakeEngine : SpeechInputEngine {
        var supported = true
        var permission = true
        var throws = false
        val requests = mutableListOf<Long>()
        var releases = 0
        override fun available() = supported
        override fun hasPermission() = permission
        override fun start(requestId: Long) {
            if (throws) throw IllegalStateException("busy")
            requests += requestId
        }
        override fun release() { releases++ }
    }
    private class Fixture {
        val engine = FakeEngine()
        var stopAccepted = true
        var stops = 0
        val text = mutableListOf<String>()
        val states = mutableListOf<SpeechInputState>()
        val session = SpeechInputSession(engine, { stops++; stopAccepted },
            { states += it }, { text += it })
    }

    @Test fun `final text is delivered once after releasing capture`() {
        val f = Fixture()
        assertEquals(SpeechInputStartResult.Started, f.session.start())
        val id = f.engine.requests.single()
        f.session.listening(id)
        f.session.processing(id)
        f.session.result(id, "  Vexa  ")
        f.session.result(id, "aumente o volume")
        f.session.error(id)
        assertEquals(listOf("Vexa"), f.text)
        assertEquals(1, f.engine.releases)
        assertEquals(1, f.stops)
        assertEquals(SpeechInputState.Completed, f.session.state)
    }

    @Test fun `no support does not request permission or stop speech`() {
        val f = Fixture()
        f.engine.supported = false
        f.engine.permission = false
        assertEquals(SpeechInputStartResult.Unavailable, f.session.start())
        assertEquals(SpeechInputState.Unavailable, f.session.state)
        assertEquals(0, f.stops)
        assertTrue(f.engine.requests.isEmpty())
    }

    @Test fun `permission grant alone never starts recording`() {
        val f = Fixture()
        f.engine.permission = false
        assertEquals(SpeechInputStartResult.PermissionRequired, f.session.start())
        f.engine.permission = true
        assertTrue(f.engine.requests.isEmpty())
        assertEquals(0, f.stops)
        assertEquals(SpeechInputStartResult.Started, f.session.start())
    }

    @Test fun `failed TTS stop prevents capture`() {
        val f = Fixture()
        f.stopAccepted = false
        assertEquals(SpeechInputStartResult.Failed, f.session.start())
        assertTrue(f.engine.requests.isEmpty())
    }

    @Test fun `busy does not restart session or repeat TTS stop`() {
        val f = Fixture()
        f.session.start()
        assertEquals(SpeechInputStartResult.Busy, f.session.start())
        assertEquals(1, f.engine.requests.size)
        assertEquals(1, f.stops)
    }

    @Test fun `canceled session cannot affect its replacement`() {
        val f = Fixture()
        f.session.start()
        val old = f.engine.requests.last()
        f.session.cancel()
        f.session.start()
        val current = f.engine.requests.last()
        f.session.listening(old)
        f.session.processing(old)
        f.session.result(old, "aumente o volume")
        f.session.error(old, SpeechInputState.TimedOut)
        assertEquals(SpeechInputState.Starting, f.session.state)
        assertTrue(f.text.isEmpty())
        f.session.result(current, "bateria")
        assertEquals(listOf("bateria"), f.text)
    }

    @Test fun `timeout and errors release without actions or auto retry`() {
        for (reason in listOf(SpeechInputState.TimedOut, SpeechInputState.Failed,
            SpeechInputState.PermissionRequired, SpeechInputState.Unavailable)) {
            val f = Fixture()
            f.session.start()
            val id = f.engine.requests.single()
            f.session.error(id, reason)
            f.session.result(id, "aumente o volume")
            assertTrue(f.text.isEmpty())
            assertEquals(1, f.engine.requests.size)
            assertEquals(1, f.engine.releases)
            assertEquals(reason, f.session.state)
        }
    }

    @Test fun `empty recognition is not a command`() {
        for (result in listOf(null, "", "  ")) {
            val f = Fixture()
            f.session.start()
            f.session.result(f.engine.requests.single(), result)
            assertTrue(f.text.isEmpty())
            assertEquals(SpeechInputState.NoMatch, f.session.state)
        }
    }

    @Test fun `shutdown invalidates callbacks and is idempotent`() {
        val f = Fixture()
        f.session.start()
        val id = f.engine.requests.single()
        f.states.clear()
        f.session.shutdown()
        f.session.shutdown()
        f.session.result(id, "aumente o volume")
        f.session.error(id)
        f.session.listening(id)
        assertEquals(SpeechInputStartResult.Unavailable, f.session.start())
        assertTrue(f.text.isEmpty())
        assertTrue(f.states.isEmpty())
        assertEquals(1, f.engine.releases)
    }

    @Test fun `failed native start is released and may be retried explicitly`() {
        val f = Fixture()
        f.engine.throws = true
        assertEquals(SpeechInputStartResult.Failed, f.session.start())
        assertEquals(1, f.engine.releases)
        f.engine.throws = false
        assertEquals(SpeechInputStartResult.Started, f.session.start())
        assertEquals(1, f.engine.requests.size)
    }
}
