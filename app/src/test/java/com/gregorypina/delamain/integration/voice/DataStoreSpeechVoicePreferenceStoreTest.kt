package com.gregorypina.delamain.integration.voice

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataStoreSpeechVoicePreferenceStoreTest {
    @Test fun `voice reset preserves unrelated future settings`() {
        val personalityKey = intPreferencesKey("future_personality_setting")
        val values = mutablePreferencesOf(
            voicePreferenceVersionKey to 1,
            voiceEngineIdKey to "engine",
            voiceIdKey to "voice",
            personalityKey to 7,
        )

        clearVoicePreferenceKeys(values)

        assertNull(values[voicePreferenceVersionKey])
        assertNull(values[voiceEngineIdKey])
        assertNull(values[voiceIdKey])
        assertEquals(7, values[personalityKey])
    }

    @Test
    fun `mute key is independent from voice preference keys`() {
        val values = mutablePreferencesOf(
            voicePreferenceVersionKey to 1,
            voiceEngineIdKey to "engine",
            voiceIdKey to "voice",
            voiceMutedKey to true,
        )

        clearVoicePreferenceKeys(values)

        assertTrue(values[voiceMutedKey] == true)
    }

    @Test
    fun `mute defaults to false when absent`() {
        val values = mutablePreferencesOf()
        assertFalse(values[booleanPreferencesKey("voice_muted")] ?: false)
    }

    @Test
    fun `personality reset preserves voice and mute keys`() {
        val values = mutablePreferencesOf(
            voicePreferenceVersionKey to 1,
            voiceEngineIdKey to "engine",
            voiceIdKey to "voice",
            voiceMutedKey to true,
            personalityDisplayNameKey to "Ana",
            personalityToneKey to 1,
        )

        clearPersonalityKeys(values)

        assertEquals("engine", values[voiceEngineIdKey])
        assertTrue(values[voiceMutedKey] == true)
        assertNull(values[personalityDisplayNameKey])
        assertNull(values[personalityToneKey])
    }
}
