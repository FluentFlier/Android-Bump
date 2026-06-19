package com.androidbump.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidbump.R
import com.androidbump.ui.theme.BumpGreen
import com.androidbump.ui.theme.BumpGreenDark
import com.androidbump.ui.theme.BumpGreenLight

@Composable
fun MainScreen(
    state: BumpUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImportContact: () -> Unit,
    onShowManual: () -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onOpenNfcSettings: () -> Unit,
) {
    AnimatedContent(
        targetState = state.screen,
        transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(200)) },
        label = "screen",
    ) { screen ->
        when (screen) {
            Screen.Setup -> SetupScreen(
                state, onNameChange, onPhoneChange, onImportContact, onShowManual, onSave,
            )
            Screen.Bump -> BumpScreen(state, onEdit, onOpenNfcSettings)
        }
    }
}

@Composable
private fun GreenBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BumpGreen, BumpGreenDark))),
    ) {
        content()
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
    GreenBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Nfc,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.setup_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                stringResource(R.string.setup_subtitle),
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(36.dp))

            if (state.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.setting_up),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                )
            } else if (!state.showManualEntry) {
                Button(
                    onClick = onImportContact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BumpGreen,
                    ),
                ) {
                    Icon(Icons.Outlined.ContactPage, contentDescription = null)
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.import_contact), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onShowManual) {
                    Text(
                        stringResource(R.string.enter_manually),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 15.sp,
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                SetupStepsCard()
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.1f),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = onNameChange,
                            label = { Text(stringResource(R.string.field_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = fieldColors(),
                        )
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = onPhoneChange,
                            label = { Text(stringResource(R.string.field_phone)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = fieldColors(),
                        )
                        Button(
                            onClick = onSave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = BumpGreen,
                            ),
                        ) {
                            Text(stringResource(R.string.continue_to_bump), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = Color(0xFFFFCDD2), textAlign = TextAlign.Center, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SetupStepsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.08f),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepRow("1", stringResource(R.string.step_setup))
            StepRow("2", stringResource(R.string.step_bump))
            StepRow("3", stringResource(R.string.step_iphone))
        }
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Text(text, color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun BumpScreen(
    state: BumpUiState,
    onEdit: () -> Unit,
    onOpenNfcSettings: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringAlpha",
    )

    val scale = if (state.bumpJustSent || state.bumpPulse) 1.06f else pulse
    val initials = state.fullName.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2).joinToString("").ifBlank { "?" }

    GreenBackdrop {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            NfcStatusChip(state.nfcStatus, onOpenNfcSettings)
            Spacer(modifier = Modifier.weight(0.15f))

            Text(
                stringResource(R.string.bump_ready),
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(28.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = ringAlpha)),
                )
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f)),
                )
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = BumpGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                state.fullName,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (state.phone.isNotBlank()) {
                Text(
                    state.phone,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            BumpInstructionCard()

            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onEdit, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(stringResource(R.string.edit_card), color = Color.White.copy(alpha = 0.65f))
            }
        }

            AnimatedVisibility(
                visible = state.bumpJustSent,
                enter = fadeIn() + scaleIn(initialScale = 0.92f),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
            ) {
                SuccessBanner()
            }
        }
    }
}

@Composable
private fun SuccessBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = BumpGreen, modifier = Modifier.size(32.dp))
            Column {
                Text(
                    stringResource(R.string.bump_sent_title),
                    color = BumpGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    stringResource(R.string.bump_sent_body),
                    color = BumpGreenDark.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
private fun BumpInstructionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.1f),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            InstructionRow(Icons.Outlined.Nfc, stringResource(R.string.bump_step_hold))
            InstructionRow(Icons.Outlined.PhoneIphone, stringResource(R.string.bump_step_tap))
        }
    }
}

@Composable
private fun InstructionRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, contentDescription = null, tint = BumpGreenLight, modifier = Modifier.size(24.dp))
        Text(text, color = Color.White.copy(alpha = 0.92f), fontSize = 15.sp, lineHeight = 22.sp)
    }
}

@Composable
private fun NfcStatusChip(status: NfcStatus, onOpenSettings: () -> Unit) {
    val (text, bg, clickable) = when (status) {
        NfcStatus.Ready -> Triple(stringResource(R.string.nfc_ready), Color.White.copy(0.12f), false)
        NfcStatus.Disabled -> Triple(stringResource(R.string.nfc_disabled), Color(0x44FF5252), true)
        NfcStatus.Unsupported, NfcStatus.NoHce -> Triple(stringResource(R.string.nfc_missing), Color(0x44FF5252), false)
        NfcStatus.Unknown -> Triple("…", Color.White.copy(0.08f), false)
    }
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                if (clickable) Icons.Outlined.Settings else Icons.Outlined.Nfc,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(16.dp),
            )
            Text(text, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
        }
    }
    if (clickable) {
        Surface(onClick = onOpenSettings, shape = RoundedCornerShape(999.dp), color = bg, content = content)
    } else {
        Surface(shape = RoundedCornerShape(999.dp), color = bg, content = content)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White.copy(alpha = 0.7f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
    focusedLabelColor = Color.White.copy(alpha = 0.85f),
    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
    cursorColor = Color.White,
)
