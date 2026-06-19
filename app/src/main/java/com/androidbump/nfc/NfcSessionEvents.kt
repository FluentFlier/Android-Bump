package com.androidbump.nfc

import java.util.concurrent.CopyOnWriteArrayList

/** Fired when an NFC reader (e.g. iPhone) finishes reading our emulated tag. */
object NfcSessionEvents {
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    fun notifySessionComplete() {
        listeners.forEach { it.invoke() }
    }
}
