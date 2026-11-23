package coredevices.util

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatTest {
    @Test
    fun twentyFourHour() {
        assertEquals("00:05", formatTimeOfDay(0, 5, is24Hour = true, language = "en"))
        assertEquals("23:32", formatTimeOfDay(23, 32, is24Hour = true, language = "en"))
    }

    @Test
    fun twelveHour() {
        assertEquals("12:05 AM", formatTimeOfDay(0, 5, is24Hour = false, language = "en"))
        assertEquals("12:00 PM", formatTimeOfDay(12, 0, is24Hour = false, language = "en"))
        assertEquals("11:32 PM", formatTimeOfDay(23, 32, is24Hour = false, language = "en"))
    }

    @Test
    fun twelveHourKorean() {
        assertEquals("오전 12:05", formatTimeOfDay(0, 5, is24Hour = false, language = "ko"))
        assertEquals("오후 11:32", formatTimeOfDay(23, 32, is24Hour = false, language = "ko"))
    }
}
