package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalityPreviewTest {
    @Test
    fun `preview uses phrase bank only`() {
        val sample = PersonalityPreview.sample(PersonalityConfig(displayName = "Ana", tone = PersonalityTone.DIRECT))
        assertEquals("Aqui, Ana.", sample)
    }

    @Test
    fun `preview without name stays local`() {
        val sample = PersonalityPreview.sample(PersonalityConfig(tone = PersonalityTone.WARM))
        assertTrue(sample.isNotBlank())
        assertTrue(!sample.contains("Ana"))
    }
}
