package com.androidbump.nfc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NdefUriEncoderTest {

    @Test
    fun encodeUriRecord_usesHttpsPrefixByte() {
        val record = NdefUriEncoder.encodeUriRecord("https://bump.example/c/abc")
        assertEquals(0xD1.toByte(), record[0])
        assertEquals(0x55.toByte(), record[3])
        assertEquals(0x04.toByte(), record[4])
        assertTrue(String(record.copyOfRange(5, record.size)).startsWith("bump.example/c/abc"))
    }

    @Test
    fun encodeUriRecord_usesLongFormatWhenPayloadExceeds255() {
        val longPath = "a".repeat(260)
        val record = NdefUriEncoder.encodeUriRecord("https://example.com/$longPath")
        assertEquals(0xC1.toByte(), record[0])
        assertEquals(0x55.toByte(), record[6])
    }

    @Test
    fun buildNdefFile_prefixesLength() {
        val file = NdefUriEncoder.buildNdefFile("https://bump.example/c/abc")
        val ndefLength = ((file[0].toInt() and 0xFF) shl 8) or (file[1].toInt() and 0xFF)
        assertEquals(file.size - 2, ndefLength)
    }

    @Test
    fun ndefFileSizeForUrl_growsWithLongerUrls() {
        val short = NdefUriEncoder.ndefFileSizeForUrl("https://example.com/#abc")
        val long = NdefUriEncoder.ndefFileSizeForUrl("https://example.com/#" + "a".repeat(400))
        assertTrue(long >= short)
    }
}

class Type4TagEngineTest {

    private val engine = Type4TagEngine("https://bump.example/c/x")

    @Test
    fun selectNdefApp_thenReadCcAndNdef() {
        assertEquals(2, selectNdefApp().size)
        selectFile(0xE1, 0x03, p2 = 0x0C)
        val cc = readBinary(0, 15)
        assertTrue(cc.size >= 2)
        assertEquals(0x90.toByte(), cc[cc.size - 2])

        selectFile(0xE1, 0x04, p2 = 0x0C)
        val ndef = readBinary(0, 255)
        assertTrue(ndef.size > 2)
        assertEquals(0x90.toByte(), ndef[ndef.size - 2])
    }

    @Test
    fun selectNdefApp_withTrailingLe() {
        val apdu = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, 0x07,
            0xD2.toByte(), 0x76, 0x00, 0x00, 0x85.toByte(), 0x01, 0x01, 0x00,
        )
        assertEquals(2, engine.processCommandApdu(apdu).size)
    }

    @Test
    fun selectFile_acceptsP2Zero() {
        selectNdefApp()
        val apdu = byteArrayOf(
            0x00, 0xA4.toByte(), 0x00, 0x00, 0x02, 0xE1.toByte(), 0x04,
        )
        assertEquals(2, engine.processCommandApdu(apdu).size)
    }

    @Test
    fun readBinary_returnsRequestedChunk() {
        selectFile(0xE1, 0x04, p2 = 0x0C)
        val first = readBinary(0, 2)
        assertEquals(4, first.size)
        assertEquals(first[0], first.copyOfRange(0, 1)[0])
    }

    private fun selectNdefApp(): ByteArray {
        val apdu = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, 0x07,
            0xD2.toByte(), 0x76, 0x00, 0x00, 0x85.toByte(), 0x01, 0x01,
        )
        return engine.processCommandApdu(apdu)
    }

    private fun selectFile(idHi: Int, idLo: Int, p2: Int = 0x0C): ByteArray {
        val apdu = byteArrayOf(
            0x00, 0xA4.toByte(), 0x00, p2.toByte(), 0x02,
            idHi.toByte(), idLo.toByte(),
        )
        return engine.processCommandApdu(apdu)
    }

    private fun readBinary(offset: Int, le: Int): ByteArray {
        val apdu = byteArrayOf(
            0x00, 0xB0.toByte(),
            ((offset shr 8) and 0xFF).toByte(),
            (offset and 0xFF).toByte(),
            le.toByte(),
        )
        return engine.processCommandApdu(apdu)
    }
}
