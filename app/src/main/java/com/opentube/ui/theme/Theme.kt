package com.opentube.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

// Dark Theme (Default)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF1C1C1C),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF1C1C1C),
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF424242),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

// Light Theme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF424242),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFF1C1C1C),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1C1C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFBDBDBD),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF)
)

// YouTube Theme (Dark with red accent)
private val YouTubeColorScheme = darkColorScheme(
    primary = Color(0xFFFF0000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCC0000),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF888888),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF303030),
    onSecondaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF212121),
    onSurface = Color(0xFFF1F1F1),
    surfaceVariant = Color(0xFF212121),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF383838),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

// Blue Theme
private val BlueColorScheme = darkColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF1976D2),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF1E88E5),
    onSecondaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF0A1929),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D2238),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A3A52),
    onSurfaceVariant = Color(0xFFB3D9FF)
)

// Sky Blue Theme
private val SkyBlueColorScheme = lightColorScheme(
    primary = Color(0xFF0288D1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF03A9F4),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF00BCD4),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF4DD0E1),
    onSecondaryContainer = Color(0xFF000000),
    background = Color(0xFFE1F5FE),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFB3E5FC),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFF81D4FA),
    onSurfaceVariant = Color(0xFF01579B)
)

// Red Theme
private val RedColorScheme = darkColorScheme(
    primary = Color(0xFFB71C1C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC62828),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFD32F2F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE53935),
    onSecondaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF1A0000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF2E0000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF4A0000),
    onSurfaceVariant = Color(0xFFFFCDD2)
)

// Pink Theme (Rosa suave)
private val PinkColorScheme = darkColorScheme(
    primary = PrimaryRosa,
    onPrimary = OnPrimaryRosa,
    primaryContainer = PrimaryContainerRosa,
    onPrimaryContainer = OnPrimaryContainerRosa,
    secondary = SecondaryRosa,
    onSecondary = OnSecondaryRosa,
    secondaryContainer = SecondaryContainerRosa,
    onSecondaryContainer = OnSecondaryContainerRosa,
    tertiary = TertiaryRosa,
    onTertiary = OnTertiaryRosa,
    tertiaryContainer = TertiaryContainerRosa,
    onTertiaryContainer = OnTertiaryContainerRosa,
    background = BackgroundDarkRosa,
    onBackground = OnBackgroundDarkRosa,
    surface = SurfaceDarkRosa,
    onSurface = OnSurfaceDarkRosa,
    surfaceVariant = SurfaceVariantDarkRosa,
    onSurfaceVariant = OnSurfaceVariantDarkRosa,
    outline = OutlineDarkRosa,
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

// Light Green Theme (Verde claro suave)
private val LightGreenColorScheme = lightColorScheme(
    primary = PrimaryVerdeClaro,
    onPrimary = OnPrimarySuave,
    primaryContainer = PrimaryContainerVerdeClaro,
    onPrimaryContainer = OnPrimaryContainerSuave,
    secondary = SecondaryVerdeClaro,
    onSecondary = OnSecondarySuave,
    secondaryContainer = SecondaryContainerVerdeClaro,
    onSecondaryContainer = OnSecondaryContainerSuave,
    tertiary = TertiaryVerdeClaro,
    onTertiary = OnTertiarySuave,
    tertiaryContainer = TertiaryContainerVerdeClaro,
    onTertiaryContainer = OnTertiaryContainerSuave,
    background = BackgroundLightSuave,
    onBackground = OnBackgroundLightSuave,
    surface = SurfaceLightSuave,
    onSurface = OnSurfaceLightSuave,
    surfaceVariant = SurfaceVariantLightSuave,
    onSurfaceVariant = OnSurfaceVariantLightSuave,
    outline = OutlineLightSuave,
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF)
)

// Dark Green Theme (Verde Spotify oscuro)
private val DarkGreenSpotifyColorScheme = darkColorScheme(
    primary = PrimaryVerdeClaro,
    onPrimary = OnPrimarySuave,
    primaryContainer = PrimaryContainerVerdeClaro,
    onPrimaryContainer = OnPrimaryContainerSuave,
    secondary = SecondaryVerdeClaro,
    onSecondary = OnSecondarySuave,
    secondaryContainer = SecondaryContainerVerdeClaro,
    onSecondaryContainer = OnSecondaryContainerSuave,
    tertiary = TertiaryVerdeClaro,
    onTertiary = OnTertiarySuave,
    tertiaryContainer = TertiaryContainerVerdeClaro,
    onTertiaryContainer = OnTertiaryContainerSuave,
    background = BackgroundDarkSuave,
    onBackground = OnBackgroundDarkSuave,
    surface = SurfaceDarkSuave,
    onSurface = OnSurfaceDarkSuave,
    surfaceVariant = SurfaceVariantDarkSuave,
    onSurfaceVariant = OnSurfaceVariantDarkSuave,
    outline = OutlineDarkSuave,
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

@Composable
fun OpenTubeTheme(
    themeMode: com.opentube.ui.screens.settings.ThemeMode = com.opentube.ui.screens.settings.ThemeMode.DARK,
    materialYouEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    
    val colorScheme = if (materialYouEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Material You (Dynamic Colors)
        if (systemInDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        // Standard Themes
        when (themeMode) {
            com.opentube.ui.screens.settings.ThemeMode.SYSTEM -> {
                if (systemInDarkTheme) DarkColorScheme else LightColorScheme
            }
            com.opentube.ui.screens.settings.ThemeMode.LIGHT -> LightColorScheme
            com.opentube.ui.screens.settings.ThemeMode.DARK -> DarkColorScheme
            com.opentube.ui.screens.settings.ThemeMode.YOUTUBE -> YouTubeColorScheme
            com.opentube.ui.screens.settings.ThemeMode.BLUE -> BlueColorScheme
            com.opentube.ui.screens.settings.ThemeMode.SKY_BLUE -> SkyBlueColorScheme
            com.opentube.ui.screens.settings.ThemeMode.RED -> RedColorScheme
            com.opentube.ui.screens.settings.ThemeMode.PINK -> PinkColorScheme
            com.opentube.ui.screens.settings.ThemeMode.LIGHT_GREEN -> LightGreenColorScheme
        }
    }
    
    val isDark = when {
        materialYouEnabled -> systemInDarkTheme
        themeMode == com.opentube.ui.screens.settings.ThemeMode.SYSTEM -> systemInDarkTheme
        themeMode == com.opentube.ui.screens.settings.ThemeMode.LIGHT -> false
        themeMode == com.opentube.ui.screens.settings.ThemeMode.SKY_BLUE -> false
        themeMode == com.opentube.ui.screens.settings.ThemeMode.LIGHT_GREEN -> false
        else -> true
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
