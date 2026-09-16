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

    fun requestForSpeech(): AudioFocusRequestResult = request(AudioFocusMode.SPEECH_OUTPUT)

    fun requestForListening(): AudioFocusRequestResult = request(AudioFocusMode.SPEECH_INPUT)

    fun abandon() {
        if (heldMode != null || port.isHeld()) {
            port.abandon()
            heldMode = null
        }
    }

    fun isHeld(): Boolean = heldMode != null

    private fun request(mode: AudioFocusMode): AudioFocusRequestResult {
        abandon()
        val result = port.request(mode) { handleLoss() }
        if (result == AudioFocusRequestResult.GRANTED) heldMode = mode
        return result
    }

    private fun handleLoss() {
        if (heldMode == null && !port.isHeld()) return
        heldMode = null
        port.abandon()
        onFocusLost()
    }
}
