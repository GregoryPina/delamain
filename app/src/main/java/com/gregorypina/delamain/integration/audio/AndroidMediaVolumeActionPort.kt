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
    override fun execute(action: LocalAction): LocalActionResult = when (action) {
        LocalAction.VolumeUp,
        LocalAction.VolumeDown,
        -> executeVolume(action)
        else -> LocalActionResult.Unavailable(action)
    }

    private fun executeVolume(action: LocalAction): LocalActionResult {
        val manager = audioManager ?: return LocalActionResult.Unavailable(action)

        return try {
            if (manager.isVolumeFixed) {
                return LocalActionResult.Fixed(action)
            }

            val before = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val limit = when (action) {
                LocalAction.VolumeUp -> manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                LocalAction.VolumeDown -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    manager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
                } else {
                    0
                }
                else -> error("Volume port does not handle $action")
            }
            val atLimit = when (action) {
                LocalAction.VolumeUp -> before >= limit
                LocalAction.VolumeDown -> before <= limit
                else -> error("Volume port does not handle $action")
            }
            if (atLimit) {
                return LocalActionResult.AtLimit(action, before)
            }

            val direction = when (action) {
                LocalAction.VolumeUp -> AudioManager.ADJUST_RAISE
                LocalAction.VolumeDown -> AudioManager.ADJUST_LOWER
                else -> error("Volume port does not handle $action")
            }
            manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
            val after = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val changedAsRequested = when (action) {
                LocalAction.VolumeUp -> after > before
                LocalAction.VolumeDown -> after < before
                else -> error("Volume port does not handle $action")
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
