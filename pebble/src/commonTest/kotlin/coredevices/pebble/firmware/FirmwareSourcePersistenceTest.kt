package coredevices.pebble.firmware

import coredevices.util.CoreConfig
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class FirmwareSourcePersistenceTest {
    @Test
    fun existingConfigsKeepCustomUpdatesAndSelectionIsPerWatch() {
        val old = Json.decodeFromString<CoreConfig>("{}")
        assertEquals(emptySet(), old.officialFirmwareWatches)
        val saved = old.copy(officialFirmwareWatches = setOf("WATCH-A"))
        val restored = Json.decodeFromString<CoreConfig>(Json.encodeToString(saved))
        assertEquals(setOf("WATCH-A"), restored.officialFirmwareWatches)
        assertEquals(false, "WATCH-B" in restored.officialFirmwareWatches)
    }
    @Test
    fun previewOptInPersistsSeparatelyAndDefaultsOffForOldConfigs() {
        val old = Json.decodeFromString<CoreConfig>("{}")
        assertEquals(emptySet(), old.prereleaseFirmwareWatches)
        val saved = old.copy(prereleaseFirmwareWatches = setOf("WATCH-A"),
            officialFirmwareWatches = setOf("WATCH-B"))
        val restored = Json.decodeFromString<CoreConfig>(Json.encodeToString(saved))
        assertEquals(setOf("WATCH-A"), restored.prereleaseFirmwareWatches)
        assertEquals(setOf("WATCH-B"), restored.officialFirmwareWatches)
        val disabled = restored.copy(prereleaseFirmwareWatches = restored.prereleaseFirmwareWatches - "WATCH-A")
        assertEquals(emptySet(), Json.decodeFromString<CoreConfig>(Json.encodeToString(disabled)).prereleaseFirmwareWatches)
    }
}
