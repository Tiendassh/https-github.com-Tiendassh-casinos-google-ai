package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CasinoColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    secondary = ElegantSecondary,
    tertiary = ElegantActivePill,
    background = ElegantBackground,
    surface = ElegantSurface,
    onPrimary = ElegantOnPrimary,
    onSecondary = ElegantOnPrimary,
    onTertiary = Color.White,
    onBackground = ElegantTertiary,
    onSurface = ElegantTertiary,
    surfaceVariant = ElegantSurfaceVariant,
    onSurfaceVariant = ElegantSecondary,
    outline = ElegantBorder,
    outlineVariant = ElegantRecoveryBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark casino theme
    dynamicColor: Boolean = false, // Always keep casino style branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CasinoColorScheme,
        typography = Typography,
        content = content
    )
}
