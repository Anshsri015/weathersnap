package com.example.weathersnap.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Color Palette

private val WeatherGreen  = Color(0xFF7CB77E)
private val WeatherGreenD = Color(0xFF4E8B51)
private val Surface       = Color(0xFFF6F6F6)
private val SurfaceDark   = Color(0xFF1C1C1E)

private val LightColorScheme = lightColorScheme(
    primary          = WeatherGreen,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD0EDCF),
    secondary        = Color(0xFF5D8A60),
    background       = Surface,
    surface          = Color.White,
    surfaceVariant   = Color(0xFFECECEC),
    onSurfaceVariant = Color(0xFF6E6E73),
    outline          = Color(0xFFCCCCCC),
    outlineVariant   = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary          = WeatherGreen,
    onPrimary        = Color.Black,
    primaryContainer = WeatherGreenD,
    secondary        = Color(0xFF8FC991),
    background       = SurfaceDark,
    surface          = Color(0xFF2C2C2E),
    surfaceVariant   = Color(0xFF3A3A3C),
    onSurfaceVariant = Color(0xFFAEAEB2),
    outline          = Color(0xFF48484A)
)

// Theme Composable

@Composable
fun WeatherSnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        content     = content
    )
}
