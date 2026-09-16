package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechOutputWatchdogTest {
    private class FakeScheduler : SpeechOutputWatchdogScheduler {
        val tasks = mutableListOf<Pair<Long, () -> Unit>>()
        private val cancelled = mutableSetOf<Int>()

        override fun schedule(delayMs: Long, action: () -> Unit): () -> Unit {
            val index = tasks.size
            tasks += delayMs to action
            return { cancelled += index }
        }

        fun fire(delayMs: Long) {
            tasks.forEachIndexed { index, (delay, action) ->
                if (delay == delayMs && index !in cancelled) action()
            }
        }
    }

    @Test
    fun `start timeout fires only for active token`() {
        val scheduler = FakeScheduler()
        val watchdog = SpeechOutputWatchdog(scheduler, startTimeoutMs = 10_000L)
        var fired = 0
        val token = SpeechOutputWatchdog.Token("a", 1L)
        watchdog.armQueued(token) { fired++ }
        watchdog.disarm()
        scheduler.fire(10_000L)
        assertEquals(0, fired)
        watchdog.armQueued(token) { fired++ }
        scheduler.fire(10_000L)
        assertEquals(1, fired)
    }

    @Test
    fun `completion timeout uses bounded formula`() {
        assertEquals(5_000L, SpeechOutputWatchdog.completionLimitMs(1))
        assertEquals(5_000L, SpeechOutputWatchdog.completionLimitMs(10))
        assertEquals(10_000L, SpeechOutputWatchdog.completionLimitMs(100))
        assertEquals(60_000L, SpeechOutputWatchdog.completionLimitMs(1_000))
    }

    @Test
    fun `speaking watchdog is cancelled by disarm`() {
        val scheduler = FakeScheduler()
        val watchdog = SpeechOutputWatchdog(scheduler)
        var fired = false
        watchdog.armSpeaking(SpeechOutputWatchdog.Token("a", 1L), 20) { fired = true }
        watchdog.disarm()
        scheduler.fire(5_000L)
        assertTrue(!fired)
    }
}
