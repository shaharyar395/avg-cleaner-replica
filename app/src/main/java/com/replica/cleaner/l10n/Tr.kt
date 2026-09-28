package com.replica.cleaner.l10n

import androidx.compose.runtime.Composable

/**
 * Looks up [english] in the active language pack.
 * Uses the English UI string as the dictionary key so call sites stay readable:
 * `tr("Quick Clean")` → localized equivalent.
 *
 * Order: exact phrase override → slug key (e.g. "quick_clean") → English fallback.
 * Identity entries that merely echo English must not block slug-based keyed translations.
 */
fun L10n.tr(english: String): String {
    val phrase = values[english]
    if (phrase != null && phrase != english) return phrase

    val slug = english.lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
    values[slug]?.let { return it }
    englishValues[slug]?.let { return it }

    // Intentional same-as-English phrase, or unknown string
    if (phrase != null) return phrase
    return english
}

@Composable
fun tr(english: String): String = LocalL10n.current.tr(english)
