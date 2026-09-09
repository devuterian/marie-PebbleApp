package coredevices.pebble.firmware

import io.rebble.libpebblecommon.connection.FirmwareUpdateCheckResult
import io.rebble.libpebblecommon.services.FirmwareVersion
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class GitHubFirmwareTest {
    private fun running(tag: String, recovery: Boolean = false) = FirmwareVersion.from(
        tag, recovery, "", Instant.fromEpochSeconds(1_900_000_000), true, false,
    )!!

    private fun release(tag: String = "v4.37.0-ver004-donut") = GitHubFirmwareRelease(
        tag, false, false, "바뀐 점", listOf(
            GitHubFirmwareAsset("normal_obelix_pvt_${tag}.pbz", "https://github.com/firmware.pbz"),
            GitHubFirmwareAsset("Pebble.apk", "https://github.com/app.apk"),
        ),
    )

    @Test
    fun findsNewerCustomRevisionAndExactHardwareAsset() {
        val result = assertIs<FirmwareUpdateCheckResult.FoundUpdate>(
            release().updateFor("obelix_pvt", running("v4.37.0-ver003-castella")),
        )
        assertEquals("v4.37.0-ver004-donut", result.version.stringVersion)
        assertEquals("https://github.com/firmware.pbz", result.url)
        assertEquals("바뀐 점", result.notes)
    }

    @Test
    fun doesNotReofferSameOrOlderVersion() {
        for (tag in listOf("v4.37.0-ver004-donut", "v4.37.0-ver005-eclair", "v4.38.0-ver001-a")) {
            assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
                release().updateFor("obelix_pvt", running(tag)))
        }
    }

    @Test
    fun comparesRevisionNumericallyAndBaseVersionFirst() {
        assertIs<FirmwareUpdateCheckResult.FoundUpdate>(release("v4.37.0-ver010-jelly")
            .updateFor("obelix_pvt", running("v4.37.0-ver009-ice")))
        assertIs<FirmwareUpdateCheckResult.FoundUpdate>(release("v4.38.0-ver011-k")
            .updateFor("obelix_pvt", running("v4.37.0-ver999-z")))
    }

    @Test
    fun excludesDraftsPrereleasesAndWrongHardware() {
        val current = running("v4.37.0-ver003-castella")
        assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
            release().copy(prerelease = true).updateFor("obelix_pvt", current))
        assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
            release().copy(draft = true).updateFor("obelix_pvt", current))
        assertIs<FirmwareUpdateCheckResult.UpdateCheckFailed>(release().updateFor("silk", current))
        assertIs<FirmwareUpdateCheckResult.UpdateCheckFailed>(release().copy(assets = emptyList())
            .updateFor("obelix_pvt", current))
    }

    @Test
    fun supportsStockAndRecoveryFirmware() {
        assertIs<FirmwareUpdateCheckResult.FoundUpdate>(release()
            .updateFor("obelix_pvt", running("v4.37.0")))
        assertIs<FirmwareUpdateCheckResult.FoundUpdate>(release()
            .updateFor("obelix_pvt", running("v4.37.0-ver004-donut", recovery = true)))
    }

    @Test
    fun usesTagRatherThanRenamedReleaseTitle() {
        val json = """{"tag_name":"v4.37.0-ver004-donut","name":"v4.37.0-ver004-dubai chewy cookie", "draft":false,"prerelease":false,"body":null,"assets":[{"name":"normal_obelix_pvt_v4.37.0-ver004-donut.pbz","browser_download_url":"https://github.com/firmware.pbz"}]}"""
        val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<GitHubFirmwareRelease>(json)
        assertIs<FirmwareUpdateCheckResult.FoundUpdate>(parsed.updateFor("obelix_pvt",
            running("v4.37.0-ver003-castella")))
    }

    @Test
    fun rejectsMalformedTags() {
        assertIs<FirmwareUpdateCheckResult.UpdateCheckFailed>(release("not-a-version")
            .updateFor("obelix_pvt", running("v4.37.0")))
    }

    @Test
    fun officialReturnAllowsSameBaseAndOlderFirmware() {
        for (tag in listOf("v4.37.0", "v4.36.2")) {
            val update = assertIs<FirmwareUpdateCheckResult.FoundUpdate>(
                release(tag).officialUpdateFor("obelix_pvt", running("v4.37.0-ver005-egg-salad"), true))
            assertEquals(tag, update.version.stringVersion)
            assertEquals(true, update.canDowngrade)
        }
    }

    @Test
    fun officialUpdatesDoNotLoopAfterReturningToStock() {
        assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
            release("v4.37.0").officialUpdateFor("obelix_pvt", running("v4.37.0"), false))
        assertIs<FirmwareUpdateCheckResult.FoundUpdate>(
            release("v4.38.0").officialUpdateFor("obelix_pvt", running("v4.37.0"), false))
    }

    @Test
    fun officialReturnRejectsUnsafeOrMissingBundles() {
        val current = running("v4.37.0-ver005-egg-salad")
        for (r in listOf(release("v4.37.0").copy(draft = true), release("v4.37.0").copy(prerelease = true))) {
            assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate, r.officialUpdateFor("obelix_pvt", current, true))
        }
        for (r in listOf(release("v4.37.0-beta1"), release("v4.37.0").copy(assets = listOf(
            GitHubFirmwareAsset("normal_obelix_pvt_v4.37.0_slot0.pbz", "unused"))),
            release("v4.37.0").copy(assets = emptyList()))) {
            assertIs<FirmwareUpdateCheckResult.UpdateCheckFailed>(r.officialUpdateFor("obelix_pvt", current, true))
        }
    }

    @Test
    fun onlyMarieFirmwareEnablesExplicitReturn() {
        assertEquals(true, running("v4.37.0-ver005-egg-salad").isMarieFirmware())
        for (tag in listOf("v4.37.0", "v4.37.0-beta1", "v4.37.0-1-g123abcd")) {
            assertEquals(false, running(tag).isMarieFirmware())
        }
    }
    @Test
    fun prereleaseChannelUsesPublishedEligibleReleaseAndSkipsDrafts() {
        val stable = release("v4.37.0-ver005-egg-salad").copy(publishedAt = "2026-09-09T05:00:00Z")
        val preview = release("v4.37.0-ver006-financier").copy(prerelease = true, publishedAt = "2026-09-09T09:00:00Z")
        val draft = release("v4.37.0-ver007-g").copy(draft = true, publishedAt = "2026-09-09T10:00:00Z")
        val otherWatch = release("v4.37.0-ver008-h").copy(assets = emptyList(), publishedAt = "2026-09-09T11:00:00Z")
        val result = assertIs<FirmwareUpdateCheckResult.FoundUpdate>(
            listOf(otherWatch, stable, draft, preview).latestUpdateFor("obelix_pvt", running("v4.37.0-ver004-donut")))
        assertEquals(preview.tag, result.version.stringVersion)
        assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
            preview.updateFor("obelix_pvt", running("v4.37.0-ver005-egg-salad")))
        assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
            listOf(preview).latestUpdateFor("obelix_pvt", running(preview.tag)))
    }

    @Test
    fun prereleaseChannelRejectsUnpublishedInvalidDatesAndNeverDowngrades() {
        val preview = release().copy(prerelease = true)
        for (date in listOf(null, "bad date")) {
            assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
                listOf(preview.copy(publishedAt = date)).latestUpdateFor("obelix_pvt", running("v4.37.0")))
        }
        assertEquals(FirmwareUpdateCheckResult.FoundNoUpdate,
            listOf(preview.copy(publishedAt = "2026-09-09T05:00:00Z"))
                .latestUpdateFor("obelix_pvt", running("v4.37.0-ver005-egg-salad")))
    }
}
