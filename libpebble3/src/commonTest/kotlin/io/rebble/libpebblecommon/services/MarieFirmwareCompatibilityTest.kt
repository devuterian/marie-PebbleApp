package io.rebble.libpebblecommon.services

import io.rebble.libpebblecommon.packets.WatchFirmwareVersion
import io.rebble.libpebblecommon.util.DataBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MarieFirmwareCompatibilityTest {
    @Test
    fun stockParserAcceptsMarieVersionAndBothSlotFlags() {
        for (slot in 0..1) {
            val packet = WatchFirmwareVersion().apply {
                versionTag.set("v4.37.0-ver005-egg-salad")
                gitHash.set("73d7ec7")
                timestamp.set(1788932279u)
                flags.set(if (slot == 0) 14u else 6u)
            }
            val received = WatchFirmwareVersion().apply { fromBytes(DataBuffer(packet.toBytes())) }
            val version = assertNotNull(received.firmwareVersion())
            assertEquals("v4.37.0-ver005-egg-salad", version.stringVersion)
            assertEquals(4, version.major)
            assertEquals(37, version.minor)
            assertEquals(0, version.patch)
            assertEquals(false, version.isRecovery)
            assertEquals(true, version.isDualSlot)
            assertEquals(slot == 0, version.isSlot0)
        }
    }
}
