package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalUnknownResponsesTest {
    @Test
    fun `rotates unknown responses without immediate repetition`() {
        val first = LocalUnknownResponses.next()
        val second = LocalUnknownResponses.next()
        val third = LocalUnknownResponses.next()
        val fourth = LocalUnknownResponses.next()

        assertNotEquals(first, second)
        assertNotEquals(second, third)
        assertEquals(first, fourth)
        assertTrue(
            setOf(first, second, third).containsAll(
                listOf(
                    "Comando local não reconhecido.",
                    "Não entendi esse comando local.",
                    "Esse pedido não está no meu catálogo local.",
                ),
            ),
        )
    }
}
