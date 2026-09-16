package com.gregorypina.delamain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalityConfigTest {
    @Test
    fun `empty name disables treatment`() {
        assertNull(PersonalityConfig.sanitize("   ").displayName)
    }

    @Test
    fun `unicode name is preserved`() {
        assertEquals("José", PersonalityConfig.sanitize("  José  ").displayName)
    }

    @Test
    fun `control characters are rejected`() {
        assertNull(PersonalityConfig.sanitize("Ana\ncomando").displayName)
    }

    @Test
    fun `long name is truncated to limit`() {
        val longName = "A".repeat(50)
        assertEquals(40, PersonalityConfig.sanitize(longName).displayName?.length)
    }
}
