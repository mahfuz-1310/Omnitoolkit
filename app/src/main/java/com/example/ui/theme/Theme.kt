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
        primary = uiColor,
        onPrimary = Color.White,
        primaryContainer = uiColor.copy(alpha = 0.12f),
        onPrimaryContainer = uiColor,
        secondary = uiColor,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF2F4F7),
        onSecondaryContainer = Color(0xFF1D2939),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF101828),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF101828),
        surfaceVariant = Color(0xFFF2F4F7),
        onSurfaceVariant = Color(0xFF475467),
        outline = Color(0xFFD0D5DD),
        outlineVariant = Color(0xFFE4E7EC)
    )

    val colorScheme = when {
        whiteTheme -> whiteColorScheme
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val finalButtonColor = if (whiteTheme) uiColor else buttonColor

    CompositionLocalProvider(LocalButtonColor provides finalButtonColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
