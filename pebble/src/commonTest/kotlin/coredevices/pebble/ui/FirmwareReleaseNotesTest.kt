package coredevices.pebble.ui

import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class FirmwareReleaseNotesTest {
    @Test
    fun exactPebbleScreenshotsAreThreeTimesLarger() {
        assertEquals(Size(432f, 504f), releaseImageSize(Size(144f, 168f)))
        assertEquals(Size(540f, 540f), releaseImageSize(Size(180f, 180f)))
        assertEquals(Size(600f, 684f), releaseImageSize(Size(200f, 228f)))
    }

    @Test
    fun collagesAndOtherImagesKeepTheirOriginalSize() {
        for (size in listOf(Size(944f, 1176f), Size(200f, 200f), Size(400f, 456f), Size.Unspecified)) {
            assertEquals(size, releaseImageSize(size))
        }
    }
}
