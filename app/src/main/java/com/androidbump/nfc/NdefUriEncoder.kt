package com.androidbump.nfc

/**
 * Builds NDEF URI records and Type 4 tag file layout for HCE.
 * iPhones background-read HTTPS URLs only — never raw vCard NDEF.
 */
object NdefUriEncoder {

    private const val URI_PREFIX_HTTPS: Byte = 0x04

    fun encodeUriRecord(url: String): ByteArray {
        require(url.startsWith("https://")) { "Share URL must use HTTPS for iPhone background NFC" }
        val payload = url.removePrefix("https://").encodeToByteArray()
        val record = ByteArray(4 + payload.size)
        record[0] = 0xD1.toByte() // MB=1 ME=1 SR=1 TNF=Well-known
        record[1] = 0x01 // type length
        record[2] = (1 + payload.size).toByte() // payload length (prefix + rest)
        record[3] = 0x55 // "U"
        record[4] = URI_PREFIX_HTTPS
        payload.copyInto(record, destinationOffset = 5)
        return record
    }

    fun buildNdefFile(url: String, maxFileSize: Int = 0x00FF): ByteArray {
        val message = encodeUriRecord(url)
        require(message.size + 2 <= maxFileSize) {
            "URL too long for NDEF file ($maxFileSize bytes max). Use a shorter backend domain."
        }
        return ByteArray(2 + message.size).apply {
            this[0] = ((message.size shr 8) and 0xFF).toByte()
            this[1] = (message.size and 0xFF).toByte()
            message.copyInto(this, destinationOffset = 2)
        }
    }

    fun buildCapabilityContainer(maxRead: Int = 0x003B, maxWrite: Int = 0x0034, ndefMaxSize: Int = 0x00FF): ByteArray {
        return byteArrayOf(
            0x00, 0x0F, // CCLEN
            0x20, // Mapping version 2.0
            ((maxRead shr 8) and 0xFF).toByte(), (maxRead and 0xFF).toByte(),
            ((maxWrite shr 8) and 0xFF).toByte(), (maxWrite and 0xFF).toByte(),
            0x04, 0x06, // NDEF File Control TLV
            0xE1.toByte(), 0x04, // File ID
            ((ndefMaxSize shr 8) and 0xFF).toByte(), (ndefMaxSize and 0xFF).toByte(),
            0x00, // Read access
            0xFF.toByte(), // Write disabled
        )
    }
}
