package com.example

import com.example.data.SampleData
import com.example.data.model.S3AudioTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun s3Track_formatsAndUri_areCorrect() {
        val track = S3AudioTrack(
            id = 42,
            bucketName = "prod-audio-stems",
            key = "synth/analog_poly.flac",
            title = "Analog Poly",
            artistOrProject = "Sub-Bass",
            format = "FLAC",
            sampleRate = "96kHz / 24-Bit",
            channels = "2.0 Stereo",
            sizeBytes = 52428800L, // 50 MB
            durationMs = 185000L,  // 3:05
            etag = "\"test-etag-123\"",
            storageClass = "STANDARD",
            streamUrl = "https://example.com/audio.flac"
        )

        assertEquals("s3://prod-audio-stems/synth/analog_poly.flac", track.fullS3Uri)
        assertEquals("50.0 MB", track.sizeFormatted)
        assertEquals("03:05", track.durationFormatted)

        val amps = track.getWaveformPoints()
        assertTrue(amps.isNotEmpty())
    }

    @Test
    fun sampleData_containsBucketsAndTracks() {
        val buckets = SampleData.initialBuckets
        assertTrue("Buckets must not be empty", buckets.isNotEmpty())
        assertTrue("prod-audio-stems bucket should exist", buckets.any { it.bucketName == "prod-audio-stems" })

        val tracks = SampleData.getInitialTracks()
        assertTrue("Tracks must not be empty", tracks.isNotEmpty())
        assertEquals(6, tracks.size)
    }
}
