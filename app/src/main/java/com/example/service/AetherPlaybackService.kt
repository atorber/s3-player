package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.AetherAudioEngine
import com.example.audio.PlaybackStatus
import com.example.data.model.S3AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AetherPlaybackService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()
        observeAudioEngine()
    }

    private fun initMediaSession() {
        try {
            mediaSession = MediaSession(this, "AetherPlaybackSession").apply {
                setCallback(object : MediaSession.Callback() {
                    override fun onPlay() {
                        AetherAudioEngine.currentEngine?.togglePlayPause()
                    }

                    override fun onPause() {
                        AetherAudioEngine.currentEngine?.togglePlayPause()
                    }

                    override fun onSkipToNext() {
                        AetherAudioEngine.currentEngine?.playNext()
                    }

                    override fun onSkipToPrevious() {
                        AetherAudioEngine.currentEngine?.playPrevious()
                    }

                    override fun onStop() {
                        AetherAudioEngine.currentEngine?.stopPlayback()
                        stopForegroundService()
                    }

                    override fun onSeekTo(pos: Long) {
                        AetherAudioEngine.currentEngine?.seekTo(pos)
                    }
                })
                setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
                isActive = true
            }
        } catch (_: Exception) {}
    }

    private fun updateMediaSession(track: S3AudioTrack?, isPlaying: Boolean, durationMs: Long, positionMs: Long) {
        val session = mediaSession ?: return
        try {
            val playbackState = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_PLAY_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackState.ACTION_STOP or
                    PlaybackState.ACTION_SEEK_TO
                )
                .setState(
                    if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                    positionMs,
                    if (isPlaying) 1.0f else 0.0f
                )
                .build()
            session.setPlaybackState(playbackState)

            if (track != null) {
                val metadata = MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artistOrProject)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, track.bucketName)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)
                    .build()
                session.setMetadata(metadata)
            }
        } catch (_: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val engine = AetherAudioEngine.currentEngine

        when (action) {
            ACTION_PLAY_PAUSE -> engine?.togglePlayPause()
            ACTION_NEXT -> engine?.playNext()
            ACTION_PREV -> engine?.playPrevious()
            ACTION_STOP -> {
                engine?.stopPlayback()
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_SYNC_STATE -> {
                val state = engine?.playerState?.value
                val currentTrack = state?.currentTrack
                val isPlaying = state?.status == PlaybackStatus.PLAYING
                updateMediaSession(currentTrack, isPlaying, state?.durationMs ?: 0L, state?.currentPositionMs ?: 0L)
                val notification = buildNotification(currentTrack, isPlaying)
                startInForeground(notification)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val state = AetherAudioEngine.currentEngine?.playerState?.value
        val isPlaying = state?.status == PlaybackStatus.PLAYING
        if (!isPlaying) {
            stopForegroundService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
        } catch (_: Exception) {}
        serviceScope.cancel()
    }

    private fun observeAudioEngine() {
        serviceScope.launch {
            AetherAudioEngine.engineFlow.collectLatest { engine ->
                if (engine == null) {
                    stopForegroundService()
                    return@collectLatest
                }

                engine.playerState.collectLatest { state ->
                    val track = state.currentTrack
                    if (track == null) {
                        stopForegroundService()
                        return@collectLatest
                    }

                    when (state.status) {
                        PlaybackStatus.PLAYING,
                        PlaybackStatus.BUFFERING,
                        PlaybackStatus.CONNECTING -> {
                            updateMediaSession(track, isPlaying = true, durationMs = state.durationMs, positionMs = state.currentPositionMs)
                            val notif = buildNotification(track, isPlaying = true)
                            startInForeground(notif)
                        }
                        PlaybackStatus.PAUSED -> {
                            updateMediaSession(track, isPlaying = false, durationMs = state.durationMs, positionMs = state.currentPositionMs)
                            val notif = buildNotification(track, isPlaying = false)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                stopForeground(STOP_FOREGROUND_DETACH)
                            } else {
                                @Suppress("DEPRECATION")
                                stopForeground(false)
                            }
                            notificationManager.notify(NOTIFICATION_ID, notif)
                        }
                        PlaybackStatus.IDLE,
                        PlaybackStatus.ERROR -> {
                            updateMediaSession(track, isPlaying = false, durationMs = 0L, positionMs = 0L)
                            stopForegroundService()
                        }
                    }
                }
            }
        }
    }

    private fun startInForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun buildNotification(track: S3AudioTrack?, isPlaying: Boolean): Notification {
        val title = track?.title ?: "S3 音频流"
        val subtitle = "${track?.artistOrProject ?: "Aether S3 Player"} · ${track?.format ?: "FLAC"}"

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, AetherPlaybackService::class.java).apply { action = ACTION_PREV }
        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, AetherPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, AetherPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AetherPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            4,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseText = if (isPlaying) "暂停" else "播放"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_aether_cube)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_previous, "上一曲", prevPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "下一曲", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "关闭", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aether 后台音频播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持 S3 音频在后台及息屏时持续播放并提供媒体控制"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "aether_playback_channel"

        const val ACTION_START = "com.example.action.START"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREV = "com.example.action.PREV"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_SYNC_STATE = "com.example.action.SYNC_STATE"

        fun start(context: Context) {
            val intent = Intent(context, AetherPlaybackService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AetherPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
