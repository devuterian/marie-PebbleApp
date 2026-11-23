package coredevices.util

import androidx.compose.ui.text.intl.Locale
import localization.clockTimeForLanguage

expect fun isLocale24HourFormat(): Boolean

fun formatTimeOfDay(hour: Int, minute: Int, is24Hour: Boolean = isLocale24HourFormat()): String =
    formatTimeOfDay(hour, minute, is24Hour, Locale.current.language)

internal fun formatTimeOfDay(hour: Int, minute: Int, is24Hour: Boolean, language: String): String {
    if (!is24Hour) return clockTimeForLanguage(hour, minute, language)
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}
