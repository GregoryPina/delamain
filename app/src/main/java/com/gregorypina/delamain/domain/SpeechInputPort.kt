package com.gregorypina.delamain.domain

enum class SpeechInputState {
    Idle, Starting, Listening, Processing, Completed, Canceled, Unavailable,
    PermissionRequired, Failed, NoMatch, TimedOut, Closed,
}
enum class SpeechInputStartResult { Started, Busy, Unavailable, PermissionRequired, Failed }

interface SpeechInputPort {
    fun start(): SpeechInputStartResult
    fun cancel()
    fun shutdown()
}

/** All calls and events are serialized by the platform adapter. */
interface SpeechInputEngine {
    fun available(): Boolean
    fun hasPermission(): Boolean
    fun start(requestId: Long)
    fun release()
}

class SpeechInputSession(
    private val engine: SpeechInputEngine,
    private val stopOutput: () -> Boolean,
    private val onState: (SpeechInputState) -> Unit,
    private val onText: (String) -> Unit,
) : SpeechInputPort {
    var state = SpeechInputState.Idle
        private set
    private var closed = false
    private var sequence = 0L
    private var active: Long? = null

    override fun start(): SpeechInputStartResult {
        if (closed) return SpeechInputStartResult.Unavailable
        if (active != null) return SpeechInputStartResult.Busy
        try {
            if (!engine.available()) {
                publish(SpeechInputState.Unavailable)
                return SpeechInputStartResult.Unavailable
            }
            if (!engine.hasPermission()) {
                publish(SpeechInputState.PermissionRequired)
                return SpeechInputStartResult.PermissionRequired
            }
            if (!stopOutput()) {
                publish(SpeechInputState.Failed)
                return SpeechInputStartResult.Failed
            }
            val id = ++sequence
            active = id
            publish(SpeechInputState.Starting)
            engine.start(id)
            return SpeechInputStartResult.Started
        } catch (_: RuntimeException) {
            active = null
            release()
            publish(SpeechInputState.Failed)
            return SpeechInputStartResult.Failed
        }
    }

    fun listening(id: Long) {
        if (current(id) && state == SpeechInputState.Starting) publish(SpeechInputState.Listening)
    }
    fun processing(id: Long) {
        if (current(id)) publish(SpeechInputState.Processing)
    }
    fun result(id: Long, text: String?) {
        if (!current(id)) return
        active = null
        release()
        val finalText = text?.trim().orEmpty()
        if (finalText.isEmpty()) publish(SpeechInputState.NoMatch)
        else {
            publish(SpeechInputState.Completed)
            onText(finalText)
        }
    }
    fun error(id: Long, reason: SpeechInputState = SpeechInputState.Failed) {
        if (!current(id)) return
        active = null
        release()
        publish(reason)
    }
    override fun cancel() {
        if (closed || active == null) return
        active = null
        release()
        publish(SpeechInputState.Canceled)
    }
    override fun shutdown() {
        if (closed) return
        closed = true
        active = null
        release()
        state = SpeechInputState.Closed
    }
    private fun current(id: Long) = !closed && active == id
    private fun publish(next: SpeechInputState) { state = next; onState(next) }
    private fun release() {
        try { engine.release() } catch (_: RuntimeException) { /* Terminal callbacks stay invalidated. */ }
    }
}
