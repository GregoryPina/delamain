package com.gregorypina.delamain.domain.command

import java.text.Normalizer
import java.util.Locale

/**
 * Deterministic local command parser.
 * It deliberately knows nothing about Android APIs so it can later be used by
 * the voice layer or other input sources.
 */
class CommandRouter {
    fun route(rawInput: String): DelamainCommand {
        val input = normalize(rawInput)
        if (input.isBlank()) return DelamainCommand.Unknown

        parseVolume(input)?.let { return it }

        return when (input) {
            in VOLUME_UP_PHRASES -> DelamainCommand.VolumeUp
            in VOLUME_DOWN_PHRASES -> DelamainCommand.VolumeDown
            in MEDIA_PLAY_PAUSE_PHRASES -> DelamainCommand.MediaPlayPause
            in MEDIA_NEXT_PHRASES -> DelamainCommand.MediaNext
            in MEDIA_PREVIOUS_PHRASES -> DelamainCommand.MediaPrevious
            in CURRENT_TIME_PHRASES -> DelamainCommand.CurrentTime
            else -> parseOpenApp(input) ?: DelamainCommand.Unknown
        }
    }

    private fun parseVolume(input: String): DelamainCommand? {
        val match = VOLUME_PERCENT_PATTERN.matchEntire(input) ?: return null
        val percent = match.groupValues[1].toIntOrNull() ?: return null
        if (percent !in 0..100) return null
        return DelamainCommand.SetVolume(percent)
    }

    private fun parseOpenApp(input: String): DelamainCommand? {
        OPEN_APP_PHRASES[input]?.let { app ->
            return DelamainCommand.OpenApp(app.first, app.second)
        }
        return null
    }

    private fun normalize(value: String): String {
        val withoutAccents = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return withoutAccents
            .replace(TERMINAL_PUNCTUATION, "")
            .trim()
            .replace(WHITESPACE, " ")
    }

    private companion object {
        val VOLUME_UP_PHRASES = setOf(
            "aumentar volume",
            "aumenta o volume",
            "volume mais",
            "mais volume",
        )
        val VOLUME_DOWN_PHRASES = setOf(
            "diminuir volume",
            "diminui o volume",
            "volume menos",
            "menos volume",
        )
        val MEDIA_PLAY_PAUSE_PHRASES = setOf(
            "pausar musica",
            "pausa musica",
            "pausar",
            "pause",
            "play pause",
        )
        val MEDIA_NEXT_PHRASES = setOf(
            "proxima musica",
            "proxima faixa",
            "proxima",
            "next",
            "next song",
        )
        val MEDIA_PREVIOUS_PHRASES = setOf(
            "musica anterior",
            "faixa anterior",
            "anterior",
            "previous",
            "previous song",
        )
        val CURRENT_TIME_PHRASES = setOf(
            "que horas",
            "que horas sao",
            "horas sao",
            "hora",
            "what time is it",
        )

        val OPEN_APP_PHRASES: Map<String, Pair<String, String>> = buildMap {
            val verbs = listOf("abrir", "abra", "open", "abre")
            val apps = listOf(
                "youtube" to ("com.google.android.youtube" to "YouTube"),
                "chrome" to ("com.android.chrome" to "Chrome"),
                "google maps" to ("com.google.android.apps.maps" to "Google Maps"),
                "maps" to ("com.google.android.apps.maps" to "Google Maps"),
                "spotify" to ("com.spotify.music" to "Spotify"),
                "whatsapp" to ("com.whatsapp" to "WhatsApp"),
            )
            verbs.forEach { verb ->
                apps.forEach { (name, app) -> put("$verb $name", app) }
            }
        }

        val VOLUME_PERCENT_PATTERN = Regex(
            "^(?:volume|som)\\s*(?:para|em|de)?\\s*(100|[1-9]?[0-9])\\s*(?:%|por cento)?$",
        )
        val TERMINAL_PUNCTUATION = Regex("[\\p{P}]+$")
        val WHITESPACE = Regex("\\s+")
    }
}
