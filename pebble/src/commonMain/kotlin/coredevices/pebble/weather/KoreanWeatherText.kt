package coredevices.pebble.weather

import androidx.compose.ui.text.intl.Locale

private val conditions = mapOf(
    "Clear sky" to "맑음", "Clear" to "맑음", "Sunny" to "맑음",
    "Mainly clear" to "대체로 맑음", "Partly cloudy" to "구름 조금",
    "Overcast" to "흐림", "Cloudy" to "흐림", "Fog" to "안개",
    "Depositing rime fog" to "얼음 안개", "Light drizzle" to "약한 이슬비",
    "Moderate drizzle" to "이슬비", "Dense drizzle" to "강한 이슬비",
    "Light freezing drizzle" to "약한 어는 이슬비", "Dense freezing drizzle" to "강한 어는 이슬비",
    "Slight rain" to "약한 비", "Light rain" to "약한 비", "Moderate rain" to "비",
    "Heavy rain" to "강한 비", "Light freezing rain" to "약한 어는 비",
    "Heavy freezing rain" to "강한 어는 비", "Slight snow fall" to "약한 눈",
    "Moderate snow fall" to "눈", "Heavy snow fall" to "많은 눈",
    "Light snow" to "약한 눈", "Heavy snow" to "많은 눈", "Snow grains" to "싸락눈",
    "Slight rain showers" to "약한 소나기", "Moderate rain showers" to "소나기",
    "Violent rain showers" to "강한 소나기", "Slight snow showers" to "약한 소낙눈",
    "Heavy snow showers" to "강한 소낙눈", "Thunderstorm" to "뇌우",
    "Thunderstorm with slight hail" to "우박을 동반한 뇌우",
    "Thunderstorm with heavy hail" to "강한 우박을 동반한 뇌우",
)

internal fun weatherText(text: String, language: String = Locale.current.language): String {
    if (!language.substringBefore('-').substringBefore('_').equals("ko", ignoreCase = true)) return text
    conditions.entries.firstOrNull { it.key.equals(text, ignoreCase = true) }?.let { return it.value }
    val match = Regex("""^(.+)\. (High|Low) of (-?[\d.]+)(C|F)\. Winds ([A-Z]+) at ([\d.]+)\. Powered by (.+)$""")
        .matchEntire(text) ?: return text
    val (condition, highLow, temp, unit, direction, speed, provider) = match.destructured
    val directions = mapOf("N" to "북", "NNE" to "북북동", "NE" to "북동", "ENE" to "동북동",
        "E" to "동", "ESE" to "동남동", "SE" to "남동", "SSE" to "남남동",
        "S" to "남", "SSW" to "남남서", "SW" to "남서", "WSW" to "서남서",
        "W" to "서", "WNW" to "서북서", "NW" to "북서", "NNW" to "북북서")
    val temperatureLabel = if (highLow == "High") "최고" else "최저"
    val speedUnit = if (unit == "C") "km/h" else "mph"
    return "${weatherText(condition, language)}. $temperatureLabel $temp°$unit. " +
        "${directions[direction] ?: direction}풍 $speed $speedUnit. 날씨 정보: $provider"
}
