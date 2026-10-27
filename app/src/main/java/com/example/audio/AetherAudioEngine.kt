package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.example.data.local.PlaylistQueueStorage
import com.example.data.model.S3AudioTrack
import com.example.service.AetherPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

enum class PlaybackStatus {
    IDLE,
    CONNECTING,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

enum class LoopMode {
    OFF,
    ALL,
    SINGLE,
    AB_REPEAT
}

enum class EqPreset(val label: String, val bands: List<Float>) {
    FLAT("原声平直", listOf(0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("重低音增强", listOf(6f, 4f, 1f, 0f, -1f)),
    ACOUSTIC_HIFI("Hi-Fi母带", listOf(2f, 1f, -1f, 2f, 4f)),
    BROADCAST_VOCALS("分轨清晰", listOf(-2f, 1f, 4f, 3f, 1f)),
    TELEMETRY_FILTER("遥测滤波", listOf(-4f, 0f, 5f, 2f, -6f))
}

data class TelemetryMetrics(
    val latencyMs: Int = 28,
    val throughputMBs: Float = 4.2f,
    val bufferPercent: Int = 0,
    val jitterMs: Float = 0.6f,
    val packetLossPct: Float = 0.0f,
    val leftChannelDb: Float = -60f,
    val rightChannelDb: Float = -60f,
    val s3RegionEdge: String = "us-east-1 (IAD89-C1)",
    val tlsCipher: String = "TLS_AES_128_GCM_SHA256"
)

data class PlayerState(
    val currentTrack: S3AudioTrack? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferPercentage: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val loopMode: LoopMode = LoopMode.OFF,
    val abPointA: Long? = null,
    val abPointB: Long? = null,
    val eqPreset: EqPreset = EqPreset.FLAT,
    val eqCustomBands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f),
    val telemetry: TelemetryMetrics = TelemetryMetrics(),
    val errorMessage: String? = null,
    val isScrubbing: Boolean = false,
    val scrubPositionMs: Long = 0L
)

class AetherAudioEngine private constructor(
    context: Context
) {
    private val context: Context = context.applicationContext
    private val engineJob = SupervisorJob()
    private val scope = CoroutineScope(engineJob + Dispatchers.Main)

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var progressJob: Job? = null
    private var telemetryJob: Job? = null
    private val random = Random()

    private val audioManager = this.context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val powerManager = this.context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val queueStorage = PlaylistQueueStorage(this.context)

    init {
        _engineFlow.value = this
        try {
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AetherAudioEngine::PlaybackWakeLock"
            )?.apply {
                setReferenceCounted(false)
            }
        } catch (e: Exception) {
            Log.w("AetherAudio", "WakeLock init failed: ${e.message}")
        }

        // Restore persisted playback queue and last track snapshot on launch
        scope.launch(Dispatchers.IO) {
            val restored = queueStorage.loadQueue()
            if (restored.queue.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    queue = restored.queue
                    val current = restored.currentTrack ?: restored.queue.firstOrNull()
                    if (current != null) {
                        val idx = restored.queue.indexOfFirst { it.key == current.key && it.bucketName == current.bucketName }
                        currentIndex = if (idx >= 0) idx else 0
                        _playerState.update {
                            it.copy(
                                currentTrack = current,
                                status = PlaybackStatus.IDLE,
                                currentPositionMs = restored.positionMs,
                                durationMs = if (current.durationMs > 0) current.durationMs else 0L,
                                bufferPercentage = if (current.isCachedLocally) 100 else 0,
                                loopMode = restored.loopMode,
                                playbackSpeed = restored.playbackSpeed
                            )
                        }
                    }
                }
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld != true) {
                wakeLock?.acquire(60 * 60 * 1000L) // 1 hour safety timeout
            }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                if (_playerState.value.status == PlaybackStatus.PLAYING) {
                    togglePlayPause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause playback
                if (_playerState.value.status == PlaybackStatus.PLAYING) {
                    togglePlayPause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower volume
                try {
                    mediaPlayer?.setVolume(0.2f, 0.2f)
                } catch (_: Exception) {}
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume volume or playback
                try {
                    mediaPlayer?.setVolume(1.0f, 1.0f)
                } catch (_: Exception) {}
            }
        }
    }

    private val _queueFlow = MutableStateFlow<List<S3AudioTrack>>(emptyList())
    val queueFlow: StateFlow<List<S3AudioTrack>> = _queueFlow.asStateFlow()

    private var queue: List<S3AudioTrack>
        get() = _queueFlow.value
        set(value) {
            _queueFlow.value = value
        }
    private var currentIndex: Int = -1

    fun persistQueueState() {
        val currentQueue = queue
        val current = _playerState.value.currentTrack
        val pos = _playerState.value.currentPositionMs
        val loop = _playerState.value.loopMode
        val speed = _playerState.value.playbackSpeed
        scope.launch(Dispatchers.IO) {
            queueStorage.saveQueue(
                queue = currentQueue,
                currentTrack = current,
                currentPositionMs = pos,
                loopMode = loop,
                playbackSpeed = speed
            )
        }
    }

    fun updateQueue(tracks: List<S3AudioTrack>) {
        queue = tracks
        val current = _playerState.value.currentTrack
        if (current != null) {
            val idx = queue.indexOfFirst { it.key == current.key && it.bucketName == current.bucketName }
            if (idx >= 0) {
                currentIndex = idx
            } else if (queue.isNotEmpty() && _playerState.value.status != PlaybackStatus.PLAYING) {
                currentIndex = 0
                _playerState.update { it.copy(currentTrack = queue.first(), currentPositionMs = 0L, durationMs = queue.first().durationMs) }
            }
        } else if (queue.isNotEmpty()) {
            currentIndex = 0
            _playerState.update { it.copy(currentTrack = queue.first(), currentPositionMs = 0L, durationMs = queue.first().durationMs) }
        }
        persistQueueState()
    }

    fun addToQueue(track: S3AudioTrack) {
        if (!queue.any { it.key == track.key && it.bucketName == track.bucketName }) {
            queue = queue + track
        }
        if (_playerState.value.currentTrack == null) {
            playTrack(track)
        } else {
            persistQueueState()
        }
    }

    fun addAllToQueue(tracks: List<S3AudioTrack>) {
        val newTracks = tracks.filter { t -> !queue.any { it.key == t.key && it.bucketName == t.bucketName } }
        queue = queue + newTracks
        if (_playerState.value.currentTrack == null && queue.isNotEmpty()) {
            currentIndex = 0
            _playerState.update {
                it.copy(
                    currentTrack = queue.first(),
                    currentPositionMs = 0L,
                    durationMs = queue.first().durationMs
                )
            }
        }
        persistQueueState()
    }

    fun insertNextInQueue(tracks: List<S3AudioTrack>) {
        val newTracks = tracks.filter { t -> !queue.any { it.key == t.key && it.bucketName == t.bucketName } }
        if (newTracks.isEmpty()) return
        val insertIdx = if (currentIndex >= 0 && currentIndex < queue.size) currentIndex + 1 else 0
        val mutable = queue.toMutableList()
        mutable.addAll(insertIdx, newTracks)
        queue = mutable
        if (_playerState.value.currentTrack == null && queue.isNotEmpty()) {
            currentIndex = 0
            _playerState.update {
                it.copy(
                    currentTrack = queue.first(),
                    currentPositionMs = 0L,
                    durationMs = queue.first().durationMs
                )
            }
        }
        persistQueueState()
    }

    fun clearPlayedQueue() {
        if (currentIndex > 0 && queue.isNotEmpty()) {
            val remaining = queue.subList(currentIndex, queue.size).toList()
            queue = remaining
            currentIndex = 0
            persistQueueState()
        }
    }

    fun removeFromQueue(track: S3AudioTrack) {
        val idx = queue.indexOfFirst { it.key == track.key && it.bucketName == track.bucketName }
        if (idx < 0) return
        val isRemovingCurrent = idx == currentIndex
        val updated = queue.toMutableList().apply { removeAt(idx) }
        queue = updated
        if (updated.isEmpty()) {
            currentIndex = -1
            stopPlayback()
            _playerState.update { it.copy(currentTrack = null) }
            scope.launch(Dispatchers.IO) { queueStorage.clear() }
        } else if (isRemovingCurrent) {
            val nextIdx = idx.coerceAtMost(updated.lastIndex)
            currentIndex = nextIdx
            playTrack(updated[nextIdx])
        } else if (idx < currentIndex) {
            currentIndex = (currentIndex - 1).coerceAtLeast(0)
            persistQueueState()
        } else {
            persistQueueState()
        }
    }

    fun clearQueue() {
        queue = emptyList()
        currentIndex = -1
        stopPlayback()
        _playerState.update { it.copy(currentTrack = null) }
        scope.launch(Dispatchers.IO) { queueStorage.clear() }
    }

    fun getCurrentQueue(): List<S3AudioTrack> = queue

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (audioManager == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    fun playTrack(track: S3AudioTrack, newQueue: List<S3AudioTrack>? = null) {
        if (newQueue != null && newQueue.isNotEmpty()) {
            queue = newQueue
            currentIndex = queue.indexOfFirst { it.key == track.key && it.bucketName == track.bucketName }
        } else {
            if (queue.isEmpty() || !queue.any { it.key == track.key && it.bucketName == track.bucketName }) {
                queue = queue + track
            }
            currentIndex = queue.indexOfFirst { it.key == track.key && it.bucketName == track.bucketName }
        }
        if (currentIndex < 0) currentIndex = 0
        persistQueueState()

        stopCurrent()

        _playerState.update {
            it.copy(
                currentTrack = track,
                status = PlaybackStatus.CONNECTING,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                bufferPercentage = if (track.isCachedLocally) 100 else 15,
                errorMessage = null,
                abPointA = null,
                abPointB = null
            )
        }

        try {
            requestAudioFocus()

            val player = MediaPlayer().apply {
                setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(track.streamUrl)

                setOnBufferingUpdateListener { _, percent ->
                    _playerState.update {
                        it.copy(
                            bufferPercentage = percent,
                            telemetry = it.telemetry.copy(bufferPercent = percent)
                        )
                    }
                }

                setOnPreparedListener { mp ->
                    val actualDuration = if (mp.duration > 0) mp.duration.toLong() else track.durationMs
                    _playerState.update {
                        it.copy(
                            status = PlaybackStatus.PLAYING,
                            durationMs = actualDuration,
                            bufferPercentage = if (track.isCachedLocally) 100 else 35
                        )
                    }
                    try {
                        mp.isLooping = (_playerState.value.loopMode == LoopMode.SINGLE)
                    } catch (_: Exception) {}
                    applySpeed(playerState.value.playbackSpeed)

                    // Init audio effects equalizer
                    try {
                        equalizer?.release()
                        equalizer = Equalizer(0, mp.audioSessionId).apply {
                            enabled = true
                            applyCurrentEqToNative()
                        }
                    } catch (e: Exception) {
                        Log.w("AetherAudio", "Equalizer init failed: ${e.message}")
                    }

                    mp.start()
                    acquireWakeLock()
                    AetherPlaybackService.start(context)
                    startProgressTracker()
                    startTelemetryStream()
                }

                setOnCompletionListener {
                    handleTrackCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    Log.w("AetherAudio", "Media error: what=$what, extra=$extra")
                    stopCurrent()
                    val err = when (what) {
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "媒体服务异常重启 ($extra)"
                        MediaPlayer.MEDIA_ERROR_UNKNOWN -> "媒体播放未知错误 ($extra)"
                        else -> "网络连接或媒体格式解析失败 ($what, $extra)"
                    }
                    _playerState.update {
                        it.copy(
                            status = PlaybackStatus.ERROR,
                            errorMessage = err
                        )
                    }
                    true
                }
            }

            mediaPlayer = player
            player.prepareAsync()

        } catch (e: Exception) {
            Log.e("AetherAudio", "Error initiating playback: ${e.message}", e)
            stopCurrent()
            _playerState.update {
                it.copy(
                    status = PlaybackStatus.ERROR,
                    errorMessage = "无法初始化音频流: ${e.message ?: "未知异常"}"
                )
            }
        }
    }

    private fun applyCurrentEqToNative() {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            val minEqLevel = eq.bandLevelRange[0] // in mB (millibels, e.g. -1500)
            val maxEqLevel = eq.bandLevelRange[1] // in mB (millibels, e.g. 1500)
            val customBands = _playerState.value.eqCustomBands

            for (i in 0 until minOf(numBands, customBands.size)) {
                val db = customBands[i]
                // Convert dB (-12 to +12) to millibels (-1200 to +1200) clamped to hardware limits
                val mB = (db * 100).toInt().coerceIn(minEqLevel.toInt(), maxEqLevel.toInt()).toShort()
                eq.setBandLevel(i.toShort(), mB)
            }
        } catch (e: Exception) {
            Log.w("AetherAudio", "Failed to apply EQ bands: ${e.message}")
        }
    }

    fun togglePlayPause() {
        val current = _playerState.value
        if (current.currentTrack == null) {
            if (queue.isNotEmpty()) {
                playTrack(queue.first())
            }
            return
        }

        when (current.status) {
            PlaybackStatus.PLAYING -> {
                mediaPlayer?.pause()
                releaseWakeLock()
                progressJob?.cancel()
                telemetryJob?.cancel()
                _playerState.update {
                    it.copy(
                        status = PlaybackStatus.PAUSED,
                        telemetry = it.telemetry.copy(leftChannelDb = -60f, rightChannelDb = -60f)
                    )
                }
                persistQueueState()
            }
            PlaybackStatus.PAUSED, PlaybackStatus.IDLE -> {
                requestAudioFocus()
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer?.start()
                        acquireWakeLock()
                        AetherPlaybackService.start(context)
                        _playerState.update { it.copy(status = PlaybackStatus.PLAYING) }
                        startProgressTracker()
                        startTelemetryStream()
                    } catch (_: Exception) {
                        current.currentTrack?.let { playTrack(it) }
                    }
                } else {
                    current.currentTrack?.let { playTrack(it) }
                }
            }
            PlaybackStatus.CONNECTING, PlaybackStatus.BUFFERING -> {
                // Already transitioning
            }
            PlaybackStatus.ERROR -> {
                current.currentTrack?.let { playTrack(it) }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _playerState.value.durationMs)
        try {
            mediaPlayer?.seekTo(target.toInt())
        } catch (_: Exception) {}
        _playerState.update { it.copy(currentPositionMs = target, scrubPositionMs = target) }
        persistQueueState()
    }

    fun setScrubbing(isScrubbing: Boolean, positionMs: Long) {
        _playerState.update {
            it.copy(
                isScrubbing = isScrubbing,
                scrubPositionMs = positionMs.coerceIn(0L, it.durationMs)
            )
        }
        if (!isScrubbing) {
            seekTo(positionMs)
        }
    }

    fun skipForward10s() {
        val current = _playerState.value.currentPositionMs
        seekTo(current + 10_000L)
    }

    fun skipBackward10s() {
        val current = _playerState.value.currentPositionMs
        seekTo(current - 10_000L)
    }

    fun skipForward30s() {
        val current = _playerState.value.currentPositionMs
        seekTo(current + 30_000L)
    }

    fun skipBackward15s() {
        val current = _playerState.value.currentPositionMs
        seekTo(current - 15_000L)
    }

    fun playNext() {
        if (queue.isEmpty()) return
        val currentTrack = _playerState.value.currentTrack
        val idx = if (currentTrack != null) queue.indexOfFirst { it.key == currentTrack.key && it.bucketName == currentTrack.bucketName } else currentIndex
        val nextIdx = if (idx >= 0) (idx + 1) % queue.size else 0
        currentIndex = nextIdx
        playTrack(queue[nextIdx], queue)
    }

    fun playPrevious() {
        if (queue.isEmpty()) return
        val currentTrack = _playerState.value.currentTrack
        val idx = if (currentTrack != null) queue.indexOfFirst { it.key == currentTrack.key && it.bucketName == currentTrack.bucketName } else currentIndex
        val prevIdx = if (idx > 0) idx - 1 else (queue.size - 1)
        currentIndex = prevIdx
        playTrack(queue[prevIdx], queue)
    }

    fun stopPlayback() {
        stopCurrent()
        abandonAudioFocus()
        AetherPlaybackService.stop(context)
        _playerState.update {
            it.copy(
                status = PlaybackStatus.IDLE,
                currentPositionMs = 0L,
                telemetry = it.telemetry.copy(leftChannelDb = -60f, rightChannelDb = -60f)
            )
        }
        persistQueueState()
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.update { it.copy(playbackSpeed = speed) }
        applySpeed(speed)
        persistQueueState()
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
            try {
                val params = mediaPlayer?.playbackParams ?: PlaybackParams()
                params.speed = speed
                mediaPlayer?.playbackParams = params
            } catch (_: Exception) {}
        }
    }

    fun toggleLoopMode(): LoopMode {
        val nextMode = when (_playerState.value.loopMode) {
            LoopMode.ALL -> LoopMode.SINGLE
            LoopMode.SINGLE -> LoopMode.OFF
            LoopMode.OFF -> LoopMode.ALL
            LoopMode.AB_REPEAT -> LoopMode.ALL
        }
        _playerState.update { it.copy(loopMode = nextMode) }
        try {
            mediaPlayer?.isLooping = (nextMode == LoopMode.SINGLE)
        } catch (_: Exception) {}
        persistQueueState()
        return nextMode
    }

    fun setLoopMode(mode: LoopMode) {
        _playerState.update { it.copy(loopMode = mode) }
        try {
            mediaPlayer?.isLooping = (mode == LoopMode.SINGLE)
        } catch (_: Exception) {}
        persistQueueState()
    }

    fun setAbPointA() {
        val pos = _playerState.value.currentPositionMs
        _playerState.update { it.copy(abPointA = pos, loopMode = LoopMode.AB_REPEAT) }
    }

    fun setAbPointB() {
        val pos = _playerState.value.currentPositionMs
        _playerState.update { it.copy(abPointB = pos, loopMode = LoopMode.AB_REPEAT) }
    }

    fun clearAbLoop() {
        _playerState.update { it.copy(abPointA = null, abPointB = null) }
    }

    fun setEqPreset(preset: EqPreset) {
        _playerState.update {
            it.copy(
                eqPreset = preset,
                eqCustomBands = preset.bands
            )
        }
        applyCurrentEqToNative()
    }

    fun setCustomEqBand(index: Int, db: Float) {
        val bands = _playerState.value.eqCustomBands.toMutableList()
        if (index in bands.indices) {
            bands[index] = db.coerceIn(-12f, 12f)
            _playerState.update { it.copy(eqCustomBands = bands) }
            applyCurrentEqToNative()
        }
    }

    private fun handleTrackCompletion() {
        val state = _playerState.value
        when (state.loopMode) {
            LoopMode.SINGLE -> {
                seekTo(0L)
                try {
                    mediaPlayer?.start()
                    _playerState.update { it.copy(status = PlaybackStatus.PLAYING) }
                } catch (e: Exception) {
                    state.currentTrack?.let { playTrack(it, queue) }
                }
            }
            LoopMode.ALL -> {
                playNext()
            }
            LoopMode.AB_REPEAT -> {
                val start = state.abPointA ?: 0L
                seekTo(start)
                try {
                    mediaPlayer?.start()
                    _playerState.update { it.copy(status = PlaybackStatus.PLAYING) }
                } catch (_: Exception) {
                    state.currentTrack?.let { playTrack(it, queue) }
                }
            }
            LoopMode.OFF -> {
                val currentTrack = state.currentTrack
                val idx = if (currentTrack != null) queue.indexOfFirst { it.key == currentTrack.key && it.bucketName == currentTrack.bucketName } else currentIndex
                if (queue.isNotEmpty() && idx >= 0 && idx < queue.size - 1) {
                    playNext()
                } else {
                    releaseWakeLock()
                    progressJob?.cancel()
                    telemetryJob?.cancel()
                    _playerState.update {
                        it.copy(
                            status = PlaybackStatus.PAUSED,
                            currentPositionMs = 0L,
                            telemetry = it.telemetry.copy(leftChannelDb = -60f, rightChannelDb = -60f)
                        )
                    }
                    persistQueueState()
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val state = _playerState.value
                if (state.status == PlaybackStatus.PLAYING && !state.isScrubbing) {
                    val mp = mediaPlayer
                    val pos = try {
                        mp?.currentPosition?.toLong() ?: (state.currentPositionMs + 50L)
                    } catch (_: Exception) {
                        state.currentPositionMs + 50L
                    }

                    // Check AB Loop boundary
                    if (state.loopMode == LoopMode.AB_REPEAT && state.abPointB != null && pos >= state.abPointB) {
                        val restart = state.abPointA ?: 0L
                        seekTo(restart)
                    } else if (mp == null && pos >= state.durationMs && state.durationMs > 0) {
                        handleTrackCompletion()
                    } else {
                        val updatedDuration = if (pos > state.durationMs) pos else state.durationMs
                        _playerState.update {
                            it.copy(
                                currentPositionMs = pos,
                                durationMs = updatedDuration
                            )
                        }
                    }
                }
                delay(40L) // 25fps fluid tick rate
            }
        }
    }

    private fun startTelemetryStream() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val state = _playerState.value
                if (state.status == PlaybackStatus.PLAYING) {
                    // Realistic dynamic audio peak bouncing (-36dB to -1.5dB)
                    val baseL = -14.0f + (random.nextFloat() * 11.0f)
                    val baseR = -14.0f + (random.nextFloat() * 11.0f)
                    val jitter = 0.3f + (random.nextFloat() * 0.5f)
                    val throughput = 3.6f + (random.nextFloat() * 1.8f)

                    _playerState.update {
                        it.copy(
                            telemetry = it.telemetry.copy(
                                leftChannelDb = baseL,
                                rightChannelDb = baseR,
                                jitterMs = jitter,
                                throughputMBs = throughput
                            )
                        )
                    }
                }
                delay(80L)
            }
        }
    }

    private fun stopCurrent() {
        releaseWakeLock()
        progressJob?.cancel()
        telemetryJob?.cancel()
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun release() {
        stopCurrent()
        abandonAudioFocus()
        AetherPlaybackService.stop(context)
        engineJob.cancel()
        synchronized(Companion) {
            if (INSTANCE == this) {
                INSTANCE = null
            }
        }
        if (_engineFlow.value == this) {
            _engineFlow.value = null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AetherAudioEngine? = null

        fun getInstance(context: Context): AetherAudioEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AetherAudioEngine(context.applicationContext).also {
                    INSTANCE = it
                    _engineFlow.value = it
                }
            }
        }

        private val _engineFlow = MutableStateFlow<AetherAudioEngine?>(null)
        val engineFlow: StateFlow<AetherAudioEngine?> = _engineFlow.asStateFlow()

        val currentEngine: AetherAudioEngine?
            get() = _engineFlow.value ?: INSTANCE
    }
}
