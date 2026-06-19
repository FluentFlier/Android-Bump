package com.androidbump.data

import org.junit.Assert.assertTrue
import org.junit.Test

class VCardBuilderTest {

    @Test
    fun build_includesNameAndPhone() {
        val vcard = VCardBuilder.build(ContactProfile("Jane Doe", "+14155551212", "jane@example.com"))
        assertTrue(vcard.contains("FN:Jane Doe"))
        assertTrue(vcard.contains("TEL;TYPE=CELL:+14155551212"))
        assertTrue(vcard.contains("EMAIL;TYPE=INTERNET:jane@example.com"))
        assertTrue(vcard.startsWith("BEGIN:VCARD"))
        assertTrue(vcard.contains("END:VCARD"))
    }
}
