package com.gregorypina.delamain.integration.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.gregorypina.delamain.domain.BatteryStatus
import com.gregorypina.delamain.domain.BatteryStatusPort

class AndroidBatteryStatusPort private constructor(
    private val context: Context?,
) : BatteryStatusPort {
    override fun read(): BatteryStatus? {
        val appContext = context ?: return null
        val batteryStatus = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        ) ?: return null

        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null

        val percent = level * 100 / scale
        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        return BatteryStatus(
            levelPercent = percent.coerceIn(0, 100),
            isCharging = isCharging,
        )
    }

    companion object {
        fun from(context: Context): AndroidBatteryStatusPort =
            AndroidBatteryStatusPort(context.applicationContext)
    }
}
