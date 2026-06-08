package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElegantGoldAccent,
    onPrimary = ElegantDarkBg,
    primaryContainer = ElegantDarkHighlight,
    onPrimaryContainer = ElegantTextColor,
    secondary = ElegantGoldAccent,
    onSecondary = ElegantDarkBg,
    tertiary = ElegantGoldAccent,
    background = ElegantDarkBg,
    surface = ElegantDarkSurface,
    onBackground = ElegantTextColor,
    onSurface = ElegantTextColor,
    onSurfaceVariant = ElegantTextMuted,
    surfaceVariant = ElegantDarkHighlight,
    outline = ElegantDarkBorder,
    error = ElegantRedAccent
)

// We unify light mode under Elegant Dark as well, to satisfy the explicit "Elegant Dark" user request.
private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true to force 'Elegant Dark'
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

