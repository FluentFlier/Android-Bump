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
    private val KEY_COMPANY = stringPreferencesKey("company")
    private val KEY_WEBSITE = stringPreferencesKey("website")
    private val KEY_PROFILE_ID = stringPreferencesKey("profile_id")
    private val KEY_EDIT_TOKEN = stringPreferencesKey("edit_token")
    private val KEY_SHARE_URL = stringPreferencesKey("share_url")
    private val KEY_BASE_URL = stringPreferencesKey("base_url")

    suspend fun loadProfile(context: Context): ContactProfile? {
        val prefs = context.dataStore.data.first()
        val name = prefs[KEY_FULL_NAME] ?: return null
        return ContactProfile(
            fullName = name,
            phone = prefs[KEY_PHONE].orEmpty(),
            email = prefs[KEY_EMAIL].orEmpty(),
            company = prefs[KEY_COMPANY].orEmpty(),
            website = prefs[KEY_WEBSITE].orEmpty(),
        )
    }

    suspend fun loadShareUrl(context: Context): String? {
        return context.dataStore.data.map { it[KEY_SHARE_URL] }.first()
    }

    suspend fun loadBaseUrl(context: Context): String {
        return context.dataStore.data.map { it[KEY_BASE_URL] }.first()
            ?: com.androidbump.BuildConfig.DEFAULT_BASE_URL
    }

    suspend fun saveProfile(context: Context, profile: ContactProfile, response: ProfileResponse, baseUrl: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_PHONE] = profile.phone
            prefs[KEY_EMAIL] = profile.email
            prefs[KEY_COMPANY] = profile.company
            prefs[KEY_WEBSITE] = profile.website
            prefs[KEY_PROFILE_ID] = response.id
            prefs[KEY_EDIT_TOKEN] = response.editToken
            prefs[KEY_SHARE_URL] = response.shareUrl
            prefs[KEY_BASE_URL] = baseUrl.trimEnd('/')
        }
        NfcShareStore.updateShareUrl(context, response.shareUrl)
    }

    suspend fun saveManualShareUrl(context: Context, shareUrl: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHARE_URL] = shareUrl
        }
        NfcShareStore.updateShareUrl(context, shareUrl)
    }

    suspend fun loadCredentials(context: Context): Pair<String, String>? {
        val prefs = context.dataStore.data.first()
        val id = prefs[KEY_PROFILE_ID] ?: return null
        val token = prefs[KEY_EDIT_TOKEN] ?: return null
        return id to token
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
