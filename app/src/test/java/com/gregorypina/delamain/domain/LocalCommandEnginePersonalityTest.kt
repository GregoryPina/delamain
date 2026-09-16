package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalCommandEnginePersonalityTest {
    @Test
    fun `direct tone changes style but keeps dispatched semantics`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port, tone = PersonalityTone.DIRECT)
        val result = engine.process("próxima música") as LocalCommandResult.Recognized
        assertTrue(result.response.contains("solicit", ignoreCase = true))
        assertEquals(1, port.calls.size)
    }

    @Test
    fun `direct tone shortens presence with configured name`() {
        val result = LocalCommandEngine(
            configuredName = "Ana",
            tone = PersonalityTone.DIRECT,
        ).process("Está aí?") as LocalCommandResult.Recognized
        assertEquals("Aqui, Ana.", result.response)
    }

    @Test
    fun `apply personality keeps variant rotation`() {
        val engine = LocalCommandEngine()
        val first = (engine.process("Vexa") as LocalCommandResult.Recognized).response
        val second = (engine.process("Vexa") as LocalCommandResult.Recognized).response
        engine.applyPersonality(PersonalityConfig(displayName = "Ana", tone = PersonalityTone.DIRECT))
        val third = (engine.process("Vexa") as LocalCommandResult.Recognized).response
        assertNotEquals(first, second)
        assertNotEquals(first, third)
    }

    private class RecordingActionPort : LocalActionPort {
        val calls = mutableListOf<LocalAction>()
        override fun execute(action: LocalAction): LocalActionResult {
            calls += action
            return LocalActionResult.Dispatched(action)
        }
    }
}
