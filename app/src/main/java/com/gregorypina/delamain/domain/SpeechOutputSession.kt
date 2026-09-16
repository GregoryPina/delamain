package com.gregorypina.delamain.domain

/** Platform calls and callbacks must be serialized on the same thread. */
interface SpeechOutputEngine {
    fun enqueue(text: String, utteranceId: String): Boolean
    fun stop(): Boolean
    fun shutdown()
}

/** Tracks one utterance. Superseded callbacks cannot change the current response. */
class SpeechOutputSession(
    private val engine: SpeechOutputEngine,
    private val onState: (SpeechOutputState) -> Unit = {},
) : SpeechOutputPort {
    var state = SpeechOutputState.Preparing
        private set
    private var initialized = false
    private var ready = false
    private var closed = false
    private var sequence = 0L
    private var activeId: String? = null

    fun initialized(available: Boolean) {
        if (closed || initialized) return
        initialized = true
        ready = available
        publish(if (available) SpeechOutputState.Ready else SpeechOutputState.Unavailable)
    }

    override fun speak(text: String): SpeechOutputResult {
        if (closed || !ready) return SpeechOutputResult.Unavailable
        // Invalidate before touching the engine: stop can itself trigger callbacks.
        activeId = null
        if (!attempt { engine.stop() } || text.isBlank()) {
            publish(SpeechOutputState.Failed)
            return SpeechOutputResult.Failed
        }
        val id = "vexa-${++sequence}"
        activeId = id
        publish(SpeechOutputState.Queued)
        if (!attempt { engine.enqueue(text, id) }) {
            activeId = null
            publish(SpeechOutputState.Failed)
            return SpeechOutputResult.Failed
        }
        return SpeechOutputResult.Queued
    }

    fun started(id: String?) {
        if (isCurrent(id) && state == SpeechOutputState.Queued) publish(SpeechOutputState.Speaking)
    }

    fun completed(id: String?) = finish(id, SpeechOutputState.Completed)
    fun failed(id: String?) = finish(id, SpeechOutputState.Failed)
    fun stopped(id: String?) = finish(id, SpeechOutputState.Stopped)

    override fun stop(): Boolean {
        if (closed) return true
        val hadRequest = activeId != null
        activeId = null
        val accepted = attempt { engine.stop() }
        if (!accepted) publish(SpeechOutputState.Failed)
        else if (hadRequest) publish(SpeechOutputState.Stopped)
        return accepted
    }

    override fun shutdown() {
        if (closed) return
        closed = true
        ready = false
        activeId = null
        // Release even if stopping fails; no observer calls after disposal.
        attempt { engine.stop() }
        attempt { engine.shutdown(); true }
        state = SpeechOutputState.Closed
    }

    private fun finish(id: String?, next: SpeechOutputState) {
        if (!isCurrent(id)) return
        activeId = null
        publish(next)
    }

    private fun isCurrent(id: String?) = !closed && id != null && id == activeId

    private fun publish(next: SpeechOutputState) {
        state = next
        onState(next)
    }

    private fun attempt(action: () -> Boolean): Boolean = try {
        action()
    } catch (_: RuntimeException) {
        false
    }
}
