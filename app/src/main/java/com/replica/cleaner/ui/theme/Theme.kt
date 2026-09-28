package com.replica.cleaner.ui.theme

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
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Settings > Themes offers exactly these three modes. */
enum class ThemeMode { Dark, Light, System }

/**
 * Extra roles Material3's ColorScheme has no slot for: the raised card colour,
 * the muted secondary text, and the fixed semantic colours.
 */
@Immutable
data class CleanerColors(
    val card: Color,
    val cardHigh: Color,
    val divider: Color,
    val textSecondary: Color,
    val amber: Color,
    val magenta: Color,
    val danger: Color,
    val isDark: Boolean
)

val LocalCleanerColors = staticCompositionLocalOf {
    CleanerColors(
        card = NavySurface,
        cardHigh = NavySurfaceHigh,
        divider = NavyOutline,
        textSecondary = TextSecondaryDark,
        amber = Amber,
        magenta = Magenta,
        danger = DangerRed,
        isDark = true
    )
}

private fun darkScheme(accent: AccentChoice) = darkColorScheme(
    primary = accent.dark,
    onPrimary = Color(0xFF07281A),
    primaryContainer = accent.deepOrSelf(),
    onPrimaryContainer = Color.White,
    secondary = accent.dark,
    onSecondary = Color(0xFF07281A),
    background = NavyBackground,
    onBackground = TextPrimaryDark,
    surface = NavyBackground,
    onSurface = TextPrimaryDark,
    surfaceVariant = NavySurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = NavyOutline,
    outlineVariant = NavyOutline,
    error = DangerRed,
    onError = Color.White
)

private fun lightScheme(accent: AccentChoice) = lightColorScheme(
    primary = accent.light,
    onPrimary = Color.White,
    primaryContainer = accent.light,
    onPrimaryContainer = Color.White,
    secondary = accent.light,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightBackground,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurface,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = DangerRed,
    onError = Color.White
)

private fun AccentChoice.deepOrSelf(): Color = light

@Composable
fun CleanerTheme(
    mode: ThemeMode = ThemeMode.Dark,
    accent: AccentChoice = AccentChoice.Green,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.System -> isSystemInDarkTheme()
    }

    val scheme = if (dark) darkScheme(accent) else lightScheme(accent)
    val extras = if (dark) {
        CleanerColors(NavySurface, NavySurfaceHigh, NavyOutline, TextSecondaryDark, Amber, Magenta, DangerRed, true)
    } else {
        CleanerColors(LightSurface, LightSurfaceHigh, LightOutline, TextSecondaryLight, AmberDeep, Magenta, DangerRed, false)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(LocalCleanerColors provides extras) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CleanerTypography,
            content = content
        )
    }
}

/** Shorthand so screens can write `CleanerTheme.colors.card`. */
object CleanerThemeTokens {
    val colors: CleanerColors
        @Composable get() = LocalCleanerColors.current
}
