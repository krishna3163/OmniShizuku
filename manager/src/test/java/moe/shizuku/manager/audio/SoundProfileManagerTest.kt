package moe.shizuku.manager.audio

import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundProfileManagerTest {

    @Test
    fun profileKindsExposeExpectedRingerModes() {
        val profiles = SoundProfileKind.entries
        assertTrue(profiles.contains(SoundProfileKind.NORMAL))
        assertTrue(profiles.contains(SoundProfileKind.CUSTOM))
        assertTrue(profiles.contains(SoundProfileKind.SILENT))
        assertTrue(profiles.contains(SoundProfileKind.VIBRATE))
    }

    @Test
    fun volumeMapCanRepresentIndependentStreams() {
        val profile = SoundProfile(
            name = "Meeting",
            kind = SoundProfileKind.MEETING,
            volumes = mapOf(AudioManager.STREAM_MUSIC to 2, AudioManager.STREAM_RING to 0)
        )
        assertEquals(2, profile.volumes[AudioManager.STREAM_MUSIC])
        assertEquals(0, profile.volumes[AudioManager.STREAM_RING])
    }
}
