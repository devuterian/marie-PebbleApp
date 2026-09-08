package io.rebble.libpebblecommon.util

import kotlin.math.pow

// Use the final background, including user overrides, after Pebble quantization.
internal fun Int.notificationTextColor(): Int {
    fun linear(shift: Int): Double {
        val value = (((this shr shift) and 0xFF) / 85 * 85) / 255.0
        return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
    val luminance = 0.2126 * linear(16) + 0.7152 * linear(8) + 0.0722 * linear(0)
    return if (luminance > 0.179) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
}
