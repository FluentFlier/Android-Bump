package com.androidbump.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BumpGreen = Color(0xFF1B5E20)
val BumpGreenLight = Color(0xFF4CAF50)
val BumpGreenDark = Color(0xFF0D3311)
val BumpBackground = Color(0xFF0A1F0C)

private val BumpColorScheme = darkColorScheme(
    primary = BumpGreenLight,
    onPrimary = BumpGreenDark,
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color.White,
    secondary = BumpGreen,
    background = BumpBackground,
    surface = Color(0xFF142818),
    surfaceVariant = Color(0xFF1E3322),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB8D4BB),
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFCDD2),
)

@Composable
fun BumpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BumpColorScheme,
        content = content,
    )
}
