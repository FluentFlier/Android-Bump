package com.androidbump.data

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactImporter {

    fun fromUri(context: Context, uri: Uri): ContactProfile? {
        val resolver = context.contentResolver
        var fullName = ""
        var phone = ""
        var email = ""

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex >= 0) fullName = cursor.getString(nameIndex).orEmpty()
                if (idIndex >= 0) {
                    val contactId = cursor.getString(idIndex)
                    phone = queryPhone(resolver, contactId)
                    email = queryEmail(resolver, contactId)
                }
            }
        }

        if (fullName.isBlank()) return null
        return ContactProfile(
            fullName = fullName.trim(),
            phone = phone.trim(),
            email = email.trim(),
        )
    }

    private fun queryPhone(resolver: android.content.ContentResolver, contactId: String): String {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val sel = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?"
        resolver.query(uri, null, sel, arrayOf(contactId), null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (idx >= 0) return c.getString(idx).orEmpty()
            }
        }
        return ""
    }

    private fun queryEmail(resolver: android.content.ContentResolver, contactId: String): String {
        val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
        val sel = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?"
        resolver.query(uri, null, sel, arrayOf(contactId), null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                if (idx >= 0) return c.getString(idx).orEmpty()
            }
        }
        return ""
    }
}
