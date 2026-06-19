package com.androidbump.data

object VCardBuilder {

    fun build(profile: ContactProfile): String {
        val parts = profile.fullName.trim().split("\\s+".toRegex())
        val last = parts.lastOrNull().orEmpty()
        val first = if (parts.size > 1) parts.dropLast(1).joinToString(" ") else ""
        val lines = buildList {
            add("BEGIN:VCARD")
            add("VERSION:3.0")
            add("N:${escape(last)};${escape(first)};;;")
            add("FN:${escape(profile.fullName.trim())}")
            if (profile.company.isNotBlank()) add("ORG:${escape(profile.company)}")
            if (profile.phone.isNotBlank()) add("TEL;TYPE=CELL:${escape(profile.phone)}")
            if (profile.email.isNotBlank()) add("EMAIL;TYPE=INTERNET:${escape(profile.email)}")
            if (profile.website.isNotBlank()) add("URL:${escape(profile.website)}")
            add("END:VCARD")
        }
        return lines.joinToString("\r\n") + "\r\n"
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace(";", "\\;").replace("\n", "\\n")
}
