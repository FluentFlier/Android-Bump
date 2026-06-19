package com.androidbump.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareUrlBuilderTest {

    @Test
    fun build_producesHttpsUrlWithEncodedVcard() {
        val url = ShareUrlBuilder.build(
            ContactProfile("Jane Doe", "+14155551212"),
            "https://example.github.io/Android-Bump",
        )
        assertTrue(url.startsWith("https://"))
        assertTrue(url.contains("#"))
    }

    @Test
    fun parseProfile_roundTripsWithBuild() {
        val profile = ContactProfile("Jane Doe", "+14155551212", "jane@example.com")
        val url = ShareUrlBuilder.build(profile, "https://example.github.io/Android-Bump")
        val parsed = ShareUrlBuilder.parseProfile(url)
        assertEquals(profile.fullName, parsed.fullName)
        assertEquals(profile.phone, parsed.phone)
        assertEquals(profile.email, parsed.email)
    }
}
