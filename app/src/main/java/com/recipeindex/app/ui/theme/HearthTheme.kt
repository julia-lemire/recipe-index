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

/**
 * Hearth Design System Theme
 *
 * Warm terracotta/clay color palette for home cooking aesthetic
 * Dark cards for browsing, light backgrounds for cooking mode
 */

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Clay,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = Clay,

    secondary = SageGreen,
    onSecondary = Cream,
    secondaryContainer = SageGreenLight,
    onSecondaryContainer = Clay,

    tertiary = ClayLight,
    onTertiary = Cream,

    background = Cream,
    onBackground = Clay,

    surface = Cream,
    onSurface = Clay,
    surfaceVariant = WarmGray,
    onSurfaceVariant = Clay,

    error = Color(0xFFB3261E),
    onError = Cream
)

private val DarkColorScheme = darkColorScheme(
    primary = Terracotta,
    onPrimary = DarkBrown,
    primaryContainer = TerracottaDark,
    onPrimaryContainer = Cream,

    secondary = SageGreen,
    onSecondary = DarkBrown,
    secondaryContainer = SageGreenLight,
    onSecondaryContainer = Cream,

    tertiary = ClayLight,
    onTertiary = Cream,

    background = DarkBrown,
    onBackground = Cream,

    surface = Clay,
    onSurface = Cream,
    surfaceVariant = ClayLight,
    onSurfaceVariant = Cream,

    error = Color(0xFFF2B8B5),
    onError = DarkBrown
)

@Composable
fun HearthTheme(
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
        typography = HearthTypography,
        content = content
    )
}
