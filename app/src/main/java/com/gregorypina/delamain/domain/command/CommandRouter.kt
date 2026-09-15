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

        when {
            input in setOf("aumentar volume", "aumenta o volume", "volume mais", "mais volume") ->
                return DelamainCommand.VolumeUp

            input in setOf("diminuir volume", "diminui o volume", "volume menos", "menos volume") ->
                return DelamainCommand.VolumeDown

            input.contains("pausar musica") || input.contains("pausa musica") ||
                input == "pausar" || input == "pause" || input == "play pause" ->
                return DelamainCommand.MediaPlayPause

            input.contains("proxima musica") || input.contains("proxima faixa") ||
                input == "proxima" || input == "next" || input == "next song" ->
                return DelamainCommand.MediaNext

            input.contains("musica anterior") || input.contains("faixa anterior") ||
                input == "anterior" || input == "previous" || input == "previous song" ->
                return DelamainCommand.MediaPrevious

            input.contains("que horas") || input.contains("horas sao") ||
                input == "hora" || input == "what time is it" ->
                return DelamainCommand.CurrentTime
        }

        parseOpenApp(input)?.let { return it }

        return DelamainCommand.Unknown
    }

    private fun parseVolume(input: String): DelamainCommand? {
        val match = Regex("(?:volume|som)\\s*(?:para|em|de)?\\s*(100|[1-9]?[0-9])\\s*(?:%|por cento)?\\b")
            .find(input)
            ?: return null

        val percent = match.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: return null
        return DelamainCommand.SetVolume(percent)
    }

    private fun parseOpenApp(input: String): DelamainCommand? {
        val apps = listOf(
            "youtube" to ("com.google.android.youtube" to "YouTube"),
            "chrome" to ("com.android.chrome" to "Chrome"),
            "google maps" to ("com.google.android.apps.maps" to "Google Maps"),
            "maps" to ("com.google.android.apps.maps" to "Google Maps"),
            "spotify" to ("com.spotify.music" to "Spotify"),
            "whatsapp" to ("com.whatsapp" to "WhatsApp")
        )

        val asksToOpen = input.startsWith("abrir ") || input.startsWith("abra ") ||
            input.startsWith("open ") || input.startsWith("abre ")

        if (!asksToOpen) return null

        return apps.firstOrNull { (name, _) -> input.contains(name) }
            ?.let { (_, app) -> DelamainCommand.OpenApp(app.first, app.second) }
    }

    private fun normalize(value: String): String {
        val withoutAccents = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return withoutAccents.replace(Regex("\\s+"), " ")
    }
}
