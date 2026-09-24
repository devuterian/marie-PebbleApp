package localization

import kotlin.test.Test
import kotlin.test.assertEquals

class KoreanTest {
    @Test fun koreanUiUsesTranslation() {
        assertEquals("설정", translateUi("Settings", "ko"))
        assertEquals("취소", translateUi("Cancel", "ko"))
    }
    @Test fun otherLanguagesKeepOriginal() {
        for (language in listOf("en", "ja", "fr")) assertEquals("Settings", translateUi("Settings", language))
    }
    @Test fun unknownUserContentIsPreserved() {
        assertEquals("My watch 123", translateUi("My watch 123", "ko"))
    }
    @Test fun koreanClockPlacesMeridiemFirst() {
        assertEquals("오전 1:51", clockTimeForLanguage(1, 51, "ko"))
        assertEquals("오후 12:06", clockTimeForLanguage(12, 6, "ko"))
        assertEquals("오전 12:00", clockTimeForLanguage(0, 0, "ko"))
    }
    @Test fun englishClockPlacesMeridiemLast() {
        assertEquals("1:51 AM", clockTimeForLanguage(1, 51, "en"))
        assertEquals("12:06 PM", clockTimeForLanguage(12, 6, "en"))
    }
}
