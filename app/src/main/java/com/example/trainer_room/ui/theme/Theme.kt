package com.example.trainer_room.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedColors(
    val surfaceHi: Color,
    val onMuted: Color,
    val primaryDim: Color,
    val androidGreen: Color,
    val outline: Color,
    val onShape: Color,
    val ok: Color,
    val tonalContent: Color,
    val deleteRed: Color,
    val isDark: Boolean
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceHi = DarkSurfaceHi,
        onMuted = DarkOnMuted,
        primaryDim = DarkPrimaryDim,
        androidGreen = DarkAndroidGreen,
        outline = DarkOutline,
        onShape = DarkOnShape,
        ok = DarkOk,
        tonalContent = DarkTonalContent,
        deleteRed = DeleteRedDark,
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
    outline = DarkOutline,
    surfaceVariant = DarkSurfaceHi
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    outline = LightOutline,
    surfaceVariant = LightSurfaceHi
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
            onShape = DarkOnShape,
            ok = DarkOk,
            tonalContent = DarkTonalContent,
            deleteRed = DeleteRedDark,
            isDark = true
        )
    } else {
        ExtendedColors(
            surfaceHi = LightSurfaceHi,
            onMuted = LightOnMuted,
            primaryDim = LightPrimaryDim,
            androidGreen = LightAndroidGreen,
            outline = LightOutline,
            onShape = LightOnShape,
            ok = LightOk,
            tonalContent = LightTonalContent,
            deleteRed = DeleteRedLight,
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
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
