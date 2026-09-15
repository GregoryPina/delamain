package com.gregorypina.delamain.domain.command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CommandRouterTest {
    private val router = CommandRouter()

    @Test
    fun `recognizes complete remote command catalog`() {
        val cases = mapOf(
            "aumentar volume" to DelamainCommand.VolumeUp,
            "aumenta o volume" to DelamainCommand.VolumeUp,
            "volume mais" to DelamainCommand.VolumeUp,
            "mais volume" to DelamainCommand.VolumeUp,
            "diminuir volume" to DelamainCommand.VolumeDown,
            "diminui o volume" to DelamainCommand.VolumeDown,
            "volume menos" to DelamainCommand.VolumeDown,
            "menos volume" to DelamainCommand.VolumeDown,
            "pausar música" to DelamainCommand.MediaPlayPause,
            "pausa musica" to DelamainCommand.MediaPlayPause,
            "pausar" to DelamainCommand.MediaPlayPause,
            "pause" to DelamainCommand.MediaPlayPause,
            "play pause" to DelamainCommand.MediaPlayPause,
            "próxima música" to DelamainCommand.MediaNext,
            "proxima faixa" to DelamainCommand.MediaNext,
            "proxima" to DelamainCommand.MediaNext,
            "next" to DelamainCommand.MediaNext,
            "next song" to DelamainCommand.MediaNext,
            "música anterior" to DelamainCommand.MediaPrevious,
            "faixa anterior" to DelamainCommand.MediaPrevious,
            "anterior" to DelamainCommand.MediaPrevious,
            "previous" to DelamainCommand.MediaPrevious,
            "previous song" to DelamainCommand.MediaPrevious,
            "que horas" to DelamainCommand.CurrentTime,
            "que horas são" to DelamainCommand.CurrentTime,
            "horas são" to DelamainCommand.CurrentTime,
            "hora" to DelamainCommand.CurrentTime,
            "what time is it" to DelamainCommand.CurrentTime,
        )

        cases.forEach { (phrase, expected) ->
            assertEquals("phrase=$phrase", expected, router.route(phrase))
        }
    }

    @Test
    fun `normalizes case accents spacing and terminal punctuation`() {
        assertEquals(DelamainCommand.MediaNext, router.route("  PRÓXIMA   MÚSICA!!!  "))
        assertEquals(DelamainCommand.CurrentTime, router.route("Que HORAS são???"))
        assertEquals(
            DelamainCommand.OpenApp("com.spotify.music", "Spotify"),
            router.route("ABRA Spotify!"),
        )
        assertEquals(DelamainCommand.SetVolume(50), router.route("Volume em 50%!!!"))
    }

    @Test
    fun `recognizes percentage only when the entire phrase is valid`() {
        listOf(
            "volume 0" to 0,
            "volume 100" to 100,
            "volume para 25%" to 25,
            "volume em 7 por cento" to 7,
            "som de 42" to 42,
        ).forEach { (phrase, percent) ->
            assertEquals(DelamainCommand.SetVolume(percent), router.route(phrase))
        }
    }

    @Test
    fun `opens only one exact supported app destination`() {
        val cases = mapOf(
            "abrir youtube" to DelamainCommand.OpenApp("com.google.android.youtube", "YouTube"),
            "abra chrome" to DelamainCommand.OpenApp("com.android.chrome", "Chrome"),
            "open google maps" to DelamainCommand.OpenApp("com.google.android.apps.maps", "Google Maps"),
            "abre maps" to DelamainCommand.OpenApp("com.google.android.apps.maps", "Google Maps"),
            "abrir spotify" to DelamainCommand.OpenApp("com.spotify.music", "Spotify"),
            "abra whatsapp" to DelamainCommand.OpenApp("com.whatsapp", "WhatsApp"),
        )

        cases.forEach { (phrase, expected) ->
            assertEquals("phrase=$phrase", expected, router.route(phrase))
        }
    }

    @Test
    fun `rejects negative compound partial and out of range commands`() {
        listOf(
            "não aumenta o volume",
            "não pausa a música",
            "pausa a música e aumenta o volume",
            "você sabe que horas são amanhã?",
            "abrir spotify e depois youtube",
            "volume 101",
            "volume -1",
            "volume 20.5",
            "por favor volume 20",
            "volume 20 agora",
            "texto volume para 50%",
            "volume para 50% por favor",
            "pausar música agora",
            "abrir o spotify",
            "abra spotify agora",
        ).forEach { phrase ->
            assertSame("phrase=$phrase", DelamainCommand.Unknown, router.route(phrase))
        }
    }
}
