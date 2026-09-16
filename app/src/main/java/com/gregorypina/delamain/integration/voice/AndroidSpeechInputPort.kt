package com.gregorypina.delamain.integration.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.gregorypina.delamain.domain.SpeechInputEngine
import com.gregorypina.delamain.domain.SpeechInputPort
import com.gregorypina.delamain.domain.SpeechInputSession
import com.gregorypina.delamain.domain.SpeechInputState

/** Explicit, single-shot recognition. Never uses the default (possibly remote) recognizer. */
class AndroidSpeechInputPort(
    context: Context,
    stopOutput: () -> Boolean,
    onState: (SpeechInputState) -> Unit,
    onText: (String) -> Unit,
) : SpeechInputPort {
    private val applicationContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var timeout: Runnable? = null
    private var closed = false
    private val session = SpeechInputSession(object : SpeechInputEngine {
        override fun available() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(applicationContext)
        override fun hasPermission() = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        override fun start(requestId: Long) = startRecognizer(requestId)
        override fun release() = releaseRecognizer()
    }, stopOutput, onState, onText)

    override fun start() = onMain { session.start() }
    override fun cancel() = onMain { session.cancel() }
    override fun shutdown() = onMain {
        if (!closed) {
            closed = true
            session.shutdown()
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun startRecognizer(id: Long) {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val instance = SpeechRecognizer.createOnDeviceSpeechRecognizer(applicationContext)
        recognizer = instance
        instance.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = dispatch { session.listening(id) }
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = dispatch { session.processing(id) }
            override fun onError(error: Int) = dispatch {
                session.error(id, when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> SpeechInputState.NoMatch
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechInputState.TimedOut
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechInputState.PermissionRequired
                    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
                    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> SpeechInputState.Unavailable
                    else -> SpeechInputState.Failed
                })
            }
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                dispatch { session.result(id, text) }
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        timeout = Runnable { if (!closed) session.error(id, SpeechInputState.TimedOut) }
            .also { handler.postDelayed(it, 15_000L) }
        instance.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        })
    }

    private fun releaseRecognizer() {
        timeout?.let { handler.removeCallbacks(it) }
        timeout = null
        val instance = recognizer
        recognizer = null
        try { instance?.cancel() } finally { instance?.destroy() }
    }
    private fun dispatch(action: () -> Unit) { handler.post { if (!closed) action() } }
    private inline fun <T> onMain(action: () -> T): T {
        check(Looper.myLooper() == Looper.getMainLooper())
        return action()
    }
}
