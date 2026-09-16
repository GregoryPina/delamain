package com.gregorypina.delamain.domain

data class BatteryStatus(
    val levelPercent: Int,
    val isCharging: Boolean,
)

fun interface BatteryStatusPort {
    fun read(): BatteryStatus?
}

object UnavailableBatteryStatusPort : BatteryStatusPort {
    override fun read(): BatteryStatus? = null
}
