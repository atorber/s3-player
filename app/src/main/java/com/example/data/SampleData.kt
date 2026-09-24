package com.example.data

import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket

object SampleData {
    val initialBuckets = listOf(
        S3Bucket(
            bucketName = "prod-audio-stems",
            region = "us-east-1",
            provider = "S3 协议 (公有/私有)",
            endpoint = "s3.us-east-1.amazonaws.com",
            latencyMs = 28,
            objectCount = 142,
            storageSizeFormatted = "18.4 GB",
            isMounted = true,
            authType = "S3 Signature V4",
            usePathStyle = false,
            useSsl = true
        ),
        S3Bucket(
            bucketName = "archive-masters-flac",
            region = "eu-west-1",
            provider = "S3 协议 (冷存/归档)",
            endpoint = "s3.eu-west-1.amazonaws.com",
            latencyMs = 36,
            objectCount = 890,
            storageSizeFormatted = "124.6 GB",
            isMounted = true,
            authType = "S3 签名认证",
            usePathStyle = false,
            useSsl = true
        ),
        S3Bucket(
            bucketName = "spatial-atmos-lossless",
            region = "auto",
            provider = "S3 兼容协议 (高速边缘)",
            endpoint = "r2.cloudflarestorage.com",
            latencyMs = 45,
            objectCount = 64,
            storageSizeFormatted = "32.1 GB",
            isMounted = true,
            authType = "S3 兼容认证",
            usePathStyle = false,
            useSsl = true
        ),
        S3Bucket(
            bucketName = "soundfx-telemetry-v3",
            region = "us-west-2",
            provider = "自建/内网 S3 (MinIO/Ceph)",
            endpoint = "minio.internal:9000",
            latencyMs = 12,
            objectCount = 412,
            storageSizeFormatted = "8.9 GB",
            isMounted = true,
            authType = "Path-Style / HTTP",
            usePathStyle = true,
            useSsl = false
        )
    )

    fun getInitialTracks(): List<S3AudioTrack> {
        val tracks = mutableListOf<S3AudioTrack>()

        tracks.add(
            S3AudioTrack(
                id = 1,
                bucketName = "prod-audio-stems",
                key = "stems/synth/lead_analog_poly_96k.flac",
                title = "Analog Poly Lead (96kHz Stem)",
                artistOrProject = "Orbital Sound Lab // Session 04",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 48291040L,
                durationMs = 372000L, // 6:12
                etag = "\"e4d909c29af5731b8d234a91f421c002\"",
                storageClass = "EXPRESS_ONEZONE",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                isCachedLocally = true,
                waveformPointsRaw = "0.22,0.35,0.48,0.72,0.85,0.64,0.78,0.91,0.52,0.43,0.67,0.88,0.94,0.73,0.61,0.45,0.58,0.81,0.89,0.76,0.62,0.54,0.68,0.83,0.77,0.69,0.59,0.42,0.51,0.74,0.86,0.92,0.68,0.55,0.71,0.82,0.79,0.63,0.49,0.38,0.46,0.65,0.78,0.84,0.71,0.59,0.48,0.37,0.42,0.56,0.63,0.71,0.58,0.47,0.39,0.31,0.28,0.34,0.41,0.36,0.29,0.24,0.18,0.12",
                isFavorite = true,
                lastModified = "2026-09-18 14:22:04 UTC"
            )
        )

        tracks.add(
            S3AudioTrack(
                id = 2,
                bucketName = "prod-audio-stems",
                key = "stems/drums/acoustic_overheads_dry.wav",
                title = "Acoustic Overheads Room Stems",
                artistOrProject = "Sub-Bass Division // Album Masters",
                format = "WAV",
                sampleRate = "192kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 86410290L,
                durationMs = 425000L, // 7:05
                etag = "\"7c1042a98f123bc4e901a8820c41098d\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                isCachedLocally = false,
                waveformPointsRaw = "0.15,0.28,0.44,0.62,0.81,0.93,0.88,0.74,0.63,0.79,0.95,0.86,0.71,0.58,0.69,0.84,0.91,0.77,0.65,0.52,0.68,0.82,0.76,0.61,0.48,0.59,0.73,0.87,0.79,0.66,0.54,0.43,0.55,0.72,0.84,0.78,0.62,0.51,0.63,0.77,0.83,0.71,0.56,0.45,0.38,0.49,0.62,0.57,0.46,0.35,0.41,0.52,0.48,0.39,0.31,0.26,0.22,0.19,0.16,0.14,0.12,0.10,0.08,0.06",
                isFavorite = false,
                lastModified = "2026-09-20 09:15:33 UTC"
            )
        )

        tracks.add(
            S3AudioTrack(
                id = 3,
                bucketName = "archive-masters-flac",
                key = "masters/v2/orbital_decay_lossless.flac",
                title = "Orbital Decay (Lossless Master)",
                artistOrProject = "Aether Dynamics // Telemetry Vol. 1",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 54109200L,
                durationMs = 348000L, // 5:48
                etag = "\"b8301fa39c29801efc012894109923da\"",
                storageClass = "INTELLIGENT_TIERING",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                isCachedLocally = true,
                waveformPointsRaw = "0.30,0.42,0.58,0.76,0.89,0.94,0.85,0.72,0.65,0.78,0.90,0.88,0.74,0.62,0.71,0.85,0.92,0.83,0.69,0.58,0.72,0.86,0.79,0.64,0.51,0.63,0.78,0.89,0.81,0.67,0.53,0.44,0.57,0.75,0.86,0.80,0.65,0.52,0.66,0.79,0.85,0.73,0.58,0.47,0.39,0.51,0.64,0.58,0.47,0.36,0.43,0.54,0.49,0.40,0.32,0.27,0.23,0.20,0.17,0.15,0.13,0.11,0.09,0.07",
                isFavorite = true,
                lastModified = "2026-09-12 21:05:49 UTC"
            )
        )

        tracks.add(
            S3AudioTrack(
                id = 4,
                bucketName = "spatial-atmos-lossless",
                key = "atmos/tokyo_shinjuku_binaural_3d.wav",
                title = "Shinjuku Midnight Binaural Spatial",
                artistOrProject = "Field Sound Archives // Tokyo 2026",
                format = "WAV",
                sampleRate = "192kHz / 24-Bit",
                channels = "5.1 Spatial",
                sizeBytes = 112450890L,
                durationMs = 502000L, // 8:22
                etag = "\"4f9104ca819230fd290192834011293c\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                isCachedLocally = false,
                waveformPointsRaw = "0.18,0.29,0.45,0.64,0.82,0.90,0.83,0.71,0.60,0.75,0.88,0.82,0.68,0.55,0.67,0.81,0.88,0.79,0.66,0.53,0.69,0.83,0.77,0.62,0.49,0.60,0.74,0.87,0.80,0.67,0.54,0.45,0.58,0.74,0.85,0.79,0.63,0.50,0.64,0.78,0.84,0.72,0.57,0.46,0.38,0.50,0.63,0.57,0.46,0.35,0.42,0.53,0.48,0.39,0.31,0.26,0.22,0.19,0.16,0.14,0.12,0.10,0.08,0.06",
                isFavorite = false,
                lastModified = "2026-09-02 11:30:12 UTC"
            )
        )

        tracks.add(
            S3AudioTrack(
                id = 5,
                bucketName = "prod-audio-stems",
                key = "stems/bass/analog_modular_sub_c0.mp3",
                title = "Modular Sub Bass C0 (48kHz)",
                artistOrProject = "Sub-Bass Division // Stems",
                format = "MP3",
                sampleRate = "48kHz / 320kbps",
                channels = "1.0 Mono",
                sizeBytes = 14210000L,
                durationMs = 215000L, // 3:35
                etag = "\"1a2b3c4d5e6f708192a3b4c5d6e7f809\"",
                storageClass = "STANDARD",
                streamUrl = "https://actions.google.com/sounds/v1/science_fiction/scifi_engine.ogg",
                isCachedLocally = true,
                waveformPointsRaw = "0.45,0.52,0.68,0.82,0.91,0.88,0.76,0.69,0.74,0.85,0.92,0.81,0.70,0.62,0.73,0.86,0.89,0.78,0.65,0.54,0.67,0.80,0.74,0.61,0.50,0.62,0.76,0.84,0.77,0.63,0.51,0.43,0.55,0.71,0.81,0.75,0.60,0.48,0.61,0.74,0.80,0.68,0.54,0.44,0.36,0.47,0.59,0.54,0.43,0.33,0.39,0.49,0.45,0.36,0.28,0.23,0.19,0.16,0.14,0.12,0.10,0.08,0.06,0.05",
                isFavorite = false,
                lastModified = "2026-09-21 16:40:00 UTC"
            )
        )

        tracks.add(
            S3AudioTrack(
                id = 6,
                bucketName = "archive-masters-flac",
                key = "field/scandinavian_rain_hydrophone.ogg",
                title = "Hydrophone Deep Nordic Rain",
                artistOrProject = "Acoustic Archives // Nordic Basin",
                format = "OGG",
                sampleRate = "96kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 38400000L,
                durationMs = 280000L, // 4:40
                etag = "\"33910cba726154388102374619472619\"",
                storageClass = "EXPRESS_ONEZONE",
                streamUrl = "https://actions.google.com/sounds/v1/ambiences/rain_heavy.ogg",
                isCachedLocally = false,
                waveformPointsRaw = "0.20,0.25,0.32,0.45,0.58,0.69,0.74,0.78,0.81,0.79,0.75,0.71,0.68,0.65,0.69,0.74,0.78,0.80,0.76,0.72,0.69,0.65,0.62,0.67,0.72,0.75,0.77,0.74,0.70,0.66,0.63,0.59,0.56,0.61,0.65,0.68,0.69,0.66,0.62,0.58,0.54,0.51,0.48,0.52,0.56,0.58,0.59,0.55,0.51,0.47,0.44,0.40,0.38,0.41,0.44,0.46,0.45,0.42,0.38,0.35,0.31,0.28,0.24,0.20",
                isFavorite = true,
                lastModified = "2026-08-29 04:12:30 UTC"
            )
        )

        return tracks
    }
}
