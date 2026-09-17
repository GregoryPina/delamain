package com.gregorypina.delamain.domain

enum class AudioFocusMode {
    SPEECH_OUTPUT,
    SPEECH_INPUT,
}

enum class AudioFocusRequestResult {
    GRANTED,
    DENIED,
    FAILED,
}

/** Acquires transient audio focus for explicit speech I/O; must be released after use. */
interface AudioFocusPort {
    fun request(mode: AudioFocusMode, onLoss: () -> Unit): AudioFocusRequestResult
    fun abandon()
    fun isHeld(): Boolean
}

object UnavailableAudioFocusPort : AudioFocusPort {
    override fun request(mode: AudioFocusMode, onLoss: () -> Unit) = AudioFocusRequestResult.FAILED
    override fun abandon() = Unit
    override fun isHeld() = false
}

/** Serializes focus acquire/abandon and forwards permanent or transient loss. */
class AudioFocusSession(
    private val port: AudioFocusPort,
    private val onFocusLost: () -> Unit,
) {
    private var heldMode: AudioFocusMode? = null
    private var generation = 0L

    fun requestForSpeech(): AudioFocusRequestResult = request(AudioFocusMode.SPEECH_OUTPUT)

    fun requestForListening(): AudioFocusRequestResult = request(AudioFocusMode.SPEECH_INPUT)

    fun abandon() {
        generation += 1
        if (heldMode != null || port.isHeld()) {
            port.abandon()
            heldMode = null
        }
    }

    fun isHeld(): Boolean = heldMode != null

    private fun request(mode: AudioFocusMode): AudioFocusRequestResult {
        abandon()
        val requestGeneration = generation
        val result = port.request(mode) {
            if (requestGeneration == generation) handleLoss()
        }
        if (result == AudioFocusRequestResult.GRANTED) heldMode = mode
        return result
    }

    private fun handleLoss() {
        if (heldMode == null && !port.isHeld()) return
        generation += 1
        heldMode = null
        port.abandon()
        onFocusLost()
    }
}
