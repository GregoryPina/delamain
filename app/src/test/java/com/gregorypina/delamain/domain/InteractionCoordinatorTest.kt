package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class InteractionCoordinatorTest {
 @Test fun `boot timer cannot overwrite interaction`() { val c=InteractionCoordinator{};val id=c.begin();c.input(SpeechInputState.Listening,id);c.bootFinished();assertEquals(InteractionFace.LISTENING,c.current().face) }
 @Test fun `old tts completion cannot overwrite new listening`() { val c=InteractionCoordinator{};val old=c.begin();c.output(SpeechOutputState.Speaking,old);val fresh=c.begin();c.input(SpeechInputState.Listening,fresh);c.output(SpeechOutputState.Completed,old);assertEquals(InteractionFace.LISTENING,c.current().face) }
 @Test fun `old error timer cannot overwrite new interaction`() { val c=InteractionCoordinator{};val old=c.begin();c.error(old);val fresh=c.begin();c.input(SpeechInputState.Processing,fresh);c.expireError(old);assertEquals(InteractionFace.THINKING,c.current().face) }
 @Test fun `queued is not speaking`() { val c=InteractionCoordinator{};val id=c.begin();c.output(SpeechOutputState.Queued,id);assertEquals(InteractionFace.IDLE,c.current().face);c.output(SpeechOutputState.Speaking,id);assertEquals(InteractionFace.SPEAKING,c.current().face) }
 @Test fun `shutdown rejects callbacks`() { val c=InteractionCoordinator{};val id=c.begin();c.shutdown();c.input(SpeechInputState.Listening,id);assertEquals(InteractionFace.IDLE,c.current().face) }
 @Test fun `stop invalidates active callback`() { val c=InteractionCoordinator{};val id=c.begin();c.output(SpeechOutputState.Speaking,id);c.stop();c.output(SpeechOutputState.Completed,id);assertEquals(InteractionFace.IDLE,c.current().face) }
}
