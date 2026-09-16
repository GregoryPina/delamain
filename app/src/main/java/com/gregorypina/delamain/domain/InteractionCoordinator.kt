package com.gregorypina.delamain.domain

enum class InteractionFace { BOOT, IDLE, LISTENING, THINKING, SPEAKING, ERROR }
data class InteractionSnapshot(val face: InteractionFace = InteractionFace.BOOT, val hud: String = "INITIALIZING", val interactionId: Long = 0)

/** Resolves visual priority between existing STT/TTS sessions. It does not duplicate their request machines. */
class InteractionCoordinator(private val onSnapshot: (InteractionSnapshot) -> Unit) {
    private var id = 0L
    private var boot = true
    private var closed = false
    private var snapshot = InteractionSnapshot()
    fun current() = snapshot
    fun begin(): Long { id += 1; boot = false; publish(InteractionFace.IDLE, "ONLINE"); return id }
    fun bootFinished() { if (!closed && id == 0L) { boot=false; publish(InteractionFace.IDLE,"ONLINE") } }
    fun input(state: SpeechInputState, interactionId: Long = id) {
        if (!active(interactionId)) return
        when(state){
            SpeechInputState.Starting -> publish(InteractionFace.IDLE,"PREPARING")
            SpeechInputState.Listening -> publish(InteractionFace.LISTENING,"LISTENING")
            SpeechInputState.Processing -> publish(InteractionFace.THINKING,"THINKING")
            SpeechInputState.Failed, SpeechInputState.Unavailable, SpeechInputState.PermissionRequired, SpeechInputState.NoMatch, SpeechInputState.TimedOut -> error(interactionId)
            SpeechInputState.Completed, SpeechInputState.Canceled, SpeechInputState.Idle -> publish(InteractionFace.IDLE,"ONLINE")
            SpeechInputState.Closed -> Unit
        }
    }
    fun output(state: SpeechOutputState, interactionId: Long = id) {
        if (!active(interactionId)) return
        when(state){
            SpeechOutputState.Queued -> publish(InteractionFace.IDLE,"VOICE QUEUED")
            SpeechOutputState.Speaking -> publish(InteractionFace.SPEAKING,"SPEAKING")
            SpeechOutputState.Failed, SpeechOutputState.Unavailable -> error(interactionId)
            SpeechOutputState.Completed, SpeechOutputState.Stopped, SpeechOutputState.Ready -> publish(InteractionFace.IDLE,"ONLINE")
            SpeechOutputState.Preparing -> publish(InteractionFace.IDLE,"PREPARING VOICE")
            SpeechOutputState.Closed -> Unit
        }
    }
    fun error(interactionId: Long=id) { if(active(interactionId)) publish(InteractionFace.ERROR,"ERROR") }
    fun expireError(interactionId: Long) { if(active(interactionId) && snapshot.face==InteractionFace.ERROR) publish(InteractionFace.IDLE,"ONLINE") }
    fun stop() { if(!closed){id+=1;boot=false;publish(InteractionFace.IDLE,"ONLINE")} }
    fun shutdown(){closed=true;id+=1}
    private fun active(candidate:Long)=!closed && candidate==id
    private fun publish(face:InteractionFace,hud:String){snapshot=InteractionSnapshot(if(boot)InteractionFace.BOOT else face,hud,id);onSnapshot(snapshot)}
}
