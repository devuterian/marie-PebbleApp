package io.rebble.libpebblecommon.calendar

import io.rebble.libpebblecommon.database.entity.BaseAttribute
import io.rebble.libpebblecommon.database.entity.CalendarEntity
import io.rebble.libpebblecommon.packets.blobdb.TimelineAttribute
import io.rebble.libpebblecommon.util.watchText
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class KoreanCalendarPinTest {
    @Test
    fun koreanPinPreservesEventTextAndActionDispatch() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.KOREA)
            val pin = CalendarEvent(
                id = "event", calendarId = "cal", title = "Lunch with Jane",
                description = "Bring notes", location = "Cafe",
                startTime = Instant.fromEpochSeconds(1000), endTime = Instant.fromEpochSeconds(2000),
                allDay = false, recurs = true, reminders = emptyList(),
                availability = CalendarEvent.Availability.Busy, status = CalendarEvent.Status.Confirmed,
                baseEventId = "event", attendees = listOf(EventAttendee(
                    name = "Jane", email = null, role = null, isCurrentUser = true,
                    attendanceStatus = EventAttendee.AttendanceStatus.Invited,
                )),
            ).toTimelinePin(CalendarEntity(
                platformId = "cal", name = "Work", ownerName = "Jane", ownerId = "jane",
                color = 0, enabled = true,
            ), supportsRsvpActions = true)
            val texts = pin.content.attributes.filterIsInstance<BaseAttribute.TextAttribute>()
            assertEquals("Lunch with Jane", texts.single { it.attribute == TimelineAttribute.Title }.text)
            val lists = pin.content.attributes.filterIsInstance<BaseAttribute.TextListAttribute>()
                .flatMap { it.text }
            assertTrue(lists.containsAll(listOf("참석자", "참석 여부", "반복", "캘린더", "응답 대기", "Jane", "Work")))
            assertEquals(listOf("calendar_accept", "calendar_maybe", "calendar_decline"),
                pin.content.actions.map { it.internalType })
            assertEquals(listOf("참석", "미정", "불참"), pin.content.actions.map {
                it.attributes.filterIsInstance<BaseAttribute.TextAttribute>().single().text
            })
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun languageVariantsAndEnglishFallback() {
        for (language in listOf("ko", "ko-KR", "ko_KR", "KO-kr")) {
            assertEquals("일출", watchText("Sunrise", "일출", language))
        }
        assertEquals("Sunrise", watchText("Sunrise", "일출", "en-US"))
    }
}
