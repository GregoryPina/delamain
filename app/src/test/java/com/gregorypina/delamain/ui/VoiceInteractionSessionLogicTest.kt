package com.gregorypina.delamain.ui

import com.gregorypina.delamain.domain.LocalCommandEngine
import com.gregorypina.delamain.domain.LocalCommandResult
import com.gregorypina.delamain.domain.LocalIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure command parsing checks used by the shared session (no Android ports). */
class VoiceInteractionSessionLogicTest {
    @Test
    fun `recognized command exposes user text without intent prefix`() {
        val engine = LocalCommandEngine()
        val result = engine.process("que horas são")
        assertTrue(result is LocalCommandResult.Recognized)
        val recognized = result as LocalCommandResult.Recognized
        assertEquals(LocalIntent.TIME, recognized.intent)
        assertTrue(recognized.response.isNotBlank())
        assertTrue(!recognized.response.startsWith("${recognized.intent}:"))
    }
}
