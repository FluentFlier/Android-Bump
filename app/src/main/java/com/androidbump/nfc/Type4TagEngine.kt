package com.androidbump.nfc

/**
 * NFC Forum Type 4 tag state machine for NDEF read-only HCE.
 * Handles iPhone and Android reader APDU variants.
 */
class Type4TagEngine(shareUrl: String) {

    companion object {
        val NDEF_AID = byteArrayOf(
            0xD2.toByte(), 0x76, 0x00, 0x00, 0x85.toByte(), 0x01, 0x01,
        )
        private val CC_FILE_ID = byteArrayOf(0xE1.toByte(), 0x03)
        private val NDEF_FILE_ID = byteArrayOf(0xE1.toByte(), 0x04)
        private val SW_OK = byteArrayOf(0x90.toByte(), 0x00)
        private val SW_NOT_FOUND = byteArrayOf(0x6A.toByte(), 0x82.toByte())
        private val SW_WRONG_LENGTH = byteArrayOf(0x67.toByte(), 0x00)
        private val SW_WRONG_PARAMS = byteArrayOf(0x6A.toByte(), 0x86.toByte())
        private val SW_INVALID_OFFSET = byteArrayOf(0x6B.toByte(), 0x00)
        private val SW_UNSUPPORTED = byteArrayOf(0x6D.toByte(), 0x00)
    }

    private enum class SelectedFile { NONE, APP, CC, NDEF }

    private val ndefFileSize = NdefUriEncoder.ndefFileSizeForUrl(shareUrl)
    private val ccFile = NdefUriEncoder.buildCapabilityContainer(ndefMaxSize = ndefFileSize)
    private val ndefFile = NdefUriEncoder.buildNdefFile(shareUrl, maxFileSize = ndefFileSize)
    private var selectedFile = SelectedFile.NONE
    private var readCompleted = false

    private var ndefBytesRead = 0

    fun processCommandApdu(commandApdu: ByteArray): ByteArray {
        if (commandApdu.size < 4) return SW_WRONG_LENGTH

        return when (commandApdu[1].toInt() and 0xFF) {
            0xA4 -> handleSelect(commandApdu)
            0xB0 -> handleReadBinary(commandApdu)
            0xA3 -> SW_OK
            else -> SW_UNSUPPORTED
        }
    }

    private fun handleSelect(apdu: ByteArray): ByteArray {
        val data = extractCommandData(apdu) ?: return SW_WRONG_PARAMS
        val p2 = apdu[3].toInt() and 0xFF

        selectedFile = when {
            data.contentEquals(NDEF_AID) -> {
                readCompleted = false
                ndefBytesRead = 0
                SelectedFile.APP
            }
            data.contentEquals(NDEF_FILE_ID) && isFileSelectP2(p2) -> {
                ndefBytesRead = 0
                SelectedFile.NDEF
            }
            data.contentEquals(CC_FILE_ID) && isFileSelectP2(p2) -> SelectedFile.CC
            else -> SelectedFile.NONE
        }
        return when {
            selectedFile != SelectedFile.NONE -> SW_OK
            else -> SW_NOT_FOUND
        }
    }

    private fun isFileSelectP2(p2: Int): Boolean = p2 == 0x0C || p2 == 0x00

    private fun handleReadBinary(apdu: ByteArray): ByteArray {
        val offset = ((apdu[2].toInt() and 0xFF) shl 8) or (apdu[3].toInt() and 0xFF)
        val le = extractLe(apdu)

        val source = when (selectedFile) {
            SelectedFile.CC -> ccFile
            SelectedFile.NDEF -> ndefFile
            SelectedFile.APP, SelectedFile.NONE -> return SW_NOT_FOUND
        }

        if (offset > source.size) return SW_INVALID_OFFSET
        if (offset == source.size) return SW_OK

        val length = when {
            le == 0 -> minOf(256, source.size - offset)
            else -> le.coerceAtMost(source.size - offset)
        }
        if (length == 0) return SW_OK

        if (!readCompleted && selectedFile == SelectedFile.NDEF) {
            ndefBytesRead = (offset + length).coerceAtLeast(ndefBytesRead)
            if (ndefBytesRead >= ndefFile.size) {
                readCompleted = true
                NfcSessionEvents.notifySessionComplete()
            }
        }

        return source.copyOfRange(offset, offset + length) + SW_OK
    }

    private fun extractLe(apdu: ByteArray): Int {
        if (apdu.size == 4) return 0
        if (apdu.size == 5) return apdu[4].toInt() and 0xFF
        val lc = apdu[4].toInt() and 0xFF
        return when {
            apdu.size == 5 + lc -> apdu.last().toInt() and 0xFF
            apdu.size == 6 + lc && apdu[5 + lc] == 0x00.toByte() -> {
                ((apdu[5 + lc + 1].toInt() and 0xFF) shl 8) or (apdu[5 + lc + 2].toInt() and 0xFF)
            }
            apdu.size >= 7 && lc == 0 && (apdu[4].toInt() and 0xFF) == 0x00 -> {
                ((apdu[5].toInt() and 0xFF) shl 8) or (apdu[6].toInt() and 0xFF)
            }
            else -> apdu.last().toInt() and 0xFF
        }
    }

    private fun extractCommandData(apdu: ByteArray): ByteArray? {
        if (apdu.size < 5) return null
        val lc = apdu[4].toInt() and 0xFF
        if (lc == 0 || apdu.size < 5 + lc) return null
        return apdu.copyOfRange(5, 5 + lc)
    }
}
