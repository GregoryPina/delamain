package com.gregorypina.delamain.integration.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.gregorypina.delamain.domain.SpeechOutputPort
import com.gregorypina.delamain.domain.SpeechOutputResult
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class AndroidTextToSpeechPort(
    context: Context,
    onReady: (AndroidTextToSpeechPort) -> Unit = {},
) : SpeechOutputPort {
    private val textToSpeech: TextToSpeech
    private val ready = AtomicBoolean(false)

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureLanguage()
                ready.set(true)
            }
            onReady(this)
        }
        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) = Unit

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) = Unit
            },
        )
    }

    override fun speak(text: String): SpeechOutputResult {
        if (!ready.get()) return SpeechOutputResult.Unavailable
        if (text.isBlank()) return SpeechOutputResult.Failed

        val utteranceId = "vexa-${System.nanoTime()}"
        val result = textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            Bundle(),
            utteranceId,
        )
        return if (result == TextToSpeech.SUCCESS) {
            SpeechOutputResult.Spoken
        } else {
            SpeechOutputResult.Failed
        }
    }

    override fun stop() {
        if (ready.get()) {
            textToSpeech.stop()
        }
    }

    override fun shutdown() {
        ready.set(false)
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private fun configureLanguage() {
        val brazilian = Locale.forLanguageTag("pt-BR")
        if (textToSpeech.isLanguageAvailable(brazilian) >= TextToSpeech.LANG_AVAILABLE) {
            textToSpeech.language = brazilian
        }
    }
}
