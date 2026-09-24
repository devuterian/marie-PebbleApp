package coredevices.coreapp.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MarieAppVersionTest {
    @Test fun readsIndependentAppRevision() {
        assertEquals(16, marieAppRevision("1.12.0.1-marie-ver016"))
        assertEquals(16, marieAppRevision("Pebble-1.12.0.1-marie-ver016.apk"))
        assertTrue(marieAppRevision("Pebble-1.12.0.1-marie-ver100.apk")!! > marieAppRevision("Pebble-1.12.0.1-marie-ver099.apk")!!)
    }
    @Test fun rejectsFirmwareAndMalformedVersions() {
        for (name in listOf("v4.37.0-ver007-gelato", "1.12.0.1", "marie-verx", "marie-ver999999999999", "marie-ver016.apk.exe")) assertNull(marieAppRevision(name))
    }
    @Test fun limitsAssetsToOwnReleaseAndRequiresDigest() {
        val url = "https://github.com/devuterian/PebbleOAO/releases/download/v1/Pebble-1-marie-ver016.apk"
        val digest = "sha256:" + "a".repeat(64)
        assertTrue(validAppAsset(url, digest))
        assertFalse(validAppAsset(url.replace("https:", "http:"), digest))
        assertFalse(validAppAsset(url.replace("github.com/", "github.com.evil.example/"), digest))
        assertFalse(validAppAsset(url.replace("devuterian", "someone"), digest))
        assertFalse(validAppAsset(url, ""))
        assertFalse(validAppAsset(url, "sha256:1234"))
    }
}
