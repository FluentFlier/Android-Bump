package com.androidbump.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidbump.R

private val BumpGreen = Color(0xFF1B5E20)
private val BumpGreenLight = Color(0xFF4CAF50)

@Composable
fun MainScreen(
    state: BumpUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImportContact: () -> Unit,
    onShowManual: () -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
) {
    when (state.screen) {
        Screen.Setup -> SetupScreen(state, onNameChange, onPhoneChange, onImportContact, onShowManual, onSave)
        Screen.Bump -> BumpScreen(state, onEdit)
    }
}

@Composable
private fun SetupScreen(
    state: BumpUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImportContact: () -> Unit,
    onShowManual: () -> Unit,
    onSave: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.setup_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Text(stringResource(R.string.setting_up), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (!state.showManualEntry) {
                Button(
                    onClick = onImportContact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(stringResource(R.string.import_contact), fontSize = 18.sp)
                }
                TextButton(onClick = onShowManual) {
                    Text(stringResource(R.string.enter_manually))
                }
            } else {
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    label = { Text(stringResource(R.string.field_phone)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(stringResource(R.string.continue_to_bump), fontSize = 18.sp)
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun BumpScreen(state: BumpUiState, onEdit: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ringAlpha",
    )

    val scale = if (state.bumpPulse) 1.08f else pulse
    val initials = state.fullName.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2)
        .joinToString("")
        .ifBlank { "?" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BumpGreen, Color(0xFF0D3311)))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.bump_ready),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = ringAlpha)),
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = BumpGreen)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(state.fullName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (state.phone.isNotBlank()) {
                Text(state.phone, color = Color.White.copy(alpha = 0.85f), fontSize = 17.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                stringResource(R.string.bump_instruction),
                color = BumpGreenLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            NfcStatusLine(state.nfcStatus)

            if (state.bumpPulse) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.bump_detected), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        TextButton(
            onClick = onEdit,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
        ) {
            Text(stringResource(R.string.edit_card), color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun NfcStatusLine(status: NfcStatus) {
    val (text, color) = when (status) {
        NfcStatus.Ready -> stringResource(R.string.nfc_ready) to Color.White.copy(alpha = 0.75f)
        NfcStatus.Disabled -> stringResource(R.string.nfc_disabled) to Color(0xFFFFCDD2)
        NfcStatus.Unsupported, NfcStatus.NoHce -> stringResource(R.string.nfc_missing) to Color(0xFFFFCDD2)
        NfcStatus.Unknown -> "…" to Color.White.copy(alpha = 0.5f)
    }
    Text(text, color = color, fontSize = 14.sp, textAlign = TextAlign.Center)
}
