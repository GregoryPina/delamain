package com.gregorypina.delamain.domain

enum class InteractionControlIntent {
    STOP_SPEECH,
    CANCEL,
    ENABLE_MUTE,
    DISABLE_MUTE,
}

object InteractionControlRecognizer {
    fun recognize(input: String): InteractionControlIntent? {
        val phrase = CommandInputNormalizer.stripOptionalPrefix(
            CommandInputNormalizer.normalize(input),
        )
        return when (phrase) {
            in STOP_SPEECH_PHRASES -> InteractionControlIntent.STOP_SPEECH
            in CANCEL_PHRASES -> InteractionControlIntent.CANCEL
            in ENABLE_MUTE_PHRASES -> InteractionControlIntent.ENABLE_MUTE
            in DISABLE_MUTE_PHRASES -> InteractionControlIntent.DISABLE_MUTE
            else -> null
        }
    }

    private val STOP_SPEECH_PHRASES = setOf(
        "pare de falar",
        "parar de falar",
    )
    private val CANCEL_PHRASES = setOf(
        "cancelar",
        "cancela",
    )
    private val ENABLE_MUTE_PHRASES = setOf(
        "modo mute",
        "silenciar voz",
        "desativar voz",
    )
    private val DISABLE_MUTE_PHRASES = setOf(
        "ativar voz",
        "sair do mute",
        "respostas por voz",
    )
}
