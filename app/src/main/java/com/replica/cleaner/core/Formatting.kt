package com.replica.cleaner.core

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * The reference app formats sizes in decimal units (kB / MB / GB), which is what
 * Android's own storage UI uses, and drops the decimal once a number reaches 100.
 */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1000.0 && unit < units.lastIndex) {
        value /= 1000.0
        unit++
    }
    val decimals = when {
        unit == 0 -> 0
        value >= 100 -> 0
        value >= 10 -> 1
        else -> 2
    }
    return String.format(Locale.US, "%.${decimals}f %s", value, units[unit])
}

/** Splits a size into number and unit so the UI can style them separately. */
fun formatBytesParts(bytes: Long): Pair<String, String> {
    val whole = formatBytes(bytes)
    val idx = whole.indexOf(' ')
    return if (idx == -1) whole to "" else whole.substring(0, idx) to whole.substring(idx + 1)
}

fun formatPercent(fraction: Float): String =
    String.format(Locale.US, "%d", (fraction.coerceIn(0f, 1f) * 100).toInt())

/** "141:18:49" — the up-time format the System Info screen uses. */
fun formatUptime(millis: Long): String {
    val h = TimeUnit.MILLISECONDS.toHours(millis)
    val m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format(Locale.US, "%d:%02d:%02d", h, m, s)
}

/** "0s", "12m", "3h 20m" — used by the app-diary / screen-time rows. */
fun formatDuration(millis: Long): String {
    if (millis < 1000) return "0s"
    val h = TimeUnit.MILLISECONDS.toHours(millis)
    val m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m"
        else -> "${s}s"
    }
}

fun formatDaysAgo(timestamp: Long): String {
    if (timestamp <= 0L) return "Never"
    val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timestamp)
    return when {
        days <= 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 30L -> "$days days ago"
        days < 365L -> "${days / 30} months ago"
        else -> "${days / 365} years ago"
    }
}
