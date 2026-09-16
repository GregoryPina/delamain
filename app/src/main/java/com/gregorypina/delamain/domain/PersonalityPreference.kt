package com.gregorypina.delamain.domain

enum class PersonalityTone {
    WARM,
    DIRECT,
}

data class PersonalityConfig(
    val displayName: String? = null,
    val tone: PersonalityTone = PersonalityTone.WARM,
) {
    companion object {
        const val MAX_NAME_LENGTH = 40

        fun sanitize(rawName: String?, tone: PersonalityTone = PersonalityTone.WARM): PersonalityConfig {
            val trimmed = rawName?.trim()?.takeIf { it.isNotEmpty() }
            val name = when {
                trimmed == null -> null
                trimmed.any(Char::isISOControl) -> null
                trimmed.length > MAX_NAME_LENGTH -> trimmed.take(MAX_NAME_LENGTH)
                else -> trimmed
            }
            return PersonalityConfig(name, tone)
        }
    }
}

data class PersonalityPreference(
    val displayName: String? = null,
    val tone: PersonalityTone = PersonalityTone.WARM,
) {
    fun toConfig(): PersonalityConfig = PersonalityConfig.sanitize(displayName, tone)
}

sealed interface PersonalityPreferenceReadResult {
    data class Found(val preference: PersonalityPreference) : PersonalityPreferenceReadResult
    data object Empty : PersonalityPreferenceReadResult
    data object Failed : PersonalityPreferenceReadResult
}

object PersonalityPreview {
    fun sample(config: PersonalityConfig): String =
        LocalPhraseBank.presenceVariants(config.tone, config.displayName).first()
}
