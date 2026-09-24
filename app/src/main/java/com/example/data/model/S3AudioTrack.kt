package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "s3_audio_tracks")
data class S3AudioTrack(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bucketName: String,
    val key: String,
    val title: String,
    val artistOrProject: String,
    val format: String,             // FLAC, WAV, MP3, OGG, AAC
    val sampleRate: String,         // 96kHz / 24-Bit, 192kHz / 24-Bit, 48kHz / 24-Bit
    val channels: String,           // 2.0 Stereo, 5.1 Spatial, 1.0 Mono
    val sizeBytes: Long,            // e.g. 48291040L
    val durationMs: Long,           // e.g. 214000L
    val etag: String,               // MD5 hash e.g. "9f8a3c2049a46b5d8"
    val storageClass: String,       // STANDARD, INTELLIGENT_TIERING, EXPRESS_ONEZONE
    val streamUrl: String,          // Live HTTP/HTTPS streaming URL
    val isCachedLocally: Boolean = false,
    val waveformPointsRaw: String = "", // Comma-separated float amplitudes
    val isFavorite: Boolean = false,
    val lastModified: String = "2026-08-14 18:42:10 UTC"
) {
    val fullS3Uri: String
        get() = "s3://$bucketName/$key"

    val sizeFormatted: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1000) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }

    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    fun getWaveformPoints(): List<Float> {
        if (waveformPointsRaw.isBlank()) return generateFallbackAmplitudes(id.toInt() + key.hashCode())
        return try {
            waveformPointsRaw.split(",").mapNotNull { it.trim().toFloatOrNull() }
        } catch (_: Exception) {
            generateFallbackAmplitudes(id.toInt() + key.hashCode())
        }
    }

    companion object {
        fun generateFallbackAmplitudes(seed: Int): List<Float> {
            val random = java.util.Random(seed.toLong())
            val points = mutableListOf<Float>()
            var current = 0.4f
            for (i in 0 until 64) {
                current += (random.nextFloat() - 0.5f) * 0.35f
                current = current.coerceIn(0.12f, 0.98f)
                // Shape it like realistic audio track dynamics
                val envelope = Math.sin(Math.PI * i / 64.0).toFloat().coerceIn(0.3f, 1.0f)
                points.add((current * envelope).coerceIn(0.08f, 0.98f))
            }
            return points
        }
    }
}
