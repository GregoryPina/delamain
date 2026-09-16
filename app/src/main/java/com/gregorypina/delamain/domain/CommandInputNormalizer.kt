package com.gregorypina.delamain.domain

import java.text.Normalizer
import java.util.Locale

internal object CommandInputNormalizer {
    const val ASSISTANT_NAME = "vexa"

    fun normalize(input: String): String = Normalizer
        .normalize(input, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase(Locale.ROOT)
        .replace(PUNCTUATION_OR_SYMBOLS, " ")
        .trim()
        .replace(WHITESPACE, " ")

    fun stripOptionalPrefix(phrase: String): String =
        phrase.removePrefix("$ASSISTANT_NAME ")

    private val COMBINING_MARKS = Regex("\\p{M}+")
    private val PUNCTUATION_OR_SYMBOLS = Regex("[\\p{P}\\p{S}]+")
    private val WHITESPACE = Regex("\\s+")
}
