package io.rebble.libpebblecommon.voice

import io.rebble.libpebblecommon.packets.Sentence
import io.rebble.libpebblecommon.packets.VoiceAttribute
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TranscriptionResultTest {
    @Test
    fun koreanMultilineResultMatchesFirmwareWordFormat() {
        val result = assertIs<TranscriptionResult.Success>(TranscriptionResult.Success(listOf(
            TranscriptionWord("내일\n오후\t세", 0.9f),
            TranscriptionWord("", 0.9f), TranscriptionWord("시에\u0000만나자", 0.9f),
        )).forWatch())
        assertEquals(listOf("내일", "오후", "세", "시에", "만나자"), result.words.map { it.word })
        val bytes = VoiceAttribute.Transcription(sentences = listOf(
            Sentence(result.words.map { it.toProtocol() }),
        )).toBytes()
        assertEquals(1u.toUByte(), bytes[0])
        assertEquals(1u.toUByte(), bytes[1])
        assertEquals(5, bytes[2].toInt() + (bytes[3].toInt() shl 8))
        var cursor = 4
        for (word in result.words) {
            val length = bytes[cursor + 1].toInt() + (bytes[cursor + 2].toInt() shl 8)
            cursor += 3
            val data = bytes.sliceArray(cursor until cursor + length)
            assertContentEquals(word.word.encodeToByteArray().toUByteArray(), data)
            assertTrue(data.all { it >= 0x20u || it == 8u.toUByte() })
            cursor += length
        }
        assertEquals(bytes.size, cursor)
    }

    @Test
    fun emptySpeechDoesNotSendInvalidEmptySentence() {
        assertEquals(TranscriptionResult.Failed, TranscriptionResult.Success(emptyList()).forWatch())
        assertEquals(TranscriptionResult.Failed, TranscriptionResult.Success(listOf(
            TranscriptionWord(" \n\t", 0.9f),
        )).forWatch())
    }

    @Test
    fun preservesErrorsAndPunctuation() {
        val error = TranscriptionResult.ConnectionError("offline")
        assertEquals(error, error.forWatch())
        val result = TranscriptionResult.Success(listOf(TranscriptionWord("\b!", 0.9f)))
        assertEquals(result, result.forWatch())
    }
}
