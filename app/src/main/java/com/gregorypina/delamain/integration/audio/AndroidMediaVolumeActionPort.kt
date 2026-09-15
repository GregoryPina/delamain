package com.gregorypina.delamain.integration.audio

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.gregorypina.delamain.domain.LocalAction
import com.gregorypina.delamain.domain.LocalActionPort
import com.gregorypina.delamain.domain.LocalActionResult

class AndroidMediaVolumeActionPort private constructor(
    private val audioManager: AudioManager?,
) : LocalActionPort {
    override fun execute(action: LocalAction): LocalActionResult {
        val manager = audioManager ?: return LocalActionResult.Unavailable(action)

        return try {
            if (manager.isVolumeFixed) {
                return LocalActionResult.Fixed(action)
            }

            val before = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val limit = when (action) {
                LocalAction.VOLUME_UP -> manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                LocalAction.VOLUME_DOWN -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    manager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
                } else {
                    0
                }
            }
            val atLimit = when (action) {
                LocalAction.VOLUME_UP -> before >= limit
                LocalAction.VOLUME_DOWN -> before <= limit
            }
            if (atLimit) {
                return LocalActionResult.AtLimit(action, before)
            }

            val direction = when (action) {
                LocalAction.VOLUME_UP -> AudioManager.ADJUST_RAISE
                LocalAction.VOLUME_DOWN -> AudioManager.ADJUST_LOWER
            }
            manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
            val after = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val changedAsRequested = when (action) {
                LocalAction.VOLUME_UP -> after > before
                LocalAction.VOLUME_DOWN -> after < before
            }

            if (changedAsRequested) {
                LocalActionResult.Changed(action, before, after)
            } else {
                LocalActionResult.Failure(action)
            }
        } catch (_: SecurityException) {
            LocalActionResult.Denied(action)
        } catch (_: RuntimeException) {
            LocalActionResult.Failure(action)
        }
    }

    companion object {
        fun from(context: Context): AndroidMediaVolumeActionPort =
            AndroidMediaVolumeActionPort(
                context.applicationContext.getSystemService(AudioManager::class.java),
            )
    }
}
