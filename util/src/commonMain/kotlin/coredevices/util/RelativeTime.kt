package coredevices.util

import localization.localized
import kotlin.time.Clock
import kotlin.time.Instant

/** "just now" / "5m ago" / "3h ago" / "2d ago": a short relative time. */
fun relativeTime(at: Instant): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val s = ((now - at.toEpochMilliseconds()) / 1000L).toInt()
    if (s < 60) return localized("just now", "방금")
    val m = s / 60
    if (m < 60) return localized("${m}m ago", "${m}분 전")
    val h = m / 60
    if (h < 24) return localized("${h}h ago", "${h}시간 전")
    return localized("${h / 24}d ago", "${h / 24}일 전")
}
