package com.gregorypina.delamain.domain

sealed interface LocalAction {
    data object VolumeUp : LocalAction
    data object VolumeDown : LocalAction
    data object MediaNext : LocalAction
    data object MediaPrevious : LocalAction
    data class OpenApp(
        val packageName: String,
        val displayName: String,
    ) : LocalAction
}

fun interface LocalActionPort {
    fun execute(action: LocalAction): LocalActionResult
}

sealed interface LocalActionResult {
    val action: LocalAction

    data class Changed(
        override val action: LocalAction,
        val before: Int,
        val after: Int,
    ) : LocalActionResult

    data class AtLimit(
        override val action: LocalAction,
        val level: Int,
    ) : LocalActionResult

    data class Fixed(override val action: LocalAction) : LocalActionResult
    data class Unavailable(override val action: LocalAction) : LocalActionResult
    data class Denied(override val action: LocalAction) : LocalActionResult
    data class Failure(override val action: LocalAction) : LocalActionResult

    data class Launched(
        override val action: LocalAction,
        val displayName: String,
    ) : LocalActionResult

    data class NotInstalled(
        override val action: LocalAction,
        val displayName: String,
    ) : LocalActionResult

    data class Dispatched(override val action: LocalAction) : LocalActionResult
}

object UnavailableLocalActionPort : LocalActionPort {
    override fun execute(action: LocalAction): LocalActionResult =
        LocalActionResult.Unavailable(action)
}
