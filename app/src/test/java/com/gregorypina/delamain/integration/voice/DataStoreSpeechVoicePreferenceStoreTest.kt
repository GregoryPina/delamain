package com.gregorypina.delamain.integration.voice

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
