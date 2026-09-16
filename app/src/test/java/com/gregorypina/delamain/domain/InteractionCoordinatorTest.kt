package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class InteractionCoordinatorTest {
    @Test
    fun `boot timer cannot overwrite interaction`() {
        val coordinator = InteractionCoordinator {}
        val id = coordinator.begin()
        coordinator.input(SpeechInputState.Listening, id)
        coordinator.bootFinished()
        assertEquals(InteractionFace.LISTENING, coordinator.current().face)
    }

    @Test
    fun `old tts completion cannot overwrite new listening`() {
        val coordinator = InteractionCoordinator {}
        val old = coordinator.begin()
        coordinator.output(SpeechOutputState.Speaking, old)
        val fresh = coordinator.begin()
        coordinator.input(SpeechInputState.Listening, fresh)
        coordinator.output(SpeechOutputState.Completed, old)
        assertEquals(InteractionFace.LISTENING, coordinator.current().face)
    }

    @Test
    fun `old error timer cannot overwrite new interaction`() {
        val coordinator = InteractionCoordinator {}
        val old = coordinator.begin()
        coordinator.error(old)
        val fresh = coordinator.begin()
        coordinator.input(SpeechInputState.Processing, fresh)
        coordinator.expireError(old)
        assertEquals(InteractionFace.THINKING, coordinator.current().face)
    }

    @Test
    fun `queued is not speaking`() {
        val coordinator = InteractionCoordinator {}
        val id = coordinator.begin()
        coordinator.output(SpeechOutputState.Queued, id)
        assertEquals(InteractionFace.IDLE, coordinator.current().face)
        coordinator.output(SpeechOutputState.Speaking, id)
        assertEquals(InteractionFace.SPEAKING, coordinator.current().face)
    }

    @Test
    fun `shutdown rejects callbacks`() {
        val coordinator = InteractionCoordinator {}
        val id = coordinator.begin()
        coordinator.shutdown()
        coordinator.input(SpeechInputState.Listening, id)
        assertEquals(InteractionFace.IDLE, coordinator.current().face)
    }

    @Test
    fun `stop invalidates active callback`() {
        val coordinator = InteractionCoordinator {}
        val id = coordinator.begin()
        coordinator.output(SpeechOutputState.Speaking, id)
        coordinator.stop()
        coordinator.output(SpeechOutputState.Completed, id)
        assertEquals(InteractionFace.IDLE, coordinator.current().face)
    }

    @Test
    fun `infra events with zero interaction id are ignored`() {
        val coordinator = InteractionCoordinator {}
        coordinator.output(SpeechOutputState.Preparing, 0L)
        assertEquals(InteractionFace.BOOT, coordinator.current().face)
    }
}
