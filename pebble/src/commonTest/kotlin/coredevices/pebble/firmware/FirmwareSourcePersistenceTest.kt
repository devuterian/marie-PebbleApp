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
}
