package moe.shizuku.manager.callrecorder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CallRecorderModelTest {

    @Test
    fun mapsRecordingCodecFromOrdinalWithSafeFallback() {
        assertEquals(RecordingCodec.OPUS, RecordingCodec.fromOrdinal(0))
        assertEquals(RecordingCodec.AAC, RecordingCodec.fromOrdinal(1))
        assertEquals(RecordingCodec.AMR_WB, RecordingCodec.fromOrdinal(2))

        // Out of bounds ordinal falls back to OPUS
        assertEquals(RecordingCodec.OPUS, RecordingCodec.fromOrdinal(-1))
        assertEquals(RecordingCodec.OPUS, RecordingCodec.fromOrdinal(999))
    }

    @Test
    fun verifiesRecordingCodecMimeTypesAndBitrates() {
        assertEquals("audio/opus", RecordingCodec.OPUS.mimeType)
        assertEquals(16_000, RecordingCodec.OPUS.defaultBitrate)

        assertEquals("audio/mp4a-latm", RecordingCodec.AAC.mimeType)
        assertEquals(32_000, RecordingCodec.AAC.defaultBitrate)

        assertEquals("audio/amr-wb", RecordingCodec.AMR_WB.mimeType)
        assertEquals(12_000, RecordingCodec.AMR_WB.defaultBitrate)
    }

    @Test
    fun mapsRecordingFailureFromCodeWithSafeFallback() {
        assertEquals(RecordingFailure.CAPTURE_START_FAILED, RecordingFailure.fromCode(1))
        assertEquals(RecordingFailure.CAPTURE_READ_FAILED, RecordingFailure.fromCode(2))
        assertEquals(RecordingFailure.ENCODE_FAILED, RecordingFailure.fromCode(3))
        assertEquals(RecordingFailure.PERMISSION_DENIED, RecordingFailure.fromCode(4))
        assertEquals(RecordingFailure.INVALID_STATE, RecordingFailure.fromCode(5))

        // Unknown code falls back to INVALID_STATE
        assertEquals(RecordingFailure.INVALID_STATE, RecordingFailure.fromCode(0))
        assertEquals(RecordingFailure.INVALID_STATE, RecordingFailure.fromCode(42))
    }

    @Test
    fun verifiesCapturePlanDefaults() {
        val plan = CapturePlan()
        assertEquals(AudioSource.VOICE_COMMUNICATION, plan.source)
        assertEquals(44100, plan.sampleRate)
        assertEquals(1, plan.channelCount)
    }

    @Test
    fun verifiesRecordingPlanDefaults() {
        val plan = RecordingPlan()
        assertEquals(AudioSource.VOICE_COMMUNICATION, plan.primary.source)
        assertNull(plan.secondary)
        assertFalse(plan.isAudioChannelSplitEnabled)
        assertEquals(RecordingCodec.AAC, plan.codec)
        assertEquals(32_000, plan.bitrate)
    }

    @Test
    fun verifiesCallDataDefaults() {
        val call = CallData()
        assertNull(call.number)
        assertEquals(CallDirection.UNKNOWN, call.direction)
        assertFalse(call.isRecorded)
        assertNull(call.recordingPath)
        assertEquals(0L, call.durationMs)
    }
}

