package com.gregorypina.delamain.integration.audio

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.gregorypina.delamain.domain.LocalAction
import com.gregorypina.delamain.domain.LocalActionPort
import com.gregorypina.delamain.domain.LocalActionResult

class AndroidMediaKeyActionPort private constructor(
    private val audioManager: AudioManager?,
) : LocalActionPort {
    override fun execute(action: LocalAction): LocalActionResult = when (action) {
        LocalAction.MediaNext -> dispatch(KeyEvent.KEYCODE_MEDIA_NEXT, action)
        LocalAction.MediaPrevious -> dispatch(KeyEvent.KEYCODE_MEDIA_PREVIOUS, action)
        else -> LocalActionResult.Unavailable(action)
    }

    private fun dispatch(keyCode: Int, action: LocalAction): LocalActionResult {
        val manager = audioManager ?: return LocalActionResult.Unavailable(action)

        return try {
            val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            manager.dispatchMediaKeyEvent(down)
            manager.dispatchMediaKeyEvent(up)
            LocalActionResult.Dispatched(action)
        } catch (_: RuntimeException) {
            LocalActionResult.Failure(action)
        }
    }

    companion object {
        fun from(context: Context): AndroidMediaKeyActionPort =
            AndroidMediaKeyActionPort(
                context.applicationContext.getSystemService(AudioManager::class.java),
            )
    }
}
