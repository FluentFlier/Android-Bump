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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    onSetDefaultNfc: () -> Unit,
    onClearError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(BumpGreen, BumpGreenDark))),
        ) {
            AnimatedContent(
                targetState = state.screen,
                transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(200)) },
                label = "screen",
                modifier = Modifier.fillMaxSize(),
            ) { screen ->
                when (screen) {
                    Screen.Setup -> SetupScreen(state, onNameChange, onPhoneChange, onImportContact, onShowManual, onSave)
                    Screen.Bump -> BumpScreen(state, onEdit, onOpenNfcSettings, onSetDefaultNfc)
                }
            }
        }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.Nfc, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.setup_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.setup_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (state.isSaving) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.setting_up), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
        } else if (!state.showManualEntry) {
            FilledButton(
                onClick = onImportContact,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Outlined.ContactPage, contentDescription = null)
                Spacer(modifier = Modifier.size(AssistChipDefaults.IconSize))
                Text(stringResource(R.string.import_contact), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onShowManual) {
                Text(stringResource(R.string.enter_manually), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            SetupStepsCard()
        } else {
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                    FilledButton(onClick = onSave, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.continue_to_bump))
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStepsCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.step_setup), style = MaterialTheme.typography.bodyMedium) },
                leadingContent = { StepBadge("1") },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.step_bump), style = MaterialTheme.typography.bodyMedium) },
                leadingContent = { StepBadge("2") },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.step_iphone), style = MaterialTheme.typography.bodyMedium) },
                leadingContent = { StepBadge("3") },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun StepBadge(label: String) {
    Box(
        modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun BumpScreen(
    state: BumpUiState,
    onEdit: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onSetDefaultNfc: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringAlpha",
    )
    val scale = if (state.bumpJustSent || state.bumpPulse) 1.06f else pulse
    val initials = state.fullName.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2).joinToString("").ifBlank { "?" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NfcStatusChip(state.nfcStatus, onOpenNfcSettings)
            Spacer(modifier = Modifier.height(12.dp))

            if (!state.nfcIsDefault) {
                DefaultNfcCard(onSetDefaultNfc)
            } else {
                EmbeddedTagHintCard()
            }

            Spacer(modifier = Modifier.weight(0.1f))
            Text(
                stringResource(R.string.bump_ready),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
            )
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(240.dp).scale(scale).clip(CircleShape).background(Color.White.copy(alpha = ringAlpha)))
                Box(Modifier.size(168.dp).scale(scale).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)))
                Card(
                    modifier = Modifier.size(124.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(initials, style = MaterialTheme.typography.displaySmall, color = BumpGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(state.fullName, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center)
            if (state.phone.isNotBlank()) {
                Text(state.phone, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f))
            }

            Spacer(modifier = Modifier.height(28.dp))
            BumpInstructionCard()
            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = onEdit, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.edit_card))
            }
        }

        AnimatedVisibility(
            visible = state.bumpJustSent,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
        ) {
            SuccessBanner()
        }
    }
}

@Composable
private fun DefaultNfcCard(onSetDefault: () -> Unit) {
    ElevatedCard(
        onClick = onSetDefault,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.nfc_not_default_title), color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(stringResource(R.string.nfc_not_default_body), color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)) },
            trailingContent = { Text(stringResource(R.string.nfc_set_default), color = BumpGreenLight, style = MaterialTheme.typography.labelLarge) },
            leadingContent = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun EmbeddedTagHintCard() {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(stringResource(R.string.nfc_embedded_tag_hint), style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(Icons.Outlined.Nfc, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize)) },
        modifier = Modifier.fillMaxWidth(),
        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    )
}

@Composable
private fun SuccessBanner() {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.bump_sent_title), fontWeight = FontWeight.Bold) },
            supportingContent = { Text(stringResource(R.string.bump_sent_body)) },
            leadingContent = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = BumpGreen, modifier = Modifier.size(32.dp)) },
        )
    }
}

@Composable
private fun BumpInstructionCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    ) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.bump_step_hold)) },
            leadingContent = { Icon(Icons.Outlined.Nfc, contentDescription = null, tint = BumpGreenLight) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.bump_step_tap)) },
            leadingContent = { Icon(Icons.Outlined.PhoneIphone, contentDescription = null, tint = BumpGreenLight) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun NfcStatusChip(status: NfcStatus, onOpenSettings: () -> Unit) {
    when (status) {
        NfcStatus.Ready -> AssistChip(
            onClick = {},
            enabled = false,
            label = { Text(stringResource(R.string.nfc_ready)) },
            leadingIcon = { Icon(Icons.Outlined.Nfc, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) },
        )
        NfcStatus.Disabled -> AssistChip(
            onClick = onOpenSettings,
            label = { Text(stringResource(R.string.nfc_disabled)) },
            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) },
            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
        )
        NfcStatus.Unsupported, NfcStatus.NoHce -> AssistChip(
            onClick = {},
            enabled = false,
            label = { Text(stringResource(R.string.nfc_missing)) },
            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
        )
        NfcStatus.Unknown -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.4f))
    }
}
