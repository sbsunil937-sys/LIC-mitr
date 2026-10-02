package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = LicGoldLight,
    onPrimary = LicNavyDark,
    primaryContainer = LicNavyLight,
    onPrimaryContainer = Color.White,
    secondary = LicEmerald,
    onSecondary = Color.White,
    background = LicNavyDarkBg,
    surface = LicNavyDarkSurface,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    error = LicRedOverdue,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LicNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0ECF8),
    onPrimaryContainer = LicNavyDark,
    secondary = LicGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = LicGoldContainer,
    onSecondaryContainer = Color(0xFF78350F),
    background = LicBgLight,
    surface = LicSurfaceLight,
    onBackground = LicTextPrimary,
    onSurface = LicTextPrimary,
    error = LicRedOverdue,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent LIC blue/gold branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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
