package com.gregorypina.delamain.domain

import java.time.Clock
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalCommandEngine(
    private val clock: Clock = Clock.systemDefaultZone(),
    configuredName: String? = null,
    private val actionPort: LocalActionPort = UnavailableLocalActionPort,
    private val batteryStatusPort: BatteryStatusPort = UnavailableBatteryStatusPort,
) {
    private val name = configuredName?.trim()?.takeIf(String::isNotEmpty)
    private val nextVariantByIntent = mutableMapOf<LocalIntent, Int>()
    private val nextVariantByResponseKey = mutableMapOf<String, Int>()

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

    private fun responsesFor(intent: LocalIntent): List<String> = when (intent) {
        LocalIntent.CALL -> listOf(
            "À disposição.",
            "Pois não?",
            "Sim?",
        )
        LocalIntent.PRESENCE -> if (name == null) {
            listOf(
                "Sempre a postos.",
                "Estou aqui.",
                "Presente.",
            )
        } else {
            listOf(
                "Sempre a postos, $name.",
                "Estou aqui, $name.",
                "Presente, $name.",
            )
        }
        LocalIntent.GREETING -> listOf(
            "Olá. Pronto para a próxima viagem?",
            "Saudações. À disposição.",
            "Olá. Como posso ajudar?",
        )
        LocalIntent.THANKS -> listOf(
            "É um prazer.",
            "Sempre às ordens.",
            "Por nada.",
        )
        LocalIntent.TIME -> {
            val time = LocalTime.now(clock).format(TIME_FORMATTER)
            listOf(
                "São $time.",
                "Agora são $time.",
                "O horário agora é $time.",
            )
        }
        LocalIntent.BATTERY_STATUS,
        LocalIntent.VOLUME_UP,
        LocalIntent.VOLUME_DOWN,
        LocalIntent.MEDIA_NEXT,
        LocalIntent.MEDIA_PREVIOUS,
        LocalIntent.OPEN_APP,
        -> error("Action responses depend on the observed result")
    }

    private fun responseForBattery(status: BatteryStatus?): String = when (status) {
        null -> pickVariant(
            KEY_BATTERY_UNAVAILABLE,
            listOf(
                "Não consigo ler a bateria neste momento.",
                "O status da bateria está indisponível agora.",
                "Sem leitura de bateria por enquanto.",
            ),
        )
        else -> if (status.isCharging) {
            pickVariant(
                KEY_BATTERY_CHARGING,
                listOf(
                    "Bateria em {percent}% e carregando.",
                    "Restam {percent}% de bateria, carregando agora.",
                    "{percent}% de bateria, com carga em andamento.",
                ),
                mapOf("percent" to status.levelPercent.toString()),
            )
        } else {
            pickVariant(
                KEY_BATTERY_LEVEL,
                listOf(
                    "Bateria em {percent}%.",
                    "Restam {percent}% de bateria.",
                    "Nível de bateria: {percent}%.",
                ),
                mapOf("percent" to status.levelPercent.toString()),
            )
        }
    }

    private fun responseFor(result: LocalActionResult): String = when (result) {
        is LocalActionResult.Changed -> when (result.action) {
            LocalAction.VolumeUp -> pickVariant(
                KEY_VOLUME_UP_CHANGED,
                listOf(
                    "Volume aumentado.",
                    "Subi o volume.",
                    "Volume um pouco mais alto.",
                ),
            )
            LocalAction.VolumeDown -> pickVariant(
                KEY_VOLUME_DOWN_CHANGED,
                listOf(
                    "Volume reduzido.",
                    "Abaixei o volume.",
                    "Volume um pouco mais baixo.",
                ),
            )
            else -> error("Volume change is not supported for ${result.action}")
        }
        is LocalActionResult.AtLimit -> when (result.action) {
            LocalAction.VolumeUp -> pickVariant(
                KEY_VOLUME_UP_AT_LIMIT,
                listOf(
                    "O volume já está no máximo.",
                    "Não há mais volume para aumentar.",
                    "Já estamos no volume máximo.",
                ),
            )
            LocalAction.VolumeDown -> pickVariant(
                KEY_VOLUME_DOWN_AT_LIMIT,
                listOf(
                    "O volume já está no mínimo.",
                    "Não há mais volume para reduzir.",
                    "Já estamos no volume mínimo.",
                ),
            )
            else -> error("Volume limit is not supported for ${result.action}")
        }
        is LocalActionResult.Fixed -> pickVariant(
            KEY_VOLUME_FIXED,
            listOf(
                "O volume deste dispositivo é fixo.",
                "Este aparelho não permite ajuste de volume.",
                "O volume aqui é fixo, não consigo alterar.",
            ),
        )
        is LocalActionResult.Unavailable -> when (result.action) {
            is LocalAction.OpenApp -> pickVariant(
                KEY_APP_UNAVAILABLE,
                listOf(
                    "Abertura de aplicativos indisponível.",
                    "Não consigo abrir aplicativos agora.",
                    "O recurso de abrir apps está indisponível.",
                ),
            )
            LocalAction.MediaNext,
            LocalAction.MediaPrevious,
            -> pickVariant(
                KEY_MEDIA_UNAVAILABLE,
                listOf(
                    "Controle de mídia indisponível.",
                    "Não consigo controlar a mídia agora.",
                    "Os comandos de mídia estão indisponíveis.",
                ),
            )
            else -> pickVariant(
                KEY_VOLUME_UNAVAILABLE,
                listOf(
                    "Controle de volume indisponível.",
                    "Não consigo ajustar o volume agora.",
                    "O controle de volume está indisponível.",
                ),
            )
        }
        is LocalActionResult.Denied -> pickVariant(
            KEY_VOLUME_DENIED,
            listOf(
                "Sem permissão para ajustar o volume.",
                "Não tenho permissão para mudar o volume.",
                "O sistema não autorizou o ajuste de volume.",
            ),
        )
        is LocalActionResult.Failure -> when (result.action) {
            is LocalAction.OpenApp -> pickVariant(
                KEY_APP_FAILURE,
                listOf(
                    "Não consegui abrir o aplicativo.",
                    "A abertura do aplicativo falhou.",
                    "Não foi possível iniciar o aplicativo.",
                ),
            )
            LocalAction.MediaNext,
            LocalAction.MediaPrevious,
            -> pickVariant(
                KEY_MEDIA_FAILURE,
                listOf(
                    "Não consegui enviar o comando de mídia.",
                    "O comando de mídia não foi enviado.",
                    "Falha ao enviar o comando de mídia.",
                ),
            )
            else -> pickVariant(
                KEY_VOLUME_FAILURE,
                listOf(
                    "Não consegui confirmar o ajuste de volume.",
                    "O volume não respondeu como esperado.",
                    "Não houve confirmação do ajuste de volume.",
                ),
            )
        }
        is LocalActionResult.Launched -> pickVariant(
            KEY_APP_LAUNCHED,
            listOf(
                "Abrindo {app}.",
                "Iniciando {app}.",
                "Vou abrir o {app}.",
            ),
            mapOf("app" to result.displayName),
        )
        is LocalActionResult.NotInstalled -> pickVariant(
            KEY_APP_NOT_INSTALLED,
            listOf(
                "{app} não está instalado.",
                "Não encontrei o {app} neste aparelho.",
                "Parece que o {app} não está instalado.",
            ),
            mapOf("app" to result.displayName),
        )
        is LocalActionResult.Dispatched -> when (result.action) {
            LocalAction.MediaNext -> pickVariant(
                KEY_MEDIA_NEXT_DISPATCHED,
                listOf(
                    "Comando de próxima faixa enviado.",
                    "Pedido de próxima faixa encaminhado.",
                    "Solicitei a próxima faixa.",
                ),
            )
            LocalAction.MediaPrevious -> pickVariant(
                KEY_MEDIA_PREVIOUS_DISPATCHED,
                listOf(
                    "Comando de faixa anterior enviado.",
                    "Pedido de faixa anterior encaminhado.",
                    "Solicitei a faixa anterior.",
                ),
            )
            else -> error("Dispatch is not supported for ${result.action}")
        }
    }

    private fun pickVariant(
        key: String,
        variants: List<String>,
        args: Map<String, String> = emptyMap(),
    ): String {
        val nextIndex = nextVariantByResponseKey.getOrDefault(key, 0) % variants.size
        nextVariantByResponseKey[key] = (nextIndex + 1) % variants.size
        return applyTemplate(variants[nextIndex], args)
    }

    private fun applyTemplate(template: String, args: Map<String, String>): String =
        args.entries.fold(template) { text, (key, value) ->
            text.replace("{$key}", value)
        }

    private companion object {
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

        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
