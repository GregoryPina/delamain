package com.gregorypina.delamain.domain

data class SpeechVoiceCandidate(
    val id: String,
    val language: String,
    val country: String,
    val requiresNetwork: Boolean,
    val installed: Boolean,
    val quality: Int,
)

data class SpeechVoiceSelection(
    val ids: List<String> = emptyList(),
    val selectedId: String? = null,
    val engineId: String? = null,
) {
    fun nextId(): String? {
        if (ids.isEmpty()) return null
        return ids[(ids.indexOf(selectedId) + 1) % ids.size]
    }
}

/** Eligibility is deliberately stricter than a language match alone. */
fun localBrazilianVoices(
    candidates: List<SpeechVoiceCandidate>,
    preferredId: String?,
): List<String> = candidates
    .filter { it.language == "pt" && it.country == "BR" && !it.requiresNetwork && it.installed }
    .sortedWith(compareByDescending<SpeechVoiceCandidate> { it.id == preferredId }
        .thenByDescending { it.quality }.thenBy { it.id })
    .map { it.id }
    .distinct()
