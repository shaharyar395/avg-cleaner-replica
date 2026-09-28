package com.replica.cleaner.l10n

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

/**
 * Supported app languages. Display names match the Settings / Language screen list.
 * Default is English.
 */
data class AppLanguage(
    val tag: String,
    val displayName: String,
    val rtl: Boolean = false
) {
    val layoutDirection: LayoutDirection
        get() = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    companion object {
        val DEFAULT = AppLanguage("en", "English")

        val all: List<AppLanguage> = listOf(
            AppLanguage("ca", "Català"),
            AppLanguage("da", "Dansk"),
            AppLanguage("de", "Deutsch"),
            AppLanguage("en", "English"),
            AppLanguage("es", "Español"),
            AppLanguage("fr", "Français"),
            AppLanguage("id", "Indonesia"),
            AppLanguage("it", "Italiano"),
            AppLanguage("hu", "Magyar"),
            AppLanguage("nl", "Nederlands"),
            AppLanguage("nb", "Norsk bokmål"),
            AppLanguage("pl", "Polski"),
            AppLanguage("pt-BR", "Português"),
            AppLanguage("pt-PT", "Português (Portugal)"),
            AppLanguage("ro", "Română"),
            AppLanguage("sk", "Slovenčina"),
            AppLanguage("fi", "Suomi"),
            AppLanguage("sv", "Svenska"),
            AppLanguage("tr", "Türkçe"),
            AppLanguage("cs", "Čeština"),
            AppLanguage("el", "Ελληνικά"),
            AppLanguage("bg", "Български"),
            AppLanguage("ru", "Русский"),
            AppLanguage("uk", "Українська"),
            AppLanguage("ar", "العربية", rtl = true),
            AppLanguage("hi", "हिन्दी"),
            AppLanguage("th", "ไทย"),
            AppLanguage("ko", "한국어"),
            AppLanguage("zh-CN", "中文 (简体)"),
            AppLanguage("ja", "日本語"),
            AppLanguage("vi", "Tiếng Việt")
        )

        fun fromDisplayName(name: String): AppLanguage =
            all.find { it.displayName == name } ?: DEFAULT

        fun fromTag(tag: String): AppLanguage =
            all.find { it.tag.equals(tag, ignoreCase = true) } ?: DEFAULT
    }
}

val LocalL10n = staticCompositionLocalOf { L10n.english }
