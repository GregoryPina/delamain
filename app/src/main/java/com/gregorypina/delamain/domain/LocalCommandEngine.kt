package com.gregorypina.delamain.domain

import java.text.Normalizer
import java.time.Clock
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class LocalCommandEngine(
    private val clock: Clock = Clock.systemDefaultZone(),
    configuredName: String? = null,
    private val actionPort: LocalActionPort = UnavailableLocalActionPort,
) {
    private val name = configuredName?.trim()?.takeIf(String::isNotEmpty)
    private val nextVariantByIntent = mutableMapOf<LocalIntent, Int>()

    @Synchronized
    fun process(input: String): LocalCommandResult {
        val normalized = normalize(input)
        val intent = when {
            normalized == ASSISTANT_NAME -> LocalIntent.CALL
            else -> recognize(stripOptionalPrefix(normalized))
        } ?: return LocalCommandResult.Unknown

        val action = when (intent) {
            LocalIntent.VOLUME_UP -> LocalAction.VOLUME_UP
            LocalIntent.VOLUME_DOWN -> LocalAction.VOLUME_DOWN
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

        return LocalCommandResult.Recognized(intent, nextResponse(intent))
    }

    private fun recognize(phrase: String): LocalIntent? = when (phrase) {
        in PRESENCE_PHRASES -> LocalIntent.PRESENCE
        in GREETING_PHRASES -> LocalIntent.GREETING
        in THANKS_PHRASES -> LocalIntent.THANKS
        in TIME_PHRASES -> LocalIntent.TIME
        in VOLUME_UP_PHRASES -> LocalIntent.VOLUME_UP
        in VOLUME_DOWN_PHRASES -> LocalIntent.VOLUME_DOWN
        else -> null
    }

    private fun stripOptionalPrefix(phrase: String): String =
        phrase.removePrefix("$ASSISTANT_NAME ")

    private fun nextResponse(intent: LocalIntent): String {
        val variants = responsesFor(intent)
        val nextIndex = nextVariantByIntent.getOrDefault(intent, 0) % variants.size
        nextVariantByIntent[intent] = (nextIndex + 1) % variants.size
        return variants[nextIndex]
    }

    private fun responsesFor(intent: LocalIntent): List<String> = when (intent) {
        LocalIntent.CALL -> listOf("À disposição.", "Pois não?")
        LocalIntent.PRESENCE -> if (name == null) {
            listOf("Sempre a postos.", "Estou aqui.")
        } else {
            listOf("Sempre a postos, $name.", "Estou aqui, $name.")
        }
        LocalIntent.GREETING -> listOf(
            "Olá. Pronto para a próxima viagem?",
            "Saudações. À disposição.",
        )
        LocalIntent.THANKS -> listOf("É um prazer.", "Sempre às ordens.")
        LocalIntent.TIME -> {
            val time = LocalTime.now(clock).format(TIME_FORMATTER)
            listOf("São $time.", "Agora são $time.")
        }
        LocalIntent.VOLUME_UP,
        LocalIntent.VOLUME_DOWN,
        -> error("Action responses depend on the observed result")
    }

    private fun responseFor(result: LocalActionResult): String = when (result) {
        is LocalActionResult.Changed -> when (result.action) {
            LocalAction.VOLUME_UP -> "Volume aumentado."
            LocalAction.VOLUME_DOWN -> "Volume reduzido."
        }
        is LocalActionResult.AtLimit -> when (result.action) {
            LocalAction.VOLUME_UP -> "O volume já está no máximo."
            LocalAction.VOLUME_DOWN -> "O volume já está no mínimo."
        }
        is LocalActionResult.Fixed -> "O volume deste dispositivo é fixo."
        is LocalActionResult.Unavailable -> "Controle de volume indisponível."
        is LocalActionResult.Denied -> "Sem permissão para ajustar o volume."
        is LocalActionResult.Failure -> "Não consegui confirmar o ajuste de volume."
    }

    private fun normalize(input: String): String = Normalizer
        .normalize(input, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase(Locale.ROOT)
        .replace(PUNCTUATION_OR_SYMBOLS, " ")
        .trim()
        .replace(WHITESPACE, " ")

    private companion object {
        const val ASSISTANT_NAME = "delamain"

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

        val COMBINING_MARKS = Regex("\\p{M}+")
        val PUNCTUATION_OR_SYMBOLS = Regex("[\\p{P}\\p{S}]+")
        val WHITESPACE = Regex("\\s+")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
