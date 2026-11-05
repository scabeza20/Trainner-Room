package com.example.trainer_room.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val surfaceHi: Color,
    val onMuted: Color,
    val primaryDim: Color,
    val androidGreen: Color,
    val outline: Color,
    val isDark: Boolean
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceHi = DarkSurfaceHi,
        onMuted = DarkOnMuted,
        primaryDim = DarkPrimaryDim,
        androidGreen = DarkAndroidGreen,
        outline = DarkOutline,
        isDark = true
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    outline = LightOutline
)

@Composable
fun TrainerRoomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            surfaceHi = DarkSurfaceHi,
            onMuted = DarkOnMuted,
            primaryDim = DarkPrimaryDim,
            androidGreen = DarkAndroidGreen,
            outline = DarkOutline,
            isDark = true
        )
    } else {
        ExtendedColors(
            surfaceHi = LightSurfaceHi,
            onMuted = LightOnMuted,
            primaryDim = LightPrimaryDim,
            androidGreen = LightAndroidGreen,
            outline = LightOutline,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object TrainerRoomTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
