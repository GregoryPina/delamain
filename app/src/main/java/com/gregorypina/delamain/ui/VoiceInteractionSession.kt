package com.gregorypina.delamain.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gregorypina.delamain.domain.AudioFocusRequestResult
import com.gregorypina.delamain.domain.AudioFocusSession
import com.gregorypina.delamain.domain.InteractionControlIntent
import com.gregorypina.delamain.domain.InteractionControlRecognizer
import com.gregorypina.delamain.domain.InteractionCoordinator
import com.gregorypina.delamain.domain.LocalCommandEngine
import com.gregorypina.delamain.domain.LocalCommandResult
import com.gregorypina.delamain.domain.LocalUnknownResponses
import com.gregorypina.delamain.domain.PersonalityConfig
import com.gregorypina.delamain.domain.PersonalityPreference
import com.gregorypina.delamain.domain.PersonalityPreview
import com.gregorypina.delamain.domain.PersonalityTone
import com.gregorypina.delamain.domain.SpeechInputStartResult
import com.gregorypina.delamain.domain.SpeechInputState
import com.gregorypina.delamain.domain.SpeechOutputResult
import com.gregorypina.delamain.domain.SpeechOutputState
import com.gregorypina.delamain.domain.SpeechVoicePreferenceCoordinator
import com.gregorypina.delamain.domain.SpeechVoicePreferenceEvent
import com.gregorypina.delamain.domain.SpeechVoiceSelection
import com.gregorypina.delamain.integration.CompositeLocalActionPort
import com.gregorypina.delamain.integration.apps.AndroidLaunchAppActionPort
import com.gregorypina.delamain.integration.audio.AndroidAudioFocusPort
import com.gregorypina.delamain.integration.audio.AndroidAudioRouteMonitor
import com.gregorypina.delamain.integration.audio.AndroidMediaKeyActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaVolumeActionPort
import com.gregorypina.delamain.integration.system.AndroidBatteryStatusPort
import com.gregorypina.delamain.integration.voice.AndroidSpeechInputPort
import com.gregorypina.delamain.integration.voice.AndroidTextToSpeechPort
import com.gregorypina.delamain.integration.voice.DataStoreSpeechVoicePreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Single voice/command session shared by the main UI and the debug panel. */
class VoiceInteractionSession(
    private val appContext: Context,
    val interactionCoordinator: InteractionCoordinator,
    private val scope: CoroutineScope,
) {
    val engine: LocalCommandEngine = LocalCommandEngine(
        actionPort = CompositeLocalActionPort(
            volumePort = AndroidMediaVolumeActionPort.from(appContext),
            mediaKeyPort = AndroidMediaKeyActionPort.from(appContext),
            launchAppPort = AndroidLaunchAppActionPort.from(appContext),
        ),
        batteryStatusPort = AndroidBatteryStatusPort.from(appContext),
    )

    var speechState: SpeechOutputState by mutableStateOf(SpeechOutputState.Preparing)
        private set
    var inputState: SpeechInputState by mutableStateOf(SpeechInputState.Idle)
        private set
    var voiceSelection: SpeechVoiceSelection by mutableStateOf(SpeechVoiceSelection())
        private set
    var statusMessage: UserMessageKey? by mutableStateOf(null)
        private set
    var lastResponse: String? by mutableStateOf(null)
        private set
    var preferenceMessage: String? by mutableStateOf(null)
        private set
    var preferenceSaved: Boolean by mutableStateOf(false)
        private set
    var voiceMuted: Boolean by mutableStateOf(false)
        private set
    var personalityTone: PersonalityTone by mutableStateOf(PersonalityTone.WARM)
        private set
    var personalityDisplayName: String? by mutableStateOf(null)
        private set
    var personalityMessage: String? by mutableStateOf(null)
        private set
    var personalityPreview: String? by mutableStateOf(null)
        private set

    private val preferenceStore = DataStoreSpeechVoicePreferenceStore(appContext)
    val preferenceCoordinator = SpeechVoicePreferenceCoordinator(preferenceStore) { token, event ->
        if (token != preferenceSessionToken) return@SpeechVoicePreferenceCoordinator
        onPreferenceEvent(event)
    }

    lateinit var speechOutput: AndroidTextToSpeechPort
        private set
    lateinit var speechInput: AndroidSpeechInputPort
        private set
    private lateinit var audioFocusSession: AudioFocusSession
    private lateinit var audioRouteMonitor: AndroidAudioRouteMonitor

    private var preferenceSessionToken = 0L
    private var listenInteractionId by mutableLongStateOf(0L)
    private var voiceRestored = false
    private var started = false

    val listening: Boolean
        get() = inputState in setOf(
            SpeechInputState.Starting,
            SpeechInputState.Listening,
            SpeechInputState.Processing,
        )

    val speaking: Boolean
        get() = speechState == SpeechOutputState.Speaking ||
            speechState == SpeechOutputState.Queued

    fun start() {
        if (started) return
        started = true
        voiceRestored = false
        preferenceSessionToken = preferenceCoordinator.openSession()
        scope.launch {
            preferenceCoordinator.restoreMute(preferenceSessionToken)
            preferenceCoordinator.restorePersonality(preferenceSessionToken)
        }
        audioFocusSession = AudioFocusSession(AndroidAudioFocusPort(appContext)) {
            handleAudioInterruption(UserMessageKey.AudioFocusLost)
        }
        audioRouteMonitor = AndroidAudioRouteMonitor(
            appContext,
            isActive = { speaking || listening || audioFocusSession.isHeld() },
            onRouteDisrupted = { handleAudioInterruption(UserMessageKey.AudioRouteChanged) },
        )
        audioRouteMonitor.start()
        speechOutput = AndroidTextToSpeechPort(
            appContext,
            onState = { state, interactionId ->
                speechState = state
                interactionCoordinator.output(state, interactionId)
                if (state in SPEECH_FOCUS_TERMINAL_STATES) releaseAudioFocus()
            },
            onVoices = { voices ->
                voiceSelection = voices
                if (!voiceRestored && voices.engineId != null && voices.ids.isNotEmpty()) {
                    voiceRestored = true
                    scope.launch {
                        preferenceCoordinator.restore(
                            preferenceSessionToken,
                            voices.engineId!!,
                            voices.ids.toSet(),
                        )
                    }
                }
            },
        )
        speechInput = AndroidSpeechInputPort(
            appContext,
            stopOutput = { speechOutput.stop() },
            onState = { state ->
                inputState = state
                interactionCoordinator.input(state, listenInteractionId)
                updateStatusFromInput(state)
                if (state in INPUT_FOCUS_TERMINAL_STATES) releaseAudioFocus()
            },
            onText = { text -> submitText(text) },
        )
    }

    fun onStop() {
        speechInput.cancel()
        speechOutput.stop()
        releaseAudioFocus()
        interactionCoordinator.stop()
    }

    fun shutdown() {
        if (!started) return
        speechInput.shutdown()
        speechOutput.shutdown()
        audioRouteMonitor.stop()
        releaseAudioFocus()
        preferenceCoordinator.closeSession(preferenceSessionToken)
        interactionCoordinator.stop()
        started = false
    }

    fun submitText(text: String): SubmitResult {
        speechInput.cancel()
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SubmitResult.EmptyInput
        InteractionControlRecognizer.recognize(trimmed)?.let { control ->
            return handleControl(control)
        }
        val interactionId = interactionCoordinator.begin()
        val (debugDisplay, speechText, userText) = when (val result = engine.process(trimmed)) {
            is LocalCommandResult.Recognized -> Triple(
                "${result.intent}: ${result.response}",
                result.response,
                result.response,
            )
            LocalCommandResult.Unknown -> {
                val unknown = LocalUnknownResponses.next()
                Triple(unknown, unknown, unknown)
            }
        }
        lastResponse = userText
        if (voiceMuted) {
            interactionCoordinator.stop()
            statusMessage = null
            return SubmitResult.TextOnly(debugDisplay)
        }
        if (!acquireSpeechFocus()) {
            interactionCoordinator.stop()
            statusMessage = UserMessageKey.AudioUnavailable
            return SubmitResult.TextOnly(debugDisplay)
        }
        return when (speechOutput.speak(speechText, interactionId)) {
            SpeechOutputResult.Queued -> {
                statusMessage = null
                SubmitResult.Spoken(debugDisplay)
            }
            SpeechOutputResult.Failed -> {
                interactionCoordinator.error(interactionId)
                statusMessage = UserMessageKey.SpeechFailed
                SubmitResult.TextOnly(debugDisplay)
            }
            SpeechOutputResult.Unavailable -> {
                interactionCoordinator.error(interactionId)
                statusMessage = UserMessageKey.VoiceUnavailable
                SubmitResult.TextOnly(debugDisplay)
            }
        }
    }

    fun selectPersonalityTone(tone: PersonalityTone) {
        personalityTone = tone
        personalityPreview = null
        engine.applyPersonality(PersonalityConfig.sanitize(personalityDisplayName, tone))
    }

    fun savePersonality(nameDraft: String) {
        val config = PersonalityConfig.sanitize(nameDraft, personalityTone)
        personalityDisplayName = config.displayName
        personalityPreview = null
        engine.applyPersonality(config)
        scope.launch {
            preferenceCoordinator.persistPersonality(
                preferenceSessionToken,
                PersonalityPreference(config.displayName, config.tone),
            )
        }
    }

    fun previewPersonality(nameDraft: String) {
        personalityPreview = PersonalityPreview.sample(
            PersonalityConfig.sanitize(nameDraft, personalityTone),
        )
    }

    fun resetPersonality() {
        personalityTone = PersonalityTone.WARM
        personalityDisplayName = null
        personalityPreview = null
        engine.applyPersonality(PersonalityConfig())
        scope.launch { preferenceCoordinator.clearPersonality(preferenceSessionToken) }
    }

    fun toggleMute() {
        if (voiceMuted) {
            applyMutePreference(false)
            statusMessage = null
        } else {
            speechOutput.stop()
            releaseAudioFocus()
            interactionCoordinator.stop()
            applyMutePreference(true)
            statusMessage = null
        }
    }

    private fun handleControl(control: InteractionControlIntent): SubmitResult {
        return when (control) {
            InteractionControlIntent.STOP_SPEECH -> {
                speechOutput.stop()
                releaseAudioFocus()
                interactionCoordinator.stop()
                statusMessage = null
                SubmitResult.ControlHandled
            }
            InteractionControlIntent.CANCEL -> {
                speechInput.cancel()
                speechOutput.stop()
                releaseAudioFocus()
                interactionCoordinator.stop()
                statusMessage = UserMessageKey.InteractionCancelled
                SubmitResult.ControlHandled
            }
            InteractionControlIntent.ENABLE_MUTE -> {
                speechOutput.stop()
                releaseAudioFocus()
                interactionCoordinator.stop()
                applyMutePreference(true)
                statusMessage = null
                SubmitResult.ControlHandled
            }
            InteractionControlIntent.DISABLE_MUTE -> {
                applyMutePreference(false)
                statusMessage = null
                SubmitResult.ControlHandled
            }
        }
    }

    private fun applyMutePreference(muted: Boolean) {
        if (voiceMuted == muted) return
        voiceMuted = muted
        scope.launch { preferenceCoordinator.persistMute(preferenceSessionToken, muted) }
    }

    fun toggleListen(requestPermission: () -> Unit): ListenAction {
        if (listening) {
            speechInput.cancel()
            releaseAudioFocus()
            interactionCoordinator.stop()
            return ListenAction.Cancelled
        }
        if (!speechOutput.stop()) {
            statusMessage = UserMessageKey.CannotStopSpeech
            return ListenAction.Blocked
        }
        if (!acquireListeningFocus()) {
            statusMessage = UserMessageKey.AudioUnavailable
            return ListenAction.Unavailable
        }
        listenInteractionId = interactionCoordinator.begin()
        return when (speechInput.start()) {
            SpeechInputStartResult.Started -> {
                interactionCoordinator.input(SpeechInputState.Starting, listenInteractionId)
                ListenAction.Started
            }
            SpeechInputStartResult.PermissionRequired -> {
                releaseAudioFocus()
                requestPermission()
                ListenAction.PermissionRequested
            }
            SpeechInputStartResult.Unavailable -> {
                releaseAudioFocus()
                statusMessage = UserMessageKey.ListenUnavailable
                ListenAction.Unavailable
            }
            SpeechInputStartResult.Busy,
            SpeechInputStartResult.Failed,
            -> {
                releaseAudioFocus()
                statusMessage = UserMessageKey.ListenFailed
                ListenAction.Failed
            }
        }
    }

    fun onMicrophonePermissionResult(granted: Boolean) {
        inputState = if (granted) SpeechInputState.Idle else SpeechInputState.PermissionRequired
        statusMessage = if (granted) null else UserMessageKey.MicrophoneDenied
    }

    fun stopSpeech() {
        speechOutput.stop()
        releaseAudioFocus()
        interactionCoordinator.stop()
    }

    fun selectVoice(id: String): Boolean {
        speechInput.cancel()
        val change = preferenceCoordinator.beginExplicitChange()
        val ok = speechOutput.selectVoice(id)
        preferenceSaved = false
        if (ok) {
            val engineId = voiceSelection.engineId
            if (engineId != null) {
                scope.launch {
                    preferenceCoordinator.saveConfirmed(change, preferenceSessionToken, engineId, id)
                }
            }
        }
        return ok
    }

    fun selectDefaultVoice(): Boolean {
        speechInput.cancel()
        val change = preferenceCoordinator.beginExplicitChange()
        val ok = speechOutput.selectDefaultVoice()
        preferenceSaved = false
        if (ok) scope.launch { preferenceCoordinator.clearPreference(change, preferenceSessionToken) }
        return ok
    }

    fun speakSample(): Boolean {
        speechInput.cancel()
        val interactionId = interactionCoordinator.begin()
        if (!acquireSpeechFocus()) {
            interactionCoordinator.error(interactionId)
            return false
        }
        return when (
            speechOutput.speak("Olá. Sou a Vexa. Pronta para acompanhar sua viagem.", interactionId)
        ) {
            SpeechOutputResult.Queued -> true
            else -> {
                interactionCoordinator.error(interactionId)
                false
            }
        }
    }

    private fun onPreferenceEvent(event: SpeechVoicePreferenceEvent) {
        when (event) {
            is SpeechVoicePreferenceEvent.Restore -> {
                val ok = speechOutput.restoreVoice(event.preference.engineId, event.preference.voiceId)
                preferenceSaved = ok
                preferenceMessage = if (ok) "Voz salva restaurada." else "Preferência indisponível."
            }
            SpeechVoicePreferenceEvent.Saved -> {
                preferenceSaved = true
                preferenceMessage = "Preferência salva."
            }
            SpeechVoicePreferenceEvent.Cleared -> {
                preferenceSaved = false
                preferenceMessage = "Preferência removida."
            }
            SpeechVoicePreferenceEvent.SaveFailed -> {
                preferenceSaved = false
                preferenceMessage = "Usando nesta sessão; não foi possível salvar."
            }
            SpeechVoicePreferenceEvent.ClearFailed -> {
                preferenceMessage = "Não foi possível remover a preferência."
            }
            SpeechVoicePreferenceEvent.ReadFailed -> {
                preferenceMessage = "Não foi possível ler a preferência."
            }
            SpeechVoicePreferenceEvent.Unavailable -> {
                preferenceSaved = false
                preferenceMessage = "Preferência indisponível; usando padrão local."
            }
            is SpeechVoicePreferenceEvent.MuteRestored -> {
                voiceMuted = event.muted
            }
            is SpeechVoicePreferenceEvent.PersonalityRestored -> {
                val config = event.preference.toConfig()
                personalityTone = config.tone
                personalityDisplayName = config.displayName
                engine.applyPersonality(config)
            }
            SpeechVoicePreferenceEvent.PersonalitySaved -> {
                personalityMessage = "Preferências de personalidade salvas."
            }
            SpeechVoicePreferenceEvent.PersonalityCleared -> {
                personalityMessage = "Personalidade restaurada ao padrão."
            }
            SpeechVoicePreferenceEvent.PersonalitySaveFailed -> {
                personalityMessage = "Não foi possível salvar a personalidade."
            }
            SpeechVoicePreferenceEvent.PersonalityClearFailed -> {
                personalityMessage = "Não foi possível restaurar a personalidade."
            }
        }
    }

    private fun acquireSpeechFocus(): Boolean =
        audioFocusSession.requestForSpeech() == AudioFocusRequestResult.GRANTED

    private fun acquireListeningFocus(): Boolean =
        audioFocusSession.requestForListening() == AudioFocusRequestResult.GRANTED

    private fun releaseAudioFocus() {
        audioFocusSession.abandon()
    }

    private fun handleAudioInterruption(message: UserMessageKey) {
        speechInput.cancel()
        speechOutput.stop()
        releaseAudioFocus()
        interactionCoordinator.stop()
        statusMessage = message
    }

    private fun updateStatusFromInput(state: SpeechInputState) {
        statusMessage = when (state) {
            SpeechInputState.PermissionRequired -> UserMessageKey.MicrophoneDenied
            SpeechInputState.Unavailable -> UserMessageKey.ListenUnavailable
            SpeechInputState.NoMatch -> UserMessageKey.NoSpeechHeard
            SpeechInputState.TimedOut -> UserMessageKey.ListenTimedOut
            SpeechInputState.Failed -> UserMessageKey.ListenFailed
            SpeechInputState.Canceled,
            SpeechInputState.Completed,
            SpeechInputState.Idle,
            -> null
            else -> statusMessage
        }
    }

    sealed interface SubmitResult {
        data class Spoken(val display: String) : SubmitResult
        data class TextOnly(val display: String) : SubmitResult
        data object ControlHandled : SubmitResult
        data object EmptyInput : SubmitResult
    }

    enum class ListenAction {
        Started, Cancelled, PermissionRequested, Unavailable, Failed, Blocked
    }

    /** String resource keys resolved in composables; avoids Android types in domain session. */
    enum class UserMessageKey {
        MicrophoneDenied,
        ListenUnavailable,
        ListenFailed,
        ListenTimedOut,
        NoSpeechHeard,
        VoiceUnavailable,
        SpeechFailed,
        CannotStopSpeech,
        InteractionCancelled,
        AudioUnavailable,
        AudioFocusLost,
        AudioRouteChanged,
    }

    private companion object {
        val SPEECH_FOCUS_TERMINAL_STATES = setOf(
            SpeechOutputState.Completed,
            SpeechOutputState.Stopped,
            SpeechOutputState.Failed,
        )
        val INPUT_FOCUS_TERMINAL_STATES = setOf(
            SpeechInputState.Completed,
            SpeechInputState.Canceled,
            SpeechInputState.Failed,
            SpeechInputState.NoMatch,
            SpeechInputState.TimedOut,
            SpeechInputState.Unavailable,
        )
    }
}
