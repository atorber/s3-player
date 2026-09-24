package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AetherColorScheme = darkColorScheme(
    primary = AwsAmber,
    onPrimary = Color(0xFF4A2800),
    primaryContainer = AwsAmberDark,
    onPrimaryContainer = AwsAmberLight,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF00373A),
    secondaryContainer = Color(0xFF004F53),
    onSecondaryContainer = ElectricCyanSoft,
    tertiary = OrbitalIndigoLight,
    onTertiary = OrbitalIndigoDark,
    tertiaryContainer = OrbitalIndigo,
    onTertiaryContainer = Color(0xFFE2DFFF),
    background = AetherVoid,
    onBackground = TextHighContrast,
    surface = AetherSurface,
    onSurface = TextHighContrast,
    surfaceVariant = AetherSurfaceTier1,
    onSurfaceVariant = TextMediumContrast,
    outline = TextLowContrast,
    outlineVariant = BorderSubtle,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Designed as deep cosmic dark theme for pro telemetry
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AetherColorScheme,
        typography = Typography,
        content = content
    )
}
