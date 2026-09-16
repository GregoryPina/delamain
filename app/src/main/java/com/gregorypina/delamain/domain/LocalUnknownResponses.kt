package com.gregorypina.delamain.domain

object LocalUnknownResponses {
    private val variants = listOf(
        "Comando local não reconhecido.",
        "Não entendi esse comando local.",
        "Esse pedido não está no meu catálogo local.",
    )
    private var nextIndex = 0

    @Synchronized
    fun next(): String {
        val index = nextIndex % variants.size
        nextIndex = (index + 1) % variants.size
        return variants[index]
    }
}
