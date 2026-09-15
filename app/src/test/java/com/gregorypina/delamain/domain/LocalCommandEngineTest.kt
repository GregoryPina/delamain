package com.gregorypina.delamain.domain

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalCommandEngineTest {
    @Test
    fun `recognizes bare assistant name as call`() {
        val result = LocalCommandEngine().process("Vexa!")

        assertRecognized(result, LocalIntent.CALL)
    }

    @Test
    fun `normalizes accents case spacing and punctuation`() {
        val result = LocalCommandEngine().process("  VEXA,   Você ESTÁ aí?!  ")

        assertRecognized(result, LocalIntent.PRESENCE)
    }

    @Test
    fun `accepts optional assistant prefix for supported phrases`() {
        val engine = LocalCommandEngine()

        assertRecognized(engine.process("Vexa, bom dia."), LocalIntent.GREETING)
        assertRecognized(engine.process("Vexa: muito obrigado!"), LocalIntent.THANKS)
        assertRecognized(engine.process("Vexa, que horas são?"), LocalIntent.TIME)
    }

    @Test
    fun `recognizes supported phrases without prefix`() {
        val engine = LocalCommandEngine()

        assertRecognized(engine.process("Está aí?"), LocalIntent.PRESENCE)
        assertRecognized(engine.process("Boa noite"), LocalIntent.GREETING)
        assertRecognized(engine.process("Obrigada"), LocalIntent.THANKS)
        assertRecognized(engine.process("Qual é a hora?"), LocalIntent.TIME)
    }

    @Test
    fun `recognizes explicit informal presence aliases`() {
        val engine = LocalCommandEngine()

        listOf(
            "Tá aí?",
            "Você tá aí?",
            "vc tá aí",
            "vc está aí",
            "cê tá aí",
        ).forEach { phrase ->
            assertRecognized(engine.process(phrase), LocalIntent.PRESENCE)
        }
    }

    @Test
    fun `returns explicit unknown for blank unrelated and partial text`() {
        val engine = LocalCommandEngine()

        assertSame(LocalCommandResult.Unknown, engine.process(""))
        assertSame(LocalCommandResult.Unknown, engine.process("Como está o trânsito?"))
        assertSame(LocalCommandResult.Unknown, engine.process("horas"))
        assertSame(LocalCommandResult.Unknown, engine.process("Vexa, abra o mapa"))
    }

    @Test
    fun `rejects old assistant name as call or prefix`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port)

        assertSame(LocalCommandResult.Unknown, engine.process("Delamain"))
        assertSame(LocalCommandResult.Unknown, engine.process("Delamain, bom dia"))
        assertSame(LocalCommandResult.Unknown, engine.process("Delamain, aumente o volume"))
        assertTrue(port.actions.isEmpty())
    }

    @Test
    fun `does not match negative phrases`() {
        val engine = LocalCommandEngine()

        assertSame(LocalCommandResult.Unknown, engine.process("Não, obrigado"))
        assertSame(LocalCommandResult.Unknown, engine.process("Não quero saber que horas são"))
        assertSame(LocalCommandResult.Unknown, engine.process("Vexa, não está aí?"))
    }

    @Test
    fun `does not match compound requests`() {
        val engine = LocalCommandEngine()

        assertSame(LocalCommandResult.Unknown, engine.process("Bom dia e que horas são?"))
        assertSame(LocalCommandResult.Unknown, engine.process("Vexa, obrigado e bom dia"))
        assertSame(LocalCommandResult.Unknown, engine.process("Está aí ou não?"))
    }

    @Test
    fun `uses the injected clock timezone for time response`() {
        val clock = Clock.fixed(
            Instant.parse("2026-09-15T12:34:56Z"),
            ZoneId.of("America/Sao_Paulo"),
        )

        val result = LocalCommandEngine(clock).process("Que horas são?")

        assertEquals(
            LocalCommandResult.Recognized(LocalIntent.TIME, "São 09:34."),
            result,
        )
    }

    @Test
    fun `avoids immediate repetition while alternatives exist`() {
        val engine = LocalCommandEngine()

        val first = assertRecognized(engine.process("Vexa"), LocalIntent.CALL).response
        val second = assertRecognized(engine.process("Vexa"), LocalIntent.CALL).response
        val third = assertRecognized(engine.process("Vexa"), LocalIntent.CALL).response

        assertNotEquals(first, second)
        assertEquals(first, third)
    }

    @Test
    fun `uses configured name only when one is provided`() {
        val unnamed = assertRecognized(
            LocalCommandEngine().process("Está aí?"),
            LocalIntent.PRESENCE,
        )
        val named = assertRecognized(
            LocalCommandEngine(configuredName = "  Ana  ").process("Está aí?"),
            LocalIntent.PRESENCE,
        )

        assertEquals("Sempre a postos.", unnamed.response)
        assertEquals("Sempre a postos, Ana.", named.response)
    }

    @Test
    fun `executes each exact volume alias once`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port)

        listOf("aumente o volume", "aumentar o volume", "aumenta o volume").forEach {
            assertRecognized(engine.process(it), LocalIntent.VOLUME_UP)
        }
        listOf(
            "diminua o volume",
            "diminuir o volume",
            "diminui o volume",
            "abaixa o volume",
            "abaixe o volume",
            "abaixar o volume",
            "baixe o volume",
        ).forEach {
            assertRecognized(engine.process(it), LocalIntent.VOLUME_DOWN)
        }

        assertEquals(
            listOf(
                LocalAction.VOLUME_UP,
                LocalAction.VOLUME_UP,
                LocalAction.VOLUME_UP,
                LocalAction.VOLUME_DOWN,
                LocalAction.VOLUME_DOWN,
                LocalAction.VOLUME_DOWN,
                LocalAction.VOLUME_DOWN,
                LocalAction.VOLUME_DOWN,
                LocalAction.VOLUME_DOWN,
                LocalAction.VOLUME_DOWN,
            ),
            port.actions,
        )
    }

    @Test
    fun `reports confirmed volume changes with observed levels`() {
        val upResult = LocalActionResult.Changed(LocalAction.VOLUME_UP, before = 4, after = 5)
        val downResult = LocalActionResult.Changed(LocalAction.VOLUME_DOWN, before = 5, after = 4)
        val port = RecordingActionPort { action ->
            if (action == LocalAction.VOLUME_UP) upResult else downResult
        }
        val engine = LocalCommandEngine(actionPort = port)

        assertEquals(
            LocalCommandResult.Recognized(LocalIntent.VOLUME_UP, "Volume aumentado.", upResult),
            engine.process("Aumente o volume"),
        )
        assertEquals(
            LocalCommandResult.Recognized(LocalIntent.VOLUME_DOWN, "Volume reduzido.", downResult),
            engine.process("Baixe o volume"),
        )
    }

    @Test
    fun `accepts assistant prefix for a volume command and executes once`() {
        val port = RecordingActionPort()
        val result = LocalCommandEngine(actionPort = port)
            .process("Vexa, aumente o volume")

        assertRecognized(result, LocalIntent.VOLUME_UP)
        assertEquals(listOf(LocalAction.VOLUME_UP), port.actions)
    }

    @Test
    fun `distinguishes upper and lower volume bounds`() {
        val port = RecordingActionPort { action -> LocalActionResult.AtLimit(action, level = 10) }
        val engine = LocalCommandEngine(actionPort = port)

        assertEquals(
            "O volume já está no máximo.",
            assertRecognized(engine.process("aumenta o volume"), LocalIntent.VOLUME_UP).response,
        )
        assertEquals(
            "O volume já está no mínimo.",
            assertRecognized(engine.process("abaixa o volume"), LocalIntent.VOLUME_DOWN).response,
        )
    }

    @Test
    fun `does not claim success for fixed unavailable denied or failed volume actions`() {
        val cases = listOf<(LocalAction) -> LocalActionResult>(
            { LocalActionResult.Fixed(it) },
            { LocalActionResult.Unavailable(it) },
            { LocalActionResult.Denied(it) },
            { LocalActionResult.Failure(it) },
        )
        val expectedResponses = listOf(
            "O volume deste dispositivo é fixo.",
            "Controle de volume indisponível.",
            "Sem permissão para ajustar o volume.",
            "Não consegui confirmar o ajuste de volume.",
        )

        cases.zip(expectedResponses).forEach { (resultFactory, expectedResponse) ->
            val engine = LocalCommandEngine(
                actionPort = RecordingActionPort(resultFactory),
            )
            val result = assertRecognized(
                engine.process("diminua o volume"),
                LocalIntent.VOLUME_DOWN,
            )
            assertEquals(expectedResponse, result.response)
        }
    }

    @Test
    fun `defaults volume actions to unavailable`() {
        val result = assertRecognized(
            LocalCommandEngine().process("aumente o volume"),
            LocalIntent.VOLUME_UP,
        )

        assertEquals("Controle de volume indisponível.", result.response)
        assertEquals(
            LocalActionResult.Unavailable(LocalAction.VOLUME_UP),
            result.actionResult,
        )
    }

    @Test
    fun `rejects negative compound and unknown volume text without executing actions`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port)

        listOf(
            "não aumente o volume",
            "não abaixe o volume",
            "aumente o volume e diga as horas",
            "pode aumentar o volume",
            "volume",
            "abaixe bastante o volume",
        ).forEach { phrase ->
            assertSame(LocalCommandResult.Unknown, engine.process(phrase))
        }

        assertTrue(port.actions.isEmpty())
    }

    private fun assertRecognized(
        result: LocalCommandResult,
        expectedIntent: LocalIntent,
    ): LocalCommandResult.Recognized {
        assertTrue(result is LocalCommandResult.Recognized)
        return (result as LocalCommandResult.Recognized).also {
            assertEquals(expectedIntent, it.intent)
        }
    }

    private class RecordingActionPort(
        private val result: (LocalAction) -> LocalActionResult = {
            LocalActionResult.Changed(
                action = it,
                before = if (it == LocalAction.VOLUME_UP) 4 else 5,
                after = if (it == LocalAction.VOLUME_UP) 5 else 4,
            )
        },
    ) : LocalActionPort {
        val actions = mutableListOf<LocalAction>()

        override fun execute(action: LocalAction): LocalActionResult {
            actions += action
            return result(action)
        }
    }
}
