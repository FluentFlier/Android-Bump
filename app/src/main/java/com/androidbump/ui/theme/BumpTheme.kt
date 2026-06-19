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
    secondary = BumpGreen,
    background = BumpBackground,
    surface = Color(0xFF142818),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB8D4BB),
    error = Color(0xFFFF8A80),
)

@Composable
fun BumpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BumpColorScheme,
        content = content,
    )
}
