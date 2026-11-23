package coredevices.util.storage

import io.rebble.libpebblecommon.connection.AppContext
import localization.localized

data class StorageUsage(val label: String, val bytes: Long)

/**
 * On-disk size of the app's own container, by top-level directory. Walks the tree, so it blocks —
 * call off the main thread.
 */
expect fun AppContext.appStorageBreakdown(): List<StorageUsage>

fun formatStorageSummary(usage: List<StorageUsage>): String = buildString {
    append("\nStorage usage:")
    usage.sortedByDescending { it.bytes }.forEach {
        append("\n  ${it.label}: ${formatBytes(it.bytes)}")
    }
    append("\n  total: ${formatBytes(usage.sumOf { it.bytes })}")
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble() / 1024
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    val rounded = (value * 10).toLong() / 10.0
    return "$rounded ${units[unit]}"
}

/** "12.3 MB · 1.1 MB mobile · 345 requests", or with [split] sent and received separately. */
fun formatDataUsage(sentBytes: Long, receivedBytes: Long, mobileBytes: Long, requests: Long, split: Boolean = false): String =
    listOfNotNull(
        if (split) localized("${formatBytes(sentBytes)} sent", "${formatBytes(sentBytes)} 보냄") else (sentBytes + receivedBytes).takeIf { it > 0 }?.let(::formatBytes),
        if (split) localized("${formatBytes(receivedBytes)} received", "${formatBytes(receivedBytes)} 받음") else null,
        mobileBytes.takeIf { it > 0 }?.let { localized("${formatBytes(it)} mobile", "모바일 ${formatBytes(it)}") },
        localized(if (requests == 1L) "1 request" else "$requests requests", "요청 ${requests}회"),
    ).joinToString(" · ")
