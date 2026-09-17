package com.gregorypina.delamain.integration.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.gregorypina.delamain.domain.AudioFocusMode
import com.gregorypina.delamain.domain.AudioFocusPort
import com.gregorypina.delamain.domain.AudioFocusRequestResult

/**
 * Transient focus for explicit TTS/STT only. Uses AudioFocusRequest (API 26+, minSdk 26).
 * Does not pause or resume third-party players manually.
 */
class AndroidAudioFocusPort(context: Context) : AudioFocusPort {
    private val audioManager = context.applicationContext.getSystemService(AudioManager::class.java)
    private var activeRequest: AudioFocusRequest? = null
    private var lossCallback: (() -> Unit)? = null
    private var generation = 0L

    override fun request(mode: AudioFocusMode, onLoss: () -> Unit): AudioFocusRequestResult {
        abandon()
        val manager = audioManager ?: return AudioFocusRequestResult.FAILED
        lossCallback = onLoss
        val requestGeneration = generation
        val attributes = when (mode) {
            AudioFocusMode.SPEECH_OUTPUT -> AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            AudioFocusMode.SPEECH_INPUT -> AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        }
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener { change ->
                if (requestGeneration != generation) return@setOnAudioFocusChangeListener
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
                    -> lossCallback?.invoke()
                }
            }
            .build()
        return when (manager.requestAudioFocus(request)) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                activeRequest = request
                AudioFocusRequestResult.GRANTED
            }
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> AudioFocusRequestResult.FAILED
            else -> AudioFocusRequestResult.DENIED
        }
    }

    override fun abandon() {
        generation += 1
        val manager = audioManager
        val request = activeRequest
        activeRequest = null
        lossCallback = null
        if (manager != null && request != null) {
            manager.abandonAudioFocusRequest(request)
        }
    }

    override fun isHeld(): Boolean = activeRequest != null
}
