package com.gregorypina.delamain.domain

sealed interface LocalCommandResult {
    data class Recognized(
        val intent: LocalIntent,
        val response: String,
        val actionResult: LocalActionResult? = null,
    ) : LocalCommandResult

    data object Unknown : LocalCommandResult
}
