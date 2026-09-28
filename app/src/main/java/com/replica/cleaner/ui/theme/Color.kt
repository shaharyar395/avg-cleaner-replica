package com.replica.cleaner.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette lifted from the reference recording: a deep navy shell with a single
 * saturated green as the action colour, amber for anything paywalled, and a
 * magenta/blue pair used only inside charts and legends.
 */

// Shell
val NavyBackground = Color(0xFF222C39)
val NavySurface = Color(0xFF2B3746)
val NavySurfaceHigh = Color(0xFF334152)
val NavyOutline = Color(0xFF3E4C5E)

val LightBackground = Color(0xFFF2F4F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFE9EDF2)
val LightOutline = Color(0xFFD7DDE5)

// Text
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFFA9B4C2)
val TextPrimaryLight = Color(0xFF16202B)
val TextSecondaryLight = Color(0xFF5E6B7A)

// Accents selectable on the Themes screen
val AccentGreen = Color(0xFF3CCB7F)
val AccentGreenDeep = Color(0xFF2FA866)
val AccentBlue = Color(0xFF3B9BE8)
val AccentBlueDeep = Color(0xFF2E7CBC)
val AccentPurple = Color(0xFF8B6FE8)
val AccentPurpleDeep = Color(0xFF6C51C4)

// Semantic
val Amber = Color(0xFFF7B32B)
val AmberDeep = Color(0xFFE09A12)
val Magenta = Color(0xFFE8567C)
val DangerRed = Color(0xFFE5484D)

// Chart series for Home free-space card (AVG reference order)
val ChartUnneeded = Color(0xFF4C9BE8)      // blue
val ChartHiddenCache = Color(0xFFF0A020)   // orange
val ChartReview = Color(0xFF3CCB7F)        // green
val ChartOther = Color(0xFF7A8899)
val ChartAudio = Color(0xFFC08A2E)
val ChartPhotos = Color(0xFF3CCB7F)
val ChartVideo = Color(0xFF55657A)

enum class AccentChoice(val light: Color, val dark: Color, val premium: Boolean) {
    Green(AccentGreenDeep, AccentGreen, false),
    Blue(AccentBlueDeep, AccentBlue, false),
    Purple(AccentPurpleDeep, AccentPurple, true)
}
