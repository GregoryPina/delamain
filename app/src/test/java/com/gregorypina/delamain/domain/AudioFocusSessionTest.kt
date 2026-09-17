package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFocusSessionTest {
    @Test
    fun `old focus loss cannot stop a newer acquisition`() {
        val callbacks = mutableListOf<() -> Unit>()
        var held = false
        val port = object : AudioFocusPort {
            override fun request(mode: AudioFocusMode, onLoss: () -> Unit): AudioFocusRequestResult {
                callbacks += onLoss
                held = true
                return AudioFocusRequestResult.GRANTED
            }
            override fun abandon() { held = false }
            override fun isHeld() = held
        }
        var losses = 0
        val session = AudioFocusSession(port) { losses++ }
        session.requestForSpeech()
        session.requestForListening()
        callbacks.first().invoke()
        assertEquals(0, losses)
        assertTrue(session.isHeld())
        callbacks.last().invoke()
        assertEquals(1, losses)
        assertFalse(session.isHeld())
    }

    @Test
    fun `granted request holds focus until abandon`() {
        val port = FakeAudioFocusPort()
        val session = AudioFocusSession(port) {}
        assertEquals(AudioFocusRequestResult.GRANTED, session.requestForSpeech())
        assertTrue(session.isHeld())
        session.abandon()
        assertFalse(session.isHeld())
        assertEquals(1, port.abandonCount)
    }

    @Test
    fun `denied request does not hold focus`() {
        val port = FakeAudioFocusPort(grantResult = AudioFocusRequestResult.DENIED)
        val session = AudioFocusSession(port) {}
        assertEquals(AudioFocusRequestResult.DENIED, session.requestForListening())
        assertFalse(session.isHeld())
        assertEquals(0, port.abandonCount)
    }

    @Test
    fun `focus loss stops active interaction once`() {
        val port = FakeAudioFocusPort()
        var losses = 0
        val session = AudioFocusSession(port) { losses += 1 }
        session.requestForSpeech()
        port.simulateLoss()
        assertEquals(1, losses)
        assertFalse(session.isHeld())
        port.simulateLoss()
        assertEquals(1, losses)
    }

    @Test
    fun `late loss after abandon is ignored`() {
        val port = FakeAudioFocusPort()
        var losses = 0
        val session = AudioFocusSession(port) { losses += 1 }
        session.requestForSpeech()
        session.abandon()
        port.simulateLoss()
        assertEquals(0, losses)
    }

    @Test
    fun `each acquisition is released once`() {
        val port = FakeAudioFocusPort()
        val session = AudioFocusSession(port) {}
        session.requestForSpeech()
        session.abandon()
        session.requestForListening()
        session.abandon()
        assertEquals(2, port.abandonCount)
    }

    private class FakeAudioFocusPort(
        private val grantResult: AudioFocusRequestResult = AudioFocusRequestResult.GRANTED,
    ) : AudioFocusPort {
        var abandonCount = 0
        private var held = false
        private var lossListener: (() -> Unit)? = null

        override fun request(mode: AudioFocusMode, onLoss: () -> Unit): AudioFocusRequestResult {
            lossListener = onLoss
            held = grantResult == AudioFocusRequestResult.GRANTED
            return grantResult
        }

        override fun abandon() {
            if (held) abandonCount += 1
            held = false
            lossListener = null
        }

        override fun isHeld(): Boolean = held

        fun simulateLoss() {
            lossListener?.invoke()
        }
    }
}
