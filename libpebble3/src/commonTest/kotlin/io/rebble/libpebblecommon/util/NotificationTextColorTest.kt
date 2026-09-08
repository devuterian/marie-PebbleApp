package io.rebble.libpebblecommon.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationTextColorTest {
    @Test
    fun darkBrandBackgroundsUseWhite() {
        for (rgb in listOf(0x555555, 0x0055FF, 0x5555AA, 0x5500AA, 0xAA5500)) {
            assertEquals(-1, rgb.notificationTextColor())
        }
    }

    @Test
    fun lightBrandBackgroundsUseBlack() {
        for (rgb in listOf(0x00AA55, 0xFF5500, 0xFFFF00, 0x00AAAA, 0xAAAAFF)) {
            assertEquals(0xFF000000.toInt(), rgb.notificationTextColor())
        }
    }

    @Test
    fun userOverridesAreQuantizedLikeTheWatch() {
        assertEquals(-1, 0xFF747474.toInt().notificationTextColor())
        assertEquals(0xFF000000.toInt(), 0xFFAAAAAA.toInt().notificationTextColor())
        assertEquals(-1, 0xFF000000.toInt().notificationTextColor())
        assertEquals(0xFF000000.toInt(), 0xFFFFFFFF.toInt().notificationTextColor())
    }
}
