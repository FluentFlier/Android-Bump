package com.androidbump.ui

import android.app.Application
import android.net.Uri
import android.nfc.NfcAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.androidbump.data.ContactImporter
import com.androidbump.data.ContactProfile
import com.androidbump.data.ProfileApi
import com.androidbump.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BumpUiState(
    val fullName: String = "",
    val phone: String = "",
    val shareUrl: String? = null,
    val screen: Screen = Screen.Setup,
    val isSaving: Boolean = false,
    val showManualEntry: Boolean = false,
    val error: String? = null,
    val nfcStatus: NfcStatus = NfcStatus.Unknown,
    val bumpPulse: Boolean = false,
)

enum class Screen { Setup, Bump }

enum class NfcStatus { Unknown, Ready, Disabled, Unsupported, NoHce }

class BumpViewModel(
    application: Application,
    private val api: ProfileApi = ProfileApi(),
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(BumpUiState())
    val state: StateFlow<BumpUiState> = _state.asStateFlow()

    private val baseUrl: String
        get() = com.androidbump.BuildConfig.DEFAULT_BASE_URL

    init {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val profile = ProfileRepository.loadProfile(context)
            val shareUrl = ProfileRepository.loadShareUrl(context)
            _state.update {
                it.copy(
                    fullName = profile?.fullName.orEmpty(),
                    phone = profile?.phone.orEmpty(),
                    shareUrl = shareUrl,
                    screen = if (shareUrl != null) Screen.Bump else Screen.Setup,
                    nfcStatus = detectNfcStatus(),
                )
            }
        }
    }

    fun updateName(value: String) = _state.update { it.copy(fullName = value, error = null) }
    fun updatePhone(value: String) = _state.update { it.copy(phone = value, error = null) }
    fun showManualEntry() = _state.update { it.copy(showManualEntry = true, error = null) }

    fun importContact(uri: Uri) {
        val profile = ContactImporter.fromUri(getApplication(), uri) ?: run {
            _state.update { it.copy(error = "Could not read that contact.") }
            return
        }
        _state.update {
            it.copy(
                fullName = profile.fullName,
                phone = profile.phone,
                error = null,
            )
        }
        saveAndStartBumping()
    }

    fun enterSetup() {
        _state.update { it.copy(screen = Screen.Setup, showManualEntry = false, error = null) }
    }

    fun onBumpDetected() {
        _state.update { it.copy(bumpPulse = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            _state.update { it.copy(bumpPulse = false) }
        }
    }

    fun saveAndStartBumping() {
        val current = _state.value
        if (current.fullName.isBlank() || current.phone.isBlank()) {
            _state.update { it.copy(error = "Name and phone are required.", showManualEntry = true) }
            return
        }
        if (baseUrl.contains("YOUR_SUBDOMAIN")) {
            _state.update {
                it.copy(error = "App not linked to server yet. See QUICKSTART.md in the repo.")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val profile = ContactProfile(
                    fullName = current.fullName.trim(),
                    phone = current.phone.trim(),
                    email = "",
                )
                val context = getApplication<Application>()
                val credentials = ProfileRepository.loadCredentials(context)
                val response = if (credentials != null) {
                    api.updateProfile(baseUrl, credentials.first, credentials.second, profile)
                } else {
                    api.createProfile(baseUrl, profile)
                }
                ProfileRepository.saveProfile(context, profile, response, baseUrl)
                _state.update {
                    it.copy(
                        isSaving = false,
                        shareUrl = response.shareUrl,
                        screen = Screen.Bump,
                        nfcStatus = detectNfcStatus(),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = "Could not connect. Check internet and try again.",
                        showManualEntry = true,
                    )
                }
            }
        }
    }

    private fun detectNfcStatus(): NfcStatus {
        val context = getApplication<Application>()
        val adapter = NfcAdapter.getDefaultAdapter(context) ?: return NfcStatus.Unsupported
        if (!adapter.isEnabled) return NfcStatus.Disabled
        if (!context.packageManager.hasSystemFeature("android.hardware.nfc.hce")) {
            return NfcStatus.NoHce
        }
        return NfcStatus.Ready
    }
}

class BumpViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BumpViewModel::class.java)) {
            return BumpViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
