package io.rebble.libpebblecommon.connection

import io.rebble.libpebblecommon.metadata.WatchType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarieTimeBluetoothTest {
    @Test
    fun timeAndSteelRemainDiscoverableOverBle() {
        assertFalse(shouldHideWatchFromBleScan(WatchType.BASALT))
        assertFalse(shouldHideWatchFromBleScan(WatchType.EMERY))
        assertTrue(shouldHideWatchFromBleScan(WatchType.APLITE))
        assertTrue(shouldHideWatchFromBleScan(WatchType.CHALK))
    }

    @Test
    fun customTimeBondIsNotMigratedToUnsupportedClassicTransport() {
        assertTrue("v4.3.0-ver001-apple-pie".isMarieTimeFirmware())
        assertTrue("4.3.0-ver002".isMarieTimeFirmware())
        assertFalse("v4.3.0".isMarieTimeFirmware())
        assertFalse("v4.37.0-ver006-flan".isMarieTimeFirmware())
    }
}
