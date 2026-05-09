package com.sarvix.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SarvixColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnPrimary,
    secondary = AccentCyan,
    onSecondary = OnSurface,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = OnSurface,
    tertiary = AccentPink,
    onTertiary = OnSurface,
    tertiaryContainer = SurfaceVariant,
    onTertiaryContainer = OnSurface,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnPrimary,
    outline = DividerColor,
    outlineVariant = DividerColor,
    scrim = Color(0xFF000000)
)

@Composable
fun SarvixTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Sarvix is always dark theme - ignore system setting
    val colorScheme = SarvixColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SarvixTypography,
        content = content
    )
}
