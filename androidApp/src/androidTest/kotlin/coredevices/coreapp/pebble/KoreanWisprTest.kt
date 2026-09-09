package coredevices.coreapp.pebble

import androidx.test.platform.app.InstrumentationRegistry
import coredevices.util.transcription.STTLanguage
import coredevices.util.transcription.TranscriptionSessionStatus
import coredevices.util.transcription.WisprFlowRESTTranscriptionService
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.koin.core.context.GlobalContext
import java.io.File
import kotlin.test.assertTrue

class KoreanWisprTest {
    @Test
    fun transcribesKoreanUsingSignedInAccount() = runBlocking {
        val args = InstrumentationRegistry.getArguments()
        val fixtureName = args.getString("koreanPcmFixture")
        assumeTrue("Supply a synthetic 16 kHz mono PCM16 fixture in the app cache", fixtureName != null)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val pcm = File(context.cacheDir, requireNotNull(fixtureName)).readBytes()
        val service = GlobalContext.get().get<WisprFlowRESTTranscriptionService>()
        assertTrue(service.isAvailable())
        val transcript = withTimeout(30_000) {
            service.transcribe(
                audioStreamFrames = flowOf(pcm), sampleRate = 16000,
                language = STTLanguage.Specific(setOf("ko")),
                conversationContext = null, dictionaryContext = null, contentContext = null,
            ).filterIsInstance<TranscriptionSessionStatus.Transcription>().first().text
        }
        assertTrue(transcript.contains("내일"), "Expected the Korean fixture's first word")
        assertTrue(transcript.contains("만나"), "Expected the Korean fixture's meeting phrase")
        assertTrue(transcript.contains("시"), "Expected a Korean time expression")
    }
}
