package com.androidbump.data

import android.util.Base64
import com.androidbump.BuildConfig

/**
 * Builds the HTTPS URL embedded in the NFC tag.
 * Contact data lives in the URL fragment — no upload, no account, no API.
 */
object ShareUrlBuilder {

    /** NDEF URI payload budget (conservative for iPhone readers). */
    private const val MAX_URI_PAYLOAD_CHARS = 900

    fun build(profile: ContactProfile, baseUrl: String = BuildConfig.SHARE_BASE_URL): String {
        val vcard = VCardBuilder.build(profile)
        val encoded = encodeVcard(vcard)
        val base = baseUrl.trimEnd('/') + "#"
        val url = base + encoded
        require(url.startsWith("https://")) { "Share URL must use HTTPS" }

        val payloadChars = url.removePrefix("https://").length
        require(payloadChars <= MAX_URI_PAYLOAD_CHARS) {
            "Contact is too long for NFC. Try a shorter name or phone number."
        }
        return url
    }

    fun parseProfile(url: String): ContactProfile {
        val hash = url.substringAfter('#', "")
        require(hash.isNotBlank()) { "Missing contact data in link." }
        val vcard = decodeVcard(hash)
        return profileFromVcard(vcard)
    }

    fun encodeVcard(vcard: String): String =
        Base64.encodeToString(
            vcard.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )

    fun decodeVcard(encoded: String): String {
        val bytes = Base64.decode(
            encoded,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        return String(bytes, Charsets.UTF_8)
    }

    private fun profileFromVcard(vcard: String): ContactProfile {
        fun field(key: String): String {
            val pattern = Regex("^$key(?::[^:]*)?:(.+)$", RegexOption.MULTILINE)
            return pattern.find(vcard)?.groupValues?.get(1)?.trim().orEmpty()
        }
        val fullName = field("FN")
        val phone = field("TEL")
        require(fullName.isNotBlank() && phone.isNotBlank()) {
            "Contact link must include name and phone."
        }
        return ContactProfile(
            fullName = fullName,
            phone = phone,
            email = field("EMAIL"),
            company = field("ORG"),
            website = field("URL"),
        )
    }
}
