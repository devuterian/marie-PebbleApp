package io.rebble.libpebblecommon.notification.processor

import io.rebble.libpebblecommon.packets.blobdb.TimelineIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationPropertiesTest {
    @Test
    fun repacksUseOriginalServiceIcons() {
        assertEquals(TimelineIcon.NotificationTelegram, NotificationProperties.lookup("fork.risin42.nagramx")?.icon)
        assertEquals(TimelineIcon.NotificationDiscord, NotificationProperties.lookup("com.aliucord")?.icon)
        assertNull(NotificationProperties.lookup("com.aliucord.manager"))
    }

    @Test
    fun repackedCameraIsNotClassifiedAsTiktok() {
        assertNull(NotificationProperties.lookup("com.ss.android.ugc.aweme"))
    }

    @Test
    fun packageMatchingRemainsCaseInsensitive() {
        assertEquals(TimelineIcon.NotificationChatGPT, NotificationProperties.lookup("COM.OPENAI.CHATGPT")?.icon)
    }
}
