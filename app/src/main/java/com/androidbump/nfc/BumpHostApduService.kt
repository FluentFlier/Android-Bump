package com.androidbump.nfc

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.androidbump.data.NfcShareStore

/**
 * Emulates an NFC Forum Type 4 tag while the app is active.
 * iPhone reads the HTTPS URL and opens Safari → native contact import.
 */
class BumpHostApduService : HostApduService() {

    @Volatile
    private var engine: Type4TagEngine? = null

    override fun onCreate() {
        super.onCreate()
        refreshEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshEngine()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return byteArrayOf(0x6F.toByte(), 0x00)
        }
        val activeEngine = engine ?: refreshEngine()
        return activeEngine?.processCommandApdu(commandApdu)
            ?: byteArrayOf(0x6A.toByte(), 0x82.toByte())
    }

    override fun onDeactivated(reason: Int) {
        // Session ended
    }

    private fun refreshEngine(): Type4TagEngine? {
        val url = NfcShareStore.getShareUrl(applicationContext) ?: return null
        return Type4TagEngine(url).also { engine = it }
    }
}
