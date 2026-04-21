package com.recipeindex.app.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Sage,
    onPrimary = White,
    primaryContainer = SagePale,
    onPrimaryContainer = Ink,

    secondary = Accent,
    onSecondary = White,
    secondaryContainer = AccentPale,
    onSecondaryContainer = Ink,

    tertiary = SageDark,
    onTertiary = White,
    tertiaryContainer = SageBackground,
    onTertiaryContainer = Ink,

    background = Background,
    onBackground = Ink,

    surface = White,
    onSurface = Ink,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = InkMid,

    outline = Border,
    outlineVariant = BorderStrong,

    error = Danger,
    onError = White,
    errorContainer = DangerSurface,
    onErrorContainer = Ink,
)

private val DarkColorScheme = darkColorScheme(
    primary = Sage,
    onPrimary = Ink,
    primaryContainer = SageDark,
    onPrimaryContainer = White,

    secondary = Accent,
    onSecondary = Ink,
    secondaryContainer = AccentPale,
    onSecondaryContainer = Ink,

    tertiary = SagePale,
    onTertiary = Ink,
    tertiaryContainer = SageDark,
    onTertiaryContainer = White,

    background = Ink,
    onBackground = White,

    surface = InkMid,
    onSurface = White,
    surfaceVariant = Color(0xFF3A4A3E),
    onSurfaceVariant = SagePale,

    outline = Color(0xFF4A5A4E),
    outlineVariant = Color(0xFF3A4A3E),

    error = Color(0xFFCF6679),
    onError = Ink,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = White,
)

@Composable
fun RecipeIndexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = RecipeIndexTypography,
        content = content
    )
}
