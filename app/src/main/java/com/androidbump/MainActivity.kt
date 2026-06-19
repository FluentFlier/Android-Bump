package com.androidbump

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidbump.nfc.BumpHostApduService
import com.androidbump.nfc.NfcSessionEvents
import com.androidbump.ui.BumpViewModel
import com.androidbump.ui.BumpViewModelFactory
import com.androidbump.ui.MainScreen
import com.androidbump.ui.Screen
import com.androidbump.ui.theme.BumpTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var cardEmulation: CardEmulation? = null
    private val apduComponent = ComponentName(this, BumpHostApduService::class.java)

    private val pickContact = registerForActivityResult(
        ActivityResultContracts.PickContact(),
    ) { uri: Uri? ->
        uri?.let { viewModel?.importContact(it) }
    }

    private var viewModel: BumpViewModel? = null
    private var pendingSetupUrl: String? = null

    private val nfcListener: () -> Unit = {
        runOnUiThread {
            vibrateBumpSuccess()
            viewModel?.onBumpDetected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        cardEmulation = nfcAdapter?.let { CardEmulation.getInstance(it) }

        setContent {
            BumpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: BumpViewModel = viewModel(factory = BumpViewModelFactory(application))
                    viewModel = vm
                    val state by vm.state.collectAsState()

                    LaunchedEffect(pendingSetupUrl) {
                        pendingSetupUrl?.let { url ->
                            vm.importFromShareUrl(url)
                            pendingSetupUrl = null
                        }
                    }

                    DisposableEffect(state.screen) {
                        if (state.screen == Screen.Bump) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                        onDispose {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }

                    MainScreen(
                        state = state,
                        onNameChange = vm::updateName,
                        onPhoneChange = vm::updatePhone,
                        onImportContact = { pickContact.launch(null) },
                        onShowManual = vm::showManualEntry,
                        onSave = vm::saveAndStartBumping,
                        onEdit = vm::enterSetup,
                        onOpenNfcSettings = ::openNfcSettings,
                    )
                }
            }
        }

        handleSetupLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSetupLink(intent)
    }

    private fun handleSetupLink(intent: Intent?) {
        val uri = intent?.data ?: return
        val url = uri.toString()
        if (!url.contains("#")) return
        val vm = viewModel
        if (vm != null) {
            vm.importFromShareUrl(url)
        } else {
            pendingSetupUrl = url
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.refreshNfcStatus()
        cardEmulation?.setPreferredService(this, apduComponent)
        NfcSessionEvents.addListener(nfcListener)
        startService(Intent(this, BumpHostApduService::class.java))
    }

    override fun onPause() {
        NfcSessionEvents.removeListener(nfcListener)
        cardEmulation?.unsetPreferredService(this)
        super.onPause()
    }

    private fun openNfcSettings() {
        startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
    }

    private fun vibrateBumpSuccess() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 40, 60, 80),
                    intArrayOf(0, 120, 0, 180),
                    -1,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 40, 60, 80), -1)
        }
    }
}
