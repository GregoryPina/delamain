package com.gregorypina.delamain.domain

enum class SpeechOutputResult { Queued, Unavailable, Failed }

enum class SpeechOutputState {
    Preparing, Ready, Unavailable, Queued, Speaking, Completed, Stopped, Failed, Closed,
}

interface SpeechOutputPort {
    fun speak(text: String, interactionId: Long = 0L): SpeechOutputResult
    /** True means the engine accepted stop, not proof of silence on the physical output. */
    fun stop(): Boolean
    fun shutdown()
}

object UnavailableSpeechOutputPort : SpeechOutputPort {
    override fun speak(text: String, interactionId: Long) = SpeechOutputResult.Unavailable
    override fun stop() = true
    override fun shutdown() = Unit
}
