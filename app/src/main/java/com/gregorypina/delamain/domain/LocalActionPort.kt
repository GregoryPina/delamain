package com.gregorypina.delamain.domain

enum class LocalAction {
    VOLUME_UP,
    VOLUME_DOWN,
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
}

object UnavailableLocalActionPort : LocalActionPort {
    override fun execute(action: LocalAction): LocalActionResult =
        LocalActionResult.Unavailable(action)
}
