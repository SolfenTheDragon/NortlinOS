package com.nortlinos.wearos.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/**
 * Amber-and-teal scheme on a pure black background. Wear OS screens are OLED, so black is free
 * and every lit container costs power; containers stay dark and saturated colors are reserved for
 * the primary action and state accents.
 */
private val NortlinOSColorScheme = ColorScheme(
    primary = Color(0xFFF4A340),
    primaryDim = Color(0xFFC87616),
    primaryContainer = Color(0xFF5A3A0E),
    onPrimary = Color.Black,
    onPrimaryContainer = Color(0xFFFFDDB5),
    secondary = Color(0xFF65D6C4),
    secondaryDim = Color(0xFF279889),
    secondaryContainer = Color(0xFF1F3B37),
    onSecondary = Color.Black,
    onSecondaryContainer = Color(0xFFCDEFE9),
    tertiary = Color(0xFFB9C8F5),
    tertiaryDim = Color(0xFF8D9BC6),
    tertiaryContainer = Color(0xFF2E3650),
    onTertiary = Color.Black,
    onTertiaryContainer = Color(0xFFDDE4FF),
    surfaceContainerLow = Color(0xFF161412),
    surfaceContainer = Color(0xFF211F1C),
    surfaceContainerHigh = Color(0xFF2E2B27),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFD4C8BA),
    outline = Color(0xFF9C8F80),
    outlineVariant = Color(0xFF4F463C),
    background = Color.Black,
    onBackground = Color.White,
    error = Color(0xFFFF8A80),
    errorDim = Color(0xFFE06C62),
    errorContainer = Color(0xFF5C1A15),
    onError = Color.Black,
    onErrorContainer = Color(0xFFFFDAD5)
)

/**
 * App-wide Wear Material 3 theme wrapper. Wrap the app's root composable in this so every
 * screen shares consistent colors, typography, and shapes.
 */
@Composable
fun NortlinOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NortlinOSColorScheme,
        content = content
    )
}
