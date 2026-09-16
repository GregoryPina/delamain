package com.gregorypina.delamain.domain

enum class SpeechOutputResult {
    Spoken,
    Unavailable,
    Failed,
}

interface SpeechOutputPort {
    fun speak(text: String): SpeechOutputResult
    fun stop()
    fun shutdown()
}

object UnavailableSpeechOutputPort : SpeechOutputPort {
    override fun speak(text: String): SpeechOutputResult = SpeechOutputResult.Unavailable
    override fun stop() = Unit
    override fun shutdown() = Unit
}
