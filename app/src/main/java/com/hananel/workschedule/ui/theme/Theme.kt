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

// Custom dark mode colors that work well with our app theme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    secondary = PrimaryBlue,
    tertiary = PrimaryGreen,
    background = Color(0xFF0F1A19),  // Very dark teal-tinted background
    surface = Color(0xFF1A2625),     // Slightly lighter surface
    onBackground = Color(0xFFE8EFEE), // Light text on dark
    onSurface = Color(0xFFE8EFEE),    // Light text on surface
    surfaceVariant = Color(0xFF243332), // Card backgrounds in dark mode
    onSurfaceVariant = Color(0xFFB0C4C2), // Secondary text - visible on dark
    outline = Color(0xFF3D5452),       // Borders
    outlineVariant = Color(0xFF2A3B3A), // Subtle borders
    surfaceContainerHighest = Color(0xFF1E2A29),
    surfaceContainerHigh = Color(0xFF1A2625),
    surfaceContainer = Color(0xFF162120),
    surfaceContainerLow = Color(0xFF121C1B),
    surfaceContainerLowest = Color(0xFF0D1514)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    secondary = PrimaryBlue,
    tertiary = PrimaryGreen,
    background = Color(0xFFF8FBFB),    // Slightly teal-tinted white
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0F5F4),
    onSurfaceVariant = Color(0xFF5A6665),
    outline = Color(0xFFB8C5C4),
    outlineVariant = Color(0xFFDCE5E4),
    surfaceContainerHighest = Color(0xFFE8EEEE),
    surfaceContainerHigh = Color(0xFFEDF2F2),
    surfaceContainer = Color(0xFFF2F6F6),
    surfaceContainerLow = Color(0xFFF7FAFA),
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
