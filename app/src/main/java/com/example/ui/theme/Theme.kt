package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalButtonColor = compositionLocalOf { AccentIndigo }

@Composable
fun NameGenProTheme(
    darkTheme: Boolean = true,
    whiteTheme: Boolean = false,
    uiColor: Color = AccentIndigo,
    buttonColor: Color = AccentIndigo,
    content: @Composable () -> Unit,
) {
    val darkColorScheme = darkColorScheme(
        primary = uiColor,
        onPrimary = TextPrimary,
        primaryContainer = uiColor.copy(alpha = 0.7f),
        onPrimaryContainer = TextPrimary,
        secondary = uiColor,
        onSecondary = TextPrimary,
        secondaryContainer = SurfaceVariant,
        onSecondaryContainer = TextPrimary,
        background = BackgroundDark,
        onBackground = TextPrimary,
        surface = SurfaceDark,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = TextSecondary,
        outline = OutlineColor
    )
    val lightColorScheme = lightColorScheme(
        primary = uiColor,
        onPrimary = Color.White,
        primaryContainer = uiColor.copy(alpha = 0.2f),
        onPrimaryContainer = Color.Black,
        secondary = uiColor,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0E0E0),
        onSecondaryContainer = Color.Black,
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF111111),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111111),
        surfaceVariant = Color(0xFFEFEFF4),
        onSurfaceVariant = Color(0xFF555555),
        outline = Color(0xFFCCCCCC)
    )
    val whiteColorScheme = lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0E0E0),
        onPrimaryContainer = Color.Black,
        secondary = Color.Black,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF0F0F0),
        onSecondaryContainer = Color.Black,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFF8F8F8),
        onSurface = Color(0xFF000000),
        surfaceVariant = Color(0xFFEEEEEE),
        onSurfaceVariant = Color(0xFF444444),
        outline = Color(0xFFDDDDDD)
    )

    val colorScheme = when {
        whiteTheme -> whiteColorScheme
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val finalButtonColor = if (whiteTheme) Color.Black else buttonColor

    CompositionLocalProvider(LocalButtonColor provides finalButtonColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
