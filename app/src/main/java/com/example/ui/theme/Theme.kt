package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val VakilDarkColorScheme = darkColorScheme(
    primary = DarkNavyPrimary,
    onPrimary = Color(0xFF071C36),
    primaryContainer = DarkNavyPrimaryContainer,
    onPrimaryContainer = DarkOnNavyPrimaryContainer,
    secondary = DarkBrassSecondary,
    onSecondary = Color(0xFF332200),
    secondaryContainer = DarkBrassSecondaryContainer,
    onSecondaryContainer = DarkOnBrassSecondaryContainer,
    tertiary = DarkTealTertiary,
    onTertiary = Color(0xFF00272A),
    tertiaryContainer = DarkTealTertiaryContainer,
    onTertiaryContainer = DarkOnTealTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF4A0007)
)

private val VakilLightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyPrimaryContainer,
    onPrimaryContainer = OnNavyPrimaryContainer,
    secondary = BrassSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrassSecondaryContainer,
    onSecondaryContainer = OnBrassSecondaryContainer,
    tertiary = TealTertiary,
    onTertiary = Color.White,
    tertiaryContainer = TealTertiaryContainer,
    onTertiaryContainer = OnTealTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = StatusCrimson,
    onError = Color.White
)

val VakilShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun VakilYarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VakilDarkColorScheme else VakilLightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VakilTypography,
            shapes = VakilShapes,
            content = content
        )
    }
}
