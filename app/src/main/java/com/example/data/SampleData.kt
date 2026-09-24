package com.example.data

import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket

object SampleData {

    val initialBuckets = listOf(
        S3Bucket(
            bucketName = "xtrader",
            region = "cn-north-3",
            provider = "S3 兼容协议 (浪潮云 OSS)",
            endpoint = "xtrader.oss.cn-north-3.inspurcloudoss.com",
            latencyMs = 18,
            objectCount = 18,
            storageSizeFormatted = "1.42 GB",
            isMounted = true,
            authType = "S3 SigV4 认证",
            usePathStyle = false,
            useSsl = true,
            accessKey = "YjNmNjhkOWMtODE5My00MjM5LTgxZGYtNWQ3MzFlNDA4NTlm"
        ),
        S3Bucket(
            bucketName = "podcast-vault",
            region = "us-east-1",
            provider = "S3 实时动态源",
            endpoint = "s3.us-east-1.amazonaws.com",
            latencyMs = 24,
            objectCount = 42,
            storageSizeFormatted = "3.84 GB",
            isMounted = true,
            authType = "IAM Signature V4",
            usePathStyle = false,
            useSsl = true
        ),
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
            bucketName = "soundfx-telemetry-v3",
            region = "auto",
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

        // 1. Currently Active Master Track from original design
        tracks.add(
            S3AudioTrack(
                id = 1,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/ep04_final_master_v2.flac",
                title = "ep04_final_master_v2.flac",
                artistOrProject = "Aether Tech Podcast · Ep 04: Edge Cloud Audio",
                format = "FLAC",
                sampleRate = "96kHz / 24-bit",
                channels = "2.0 Stereo",
                sizeBytes = 88291040L,
                durationMs = 2892000L, // 48:12
                etag = "\"9b2d8471a8c0e12ff4a91f421c002\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                isCachedLocally = true,
                waveformPointsRaw = "0.22,0.35,0.48,0.72,0.85,0.64,0.78,0.91,0.52,0.43,0.67,0.88,0.94,0.73,0.61,0.45,0.58,0.81,0.89,0.76,0.62,0.54,0.68,0.83,0.77,0.69,0.59,0.42,0.51,0.74,0.86,0.92,0.68,0.55,0.71,0.82,0.79,0.63,0.49,0.38,0.46,0.65,0.78,0.84,0.71,0.59,0.48,0.37,0.42,0.56,0.63,0.71,0.58,0.47,0.39,0.31,0.28,0.34,0.41,0.36,0.29,0.24,0.18,0.12",
                isFavorite = true,
                lastModified = "2026-09-24 07:18:04 UTC"
            )
        )

        // 2. Freshly Discovered Upload (PutObject event)
        tracks.add(
            S3AudioTrack(
                id = 2,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/guest_recording_track_02.flac",
                title = "guest_recording_track_02.flac",
                artistOrProject = "通过 S3 PutObject 事件自动添加 · ep04-live/",
                format = "FLAC",
                sampleRate = "96kHz / 24-bit",
                channels = "2.0 Stereo",
                sizeBytes = 67108864L, // 64 MB
                durationMs = 1240000L, // 20:40
                etag = "\"4d19a008c234a91f421e4d909c29af57\"",
                storageClass = "INTELLIGENT_TIERING",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                isCachedLocally = false,
                waveformPointsRaw = "0.15,0.28,0.44,0.62,0.81,0.93,0.88,0.74,0.63,0.79,0.95,0.86,0.71,0.58,0.69,0.84,0.91,0.77,0.65,0.52,0.68,0.82,0.76,0.61,0.48,0.59,0.73,0.87,0.79,0.66,0.54,0.43,0.55,0.72,0.84,0.78,0.62,0.51,0.63,0.77,0.83,0.71,0.56,0.45,0.38,0.49,0.62,0.57,0.46,0.35,0.41,0.52,0.48,0.39,0.31,0.26,0.22,0.19,0.16,0.14,0.12,0.10,0.08,0.06",
                isFavorite = false,
                lastModified = "2分钟前"
            )
        )

        // 3. Newly enqueued ad sponsor
        tracks.add(
            S3AudioTrack(
                id = 3,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/ep04_ad_sponsor_break.mp3",
                title = "ep04_ad_sponsor_break.mp3",
                artistOrProject = "最新 · 自动入队 · 03:45",
                format = "MP3",
                sampleRate = "48kHz / 16-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 13002342L, // 12.4 MB
                durationMs = 225000L, // 03:45
                etag = "\"8f4a91f421c0029af5731b8d234a91f4\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                isCachedLocally = true,
                waveformPointsRaw = "0.3,0.5,0.7,0.8,0.6,0.9,0.7,0.5,0.6,0.8,0.7,0.4,0.3,0.5,0.6,0.4",
                isFavorite = false,
                lastModified = "刚刚发现 · 2分钟前"
            )
        )

        // 4. Host Interviews Part 1
        tracks.add(
            S3AudioTrack(
                id = 4,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/interviews_part1_host.wav",
                title = "interviews_part1_host.wav",
                artistOrProject = "录音主轨 · 22:30 就绪",
                format = "WAV",
                sampleRate = "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 117440512L, // 112 MB
                durationMs = 1350000L, // 22:30
                etag = "\"a1b2c3d4e5f67890123456789abcdef0\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-24 06:30:00 UTC"
            )
        )

        // 5. Remote Interviews Part 2
        tracks.add(
            S3AudioTrack(
                id = 5,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/interviews_part2_remote.wav",
                title = "interviews_part2_remote.wav",
                artistOrProject = "远端连线轨 · 19:40 就绪",
                format = "WAV",
                sampleRate = "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 102760448L, // 98 MB
                durationMs = 1180000L, // 19:40
                etag = "\"c3d4e5f6a1b27890123456789abcdef1\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-24 06:45:00 UTC"
            )
        )

        // 6. Outro Credits Music
        tracks.add(
            S3AudioTrack(
                id = 6,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/outro_credits_music.mp3",
                title = "outro_credits_music.mp3",
                artistOrProject = "尾声配乐 · 02:50 就绪",
                format = "MP3",
                sampleRate = "48kHz / 16-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 8598323L, // 8.2 MB
                durationMs = 170000L, // 02:50
                etag = "\"f6a1b2c3d4e57890123456789abcdef2\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-24 05:20:00 UTC"
            )
        )

        // 7. Intro Theme WAV
        tracks.add(
            S3AudioTrack(
                id = 7,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/ep04_intro_theme.wav",
                title = "ep04_intro_theme.wav",
                artistOrProject = "片头音乐 · 02:15",
                format = "WAV",
                sampleRate = "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 40475033L, // 38.6 MB
                durationMs = 135000L,
                etag = "\"e57890123456789abcdef2f6a1b2c3d4\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                isCachedLocally = true,
                isFavorite = false,
                lastModified = "2026-09-24 04:12:00 UTC"
            )
        )

        // 8. Backup interview guest M4A
        tracks.add(
            S3AudioTrack(
                id = 8,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/backup_interview_guest.m4a",
                title = "backup_interview_guest.m4a",
                artistOrProject = "备用连线录音 · 32:00",
                format = "M4A",
                sampleRate = "44.1kHz / 16-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 29464985L, // 28.1 MB
                durationMs = 1920000L,
                etag = "\"90123456789abcdef2f6a1b2c3d4e578\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-24 03:50:00 UTC"
            )
        )

        // Subfolder: interviews-raw/
        tracks.add(
            S3AudioTrack(
                id = 9,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/interviews-raw/interview_stem_host_uncompressed.wav",
                title = "interview_stem_host_uncompressed.wav",
                artistOrProject = "分轨工程 · 主持人未压缩干音",
                format = "WAV",
                sampleRate = "96kHz / 24-Bit",
                channels = "1.0 Mono",
                sizeBytes = 142606336L, // 136 MB
                durationMs = 2700000L,  // 45:00
                etag = "\"a3e7890123456789abcdef2f6a1b2c3d\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                isCachedLocally = true,
                isFavorite = true,
                lastModified = "2026-09-24 02:15:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 10,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/interviews-raw/interview_stem_guest_isolated.wav",
                title = "interview_stem_guest_isolated.wav",
                artistOrProject = "分轨工程 · 嘉宾独立音轨",
                format = "WAV",
                sampleRate = "96kHz / 24-Bit",
                channels = "1.0 Mono",
                sizeBytes = 138412032L, // 132 MB
                durationMs = 2650000L,
                etag = "\"b4f7890123456789abcdef2f6a1b2c3e\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-24 02:18:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 11,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/interviews-raw/studio_ambience_room_tone.flac",
                title = "studio_ambience_room_tone.flac",
                artistOrProject = "录音室环境底噪采样 · 消除底噪基准",
                format = "FLAC",
                sampleRate = "192kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 45088768L, // 43 MB
                durationMs = 600000L, // 10:00
                etag = "\"c507890123456789abcdef2f6a1b2c3f\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-24 01:40:00 UTC"
            )
        )

        // Subfolder: master-mixes/
        tracks.add(
            S3AudioTrack(
                id = 12,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/master-mixes/ep04_surround_5_1_master.flac",
                title = "ep04_surround_5_1_master.flac",
                artistOrProject = "5.1 环绕声沉浸式空间音频母带",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "5.1 Spatial",
                sizeBytes = 220200960L, // 210 MB
                durationMs = 2892000L,
                etag = "\"d617890123456789abcdef2f6a1b2c40\"",
                storageClass = "INTELLIGENT_TIERING",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                isCachedLocally = false,
                isFavorite = true,
                lastModified = "2026-09-24 07:45:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 13,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep04-live/master-mixes/ep04_radio_edit_broadcast.mp3",
                title = "ep04_radio_edit_broadcast.mp3",
                artistOrProject = "广播精简混音版 (320kbps CBR)",
                format = "MP3",
                sampleRate = "48kHz / 16-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 72351744L, // 69 MB
                durationMs = 1800000L, // 30:00
                etag = "\"e727890123456789abcdef2f6a1b2c41\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-24 07:50:00 UTC"
            )
        )

        // Subfolder: sound-effects/
        tracks.add(
            S3AudioTrack(
                id = 14,
                bucketName = "xtrader",
                key = "sound-effects/sci_fi_transceiver_chirp.wav",
                title = "sci_fi_transceiver_chirp.wav",
                artistOrProject = "片头过渡音效 · 科幻电台啸叫",
                format = "WAV",
                sampleRate = "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 3145728L, // 3 MB
                durationMs = 8000L,
                etag = "\"f837890123456789abcdef2f6a1b2c42\"",
                storageClass = "EXPRESS_ONEZONE",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3",
                isCachedLocally = true,
                isFavorite = false,
                lastModified = "2026-09-23 18:20:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 15,
                bucketName = "xtrader",
                key = "sound-effects/cloud_sync_chime.mp3",
                title = "cloud_sync_chime.mp3",
                artistOrProject = "系统提示音 · 云端同步完成",
                format = "MP3",
                sampleRate = "44.1kHz / 16-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 1572864L, // 1.5 MB
                durationMs = 4000L,
                etag = "\"0947890123456789abcdef2f6a1b2c43\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-23 19:10:00 UTC"
            )
        )

        // Tracks in podcast-vault bucket
        tracks.add(
            S3AudioTrack(
                id = 16,
                bucketName = "podcast-vault",
                key = "music/ambient_soundscape_01.flac",
                title = "ambient_soundscape_01.flac",
                artistOrProject = "Podcast Vault · 氛围背景音乐",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 52428800L,
                durationMs = 420000L,
                etag = "\"1057890123456789abcdef2f6a1b2c44\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
                isCachedLocally = false,
                isFavorite = true,
                lastModified = "2026-09-20 12:00:00 UTC"
            )
        )

        // Tracks in soundfx-telemetry-v3 bucket
        tracks.add(
            S3AudioTrack(
                id = 17,
                bucketName = "soundfx-telemetry-v3",
                key = "telemetry/pulse_beacon_440hz.wav",
                title = "pulse_beacon_440hz.wav",
                artistOrProject = "MinIO 内网校准音频 · 440Hz 基准脉冲",
                format = "WAV",
                sampleRate = "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 12582912L,
                durationMs = 60000L,
                etag = "\"2167890123456789abcdef2f6a1b2c45\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-21 08:30:00 UTC"
            )
        )

        // Additional multi-tier tracks for xtrader tier-by-tier navigation
        tracks.add(
            S3AudioTrack(
                id = 18,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep03-studio/ep03_full_mix.flac",
                title = "ep03_full_mix.flac",
                artistOrProject = "录音室混音母带 · 第03期现场",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 68157440L,
                durationMs = 2100000L,
                etag = "\"3277890123456789abcdef2f6a1b2c46\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-18 10:20:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 19,
                bucketName = "xtrader",
                key = "podcasts/2025-season/ep03-studio/ep03_interview_cut.wav",
                title = "ep03_interview_cut.wav",
                artistOrProject = "连线专访粗剪轨",
                format = "WAV",
                sampleRate = "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 44040192L,
                durationMs = 950000L,
                etag = "\"4387890123456789abcdef2f6a1b2c47\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-18 11:15:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 20,
                bucketName = "xtrader",
                key = "podcasts/2024-archive/ep01-origins/ep01_origins_broadcast.mp3",
                title = "ep01_origins_broadcast.mp3",
                artistOrProject = "2024 创刊特辑 · 广播重播版",
                format = "MP3",
                sampleRate = "48kHz / 16-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 38797312L,
                durationMs = 1980000L,
                etag = "\"5497890123456789abcdef2f6a1b2c48\"",
                storageClass = "GLACIER_IR",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2024-12-15 09:00:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 21,
                bucketName = "xtrader",
                key = "podcasts/2024-archive/ep02-deep-dive/ep02_audio_master.flac",
                title = "ep02_audio_master.flac",
                artistOrProject = "2024 专题深潜 · 无损归档",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 77594624L,
                durationMs = 2450000L,
                etag = "\"6507890123456789abcdef2f6a1b2c49\"",
                storageClass = "GLACIER_IR",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2024-12-28 14:30:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 22,
                bucketName = "xtrader",
                key = "sound-effects/ambience/rain_thunder_storm_loop.flac",
                title = "rain_thunder_storm_loop.flac",
                artistOrProject = "双耳声学环境采样 · 雷暴循环",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 31457280L,
                durationMs = 180000L,
                etag = "\"7617890123456789abcdef2f6a1b2c50\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                isCachedLocally = true,
                isFavorite = true,
                lastModified = "2026-09-22 16:40:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 23,
                bucketName = "xtrader",
                key = "sound-effects/foley/footsteps_gravel_stereo.wav",
                title = "footsteps_gravel_stereo.wav",
                artistOrProject = "影视拟音库 · 碎石路步行动作",
                format = "WAV",
                sampleRate = "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = 8388608L,
                durationMs = 45000L,
                etag = "\"8727890123456789abcdef2f6a1b2c51\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-22 17:10:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 24,
                bucketName = "xtrader",
                key = "studio-sessions/take-01-drums/drum_kick_overhead_96k.wav",
                title = "drum_kick_overhead_96k.wav",
                artistOrProject = "录音工程分轨 · 底鼓顶置电容麦",
                format = "WAV",
                sampleRate = "96kHz / 24-Bit",
                channels = "1.0 Mono",
                sizeBytes = 50331648L,
                durationMs = 900000L,
                etag = "\"9837890123456789abcdef2f6a1b2c52\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-23 20:00:00 UTC"
            )
        )
        tracks.add(
            S3AudioTrack(
                id = 25,
                bucketName = "xtrader",
                key = "studio-sessions/take-02-vocals/lead_vocal_dry_take02.flac",
                title = "lead_vocal_dry_take02.flac",
                artistOrProject = "录音工程分轨 · 人声主轨乾音",
                format = "FLAC",
                sampleRate = "96kHz / 24-Bit",
                channels = "1.0 Mono",
                sizeBytes = 62914560L,
                durationMs = 1200000L,
                etag = "\"0947890123456789abcdef2f6a1b2c53\"",
                storageClass = "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "2026-09-23 21:30:00 UTC"
            )
        )

        return tracks
    }
}
