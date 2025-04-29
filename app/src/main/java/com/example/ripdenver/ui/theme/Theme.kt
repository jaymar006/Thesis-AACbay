package com.example.ripdenver.ui.theme

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

private val md_theme_light_primary = Color(0xFFB68D00)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFFFE08D)
private val md_theme_light_onPrimaryContainer = Color(0xFF362A00)
private val md_theme_light_secondary = Color(0xFFB68D00)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFFFE08D)
private val md_theme_light_onSecondaryContainer = Color(0xFF362A00)
private val md_theme_light_surface = Color(0xFFFFF8DC)
private val md_theme_light_onSurface = Color(0xFF1C1B1F)
private val md_theme_light_surfaceVariant = Color(0xFFEEE1CF)
private val md_theme_light_onSurfaceVariant = Color(0xFF4A4639)
private val md_theme_light_background = Color(0xFFFFFBE6)
private val md_theme_light_error = Color(0xFFBA1A1A)



private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)



val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    background = md_theme_light_background,
    error = md_theme_light_error
)

private val BrownOrangeLight = lightColorScheme(
    primary = Color(0xFFE9A319), // Orange
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFAD59A), // Light Orange
    onPrimaryContainer = Color(0xFF3C2800),
    secondary = Color(0xFFA86523), // Brown
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCEFCB), // Very Light Orange
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = Color(0xFF855000),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF2B1700),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFF0E0CF),
    onSurfaceVariant = Color(0xFF4F4539),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val BrownOrangeDark = darkColorScheme(
    primary = Color(0xFFFFB951), // Light Orange
    onPrimary = Color(0xFF462B00),
    primaryContainer = Color(0xFFA86523), // Brown
    onPrimaryContainer = Color(0xFFFFDDB7),
    secondary = Color(0xFFE9A319), // Orange
    onSecondary = Color(0xFF422B00),
    secondaryContainer = Color(0xFF5D3F00),
    onSecondaryContainer = Color(0xFFFFDDB7),
    tertiary = Color(0xFFFFB77C),
    onTertiary = Color(0xFF482800),
    tertiaryContainer = Color(0xFF663C00),
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF1F1B16),
    onBackground = Color(0xFFEAE1D9),
    surface = Color(0xFF1F1B16),
    onSurface = Color(0xFFEAE1D9),
    surfaceVariant = Color(0xFF4F4539),
    onSurfaceVariant = Color(0xFFD3C4B4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)


val availableColors = listOf(
    "#FF0000", // Red
    "#FFA500", // Orange
    "#FFFF00", // Yellow
    "#008000", // Green
    "#0000FF", // Blue
    "#4B0082", // Indigo
    "#9400D3", // Violet
    "#FF69B4", // Pink
    "#A52A2A", // Brown
    "#808080", // Gray
    "#FFFFFF", // White
    "#000000"  // Black
)

@Composable
fun RIPDenverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BrownOrangeDark
        else -> BrownOrangeLight  // This is where your light theme is applied
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}