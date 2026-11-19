package com.example.hijriconverter

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B6B4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F0D3),
    onPrimaryContainer = Color(0xFF002114),
    secondaryContainer = Color(0xFFD7E8DD),
    onSecondaryContainer = Color(0xFF111F18),
    background = Color(0xFFF8FAF7),
    surface = Color(0xFFF8FAF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CD4B7),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF00513A),
    onPrimaryContainer = Color(0xFFB8F0D3),
    secondaryContainer = Color(0xFF2E4036),
    onSecondaryContainer = Color(0xFFD7E8DD),
    background = Color(0xFF101411),
    surface = Color(0xFF101411),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
