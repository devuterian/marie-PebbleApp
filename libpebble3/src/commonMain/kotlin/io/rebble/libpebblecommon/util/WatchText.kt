package io.rebble.libpebblecommon.util

import androidx.compose.ui.text.intl.Locale

internal fun isKoreanLanguage(language: String): Boolean =
    language.substringBefore('-').substringBefore('_').equals("ko", ignoreCase = true)

fun watchText(english: String, korean: String, language: String = Locale.current.language): String =
    if (isKoreanLanguage(language)) korean else english
