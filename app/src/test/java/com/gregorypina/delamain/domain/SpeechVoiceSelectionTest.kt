package com.gregorypina.delamain.domain

import org.junit.Assert.*
import org.junit.Test

class SpeechVoiceSelectionTest {
    private fun voice(id: String, network: Boolean = false, installed: Boolean = true,
        language: String = "pt", country: String = "BR", quality: Int = 300) =
        SpeechVoiceCandidate(id, language, country, network, installed, quality)

    @Test fun `network missing data and other locales are never offered`() {
        val candidates = listOf(voice("local"), voice("network", network = true),
            voice("missing", installed = false), voice("pt-PT", country = "PT"),
            voice("english", language = "en", country = "US"))
        assertEquals(listOf("local"), localBrazilianVoices(candidates, "network"))
    }
    @Test fun `eligible current voice wins over quality ranking`() {
        assertEquals(listOf("current", "high"), localBrazilianVoices(
            listOf(voice("high", quality = 500), voice("current", quality = 100)), "current"))
    }
    @Test fun `fallback order is deterministic and removes duplicates`() {
        val a = voice("a")
        assertEquals(listOf("a", "b"), localBrazilianVoices(listOf(voice("b"), a, a), null))
    }
    @Test fun `empty catalog cannot choose a voice`() {
        assertNull(SpeechVoiceSelection().nextId())
        assertTrue(localBrazilianVoices(listOf(voice("net", network = true)), null).isEmpty())
    }
    @Test fun `cycling wraps and recovers from failed selection`() {
        assertEquals("b", SpeechVoiceSelection(listOf("a", "b"), "a").nextId())
        assertEquals("a", SpeechVoiceSelection(listOf("a", "b"), "b").nextId())
        assertEquals("a", SpeechVoiceSelection(listOf("a", "b"), null).nextId())
        assertEquals("a", SpeechVoiceSelection(listOf("a"), "a").nextId())
    }
}
