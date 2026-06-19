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
import com.androidbump.data.ProfileRepository
import com.androidbump.data.ShareUrlBuilder
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
    val bumpJustSent: Boolean = false,
)

enum class Screen { Setup, Bump }

enum class NfcStatus { Unknown, Ready, Disabled, Unsupported, NoHce }

class BumpViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(BumpUiState())
    val state: StateFlow<BumpUiState> = _state.asStateFlow()

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
            it.copy(fullName = profile.fullName, phone = profile.phone, error = null)
        }
        saveAndStartBumping()
    }

    fun importFromShareUrl(url: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val profile = ShareUrlBuilder.parseProfile(url)
                val shareUrl = ShareUrlBuilder.build(profile)
                ProfileRepository.savePublishedProfile(getApplication(), profile, shareUrl)
                _state.update {
                    it.copy(
                        fullName = profile.fullName,
                        phone = profile.phone,
                        isSaving = false,
                        shareUrl = shareUrl,
                        screen = Screen.Bump,
                        nfcStatus = detectNfcStatus(),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Could not load setup link.",
                    )
                }
            }
        }
    }

    fun enterSetup() {
        _state.update { it.copy(screen = Screen.Setup, showManualEntry = false, error = null) }
    }

    fun refreshNfcStatus() {
        _state.update { it.copy(nfcStatus = detectNfcStatus()) }
    }

    fun onBumpDetected() {
        _state.update { it.copy(bumpPulse = true, bumpJustSent = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _state.update { it.copy(bumpPulse = false) }
            kotlinx.coroutines.delay(3500)
            _state.update { it.copy(bumpJustSent = false) }
        }
    }

    fun saveAndStartBumping() {
        val current = _state.value
        if (current.fullName.isBlank() || current.phone.isBlank()) {
            _state.update { it.copy(error = "Name and phone are required.", showManualEntry = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val profile = ContactProfile(
                    fullName = current.fullName.trim(),
                    phone = current.phone.trim(),
                )
                val shareUrl = ShareUrlBuilder.build(profile)
                ProfileRepository.savePublishedProfile(getApplication(), profile, shareUrl)
                _state.update {
                    it.copy(
                        isSaving = false,
                        shareUrl = shareUrl,
                        screen = Screen.Bump,
                        nfcStatus = detectNfcStatus(),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Could not prepare your bump link.",
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
