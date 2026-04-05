package com.hananel.workschedule.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Custom dark mode colors — slate-navy palette, modern indigo brand
private val DarkColorScheme = darkColorScheme(
    primary = AccentIndigo,
    secondary = PrimaryBlue,
    tertiary = PrimaryGreen,
    background = Color(0xFF0F1117),          // Near-black with slight blue cast
    surface = Color(0xFF191E2E),             // Dark navy surface
    onBackground = Color(0xFFEAECF4),        // Blue-tinted white text
    onSurface = Color(0xFFEAECF4),           // Blue-tinted white text
    surfaceVariant = Color(0xFF232840),      // Card backgrounds — indigo-navy
    onSurfaceVariant = Color(0xFFADB5CC),    // Secondary text
    outline = Color(0xFF3D4568),             // Borders
    outlineVariant = Color(0xFF272D45),      // Subtle borders
    surfaceContainerHighest = Color(0xFF1E2438),
    surfaceContainerHigh = Color(0xFF191E2E),
    surfaceContainer = Color(0xFF151929),
    surfaceContainerLow = Color(0xFF111523),
    surfaceContainerLowest = Color(0xFF0C0F1C)
)

private val LightColorScheme = lightColorScheme(
    primary = AccentIndigo,
    secondary = PrimaryBlue,
    tertiary = PrimaryGreen,
    background = Color(0xFFF5F6FB),          // Slightly indigo-tinted white
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1E3A),
    onSurface = Color(0xFF1A1E3A),
    surfaceVariant = Color(0xFFEEF0F8),
    onSurfaceVariant = Color(0xFF4A5070),
    outline = Color(0xFFB0B8D8),
    outlineVariant = Color(0xFFDCE0F0),
    surfaceContainerHighest = Color(0xFFE4E7F4),
    surfaceContainerHigh = Color(0xFFEAECF8),
    surfaceContainer = Color(0xFFEFF1FB),
    surfaceContainerLow = Color(0xFFF5F6FB),
    surfaceContainerLowest = Color(0xFFFFFFFF)
)

@Composable
fun WorkScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to ensure consistent brand colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Only use dynamic colors on Android 12+ if explicitly enabled
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
