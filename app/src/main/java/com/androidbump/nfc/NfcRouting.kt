package com.androidbump.nfc

import android.content.ComponentName
import android.content.Context
import android.nfc.cardemulation.CardEmulation
import com.androidbump.nfc.BumpHostApduService

object NfcRouting {

    fun serviceComponent(context: Context): ComponentName =
        ComponentName(context, BumpHostApduService::class.java)

    fun isDefaultForNdef(context: Context): Boolean {
        val emulation = CardEmulation.getInstance(
            android.nfc.NfcAdapter.getDefaultAdapter(context) ?: return false,
        ) ?: return false
        return emulation.isDefaultServiceForCategory(
            serviceComponent(context),
            CardEmulation.CATEGORY_OTHER,
        )
    }
}
