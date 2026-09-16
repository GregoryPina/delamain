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
    private val onState: (SpeechOutputState, Long) -> Unit = { _, _ -> },
    private val watchdog: SpeechOutputWatchdog? = null,
) : SpeechOutputPort {
    var state = SpeechOutputState.Preparing
        private set
    private var initialized = false
    private var ready = false
    private var closed = false
    private var sequence = 0L
    private var activeId: String? = null
    private var activeInteractionId: Long = 0L
    private var activeTextLength = 0

    fun initialized(available: Boolean) {
        if (closed || initialized) return
        initialized = true
        ready = available
        publish(if (available) SpeechOutputState.Ready else SpeechOutputState.Unavailable, 0L)
    }

    /** Called after stopping output and applying a voice selection on the engine. */
    fun voiceAvailabilityChanged(available: Boolean) {
        if (closed) return
        watchdog?.disarm()
        activeId = null
        initialized = true
        ready = available
        publish(if (available) SpeechOutputState.Ready else SpeechOutputState.Unavailable, 0L)
    }

    override fun speak(text: String, interactionId: Long): SpeechOutputResult {
        if (closed || !ready) return SpeechOutputResult.Unavailable
        watchdog?.disarm()
        activeId = null
        if (!attempt { engine.stop() } || text.isBlank()) {
            publish(SpeechOutputState.Failed, interactionId)
            return SpeechOutputResult.Failed
        }
        val id = "vexa-${++sequence}"
        activeId = id
        activeInteractionId = interactionId
        activeTextLength = text.length
        publish(SpeechOutputState.Queued, interactionId)
        armQueuedWatchdog(id, interactionId)
        if (!attempt { engine.enqueue(text, id) }) {
            watchdog?.disarm()
            activeId = null
            publish(SpeechOutputState.Failed, interactionId)
            return SpeechOutputResult.Failed
        }
        return SpeechOutputResult.Queued
    }

    fun started(id: String?) {
        if (!isCurrent(id)) return
        watchdog?.armSpeaking(
            SpeechOutputWatchdog.Token(id!!, activeInteractionId),
            activeTextLength,
            onTimeout = { failCurrent(SpeechOutputState.Failed) },
        )
        if (state == SpeechOutputState.Queued) publish(SpeechOutputState.Speaking, activeInteractionId)
    }

    fun completed(id: String?) = finish(id, SpeechOutputState.Completed)
    fun failed(id: String?) = finish(id, SpeechOutputState.Failed)
    fun stopped(id: String?) = finish(id, SpeechOutputState.Stopped)

    override fun stop(): Boolean {
        if (closed) return true
        watchdog?.disarm()
        val hadRequest = activeId != null
        activeId = null
        val accepted = attempt { engine.stop() }
        if (!accepted) publish(SpeechOutputState.Failed, activeInteractionId)
        else if (hadRequest) publish(SpeechOutputState.Stopped, activeInteractionId)
        return accepted
    }

    override fun shutdown() {
        if (closed) return
        closed = true
        watchdog?.disarm()
        ready = false
        activeId = null
        attempt { engine.stop() }
        attempt { engine.shutdown(); true }
        state = SpeechOutputState.Closed
    }

    private fun finish(id: String?, next: SpeechOutputState) {
        if (!isCurrent(id)) return
        watchdog?.disarm()
        activeId = null
        publish(next, activeInteractionId)
    }

    private fun failCurrent(next: SpeechOutputState) {
        if (activeId == null || closed) return
        watchdog?.disarm()
        activeId = null
        publish(next, activeInteractionId)
    }

    private fun armQueuedWatchdog(utteranceId: String, interactionId: Long) {
        watchdog?.armQueued(
            SpeechOutputWatchdog.Token(utteranceId, interactionId),
            onTimeout = {
                if (isCurrent(utteranceId) && state == SpeechOutputState.Queued) {
                    failCurrent(SpeechOutputState.Failed)
                }
            },
        )
    }

    private fun isCurrent(id: String?) = !closed && id != null && id == activeId

    private fun publish(next: SpeechOutputState, interactionId: Long) {
        state = next
        onState(next, interactionId)
    }

    private fun attempt(action: () -> Boolean): Boolean = try {
        action()
    } catch (_: RuntimeException) {
        false
    }
}
