package com.androidbump.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "android_bump")

object ProfileRepository {
    private val KEY_FULL_NAME = stringPreferencesKey("full_name")
    private val KEY_PHONE = stringPreferencesKey("phone")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_SHARE_URL = stringPreferencesKey("share_url")

    suspend fun loadProfile(context: Context): ContactProfile? {
        val prefs = context.dataStore.data.first()
        val name = prefs[KEY_FULL_NAME] ?: return null
        return ContactProfile(
            fullName = name,
            phone = prefs[KEY_PHONE].orEmpty(),
            email = prefs[KEY_EMAIL].orEmpty(),
        )
    }

    suspend fun loadShareUrl(context: Context): String? {
        return context.dataStore.data.map { it[KEY_SHARE_URL] }.first()
    }

    suspend fun savePublishedProfile(context: Context, profile: ContactProfile, shareUrl: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_PHONE] = profile.phone
            prefs[KEY_EMAIL] = profile.email
            prefs[KEY_SHARE_URL] = shareUrl
        }
        NfcShareStore.updateShareUrl(context, shareUrl)
    }
}

/** Synchronous bridge for HostApduService (no coroutines on NFC thread). */
object NfcShareStore {
    @Volatile
    private var cachedUrl: String? = null

    fun getShareUrl(context: Context): String? {
        cachedUrl?.let { return it }
        return runBlocking {
            ProfileRepository.loadShareUrl(context).also { cachedUrl = it }
        }
    }

    fun updateShareUrl(context: Context, url: String) {
        cachedUrl = url
    }
}
