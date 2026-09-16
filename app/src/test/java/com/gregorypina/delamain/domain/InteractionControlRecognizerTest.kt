package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InteractionControlRecognizerTest {
    @Test
    fun `stop speech phrases are exact matches`() {
        assertEquals(InteractionControlIntent.STOP_SPEECH, InteractionControlRecognizer.recognize("pare de falar"))
        assertEquals(InteractionControlIntent.STOP_SPEECH, InteractionControlRecognizer.recognize("Vexa, parar de falar"))
    }

    @Test
    fun `cancel phrases are exact matches`() {
        assertEquals(InteractionControlIntent.CANCEL, InteractionControlRecognizer.recognize("cancelar"))
        assertEquals(InteractionControlIntent.CANCEL, InteractionControlRecognizer.recognize("cancela"))
    }

    @Test
    fun `mute toggle phrases are exact matches`() {
        assertEquals(InteractionControlIntent.ENABLE_MUTE, InteractionControlRecognizer.recognize("modo mute"))
        assertEquals(InteractionControlIntent.ENABLE_MUTE, InteractionControlRecognizer.recognize("silenciar voz"))
        assertEquals(InteractionControlIntent.DISABLE_MUTE, InteractionControlRecognizer.recognize("ativar voz"))
        assertEquals(InteractionControlIntent.DISABLE_MUTE, InteractionControlRecognizer.recognize("sair do mute"))
    }

    @Test
    fun `negations and compound phrases do not match by substring`() {
        assertNull(InteractionControlRecognizer.recognize("não pare de falar"))
        assertNull(InteractionControlRecognizer.recognize("explique a expressão pare de falar"))
        assertNull(InteractionControlRecognizer.recognize("cancele e abra spotify"))
        assertNull(InteractionControlRecognizer.recognize("modo silencioso"))
    }
}
