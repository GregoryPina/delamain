package com.gregorypina.delamain.domain

import java.time.Clock

class LocalCommandEngine(
    private val clock: Clock = Clock.systemDefaultZone(),
    configuredName: String? = null,
    tone: PersonalityTone = PersonalityTone.WARM,
    private val actionPort: LocalActionPort = UnavailableLocalActionPort,
    private val batteryStatusPort: BatteryStatusPort = UnavailableBatteryStatusPort,
) {
    private var personality = PersonalityConfig.sanitize(configuredName, tone)
    private val nextVariantByIntent = mutableMapOf<LocalIntent, Int>()
    private val nextVariantByResponseKey = mutableMapOf<String, Int>()

    @Synchronized
    fun applyPersonality(config: PersonalityConfig) {
        personality = config
    }

    fun currentPersonality(): PersonalityConfig = personality

    @Synchronized
    fun process(input: String): LocalCommandResult {
        val normalized = CommandInputNormalizer.normalize(input)
        val intent = when {
            normalized == CommandInputNormalizer.ASSISTANT_NAME -> LocalIntent.CALL
            else -> recognize(CommandInputNormalizer.stripOptionalPrefix(normalized))
        } ?: return LocalCommandResult.Unknown

        val strippedPhrase = CommandInputNormalizer.stripOptionalPrefix(normalized)
        val action = when (intent) {
            LocalIntent.VOLUME_UP -> LocalAction.VolumeUp
            LocalIntent.VOLUME_DOWN -> LocalAction.VolumeDown
            LocalIntent.MEDIA_NEXT -> LocalAction.MediaNext
            LocalIntent.MEDIA_PREVIOUS -> LocalAction.MediaPrevious
            LocalIntent.OPEN_APP -> {
                val target = OPEN_APP_PHRASES[strippedPhrase]
                    ?: return LocalCommandResult.Unknown
                LocalAction.OpenApp(target.first, target.second)
            }
            else -> null
        }
        if (action != null) {
            val actionResult = actionPort.execute(action)
            return LocalCommandResult.Recognized(
                intent = intent,
                response = responseFor(actionResult),
                actionResult = actionResult,
            )
        }

        if (intent == LocalIntent.BATTERY_STATUS) {
            return LocalCommandResult.Recognized(
                intent = intent,
                response = responseForBattery(batteryStatusPort.read()),
            )
        }

        return LocalCommandResult.Recognized(intent, nextResponse(intent))
    }

    private fun recognize(phrase: String): LocalIntent? = when {
        phrase in OPEN_APP_PHRASES -> LocalIntent.OPEN_APP
        phrase in PRESENCE_PHRASES -> LocalIntent.PRESENCE
        phrase in GREETING_PHRASES -> LocalIntent.GREETING
        phrase in THANKS_PHRASES -> LocalIntent.THANKS
        phrase in TIME_PHRASES -> LocalIntent.TIME
        phrase in BATTERY_STATUS_PHRASES -> LocalIntent.BATTERY_STATUS
        phrase in VOLUME_UP_PHRASES -> LocalIntent.VOLUME_UP
        phrase in VOLUME_DOWN_PHRASES -> LocalIntent.VOLUME_DOWN
        phrase in MEDIA_NEXT_PHRASES -> LocalIntent.MEDIA_NEXT
        phrase in MEDIA_PREVIOUS_PHRASES -> LocalIntent.MEDIA_PREVIOUS
        else -> null
    }

    private fun nextResponse(intent: LocalIntent): String {
        val variants = responsesFor(intent)
        val nextIndex = nextVariantByIntent.getOrDefault(intent, 0) % variants.size
        nextVariantByIntent[intent] = (nextIndex + 1) % variants.size
        return variants[nextIndex]
    }

    private fun responsesFor(intent: LocalIntent): List<String> =
        LocalPhraseBank.intentVariants(intent, personality.tone, personality.displayName, clock)

    private fun responseForBattery(status: BatteryStatus?): String = when (status) {
        null -> pickVariant(KEY_BATTERY_UNAVAILABLE)
        else -> if (status.isCharging) {
            pickVariant(
                KEY_BATTERY_CHARGING,
                mapOf("percent" to status.levelPercent.toString()),
            )
        } else {
            pickVariant(
                KEY_BATTERY_LEVEL,
                mapOf("percent" to status.levelPercent.toString()),
            )
        }
    }

    private fun responseFor(result: LocalActionResult): String = when (result) {
        is LocalActionResult.Changed -> when (result.action) {
            LocalAction.VolumeUp -> pickVariant(KEY_VOLUME_UP_CHANGED)
            LocalAction.VolumeDown -> pickVariant(KEY_VOLUME_DOWN_CHANGED)
            else -> error("Volume change is not supported for ${result.action}")
        }
        is LocalActionResult.AtLimit -> when (result.action) {
            LocalAction.VolumeUp -> pickVariant(KEY_VOLUME_UP_AT_LIMIT)
            LocalAction.VolumeDown -> pickVariant(KEY_VOLUME_DOWN_AT_LIMIT)
            else -> error("Volume limit is not supported for ${result.action}")
        }
        is LocalActionResult.Fixed -> pickVariant(KEY_VOLUME_FIXED)
        is LocalActionResult.Unavailable -> when (result.action) {
            is LocalAction.OpenApp -> pickVariant(KEY_APP_UNAVAILABLE)
            LocalAction.MediaNext,
            LocalAction.MediaPrevious,
            -> pickVariant(KEY_MEDIA_UNAVAILABLE)
            else -> pickVariant(KEY_VOLUME_UNAVAILABLE)
        }
        is LocalActionResult.Denied -> pickVariant(KEY_VOLUME_DENIED)
        is LocalActionResult.Failure -> when (result.action) {
            is LocalAction.OpenApp -> pickVariant(KEY_APP_FAILURE)
            LocalAction.MediaNext,
            LocalAction.MediaPrevious,
            -> pickVariant(KEY_MEDIA_FAILURE)
            else -> pickVariant(KEY_VOLUME_FAILURE)
        }
        is LocalActionResult.Launched -> pickVariant(
            KEY_APP_LAUNCHED,
            mapOf("app" to result.displayName),
        )
        is LocalActionResult.NotInstalled -> pickVariant(
            KEY_APP_NOT_INSTALLED,
            mapOf("app" to result.displayName),
        )
        is LocalActionResult.Dispatched -> when (result.action) {
            LocalAction.MediaNext -> pickVariant(KEY_MEDIA_NEXT_DISPATCHED)
            LocalAction.MediaPrevious -> pickVariant(KEY_MEDIA_PREVIOUS_DISPATCHED)
            else -> error("Dispatch is not supported for ${result.action}")
        }
    }

    private fun pickVariant(
        key: String,
        args: Map<String, String> = emptyMap(),
    ): String {
        val variants = LocalPhraseBank.variants(key, personality.tone)
        val nextIndex = nextVariantByResponseKey.getOrDefault(key, 0) % variants.size
        nextVariantByResponseKey[key] = (nextIndex + 1) % variants.size
        return applyTemplate(variants[nextIndex], args)
    }

    private fun applyTemplate(template: String, args: Map<String, String>): String =
        args.entries.fold(template) { text, (key, value) ->
            text.replace("{$key}", value)
        }

    internal companion object {
        const val KEY_BATTERY_UNAVAILABLE = "battery_unavailable"
        const val KEY_BATTERY_CHARGING = "battery_charging"
        const val KEY_BATTERY_LEVEL = "battery_level"
        const val KEY_VOLUME_UP_CHANGED = "volume_up_changed"
        const val KEY_VOLUME_DOWN_CHANGED = "volume_down_changed"
        const val KEY_VOLUME_UP_AT_LIMIT = "volume_up_at_limit"
        const val KEY_VOLUME_DOWN_AT_LIMIT = "volume_down_at_limit"
        const val KEY_VOLUME_FIXED = "volume_fixed"
        const val KEY_VOLUME_UNAVAILABLE = "volume_unavailable"
        const val KEY_VOLUME_DENIED = "volume_denied"
        const val KEY_VOLUME_FAILURE = "volume_failure"
        const val KEY_APP_UNAVAILABLE = "app_unavailable"
        const val KEY_APP_FAILURE = "app_failure"
        const val KEY_APP_LAUNCHED = "app_launched"
        const val KEY_APP_NOT_INSTALLED = "app_not_installed"
        const val KEY_MEDIA_UNAVAILABLE = "media_unavailable"
        const val KEY_MEDIA_FAILURE = "media_failure"
        const val KEY_MEDIA_NEXT_DISPATCHED = "media_next_dispatched"
        const val KEY_MEDIA_PREVIOUS_DISPATCHED = "media_previous_dispatched"

        val PRESENCE_PHRASES = setOf(
            "esta ai",
            "ta ai",
            "voce esta ai",
            "voce ta ai",
            "vc esta ai",
            "vc ta ai",
            "ce ta ai",
        )
        val GREETING_PHRASES = setOf("oi", "ola", "bom dia", "boa tarde", "boa noite")
        val THANKS_PHRASES = setOf("obrigado", "obrigada", "muito obrigado", "muito obrigada")
        val TIME_PHRASES = setOf("que horas sao", "que hora e", "qual e a hora")
        val BATTERY_STATUS_PHRASES = setOf(
            "status do sistema",
            "status da bateria",
            "como esta a bateria",
            "nivel da bateria",
            "quanto de bateria",
            "bateria",
        )
        val VOLUME_UP_PHRASES = setOf(
            "aumente o volume",
            "aumentar o volume",
            "aumenta o volume",
        )
        val VOLUME_DOWN_PHRASES = setOf(
            "diminua o volume",
            "diminuir o volume",
            "diminui o volume",
            "abaixa o volume",
            "abaixe o volume",
            "abaixar o volume",
            "baixe o volume",
        )
        val MEDIA_NEXT_PHRASES = setOf(
            "proxima musica",
            "proxima faixa",
            "proxima",
        )
        val MEDIA_PREVIOUS_PHRASES = setOf(
            "musica anterior",
            "faixa anterior",
            "anterior",
        )

        val OPEN_APP_PHRASES: Map<String, Pair<String, String>> = buildMap {
            val verbs = listOf("abrir", "abra", "abre")
            val apps = listOf(
                "youtube" to ("com.google.android.youtube" to "YouTube"),
                "chrome" to ("com.android.chrome" to "Chrome"),
                "google maps" to ("com.google.android.apps.maps" to "Google Maps"),
                "maps" to ("com.google.android.apps.maps" to "Google Maps"),
                "spotify" to ("com.spotify.music" to "Spotify"),
                "whatsapp" to ("com.whatsapp" to "WhatsApp"),
            )
            verbs.forEach { verb ->
                apps.forEach { (name, app) ->
                    put("$verb $name", app)
                    put("$verb o $name", app)
                }
            }
        }

    }
}
