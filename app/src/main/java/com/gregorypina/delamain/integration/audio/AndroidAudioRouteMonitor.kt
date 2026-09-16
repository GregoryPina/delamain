package com.gregorypina.delamain.integration.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper

/**
 * Notifies when the active route may have changed during speech I/O.
 * Does not restart capture or replay responses automatically.
 */
class AndroidAudioRouteMonitor(
    context: Context,
    private val isActive: () -> Boolean,
    private val onRouteDisrupted: () -> Unit,
) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var started = false

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY && isActive()) {
                onRouteDisrupted()
            }
        }
    }

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            if (!isActive()) return
            val removedRoutable = removedDevices.any { device ->
                device.type in ROUTABLE_DEVICE_TYPES
            }
            if (removedRoutable) onRouteDisrupted()
        }
    }

    fun start() {
        if (started) return
        started = true
        appContext.registerReceiver(
            becomingNoisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
        )
        audioManager?.registerAudioDeviceCallback(deviceCallback, handler)
    }

    fun stop() {
        if (!started) return
        started = false
        try {
            appContext.unregisterReceiver(becomingNoisyReceiver)
        } catch (_: IllegalArgumentException) {
            // Already unregistered.
        }
        audioManager?.unregisterAudioDeviceCallback(deviceCallback)
    }

    private companion object {
        val ROUTABLE_DEVICE_TYPES = setOf(
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE,
        )
    }
}
