package com.androidbump.nfc

/**
 * Builds NDEF URI records and Type 4 tag file layout for HCE.
 * iPhones background-read HTTPS URLs only — never raw vCard NDEF.
 */
object NdefUriEncoder {

    private const val URI_PREFIX_HTTPS: Byte = 0x04
    private const val MIN_NDEF_FILE_SIZE = 0x00FF
    private const val MAX_NDEF_FILE_SIZE = 0x0800

    fun encodeUriRecord(url: String): ByteArray {
        require(url.startsWith("https://")) { "Share URL must use HTTPS for iPhone background NFC" }
        val uriBody = url.removePrefix("https://").encodeToByteArray()
        val payloadSize = 1 + uriBody.size

        return if (payloadSize <= 255) {
            ByteArray(5 + uriBody.size).apply {
                this[0] = 0xD1.toByte() // MB ME SR TNF=well-known
                this[1] = 0x01
                this[2] = payloadSize.toByte()
                this[3] = 0x55
                this[4] = URI_PREFIX_HTTPS
                uriBody.copyInto(this, destinationOffset = 5)
            }
        } else {
            ByteArray(8 + uriBody.size).apply {
                this[0] = 0xC1.toByte() // MB ME, no SR — long payload
                this[1] = 0x01
                this[2] = ((payloadSize shr 24) and 0xFF).toByte()
                this[3] = ((payloadSize shr 16) and 0xFF).toByte()
                this[4] = ((payloadSize shr 8) and 0xFF).toByte()
                this[5] = (payloadSize and 0xFF).toByte()
                this[6] = 0x55
                this[7] = URI_PREFIX_HTTPS
                uriBody.copyInto(this, destinationOffset = 8)
            }
        }
    }

    fun ndefFileSizeForUrl(url: String): Int {
        val required = encodeUriRecord(url).size + 2
        return required.coerceIn(MIN_NDEF_FILE_SIZE, MAX_NDEF_FILE_SIZE)
    }

    fun buildNdefFile(url: String, maxFileSize: Int = ndefFileSizeForUrl(url)): ByteArray {
        val message = encodeUriRecord(url)
        require(message.size + 2 <= maxFileSize) {
            "URL too long for NDEF file ($maxFileSize bytes max)."
        }
        return ByteArray(2 + message.size).apply {
            this[0] = ((message.size shr 8) and 0xFF).toByte()
            this[1] = (message.size and 0xFF).toByte()
            message.copyInto(this, destinationOffset = 2)
        }
    }

    fun buildCapabilityContainer(ndefMaxSize: Int = MIN_NDEF_FILE_SIZE): ByteArray {
        val maxRead = (ndefMaxSize + 2).coerceAtLeast(0x003B)
        val maxWrite = 0x0034
        return byteArrayOf(
            0x00, 0x0F,
            0x20,
            ((maxRead shr 8) and 0xFF).toByte(), (maxRead and 0xFF).toByte(),
            ((maxWrite shr 8) and 0xFF).toByte(), (maxWrite and 0xFF).toByte(),
            0x04, 0x06,
            0xE1.toByte(), 0x04,
            ((ndefMaxSize shr 8) and 0xFF).toByte(), (ndefMaxSize and 0xFF).toByte(),
            0x00,
            0xFF.toByte(),
        )
    }
}
