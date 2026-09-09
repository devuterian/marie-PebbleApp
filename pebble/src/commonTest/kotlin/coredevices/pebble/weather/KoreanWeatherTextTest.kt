package coredevices.pebble.weather

import kotlin.test.Test
import kotlin.test.assertEquals

class KoreanWeatherTextTest {
    @Test
    fun translatesActualServerNarrativeAndKeepsAttribution() {
        assertEquals("대체로 맑음. 최고 25.1°C. 북동풍 12 km/h. 날씨 정보: Open-Meteo",
            weatherText("Mainly clear. High of 25.1C. Winds NE at 12. Powered by Open-Meteo", "ko-KR"))
        assertEquals("맑음. 최저 -4°F. 남풍 5 mph. 날씨 정보: Open-Meteo",
            weatherText("Clear. Low of -4F. Winds S at 5. Powered by Open-Meteo", "ko"))
    }

    @Test
    fun preservesOtherLanguagesAndUnknownNarratives() {
        assertEquals("Mainly clear", weatherText("Mainly clear", "en"))
        assertEquals("이미 한국어인 예보", weatherText("이미 한국어인 예보", "ko"))
        assertEquals("Unrecognized forecast", weatherText("Unrecognized forecast", "ko"))
        assertEquals("구름 조금", weatherText("Partly cloudy", "ko_KR"))
    }
}
