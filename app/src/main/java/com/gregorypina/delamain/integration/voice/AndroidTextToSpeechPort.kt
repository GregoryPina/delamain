package com.gregorypina.delamain.integration.voice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.gregorypina.delamain.domain.SpeechOutputEngine
import com.gregorypina.delamain.domain.SpeechOutputPort
import com.gregorypina.delamain.domain.SpeechOutputSession
import com.gregorypina.delamain.domain.SpeechOutputState
import java.util.Locale

/** Construct and call on the main thread; binder callbacks are posted to that thread. */
class AndroidTextToSpeechPort(
    context: Context,
    onState: (SpeechOutputState) -> Unit = {},
) : SpeechOutputPort {
    private val handler = Handler(Looper.getMainLooper())
    private var textToSpeech: TextToSpeech? = null
    private var closed = false
    private val session = SpeechOutputSession(object : SpeechOutputEngine {
        override fun enqueue(text: String, utteranceId: String): Boolean {
            val tts = textToSpeech ?: return false
            if (text.length > TextToSpeech.getMaxSpeechInputLength()) return false
            return tts.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId) == TextToSpeech.SUCCESS
        }
        override fun stop() = textToSpeech?.stop()?.let { it == TextToSpeech.SUCCESS } ?: true
        override fun shutdown() {
            val tts = textToSpeech
            textToSpeech = null
            tts?.shutdown()
        }
    }, onState)

    init {
        checkMainThread()
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                // Posting also prevents an early onInit from accessing an unassigned TTS instance.
                handler.post {
                    if (!closed) {
                        val available = try {
                            status == TextToSpeech.SUCCESS && configureVoice()
                        } catch (_: RuntimeException) {
                            false
                        }
                        session.initialized(available)
                    }
                }
            }
        } catch (_: RuntimeException) {
            handler.post { if (!closed) session.initialized(false) }
        }
    }

    override fun speak(text: String) = onMain { session.speak(text) }
    override fun stop() = onMain { session.stop() }

    override fun shutdown() = onMain {
        if (!closed) {
            closed = true
            handler.removeCallbacksAndMessages(null)
            session.shutdown()
        }
    }

    private fun configureVoice(): Boolean {
        val tts = textToSpeech ?: return false
        val locale = Locale.forLanguageTag("pt-BR")
        if (tts.isLanguageAvailable(locale) < TextToSpeech.LANG_COUNTRY_AVAILABLE) return false
        val voice = tts.voices.orEmpty()
            .filter {
                it.locale.language == "pt" && it.locale.country == "BR" &&
                    !it.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
            }
            .sortedWith(
                compareByDescending<android.speech.tts.Voice> { it.name == tts.voice?.name }
                    .thenByDescending { it.quality }.thenBy { it.name },
            )
            .firstOrNull() ?: return false
        if (tts.setVoice(voice) != TextToSpeech.SUCCESS) return false
        if (tts.voice?.name != voice.name || tts.voice?.isNetworkConnectionRequired != false) return false
        return tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = dispatch { session.started(utteranceId) }
            override fun onDone(utteranceId: String?) = dispatch { session.completed(utteranceId) }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = dispatch { session.failed(utteranceId) }
            override fun onError(utteranceId: String?, errorCode: Int) = dispatch { session.failed(utteranceId) }
            override fun onStop(utteranceId: String?, interrupted: Boolean) = dispatch { session.stopped(utteranceId) }
        }) == TextToSpeech.SUCCESS
    }

    private fun dispatch(action: () -> Unit) {
        handler.post { if (!closed) action() }
    }

    private fun checkMainThread() = check(Looper.myLooper() == Looper.getMainLooper())
    private inline fun <T> onMain(action: () -> T): T {
        checkMainThread()
        return action()
    }
}
