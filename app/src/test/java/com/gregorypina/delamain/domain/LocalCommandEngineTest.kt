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
    fun `help recognizes only complete phrases and never executes examples`() {
        val engine = LocalCommandEngine(actionPort = object : LocalActionPort {
            override fun execute(action: LocalAction): LocalActionResult = error("Help must not execute an action")
        })
        listOf("VEXA, o que você sabe fazer?", "ajuda", "comandos").forEach {
            assertRecognized(engine.process(it), LocalIntent.HELP)
        }
        listOf("não quero ajuda", "ajuda e abra Spotify", "quais comandos e aumente o volume").forEach {
            assertSame(LocalCommandResult.Unknown, engine.process(it))
        }
    }

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
        val fourth = assertRecognized(engine.process("Vexa"), LocalIntent.CALL).response

        assertNotEquals(first, second)
        assertNotEquals(second, third)
        assertEquals(first, fourth)
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
                LocalAction.VolumeUp,
                LocalAction.VolumeUp,
                LocalAction.VolumeUp,
                LocalAction.VolumeDown,
                LocalAction.VolumeDown,
                LocalAction.VolumeDown,
                LocalAction.VolumeDown,
                LocalAction.VolumeDown,
                LocalAction.VolumeDown,
                LocalAction.VolumeDown,
            ),
            port.actions,
        )
    }

    @Test
    fun `reports confirmed volume changes with observed levels`() {
        val upResult = LocalActionResult.Changed(LocalAction.VolumeUp, before = 4, after = 5)
        val downResult = LocalActionResult.Changed(LocalAction.VolumeDown, before = 5, after = 4)
        val port = RecordingActionPort { action ->
            if (action == LocalAction.VolumeUp) upResult else downResult
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
        assertEquals(listOf(LocalAction.VolumeUp), port.actions)
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
            LocalActionResult.Unavailable(LocalAction.VolumeUp),
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

    @Test
    fun `recognizes allowlisted open app phrases`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port)

        listOf(
            "abra o youtube",
            "abrir spotify",
            "abre o chrome",
            "abrir google maps",
            "abra maps",
            "abrir whatsapp",
        ).forEach { phrase ->
            assertRecognized(engine.process(phrase), LocalIntent.OPEN_APP)
        }

        assertEquals(
            listOf(
                LocalAction.OpenApp("com.google.android.youtube", "YouTube"),
                LocalAction.OpenApp("com.spotify.music", "Spotify"),
                LocalAction.OpenApp("com.android.chrome", "Chrome"),
                LocalAction.OpenApp("com.google.android.apps.maps", "Google Maps"),
                LocalAction.OpenApp("com.google.android.apps.maps", "Google Maps"),
                LocalAction.OpenApp("com.whatsapp", "WhatsApp"),
            ),
            port.actions,
        )
    }

    @Test
    fun `accepts assistant prefix for open app commands`() {
        val port = RecordingActionPort()
        val result = LocalCommandEngine(actionPort = port).process("Vexa, abra o spotify")

        assertRecognized(result, LocalIntent.OPEN_APP)
        assertEquals(
            listOf(LocalAction.OpenApp("com.spotify.music", "Spotify")),
            port.actions,
        )
    }

    @Test
    fun `reports launched app result`() {
        val spotify = LocalAction.OpenApp("com.spotify.music", "Spotify")
        val launched = LocalActionResult.Launched(spotify, "Spotify")
        val engine = LocalCommandEngine(
            actionPort = RecordingActionPort { launched },
        )

        assertEquals(
            LocalCommandResult.Recognized(LocalIntent.OPEN_APP, "Abrindo Spotify.", launched),
            engine.process("abra o spotify"),
        )
    }

    @Test
    fun `reports not installed app result`() {
        val spotify = LocalAction.OpenApp("com.spotify.music", "Spotify")
        val notInstalled = LocalActionResult.NotInstalled(spotify, "Spotify")
        val engine = LocalCommandEngine(
            actionPort = RecordingActionPort { notInstalled },
        )

        assertEquals(
            LocalCommandResult.Recognized(
                LocalIntent.OPEN_APP,
                "Spotify não está instalado.",
                notInstalled,
            ),
            engine.process("abrir spotify"),
        )
    }

    @Test
    fun `does not claim success for unavailable or failed app launches`() {
        val youtube = LocalAction.OpenApp("com.google.android.youtube", "YouTube")
        val cases = listOf(
            LocalActionResult.Unavailable(youtube) to "Abertura de aplicativos indisponível.",
            LocalActionResult.Failure(youtube) to "Não consegui abrir o aplicativo.",
        )

        cases.forEach { (actionResult, expectedResponse) ->
            val engine = LocalCommandEngine(
                actionPort = RecordingActionPort { actionResult },
            )
            val result = assertRecognized(
                engine.process("abra o youtube"),
                LocalIntent.OPEN_APP,
            )
            assertEquals(expectedResponse, result.response)
            assertEquals(actionResult, result.actionResult)
        }
    }

    @Test
    fun `defaults open app actions to unavailable`() {
        val result = assertRecognized(
            LocalCommandEngine().process("abra o youtube"),
            LocalIntent.OPEN_APP,
        )

        assertEquals("Abertura de aplicativos indisponível.", result.response)
        assertEquals(
            LocalActionResult.Unavailable(
                LocalAction.OpenApp("com.google.android.youtube", "YouTube"),
            ),
            result.actionResult,
        )
    }

    @Test
    fun `recognizes allowlisted media track phrases`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port)

        listOf(
            "próxima música",
            "proxima faixa",
            "próxima",
        ).forEach {
            assertRecognized(engine.process(it), LocalIntent.MEDIA_NEXT)
        }
        listOf(
            "música anterior",
            "faixa anterior",
            "anterior",
        ).forEach {
            assertRecognized(engine.process(it), LocalIntent.MEDIA_PREVIOUS)
        }

        assertEquals(
            listOf(
                LocalAction.MediaNext,
                LocalAction.MediaNext,
                LocalAction.MediaNext,
                LocalAction.MediaPrevious,
                LocalAction.MediaPrevious,
                LocalAction.MediaPrevious,
            ),
            port.actions,
        )
    }

    @Test
    fun `accepts assistant prefix for media track commands`() {
        val port = RecordingActionPort()
        val result = LocalCommandEngine(actionPort = port).process("Vexa, próxima música")

        assertRecognized(result, LocalIntent.MEDIA_NEXT)
        assertEquals(listOf(LocalAction.MediaNext), port.actions)
    }

    @Test
    fun `reports dispatched media track results without claiming playback change`() {
        val next = LocalActionResult.Dispatched(LocalAction.MediaNext)
        val previous = LocalActionResult.Dispatched(LocalAction.MediaPrevious)
        val engine = LocalCommandEngine(
            actionPort = RecordingActionPort { action ->
                when (action) {
                    LocalAction.MediaNext -> next
                    LocalAction.MediaPrevious -> previous
                    else -> error("unexpected action")
                }
            },
        )

        assertEquals(
            LocalCommandResult.Recognized(
                LocalIntent.MEDIA_NEXT,
                "Comando de próxima faixa enviado.",
                next,
            ),
            engine.process("próxima faixa"),
        )
        assertEquals(
            LocalCommandResult.Recognized(
                LocalIntent.MEDIA_PREVIOUS,
                "Comando de faixa anterior enviado.",
                previous,
            ),
            engine.process("faixa anterior"),
        )
    }

    @Test
    fun `defaults media track actions to unavailable`() {
        val result = assertRecognized(
            LocalCommandEngine().process("próxima música"),
            LocalIntent.MEDIA_NEXT,
        )

        assertEquals("Controle de mídia indisponível.", result.response)
        assertEquals(LocalActionResult.Unavailable(LocalAction.MediaNext), result.actionResult)
    }

    @Test
    fun `recognizes battery status phrases`() {
        val engine = LocalCommandEngine(
            batteryStatusPort = { BatteryStatus(levelPercent = 80, isCharging = false) },
        )

        listOf(
            "status do sistema",
            "como está a bateria?",
            "nível da bateria",
            "bateria",
        ).forEach { phrase ->
            assertRecognized(engine.process(phrase), LocalIntent.BATTERY_STATUS)
        }
    }

    @Test
    fun `reports battery level from port`() {
        val engine = LocalCommandEngine(
            batteryStatusPort = { BatteryStatus(levelPercent = 72, isCharging = false) },
        )

        val result = assertRecognized(engine.process("status da bateria"), LocalIntent.BATTERY_STATUS)

        assertEquals("Bateria em 72%.", result.response)
    }

    @Test
    fun `reports charging battery status`() {
        val engine = LocalCommandEngine(
            batteryStatusPort = { BatteryStatus(levelPercent = 54, isCharging = true) },
        )

        val result = assertRecognized(engine.process("bateria"), LocalIntent.BATTERY_STATUS)

        assertEquals("Bateria em 54% e carregando.", result.response)
    }

    @Test
    fun `reports unavailable battery status`() {
        val engine = LocalCommandEngine(batteryStatusPort = { null })

        val result = assertRecognized(engine.process("status do sistema"), LocalIntent.BATTERY_STATUS)

        assertEquals("Não consigo ler a bateria neste momento.", result.response)
    }

    @Test
    fun `rotates app not installed responses without immediate repetition`() {
        val spotify = LocalAction.OpenApp("com.spotify.music", "Spotify")
        val notInstalled = LocalActionResult.NotInstalled(spotify, "Spotify")
        val engine = LocalCommandEngine(
            actionPort = RecordingActionPort { notInstalled },
        )

        val first = assertRecognized(engine.process("abrir spotify"), LocalIntent.OPEN_APP).response
        val second = assertRecognized(engine.process("abrir spotify"), LocalIntent.OPEN_APP).response
        val third = assertRecognized(engine.process("abrir spotify"), LocalIntent.OPEN_APP).response
        val fourth = assertRecognized(engine.process("abrir spotify"), LocalIntent.OPEN_APP).response

        assertNotEquals(first, second)
        assertNotEquals(second, third)
        assertEquals(first, fourth)
        assertTrue(first.contains("Spotify"))
        assertTrue(second.contains("Spotify"))
    }

    @Test
    fun `rejects negative compound and unknown media text without executing actions`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port)

        listOf(
            "não pule a música",
            "próxima música e aumente o volume",
            "pule a faixa",
            "musica",
        ).forEach { phrase ->
            assertSame(LocalCommandResult.Unknown, engine.process(phrase))
        }

        assertTrue(port.actions.isEmpty())
    }

    @Test
    fun `rejects negative compound and unknown open app text without executing actions`() {
        val port = RecordingActionPort()
        val engine = LocalCommandEngine(actionPort = port)

        listOf(
            "não abrir spotify",
            "abrir spotify e youtube",
            "abrir netflix",
            "abrir",
            "abra aplicativo",
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
        private val result: (LocalAction) -> LocalActionResult = { action ->
            when (action) {
                LocalAction.VolumeUp -> LocalActionResult.Changed(action, before = 4, after = 5)
                LocalAction.VolumeDown -> LocalActionResult.Changed(action, before = 5, after = 4)
                LocalAction.MediaNext,
                LocalAction.MediaPrevious,
                -> LocalActionResult.Dispatched(action)
                is LocalAction.OpenApp -> LocalActionResult.Launched(action, action.displayName)
            }
        },
    ) : LocalActionPort {
        val actions = mutableListOf<LocalAction>()

        override fun execute(action: LocalAction): LocalActionResult {
            actions += action
            return result(action)
        }
    }
}
