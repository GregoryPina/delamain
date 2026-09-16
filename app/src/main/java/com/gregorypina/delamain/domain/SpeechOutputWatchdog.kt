package com.gregorypina.delamain.domain

fun interface SpeechOutputWatchdogScheduler {
    fun schedule(delayMs: Long, action: () -> Unit): () -> Unit
}

/** Cancelable timers for TTS start and completion. Never reports success on timeout. */
class SpeechOutputWatchdog(
    private val scheduler: SpeechOutputWatchdogScheduler,
    private val startTimeoutMs: Long = DEFAULT_START_TIMEOUT_MS,
    private val minCompletionMs: Long = DEFAULT_MIN_COMPLETION_MS,
    private val maxCompletionMs: Long = DEFAULT_MAX_COMPLETION_MS,
    private val msPerCharacter: Long = DEFAULT_MS_PER_CHARACTER,
) {
    data class Token(val utteranceId: String, val interactionId: Long)

    private var cancelStart: (() -> Unit)? = null
    private var cancelCompletion: (() -> Unit)? = null
    private var active: Token? = null

    fun armQueued(token: Token, onTimeout: () -> Unit) {
        disarm()
        active = token
        cancelStart = scheduler.schedule(startTimeoutMs) {
            if (active == token) onTimeout()
        }
    }

    fun armSpeaking(token: Token, textLength: Int, onTimeout: () -> Unit) {
        cancelStart?.invoke()
        cancelStart = null
        val delay = completionLimitMs(textLength, minCompletionMs, maxCompletionMs, msPerCharacter)
        cancelCompletion = scheduler.schedule(delay) {
            if (active == token) onTimeout()
        }
    }

    fun disarm() {
        cancelStart?.invoke()
        cancelCompletion?.invoke()
        cancelStart = null
        cancelCompletion = null
        active = null
    }

    companion object {
        const val DEFAULT_START_TIMEOUT_MS = 10_000L
        const val DEFAULT_MIN_COMPLETION_MS = 5_000L
        const val DEFAULT_MAX_COMPLETION_MS = 60_000L
        const val DEFAULT_MS_PER_CHARACTER = 100L

        fun completionLimitMs(
            textLength: Int,
            minCompletionMs: Long = DEFAULT_MIN_COMPLETION_MS,
            maxCompletionMs: Long = DEFAULT_MAX_COMPLETION_MS,
            msPerCharacter: Long = DEFAULT_MS_PER_CHARACTER,
        ): Long = (textLength.coerceAtLeast(1) * msPerCharacter).coerceIn(minCompletionMs, maxCompletionMs)
    }
}
