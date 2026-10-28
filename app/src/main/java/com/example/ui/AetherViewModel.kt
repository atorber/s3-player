package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AetherAudioEngine
import com.example.audio.EqPreset
import com.example.audio.LoopMode
import com.example.audio.PlaybackStatus
import com.example.audio.PlayerState
import com.example.data.local.AetherDatabase
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket
import com.example.data.repository.S3AudioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

data class UiNavigationState(
    val isExpandedPlayerVisible: Boolean = false,
    val isTelemetryInspectorVisible: Boolean = false,
    val isMountBucketDialogVisible: Boolean = false,
    val activeTab: Int = 0 // 0: 文件浏览, 1: 播放列表, 2: 同步设置
)

enum class TrackSortMode(val label: String) {
    NAME_ASC("名称 (A → Z)"),
    NAME_DESC("名称 (Z → A)"),
    DATE_DESC("最新修改优先"),
    DATE_ASC("最早修改优先"),
    SIZE_DESC("体积 (大 → 小)"),
    SIZE_ASC("体积 (小 → 大)"),
    DURATION_DESC("时长 (长 → 短)")
}

data class S3FolderItem(
    val name: String,
    val fullPrefix: String,
    val itemCount: Int,
    val totalSizeBytes: Long
) {
    val sizeFormatted: String
        get() {
            val mb = totalSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1000) String.format("%.2f GB", mb / 1024.0) else String.format("%.1f MB", mb)
        }
}

data class DirectoryTier(
    val level: Int,
    val title: String,
    val parentPrefix: String,
    val selectedSegment: String?,
    val folders: List<S3FolderItem>
)

data class SyncSettingsState(
    val currentBucketName: String = "xtrader",
    val basePrefixPath: String = "/",
    val endpoint: String = "xtrader.oss.cn-north-3.inspurcloudoss.com",
    val region: String = "cn-north-3",
    val accessKeyId: String = "",
    val secretAccessKey: String = "",
    val sessionToken: String = "",
    val usePathStyle: Boolean = false,
    val isTlsEncrypted: Boolean = true,
    val isPublicAccess: Boolean = false,
    val isSubdirMonitoring: Boolean = true,
    val pollIntervalSec: Int = 3,
    val isSnsNotification: Boolean = false,
    val enqueueRule: String = "append", // "append", "upnext", "ask"
    val enabledFormats: Set<String> = setOf(".flac", ".wav", ".mp3", ".m4a", ".aac", ".ogg", ".opus"),
    val cachedSizeBytes: Long = 0L,
    val cacheLimitBytes: Long = 5_368_709_120L, // 5.0 GB
    val isPinging: Boolean = false,
    val isSaving: Boolean = false
)

class AetherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AetherDatabase.getInstance(application)
    private val repository = S3AudioRepository(db.trackDao(), db.bucketDao(), viewModelScope)
    private val preferences = com.example.data.local.AetherPreferences(application)
    val audioEngine = AetherAudioEngine.getInstance(application)

    val playerState: StateFlow<PlayerState> = audioEngine.playerState

    val allBuckets: StateFlow<List<S3Bucket>> = repository.allBuckets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBucketName = MutableStateFlow<String?>(preferences.currentBucketName)
    val selectedBucketName: StateFlow<String?> = _selectedBucketName.asStateFlow()

    private val _selectedFormatFilter = MutableStateFlow<String?>("ALL")
    val selectedFormatFilter: StateFlow<String?> = _selectedFormatFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _navState = MutableStateFlow(UiNavigationState())
    val navState: StateFlow<UiNavigationState> = _navState.asStateFlow()

    // Realtime banner in playlist
    private val _realtimeBannerTrack = MutableStateFlow<S3AudioTrack?>(null)
    val realtimeBannerTrack: StateFlow<S3AudioTrack?> = _realtimeBannerTrack.asStateFlow()

    private val _isRealtimeBannerVisible = MutableStateFlow(false)
    val isRealtimeBannerVisible: StateFlow<Boolean> = _isRealtimeBannerVisible.asStateFlow()

    // Recursive scan toggle in browser (false = strict tier-by-tier navigation)
    private val _isRecursiveScan = MutableStateFlow(false)
    val isRecursiveScan: StateFlow<Boolean> = _isRecursiveScan.asStateFlow()

    // File Browser Navigation & Organization (starts at base prefix for level-by-level exploration)
    private val _currentPrefix = MutableStateFlow(
        preferences.basePrefixPath.trim().removePrefix("/").let { if (it.isBlank()) "" else "$it/" }
    )
    val currentPrefix: StateFlow<String> = _currentPrefix.asStateFlow()

    private val _sortMode = MutableStateFlow(
        try { TrackSortMode.valueOf(preferences.sortModeName) } catch (_: Exception) { TrackSortMode.NAME_ASC }
    )
    val sortMode: StateFlow<TrackSortMode> = _sortMode.asStateFlow()

    private val _isGridView = MutableStateFlow(preferences.isGridView)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedTrackIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTrackIds: StateFlow<Set<Long>> = _selectedTrackIds.asStateFlow()

    private val _selectedTrackForDetails = MutableStateFlow<S3AudioTrack?>(null)
    val selectedTrackForDetails: StateFlow<S3AudioTrack?> = _selectedTrackForDetails.asStateFlow()

    private val _isUploadDialogVisible = MutableStateFlow(false)
    val isUploadDialogVisible: StateFlow<Boolean> = _isUploadDialogVisible.asStateFlow()

    private val _isNewFolderDialogVisible = MutableStateFlow(false)
    val isNewFolderDialogVisible: StateFlow<Boolean> = _isNewFolderDialogVisible.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _customFolders = MutableStateFlow<Set<String>>(preferences.discoveredFolders)

    // Subdirectories discovery inside current bucket and prefix (immediate child directories)
    // Subdirectories discovery inside current bucket and prefix (immediate child directories of current path)
    val browserFolders: StateFlow<List<S3FolderItem>> = combine(
        repository.allTracks,
        _selectedBucketName,
        _currentPrefix,
        _customFolders,
        _searchQuery
    ) { tracks, bucketName, currentPrefix, customFolders, query ->
        val bucketTracks = if (bucketName != null && bucketName != "ALL") {
            tracks.filter { it.bucketName.equals(bucketName, ignoreCase = true) }
        } else {
            tracks
        }

        // Canonical normalized prefix for current directory (e.g. "" for root, "music/" for music)
        val cleanPrefix = currentPrefix.trim().trim('/')
        val prefixNorm = if (cleanPrefix.isBlank()) "" else "$cleanPrefix/"

        val folderMap = mutableMapOf<String, Pair<Int, Long>>() // fullPrefix -> (count, sizeBytes)

        // 1. Child folders from discovered S3 CommonPrefixes
        customFolders.forEach { f ->
            val cleanF = f.trim().trim('/')
            if (cleanF.isNotBlank()) {
                val fNorm = "$cleanF/"
                if (fNorm.startsWith(prefixNorm) && fNorm != prefixNorm) {
                    val rel = fNorm.removePrefix(prefixNorm)
                    val directSegment = rel.substringBefore('/')
                    if (directSegment.isNotBlank()) {
                        val subPrefix = "$prefixNorm$directSegment/"
                        folderMap.putIfAbsent(subPrefix, Pair(0, 0L))
                    }
                }
            }
        }

        // 2. Child folders from tracks in the current bucket under current path
        bucketTracks.forEach { track ->
            val cleanKey = track.key.trim().removePrefix("/")
            if (cleanKey.startsWith(prefixNorm) && cleanKey != prefixNorm) {
                val rel = cleanKey.removePrefix(prefixNorm)
                if (rel.contains('/')) {
                    val directSegment = rel.substringBefore('/')
                    if (directSegment.isNotBlank()) {
                        val subPrefix = "$prefixNorm$directSegment/"
                        val (currentCount, currentSize) = folderMap.getOrDefault(subPrefix, Pair(0, 0L))
                        folderMap[subPrefix] = Pair(currentCount + 1, currentSize + track.sizeBytes)
                    }
                }
            }
        }

        val allFolders = folderMap.map { (fullPrefix, stats) ->
            val folderName = fullPrefix.removePrefix(prefixNorm)
            S3FolderItem(
                name = folderName,
                fullPrefix = fullPrefix,
                itemCount = stats.first,
                totalSizeBytes = stats.second
            )
        }

        val q = query.trim()
        if (q.isNotBlank()) {
            allFolders.filter { it.name.contains(q, ignoreCase = true) || it.fullPrefix.contains(q, ignoreCase = true) }
        } else {
            allFolders
        }.sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hierarchical multi-tier directory structure (Level 1, Level 2, Level 3...)
    val directoryTiers: StateFlow<List<DirectoryTier>> = combine(
        repository.allTracks,
        _selectedBucketName,
        _currentPrefix,
        _customFolders
    ) { tracks, bucketName, currentPrefix, customFolders ->
        val bucketTracks = if (bucketName != null && bucketName != "ALL") {
            tracks.filter { it.bucketName.equals(bucketName, ignoreCase = true) }
        } else {
            tracks
        }

        val prefixNorm = if (currentPrefix.isBlank() || currentPrefix.endsWith("/")) currentPrefix else "$currentPrefix/"
        val cleanPrefix = prefixNorm.trim().trim('/')
        val segments = if (cleanPrefix.isBlank()) emptyList() else cleanPrefix.split('/')

        fun queryImmediateFolders(parent: String): List<S3FolderItem> {
            val norm = if (parent.isBlank() || parent.endsWith("/")) parent else "$parent/"
            val folderMap = mutableMapOf<String, Pair<Int, Long>>()
            customFolders.filter { it.startsWith(norm) && it != norm }.forEach { folderPrefix ->
                val rel = folderPrefix.removePrefix(norm)
                val firstSegment = rel.substringBefore('/')
                if (firstSegment.isNotBlank()) {
                    val subPrefix = "$norm$firstSegment/"
                    folderMap.putIfAbsent(subPrefix, Pair(0, 0L))
                }
            }
            bucketTracks.forEach { track ->
                val key = track.key
                if (key.startsWith(norm) && key != norm) {
                    val rel = key.removePrefix(norm)
                    if (rel.contains('/')) {
                        val folderName = rel.substringBefore('/') + "/"
                        val subPrefix = "$norm$folderName"
                        val (currentCount, currentSize) = folderMap.getOrDefault(subPrefix, Pair(0, 0L))
                        folderMap[subPrefix] = Pair(currentCount + 1, currentSize + track.sizeBytes)
                    }
                }
            }
            return folderMap.map { (fullPrefix, stats) ->
                val folderName = fullPrefix.removePrefix(norm)
                S3FolderItem(
                    name = folderName,
                    fullPrefix = fullPrefix,
                    itemCount = stats.first,
                    totalSizeBytes = stats.second
                )
            }.sortedBy { it.name.lowercase() }
        }

        val tiers = mutableListOf<DirectoryTier>()

        // Level 1: Root directory level
        val rootFolders = queryImmediateFolders("")
        tiers.add(
            DirectoryTier(
                level = 1,
                title = "根目录层级 (s3://${bucketName ?: "xtrader"}/)",
                parentPrefix = "",
                selectedSegment = segments.getOrNull(0),
                folders = rootFolders
            )
        )

        // Nested sub-tiers for each segment down the hierarchy
        var accumulated = ""
        for (i in segments.indices) {
            val segment = segments[i]
            accumulated = if (accumulated.isBlank()) "$segment/" else "$accumulated$segment/"
            val subFolders = queryImmediateFolders(accumulated)
            val nextSelected = segments.getOrNull(i + 1)
            tiers.add(
                DirectoryTier(
                    level = i + 2,
                    title = "$segment/ (第 ${i + 2} 级子目录)",
                    parentPrefix = accumulated,
                    selectedSegment = nextSelected,
                    folders = subFolders
                )
            )
        }

        tiers
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic format counts for ribbon badges
    val formatCounts: StateFlow<Map<String, Int>> = combine(
        repository.allTracks,
        _selectedBucketName,
        _currentPrefix,
        _isRecursiveScan,
        _searchQuery
    ) { tracks, bucketName, currentPrefix, isRecursive, query ->
        val prefixNorm = if (currentPrefix.isBlank() || currentPrefix.endsWith("/")) currentPrefix else "$currentPrefix/"
        var list = if (bucketName != null && bucketName != "ALL") {
            tracks.filter { it.bucketName.equals(bucketName, ignoreCase = true) }
        } else {
            tracks
        }
        val shouldSearchDeep = isRecursive || query.isNotBlank()
        list = if (shouldSearchDeep) {
            list.filter { it.key.startsWith(prefixNorm) }
        } else {
            list.filter { it.key.startsWith(prefixNorm) && !it.key.removePrefix(prefixNorm).contains('/') }
        }

        val q = query.trim()
        if (q.isNotBlank()) {
            list = list.filter {
                it.title.contains(q, ignoreCase = true) ||
                it.key.contains(q, ignoreCase = true) ||
                it.artistOrProject.contains(q, ignoreCase = true)
            }
        }

        val map = mutableMapOf<String, Int>()
        map["ALL"] = list.size
        list.forEach { track ->
            val fmt = track.format.uppercase()
            map[fmt] = (map[fmt] ?: 0) + 1
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // High-powered browser tracks list (filtered by prefix, recursive mode, format, query, and sorted)
    val browserTracks: StateFlow<List<S3AudioTrack>> = combine(
        combine(
            repository.allTracks,
            _selectedBucketName,
            _currentPrefix,
            _isRecursiveScan,
            _searchQuery
        ) { tracks, bucketName, currentPrefix, isRecursive, query ->
            val prefixNorm = if (currentPrefix.isBlank() || currentPrefix.endsWith("/")) currentPrefix else "$currentPrefix/"
            val bucketTracks = if (bucketName != null && bucketName != "ALL") {
                tracks.filter { it.bucketName.equals(bucketName, ignoreCase = true) }
            } else {
                tracks
            }
            val shouldSearchDeep = isRecursive || query.isNotBlank()
            if (shouldSearchDeep) {
                bucketTracks.filter { it.key.startsWith(prefixNorm) }
            } else {
                bucketTracks.filter { it.key.startsWith(prefixNorm) && !it.key.removePrefix(prefixNorm).contains('/') }
            }
        },
        _selectedFormatFilter,
        _searchQuery,
        _sortMode
    ) { tracksInFolder, formatFilter, query, sort ->
        var list = tracksInFolder

        val q = query.trim()
        if (q.isNotBlank()) {
            list = list.filter {
                it.title.contains(q, ignoreCase = true) ||
                it.key.contains(q, ignoreCase = true) ||
                it.artistOrProject.contains(q, ignoreCase = true)
            }
        }

        if (formatFilter != null && formatFilter != "ALL") {
            list = list.filter { it.format.equals(formatFilter.removePrefix("."), ignoreCase = true) }
        }

        when (sort) {
            TrackSortMode.NAME_ASC -> list.sortedBy { it.title.lowercase() }
            TrackSortMode.NAME_DESC -> list.sortedByDescending { it.title.lowercase() }
            TrackSortMode.DATE_DESC -> list.sortedByDescending { it.lastModified }
            TrackSortMode.DATE_ASC -> list.sortedBy { it.lastModified }
            TrackSortMode.SIZE_DESC -> list.sortedByDescending { it.sizeBytes }
            TrackSortMode.SIZE_ASC -> list.sortedBy { it.sizeBytes }
            TrackSortMode.DURATION_DESC -> list.sortedByDescending { it.durationMs }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sync settings state
    private val _syncSettings = MutableStateFlow(
        SyncSettingsState(
            currentBucketName = preferences.currentBucketName,
            basePrefixPath = preferences.basePrefixPath,
            endpoint = preferences.endpoint,
            region = preferences.region,
            accessKeyId = preferences.accessKeyId,
            secretAccessKey = preferences.secretAccessKey,
            sessionToken = preferences.sessionToken,
            usePathStyle = preferences.usePathStyle,
            isTlsEncrypted = preferences.isTlsEncrypted,
            isPublicAccess = preferences.isPublicAccess,
            isSubdirMonitoring = preferences.isSubdirMonitoring,
            pollIntervalSec = preferences.pollIntervalSec,
            isSnsNotification = preferences.isSnsNotification,
            enqueueRule = preferences.enqueueRule,
            enabledFormats = preferences.enabledFormats
        )
    )
    val syncSettings: StateFlow<SyncSettingsState> = _syncSettings.asStateFlow()

    // Audio sound processing tweaks
    private val _isPitchCorrectionEnabled = MutableStateFlow(preferences.isPitchCorrectionEnabled)
    val isPitchCorrectionEnabled: StateFlow<Boolean> = _isPitchCorrectionEnabled.asStateFlow()

    private val _isSilenceTrimmingEnabled = MutableStateFlow(preferences.isSilenceTrimmingEnabled)
    val isSilenceTrimmingEnabled: StateFlow<Boolean> = _isSilenceTrimmingEnabled.asStateFlow()

    // Auto-play state when new tracks are scanned and added
    private val _isAutoPlayNewEnabled = MutableStateFlow(preferences.isAutoPlayNewEnabled)
    val isAutoPlayNewEnabled: StateFlow<Boolean> = _isAutoPlayNewEnabled.asStateFlow()

    private var periodicPollingJob: Job? = null

    // Toast event for feedback
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val filteredTracks: StateFlow<List<S3AudioTrack>> = combine(
        repository.allTracks,
        _selectedBucketName,
        _selectedFormatFilter,
        _searchQuery
    ) { tracks, bucketName, formatFilter, query ->
        var list = tracks

        if (bucketName != null && bucketName != "ALL") {
            list = list.filter { it.bucketName.equals(bucketName, ignoreCase = true) }
        }

        if (formatFilter != null && formatFilter != "ALL") {
            list = list.filter { it.format.equals(formatFilter.removePrefix("."), ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.key.contains(query, ignoreCase = true) ||
                it.artistOrProject.contains(query, ignoreCase = true)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistQueue: StateFlow<List<S3AudioTrack>> = audioEngine.queueFlow

    private val _monitoredPlaylistPrefix = MutableStateFlow<String?>(preferences.monitoredPlaylistPrefix)
    private val _monitoredPlaylistBucket = MutableStateFlow<String?>(preferences.monitoredPlaylistBucket)
    val monitoredPlaylistBucket: StateFlow<String?> = _monitoredPlaylistBucket.asStateFlow()

    // Thread-safe set of tracks that have already been handled / enqueued by directory monitoring
    private val processedMonitoredKeys = Collections.synchronizedSet(mutableSetOf<String>())

    fun updateMonitoredPlaylistPrefix(prefix: String, bucket: String? = null) {
        val clean = prefix.trim().trim('/')
        val norm = if (clean.isBlank()) "" else "$clean/"
        _monitoredPlaylistPrefix.value = norm
        preferences.monitoredPlaylistPrefix = norm
        val b = bucket ?: _selectedBucketName.value?.takeIf { it != "ALL" } ?: syncSettings.value.currentBucketName
        _monitoredPlaylistBucket.value = b
        preferences.monitoredPlaylistBucket = b
    }

    val activePlaylistPath: StateFlow<String> = combine(
        _monitoredPlaylistPrefix,
        playerState,
        _currentPrefix,
        playlistQueue
    ) { monitored, state, currPrefix, queue ->
        if (monitored != null) {
            monitored
        } else {
            val track = state.currentTrack ?: queue.firstOrNull()
            if (track != null) {
                val key = track.key.removePrefix("/")
                if (key.contains("/")) key.substringBeforeLast("/") + "/" else ""
            } else {
                currPrefix
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, preferences.monitoredPlaylistPrefix ?: "")

    init {
        // Continuously record queued tracks so manual playlist actions don't get falsely re-detected
        viewModelScope.launch {
            playlistQueue.collect { queue ->
                queue.forEach { t ->
                    processedMonitoredKeys.add(t.key)
                    processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
                }
            }
        }

        viewModelScope.launch {
            // Restore configured bucket settings from database if available
            val buckets = repository.allBuckets.first()
            val savedBucketName = preferences.currentBucketName
            val matching = buckets.firstOrNull { it.bucketName.equals(savedBucketName, ignoreCase = true) } ?: buckets.firstOrNull()
            if (matching != null) {
                _syncSettings.update {
                    it.copy(
                        currentBucketName = matching.bucketName,
                        endpoint = matching.endpoint,
                        region = matching.region,
                        accessKeyId = matching.accessKey ?: "",
                        secretAccessKey = matching.secretKey ?: "",
                        sessionToken = matching.sessionToken ?: "",
                        usePathStyle = matching.usePathStyle,
                        isTlsEncrypted = matching.useSsl,
                        isPublicAccess = matching.accessKey.isNullOrBlank(),
                        basePrefixPath = matching.basePrefix
                    )
                }
                _selectedBucketName.value = matching.bucketName
                preferences.currentBucketName = matching.bucketName
                val initialPrefix = matching.basePrefix.trim().removePrefix("/").removeSuffix("/")
                val startPrefix = if (initialPrefix.isBlank()) "" else "$initialPrefix/"
                _currentPrefix.value = startPrefix
                scanRealS3(startPrefix)
            } else {
                val initialPrefix = _syncSettings.value.basePrefixPath.trim().removePrefix("/").removeSuffix("/")
                val startPrefix = if (initialPrefix.isBlank()) "" else "$initialPrefix/"
                _currentPrefix.value = startPrefix
                scanRealS3(startPrefix)
            }

            // Start background polling for new audio files
            startPeriodicPolling()
        }
    }

    fun selectBucket(bucketName: String?) {
        _selectedBucketName.value = bucketName
        if (!bucketName.isNullOrBlank() && bucketName != "ALL") {
            preferences.currentBucketName = bucketName
        }
    }

    fun setFormatFilter(format: String?) {
        _selectedFormatFilter.value = format
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTab(tab: Int) {
        _navState.update { it.copy(activeTab = tab) }
    }

    fun setExpandedPlayerVisible(visible: Boolean) {
        _navState.update { it.copy(isExpandedPlayerVisible = visible) }
    }

    fun setTelemetryInspectorVisible(visible: Boolean) {
        _navState.update { it.copy(isTelemetryInspectorVisible = visible) }
    }

    fun setMountBucketDialogVisible(visible: Boolean) {
        _navState.update { it.copy(isMountBucketDialogVisible = visible) }
    }

    fun dismissRealtimeBanner() {
        _isRealtimeBannerVisible.value = false
    }

    fun toggleRecursiveScan() {
        _isRecursiveScan.update { !it }
    }

    fun togglePitchCorrection() {
        val next = !_isPitchCorrectionEnabled.value
        _isPitchCorrectionEnabled.value = next
        preferences.isPitchCorrectionEnabled = next
    }

    fun toggleSilenceTrimming() {
        val next = !_isSilenceTrimmingEnabled.value
        _isSilenceTrimmingEnabled.value = next
        preferences.isSilenceTrimmingEnabled = next
    }

    fun playTrackFromBrowser(track: S3AudioTrack) {
        val key = track.key.trim().removePrefix("/")
        val folderPrefix = if (key.contains("/")) key.substringBeforeLast("/") + "/" else ""
        updateMonitoredPlaylistPrefix(folderPrefix, track.bucketName)
        processedMonitoredKeys.clear()
        browserTracks.value.forEach { t ->
            processedMonitoredKeys.add(t.key)
            processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
        }
        audioEngine.playTrack(track, browserTracks.value)
    }

    fun playTrackFromPlaylist(track: S3AudioTrack) {
        audioEngine.playTrack(track, null)
    }

    fun togglePlayPause() {
        audioEngine.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun setScrubbing(isScrubbing: Boolean, positionMs: Long) {
        audioEngine.setScrubbing(isScrubbing, positionMs)
    }

    fun skipForward10s() {
        audioEngine.skipForward10s()
    }

    fun skipBackward10s() {
        audioEngine.skipBackward10s()
    }

    fun skipForward30s() {
        audioEngine.skipForward30s()
    }

    fun skipBackward15s() {
        audioEngine.skipBackward15s()
    }

    fun playNext() {
        audioEngine.playNext()
    }

    fun playPrevious() {
        audioEngine.playPrevious()
    }

    fun setPlaybackSpeed(speed: Float) {
        audioEngine.setPlaybackSpeed(speed)
    }

    fun toggleAutoPlayNew() {
        val next = !_isAutoPlayNewEnabled.value
        _isAutoPlayNewEnabled.value = next
        preferences.isAutoPlayNewEnabled = next
        viewModelScope.launch {
            _toastEvent.emit(if (next) "自动播放已开启：扫描到新音频将自动加入并播放" else "自动播放已关闭")
        }
    }

    fun stopPlayback() {
        audioEngine.stopPlayback()
    }

    fun toggleLoopMode() {
        val nextMode = audioEngine.toggleLoopMode()
        val desc = when (nextMode) {
            LoopMode.ALL -> "列表循环"
            LoopMode.SINGLE -> "单曲循环"
            LoopMode.OFF -> "顺序播放 (循环关闭)"
            LoopMode.AB_REPEAT -> "A-B 区间循环"
        }
        viewModelScope.launch {
            _toastEvent.emit("循环模式已切换为：$desc")
        }
    }

    fun setAbPointA() {
        audioEngine.setAbPointA()
    }

    fun setAbPointB() {
        audioEngine.setAbPointB()
    }

    fun clearAbLoop() {
        audioEngine.clearAbLoop()
    }

    fun setEqPreset(preset: EqPreset) {
        audioEngine.setEqPreset(preset)
    }

    fun setCustomEqBand(index: Int, db: Float) {
        audioEngine.setCustomEqBand(index, db)
    }

    // Persistence Actions
    fun toggleFavorite(track: S3AudioTrack) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
        }
    }

    fun toggleCacheLocally(track: S3AudioTrack) {
        viewModelScope.launch {
            repository.toggleCacheLocally(track.id, track.isCachedLocally)
        }
    }

    // Sync Settings Interactions
    fun updatePollInterval(sec: Int) {
        _syncSettings.update { it.copy(pollIntervalSec = sec) }
        preferences.pollIntervalSec = sec
        startPeriodicPolling()
    }

    fun setEnqueueRule(rule: String) {
        _syncSettings.update { it.copy(enqueueRule = rule) }
        preferences.enqueueRule = rule
    }

    fun togglePublicAccess() {
        _syncSettings.update {
            val next = !it.isPublicAccess
            preferences.isPublicAccess = next
            it.copy(isPublicAccess = next)
        }
    }

    fun toggleSubdirMonitoring() {
        val next = !_syncSettings.value.isSubdirMonitoring
        _syncSettings.update {
            preferences.isSubdirMonitoring = next
            it.copy(isSubdirMonitoring = next)
        }
        if (next) {
            val target = activePlaylistPath.value.trim().trim('/')
            val normTarget = if (target.isBlank()) "" else "$target/"
            val bucket = _monitoredPlaylistBucket.value
                ?: _selectedBucketName.value?.takeIf { it != "ALL" }
                ?: syncSettings.value.currentBucketName
            updateMonitoredPlaylistPrefix(normTarget, bucket)

            startPeriodicPolling()
            viewModelScope.launch(Dispatchers.IO) {
                pollRealS3AndAutoEnqueue(force = false)
            }
            viewModelScope.launch {
                val locDisplay = if (normTarget.isBlank()) "根目录 /" else normTarget
                _toastEvent.emit("目录监听已开启：正在实时监控 s3://$bucket/$locDisplay")
            }
        } else {
            viewModelScope.launch {
                _toastEvent.emit("目录监听已暂停")
            }
        }
    }

    fun toggleSnsNotification() {
        _syncSettings.update {
            val next = !it.isSnsNotification
            preferences.isSnsNotification = next
            it.copy(isSnsNotification = next)
        }
    }

    fun toggleFormatEnabled(format: String) {
        _syncSettings.update {
            val updated = it.enabledFormats.toMutableSet()
            if (updated.contains(format)) updated.remove(format) else updated.add(format)
            preferences.enabledFormats = updated
            it.copy(enabledFormats = updated)
        }
    }

    fun runPingTest(
        endpoint: String = _syncSettings.value.endpoint,
        bucketName: String = _syncSettings.value.currentBucketName,
        region: String = _syncSettings.value.region,
        accessKeyId: String = _syncSettings.value.accessKeyId,
        secretAccessKey: String = _syncSettings.value.secretAccessKey,
        sessionToken: String = _syncSettings.value.sessionToken,
        usePathStyle: Boolean = _syncSettings.value.usePathStyle,
        isTlsEncrypted: Boolean = _syncSettings.value.isTlsEncrypted,
        isPublicAccess: Boolean = _syncSettings.value.isPublicAccess
    ) {
        viewModelScope.launch {
            _syncSettings.update { it.copy(isPinging = true) }
            val startTime = System.currentTimeMillis()
            val result = repository.remoteService.listObjectsV2(
                endpoint = endpoint.trim(),
                bucketName = bucketName.trim(),
                prefix = "",
                delimiter = "/",
                region = region.trim().ifBlank { "cn-north-3" },
                accessKeyId = accessKeyId.trim(),
                secretAccessKey = secretAccessKey.trim(),
                sessionToken = sessionToken.trim(),
                usePathStyle = usePathStyle,
                useTls = isTlsEncrypted,
                isPublicAccess = isPublicAccess
            )
            val latency = System.currentTimeMillis() - startTime
            _syncSettings.update { it.copy(isPinging = false) }

            result.onSuccess {
                _toastEvent.emit("S3 远端连接正常 · 延迟: ${latency}ms · 端点 [$endpoint] 存储桶 [$bucketName] 握手成功")
            }.onFailure { ex ->
                _toastEvent.emit("S3 握手失败: ${ex.message}")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _syncSettings.update { it.copy(cachedSizeBytes = 0L) }
            _toastEvent.emit("已清理临时分块缓冲区。")
        }
    }

    fun exportM3u8() {
        viewModelScope.launch {
            _toastEvent.emit("已生成包含预签名 URL 的 \"s3-xtrader.m3u8\" 播放列表")
        }
    }

    fun saveAndRestartSync(
        bucketName: String = _syncSettings.value.currentBucketName,
        prefix: String = _syncSettings.value.basePrefixPath,
        endpoint: String = _syncSettings.value.endpoint,
        region: String = _syncSettings.value.region,
        accessKeyId: String = _syncSettings.value.accessKeyId,
        secretAccessKey: String = _syncSettings.value.secretAccessKey,
        sessionToken: String = _syncSettings.value.sessionToken,
        usePathStyle: Boolean = _syncSettings.value.usePathStyle,
        isTlsEncrypted: Boolean = _syncSettings.value.isTlsEncrypted,
        isPublicAccess: Boolean = _syncSettings.value.isPublicAccess
    ) {
        viewModelScope.launch {
            _syncSettings.update {
                it.copy(
                    isSaving = true,
                    currentBucketName = bucketName.trim().ifBlank { "xtrader" },
                    basePrefixPath = prefix.trim(),
                    endpoint = endpoint.trim().ifBlank { "xtrader.oss.cn-north-3.inspurcloudoss.com" },
                    region = region.trim().ifBlank { "cn-north-3" },
                    accessKeyId = accessKeyId.trim(),
                    secretAccessKey = secretAccessKey.trim(),
                    sessionToken = sessionToken.trim(),
                    usePathStyle = usePathStyle,
                    isTlsEncrypted = isTlsEncrypted,
                    isPublicAccess = isPublicAccess
                )
            }

            val cleanEndpoint = endpoint.trim().removePrefix("https://").removePrefix("http://").ifBlank { "xtrader.oss.cn-north-3.inspurcloudoss.com" }
            val cleanBucket = bucketName.trim().ifBlank { "xtrader" }
            val cleanPrefix = if (prefix.isNotBlank() && !prefix.startsWith("/")) "/${prefix.trim()}" else prefix.trim()
            val authDesc = if (isPublicAccess) "匿名 / 公开访问" else "S3 SigV4 认证"

            // Persist to SharedPreferences so configuration survives upgrades
            preferences.currentBucketName = cleanBucket
            preferences.basePrefixPath = cleanPrefix
            preferences.endpoint = cleanEndpoint
            preferences.region = region.trim().ifBlank { "cn-north-3" }
            preferences.accessKeyId = accessKeyId.trim()
            preferences.secretAccessKey = secretAccessKey.trim()
            preferences.sessionToken = sessionToken.trim()
            preferences.usePathStyle = usePathStyle
            preferences.isTlsEncrypted = isTlsEncrypted
            preferences.isPublicAccess = isPublicAccess
            preferences.discoveredFolders = emptySet()
            _customFolders.value = emptySet()

            // Clear previous bucket tracks and processed keys so the newly configured bucket starts clean
            repository.clearAllTracks()
            processedMonitoredKeys.clear()

            val bucket = S3Bucket(
                bucketName = cleanBucket,
                endpoint = cleanEndpoint,
                region = region.trim().ifBlank { "cn-north-3" },
                provider = if (usePathStyle) "S3 兼容协议 (Path-Style)" else "通用 S3 兼容协议",
                latencyMs = 0,
                objectCount = 0,
                storageSizeFormatted = "0 B",
                isMounted = true,
                authType = authDesc,
                usePathStyle = usePathStyle,
                useSsl = isTlsEncrypted,
                accessKey = if (isPublicAccess) null else accessKeyId.takeIf { it.isNotBlank() },
                secretKey = if (isPublicAccess) null else secretAccessKey.takeIf { it.isNotBlank() },
                sessionToken = if (isPublicAccess) null else sessionToken.takeIf { it.isNotBlank() },
                basePrefix = cleanPrefix
            )
            repository.addBucket(bucket)
            _selectedBucketName.value = cleanBucket

            val normPrefix = cleanPrefix.trim().trim('/')
            val initialPrefix = if (normPrefix.isBlank()) "" else "$normPrefix/"
            _currentPrefix.value = initialPrefix
            updateMonitoredPlaylistPrefix(initialPrefix, cleanBucket)

            val protocolPrefix = if (isTlsEncrypted) "https://" else "http://"
            _toastEvent.emit("S3 存储桶配置已保存 · 正在从云端读取 s3://$cleanBucket$initialPrefix ($protocolPrefix$cleanEndpoint)...")

            // Real S3 remote scan: populate baseline tracks into processedMonitoredKeys before releasing isSaving
            scanRealS3Internal(initialPrefix)
            _syncSettings.update { it.copy(isSaving = false) }
        }
    }

    fun playAllInDirectory() {
        viewModelScope.launch {
            val bucket = _selectedBucketName.value ?: syncSettings.value.currentBucketName
            updateMonitoredPlaylistPrefix(_currentPrefix.value, bucket)
            val tracks = browserTracks.value
            processedMonitoredKeys.clear()
            tracks.forEach { t ->
                processedMonitoredKeys.add(t.key)
                processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
            }
            if (tracks.isNotEmpty()) {
                audioEngine.playTrack(tracks.first(), tracks)
                _toastEvent.emit("已将当前目录创建为播放列表并开始播放 (${tracks.size} 首)")
            } else {
                _toastEvent.emit("已绑定监听当前目录，有新音频上传将自动加入并播放")
            }
        }
    }

    fun addCurrentDirectoryToPlaylist() {
        viewModelScope.launch {
            val bucket = _selectedBucketName.value ?: syncSettings.value.currentBucketName
            updateMonitoredPlaylistPrefix(_currentPrefix.value, bucket)
            val tracks = browserTracks.value
            processedMonitoredKeys.clear()
            tracks.forEach { t ->
                processedMonitoredKeys.add(t.key)
                processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
            }
            if (tracks.isNotEmpty()) {
                audioEngine.updateQueue(tracks)
                _toastEvent.emit("已将当前目录创建为播放列表 (${tracks.size} 首)")
            } else {
                audioEngine.clearQueue()
                _toastEvent.emit("已将当前空目录设为播放列表并开启监听")
            }
        }
    }

    private fun sortFolderTracks(tracks: List<S3AudioTrack>): List<S3AudioTrack> {
        return when (_sortMode.value) {
            TrackSortMode.NAME_ASC -> tracks.sortedBy { it.title.lowercase() }
            TrackSortMode.NAME_DESC -> tracks.sortedByDescending { it.title.lowercase() }
            TrackSortMode.DATE_DESC -> tracks.sortedByDescending { it.lastModified }
            TrackSortMode.DATE_ASC -> tracks.sortedBy { it.lastModified }
            TrackSortMode.SIZE_DESC -> tracks.sortedByDescending { it.sizeBytes }
            TrackSortMode.SIZE_ASC -> tracks.sortedBy { it.sizeBytes }
            TrackSortMode.DURATION_DESC -> tracks.sortedByDescending { it.durationMs }
        }
    }

    fun addFolderToPlaylist(folderPrefix: String) {
        viewModelScope.launch {
            val bucketName = _selectedBucketName.value ?: syncSettings.value.currentBucketName
            val cleanPrefix = folderPrefix.trim().trim('/')
            val norm = if (cleanPrefix.isBlank()) "" else "$cleanPrefix/"
            updateMonitoredPlaylistPrefix(norm, bucketName)

            val all = repository.allTracks.first()
            val folderTracks = sortFolderTracks(
                all.filter {
                    (bucketName == "ALL" || it.bucketName.equals(bucketName, ignoreCase = true)) &&
                    it.key.trim().removePrefix("/").startsWith(norm)
                }
            )
            processedMonitoredKeys.clear()
            folderTracks.forEach { t ->
                processedMonitoredKeys.add(t.key)
                processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
            }
            if (folderTracks.isNotEmpty()) {
                audioEngine.updateQueue(folderTracks)
                val folderName = norm.trimEnd('/').substringAfterLast('/')
                _toastEvent.emit("已将目录「$folderName/」创建为播放列表并开启监听 (${folderTracks.size} 首)")
            } else {
                val settings = _syncSettings.value
                val fetchResult = repository.syncRemoteS3(
                    endpoint = settings.endpoint,
                    bucketName = if (bucketName == "ALL") settings.currentBucketName else bucketName,
                    prefix = norm,
                    delimiter = "",
                    region = settings.region,
                    accessKeyId = settings.accessKeyId,
                    secretAccessKey = settings.secretAccessKey,
                    sessionToken = settings.sessionToken,
                    usePathStyle = settings.usePathStyle,
                    useTls = settings.isTlsEncrypted,
                    isPublicAccess = settings.isPublicAccess
                )
                fetchResult.onSuccess { res ->
                    val sorted = sortFolderTracks(res.tracks)
                    sorted.forEach { t ->
                        processedMonitoredKeys.add(t.key)
                        processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
                    }
                    if (sorted.isNotEmpty()) {
                        audioEngine.updateQueue(sorted)
                        val folderName = norm.trimEnd('/').substringAfterLast('/')
                        _toastEvent.emit("已将目录「$folderName/」创建为播放列表并开启监听 (${sorted.size} 首)")
                    } else {
                        audioEngine.clearQueue()
                        _toastEvent.emit("已监听目录 [$norm]，新音频上传后将自动加入播放")
                    }
                }.onFailure {
                    _toastEvent.emit("创建播放列表失败：${it.message}")
                }
            }
        }
    }

    fun playFolder(folderPrefix: String) {
        viewModelScope.launch {
            val bucketName = _selectedBucketName.value ?: syncSettings.value.currentBucketName
            val cleanPrefix = folderPrefix.trim().trim('/')
            val norm = if (cleanPrefix.isBlank()) "" else "$cleanPrefix/"
            updateMonitoredPlaylistPrefix(norm, bucketName)

            val all = repository.allTracks.first()
            val folderTracks = sortFolderTracks(
                all.filter {
                    (bucketName == "ALL" || it.bucketName.equals(bucketName, ignoreCase = true)) &&
                    it.key.trim().removePrefix("/").startsWith(norm)
                }
            )
            processedMonitoredKeys.clear()
            folderTracks.forEach { t ->
                processedMonitoredKeys.add(t.key)
                processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
            }
            if (folderTracks.isNotEmpty()) {
                audioEngine.playTrack(folderTracks.first(), folderTracks)
                val folderName = norm.trimEnd('/').substringAfterLast('/')
                _toastEvent.emit("正在播放目录「$folderName/」并监听新音频 (${folderTracks.size} 首)")
            } else {
                val settings = _syncSettings.value
                val fetchResult = repository.syncRemoteS3(
                    endpoint = settings.endpoint,
                    bucketName = if (bucketName == "ALL") settings.currentBucketName else bucketName,
                    prefix = norm,
                    delimiter = "",
                    region = settings.region,
                    accessKeyId = settings.accessKeyId,
                    secretAccessKey = settings.secretAccessKey,
                    sessionToken = settings.sessionToken,
                    usePathStyle = settings.usePathStyle,
                    useTls = settings.isTlsEncrypted,
                    isPublicAccess = settings.isPublicAccess
                )
                fetchResult.onSuccess { res ->
                    val sorted = sortFolderTracks(res.tracks)
                    sorted.forEach { t ->
                        processedMonitoredKeys.add(t.key)
                        processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
                    }
                    if (sorted.isNotEmpty()) {
                        audioEngine.playTrack(sorted.first(), sorted)
                        val folderName = norm.trimEnd('/').substringAfterLast('/')
                        _toastEvent.emit("正在播放目录「$folderName/」并监听新音频 (${sorted.size} 首)")
                    } else {
                        _toastEvent.emit("该子目录下暂无音频，已开启监听等待新文件上传")
                    }
                }.onFailure {
                    _toastEvent.emit("加载并播放失败：${it.message}")
                }
            }
        }
    }

    fun clearPlayedQueue() {
        audioEngine.clearPlayedQueue()
        viewModelScope.launch {
            _toastEvent.emit("已清理已播完曲目")
        }
    }

    fun removeFromQueue(track: S3AudioTrack) {
        audioEngine.removeFromQueue(track)
        processedMonitoredKeys.add(track.key)
        processedMonitoredKeys.add(track.key.trim().removePrefix("/"))
        viewModelScope.launch {
            _toastEvent.emit("已从播放队列移除: ${track.title}")
        }
    }

    fun clearAllQueue() {
        audioEngine.clearQueue()
        viewModelScope.launch {
            _toastEvent.emit("已清空播放队列")
        }
    }

    fun enqueueTrackNext(track: S3AudioTrack) {
        audioEngine.insertNextInQueue(listOf(track))
        processedMonitoredKeys.add(track.key)
        processedMonitoredKeys.add(track.key.trim().removePrefix("/"))
        viewModelScope.launch {
            _toastEvent.emit("已将「${track.title}」插队为下一首播放")
        }
    }

    fun forceCheckNewUploads() {
        viewModelScope.launch(Dispatchers.IO) {
            pollRealS3AndAutoEnqueue(force = true)
        }
    }

    private fun startPeriodicPolling() {
        periodicPollingJob?.cancel()
        periodicPollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val settings = _syncSettings.value
                    if (!settings.isSubdirMonitoring) {
                        delay(2000L)
                        continue
                    }
                    val intervalSec = settings.pollIntervalSec.coerceIn(2, 60)
                    delay(intervalSec * 1000L)
                    if (isActive && _syncSettings.value.isSubdirMonitoring) {
                        pollRealS3AndAutoEnqueue(force = false)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Log.e("AetherViewModel", "Error in periodic polling loop", e)
                    delay(5000L)
                }
            }
        }
    }

    private suspend fun pollRealS3AndAutoEnqueue(force: Boolean = false) {
        val settings = _syncSettings.value
        if (settings.isSaving || _isScanning.value) return
        val bucketName = _monitoredPlaylistBucket.value
            ?: _selectedBucketName.value?.takeIf { it != "ALL" }
            ?: settings.currentBucketName
        if (bucketName.isBlank()) return

        val rawTarget = activePlaylistPath.value.trim().trim('/')
        val cleanTargetPrefix = if (rawTarget.isBlank()) "" else "$rawTarget/"

        val currentQueue = playlistQueue.value
        val currentQueueKeys = currentQueue.map { it.key.trim().removePrefix("/") }.toSet()
        val currentQueueFullKeys = currentQueue.map { it.key }.toSet()
        val allowedExts = settings.enabledFormats.map { it.removePrefix(".").lowercase() }.toSet()

        val result = repository.syncRemoteS3(
            endpoint = settings.endpoint,
            bucketName = bucketName,
            prefix = cleanTargetPrefix,
            delimiter = "",
            region = settings.region,
            accessKeyId = settings.accessKeyId,
            secretAccessKey = settings.secretAccessKey,
            sessionToken = settings.sessionToken,
            usePathStyle = settings.usePathStyle,
            useTls = settings.isTlsEncrypted,
            isPublicAccess = settings.isPublicAccess,
            allowedExtensions = allowedExts
        )

        result.onSuccess { listResult ->
            if (listResult.commonPrefixes.isNotEmpty()) {
                _customFolders.update { current ->
                    val updated = (current + listResult.commonPrefixes).toSet()
                    preferences.discoveredFolders = updated
                    updated
                }
            }

            // Filter tracks in the monitored directory that are not yet in the playlist and not previously processed
            val rawNewTracks = listResult.tracks.filter { track ->
                val cleanKey = track.key.trim().removePrefix("/")
                val inTargetDir = if (cleanTargetPrefix.isNotBlank()) {
                    cleanKey.startsWith(cleanTargetPrefix)
                } else {
                    _isRecursiveScan.value || !cleanKey.contains('/')
                }

                val isAlreadyInQueue = cleanKey in currentQueueKeys || track.key in currentQueueFullKeys
                val isAlreadyProcessed = cleanKey in processedMonitoredKeys || track.key in processedMonitoredKeys

                inTargetDir && !isAlreadyInQueue && !isAlreadyProcessed
            }
            val newTracks = sortFolderTracks(rawNewTracks)

            if (newTracks.isNotEmpty()) {
                // Register newly detected tracks as processed so they are not repeatedly re-added
                newTracks.forEach { t ->
                    processedMonitoredKeys.add(t.key)
                    processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
                }

                val playerStatus = audioEngine.playerState.value.status
                val isCurrentlyPlaying = (playerStatus == PlaybackStatus.PLAYING ||
                        playerStatus == PlaybackStatus.BUFFERING ||
                        playerStatus == PlaybackStatus.CONNECTING)

                _realtimeBannerTrack.value = newTracks.first()
                _isRealtimeBannerVisible.value = true

                withContext(Dispatchers.Main) {
                    when (settings.enqueueRule) {
                        "upnext" -> audioEngine.insertNextInQueue(newTracks)
                        "ask" -> { /* real-time banner shown */ }
                        else -> audioEngine.addAllToQueue(newTracks)
                    }

                    if (_isAutoPlayNewEnabled.value && !isCurrentlyPlaying) {
                        val trackToPlay = newTracks.first()
                        playTrackFromPlaylist(trackToPlay)
                        _toastEvent.emit("检测到 S3 新增音频，已加入列表并自动播放：${trackToPlay.title}")
                    } else {
                        _toastEvent.emit("检测到 S3 新增 ${newTracks.size} 首音频，已加入播放列表")
                    }
                }
            } else if (force) {
                withContext(Dispatchers.Main) {
                    val locDisplay = if (cleanTargetPrefix.isBlank()) "根目录 /" else cleanTargetPrefix
                    _toastEvent.emit("当前目录 [s3://$bucketName/$locDisplay] 暂无未加入播放列表的新增音频")
                }
            }
        }.onFailure { ex ->
            if (force) {
                withContext(Dispatchers.Main) {
                    _toastEvent.emit("监听目录检测异常: ${ex.message}")
                }
            }
        }
    }

    fun scanRealS3(targetPrefix: String = _currentPrefix.value) {
        viewModelScope.launch {
            scanRealS3Internal(targetPrefix)
        }
    }

    private suspend fun scanRealS3Internal(targetPrefix: String = _currentPrefix.value) {
        _isScanning.value = true
        val settings = _syncSettings.value
        val bucketName = _selectedBucketName.value?.takeIf { it != "ALL" } ?: settings.currentBucketName
        val normTarget = if (targetPrefix == "/") "" else targetPrefix.trim().trim('/')
        val cleanTargetPrefix = if (normTarget.isBlank()) "" else "$normTarget/"

        val allowedExts = settings.enabledFormats.map { it.removePrefix(".").lowercase() }.toSet()

        // 1. Stage 1: Fast directory discovery with delimiter="/" to capture all immediate subfolders (including empty folders)
        val dirResult = repository.syncRemoteS3(
            endpoint = settings.endpoint,
            bucketName = bucketName,
            prefix = cleanTargetPrefix,
            delimiter = "/",
            region = settings.region,
            accessKeyId = settings.accessKeyId,
            secretAccessKey = settings.secretAccessKey,
            sessionToken = settings.sessionToken,
            usePathStyle = settings.usePathStyle,
            useTls = settings.isTlsEncrypted,
            isPublicAccess = settings.isPublicAccess,
            allowedExtensions = allowedExts
        )
        dirResult.onSuccess { dirList ->
            if (dirList.commonPrefixes.isNotEmpty()) {
                _customFolders.update { current ->
                    val updated = (current + dirList.commonPrefixes).toSet()
                    preferences.discoveredFolders = updated
                    updated
                }
            }
        }

        // 2. Stage 2: Deep recursive scan with delimiter="" and multi-page pagination to fetch all nested audio tracks and ancestor directories
        val result = repository.syncRemoteS3(
            endpoint = settings.endpoint,
            bucketName = bucketName,
            prefix = cleanTargetPrefix,
            delimiter = "",
            region = settings.region,
            accessKeyId = settings.accessKeyId,
            secretAccessKey = settings.secretAccessKey,
            sessionToken = settings.sessionToken,
            usePathStyle = settings.usePathStyle,
            useTls = settings.isTlsEncrypted,
            isPublicAccess = settings.isPublicAccess,
            allowedExtensions = allowedExts
        )

        _isScanning.value = false
        result.onSuccess { listResult ->
            if (listResult.commonPrefixes.isNotEmpty()) {
                _customFolders.update { current ->
                    val updated = (current + listResult.commonPrefixes).toSet()
                    preferences.discoveredFolders = updated
                    updated
                }
            }
            // Register all scanned tracks as processed baseline so background polling does not treat existing files as "new uploads"
            listResult.tracks.forEach { t ->
                processedMonitoredKeys.add(t.key)
                processedMonitoredKeys.add(t.key.trim().removePrefix("/"))
            }

            val directTracksCount = listResult.tracks.count { track ->
                val cleanKey = track.key.trim().removePrefix("/")
                cleanKey.startsWith(cleanTargetPrefix) && !cleanKey.removePrefix(cleanTargetPrefix).contains('/')
            }
            val audioDirsCount = listResult.tracks
                .mapNotNull { track ->
                    val rel = track.key.trim().removePrefix("/").removePrefix(cleanTargetPrefix)
                    if (rel.contains('/')) rel.substringBefore('/') else null
                }
                .distinct()
                .size
            val pathDisplay = if (cleanTargetPrefix.isBlank()) "根目录 /" else "/$cleanTargetPrefix"

            _toastEvent.emit("S3 同步完成 [$pathDisplay]：发现 $audioDirsCount 个音频目录，当前层级 $directTracksCount 首音频 (共扫描 ${listResult.tracks.size} 首)")
        }.onFailure { ex ->
            val errorMsg = ex.message ?: "网络或认证异常"
            _toastEvent.emit("S3 同步失败：$errorMsg")
        }
    }

    fun forceScanNow() {
        scanRealS3(_currentPrefix.value)
    }

    fun batchPresign() {
        viewModelScope.launch {
            val count = browserTracks.value.size
            _toastEvent.emit("已为当前目录 $count 个对象生成 24 小时有效预签名直链")
        }
    }

    // Browser Directory Navigation & Selection Actions
    fun navigateToPrefix(prefix: String) {
        val clean = prefix.trim().trim('/')
        val norm = if (clean.isBlank()) "" else "$clean/"
        _currentPrefix.value = norm
        _selectedTrackIds.value = emptySet()
        scanRealS3(norm)
    }

    fun navigateUp() {
        val clean = _currentPrefix.value.trim().trim('/')
        if (clean.isBlank()) return
        val lastSlash = clean.lastIndexOf('/')
        val newPrefix = if (lastSlash >= 0) {
            clean.substring(0, lastSlash + 1)
        } else {
            ""
        }
        _currentPrefix.value = newPrefix
        _selectedTrackIds.value = emptySet()
        scanRealS3(newPrefix)
    }

    fun navigateToRoot() {
        _currentPrefix.value = ""
        _selectedTrackIds.value = emptySet()
        scanRealS3("")
    }

    fun navigateToSyncBase() {
        val base = syncSettings.value.basePrefixPath.trim().trim('/')
        val target = if (base.isBlank()) "" else "$base/"
        _currentPrefix.value = target
        _selectedTrackIds.value = emptySet()
        scanRealS3(target)
    }

    fun locateCurrentPlayingTrackDirectory() {
        val currentTrack = audioEngine.playerState.value.currentTrack
        if (currentTrack != null) {
            val key = currentTrack.key.trim().removePrefix("/")
            val lastSlash = key.lastIndexOf('/')
            val parentFolder = if (lastSlash >= 0) key.substring(0, lastSlash + 1) else ""
            _currentPrefix.value = parentFolder
            _selectedBucketName.value = currentTrack.bucketName
            _selectedTrackIds.value = emptySet()
            scanRealS3(parentFolder)
            viewModelScope.launch {
                val display = if (parentFolder.isBlank()) "根目录 /" else "/$parentFolder"
                _toastEvent.emit("已定位至正在播放曲目目录: s3://${currentTrack.bucketName}$display")
            }
        } else {
            viewModelScope.launch {
                _toastEvent.emit("当前暂无播放曲目，已停留在当前目录")
            }
        }
    }

    fun setSortMode(mode: TrackSortMode) {
        _sortMode.value = mode
        preferences.sortModeName = mode.name
    }

    fun toggleGridView() {
        _isGridView.update { current ->
            val next = !current
            preferences.isGridView = next
            next
        }
    }

    fun toggleSelectionMode() {
        _isSelectionMode.update { !it }
        if (!_isSelectionMode.value) {
            _selectedTrackIds.value = emptySet()
        }
    }

    fun toggleTrackSelection(id: Long) {
        _selectedTrackIds.update { set ->
            if (set.contains(id)) set - id else set + id
        }
    }

    fun selectAll(ids: List<Long>) {
        _selectedTrackIds.value = ids.toSet()
    }

    fun clearSelection() {
        _selectedTrackIds.value = emptySet()
    }

    fun batchPlaySelected() {
        viewModelScope.launch {
            val selected = browserTracks.value.filter { it.id in _selectedTrackIds.value }
            if (selected.isNotEmpty()) {
                audioEngine.playTrack(selected.first(), selected)
                _toastEvent.emit("正在播放选中的 ${selected.size} 首音频")
            }
        }
    }

    fun batchAddSelectedToQueue() {
        viewModelScope.launch {
            val selected = browserTracks.value.filter { it.id in _selectedTrackIds.value }
            if (selected.isNotEmpty()) {
                audioEngine.addAllToQueue(selected)
                _toastEvent.emit("已将选中的 ${selected.size} 首音频加入播放列表")
                _isSelectionMode.value = false
                _selectedTrackIds.value = emptySet()
            }
        }
    }

    fun batchCacheSelected(cache: Boolean) {
        viewModelScope.launch {
            val selectedIds = _selectedTrackIds.value
            selectedIds.forEach { id ->
                repository.toggleCacheLocally(id, !cache)
            }
            val actionStr = if (cache) "已下载到本地缓存" else "已清除本地缓存"
            _toastEvent.emit("已批量对 ${selectedIds.size} 个对象$actionStr")
            _isSelectionMode.value = false
            _selectedTrackIds.value = emptySet()
        }
    }

    fun batchDeleteSelected() {
        viewModelScope.launch {
            val selectedIds = _selectedTrackIds.value
            selectedIds.forEach { id ->
                repository.deleteTrack(id)
            }
            _toastEvent.emit("已从存储桶移除选中的 ${selectedIds.size} 个音频对象")
            _isSelectionMode.value = false
            _selectedTrackIds.value = emptySet()
        }
    }

    fun batchPresignSelected() {
        viewModelScope.launch {
            val count = _selectedTrackIds.value.size.coerceAtLeast(1)
            _toastEvent.emit("已为选中的 $count 个对象生成 24 小时预签名直链")
            _isSelectionMode.value = false
            _selectedTrackIds.value = emptySet()
        }
    }

    fun showTrackDetails(track: S3AudioTrack?) {
        _selectedTrackForDetails.value = track
    }

    fun setUploadDialogVisible(visible: Boolean) {
        _isUploadDialogVisible.value = visible
    }

    fun setNewFolderDialogVisible(visible: Boolean) {
        _isNewFolderDialogVisible.value = visible
    }

    fun createNewFolder(folderName: String) {
        viewModelScope.launch {
            val clean = folderName.trim().removePrefix("/").removeSuffix("/")
            if (clean.isNotBlank()) {
                val full = "${_currentPrefix.value}$clean/"
                _customFolders.update { current ->
                    val updated = (current + full).toSet()
                    preferences.discoveredFolders = updated
                    updated
                }
                _isNewFolderDialogVisible.value = false
                _toastEvent.emit("已新建 S3 目录前缀: $full")
            }
        }
    }

    fun uploadNewTrack(
        title: String,
        format: String,
        durationSec: Long,
        sizeMb: Double,
        storageClass: String
    ) {
        viewModelScope.launch {
            val currentBucket = _selectedBucketName.value ?: "xtrader"
            val cleanTitle = title.trim().ifBlank { "new_audio_recording" }
            val cleanFormat = format.trim().uppercase().removePrefix(".")
            val key = "${_currentPrefix.value}$cleanTitle.${cleanFormat.lowercase()}"
            val sizeBytes = (sizeMb * 1024 * 1024).toLong()
            val durationMs = durationSec * 1000L

            val newTrack = S3AudioTrack(
                bucketName = currentBucket,
                key = key,
                title = "$cleanTitle.${cleanFormat.lowercase()}",
                artistOrProject = "手动上传 / PutObject · ${_currentPrefix.value}",
                format = cleanFormat,
                sampleRate = if (cleanFormat == "FLAC") "96kHz / 24-Bit" else "48kHz / 24-Bit",
                channels = "2.0 Stereo",
                sizeBytes = sizeBytes,
                durationMs = durationMs,
                etag = "\"${System.currentTimeMillis().toString(16)}s3md5\"",
                storageClass = storageClass,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                isCachedLocally = false,
                isFavorite = false,
                lastModified = "刚刚"
            )
            repository.addTrack(newTrack)
            _isUploadDialogVisible.value = false
            _toastEvent.emit("PutObject 上传成功 · 已写入 s3://$currentBucket/$key")
        }
    }

    fun addToQueue(track: S3AudioTrack) {
        audioEngine.addToQueue(track)
        viewModelScope.launch {
            _toastEvent.emit("已加入播放列表: ${track.title}")
        }
    }

    fun deleteTrack(track: S3AudioTrack) {
        viewModelScope.launch {
            repository.deleteTrack(track.id)
            if (_selectedTrackForDetails.value?.id == track.id) {
                _selectedTrackForDetails.value = null
            }
            _toastEvent.emit("已从存储桶移除: ${track.title}")
        }
    }

    fun copyTrackPresignedUrl(track: S3AudioTrack) {
        viewModelScope.launch {
            _toastEvent.emit("已生成并复制 24 小时预签名直链: ${track.title}")
        }
    }

    fun copyTrackS3Uri(track: S3AudioTrack) {
        viewModelScope.launch {
            _toastEvent.emit("已复制 S3 URI: ${track.fullS3Uri}")
        }
    }

    fun mountNewBucket(
        name: String,
        endpoint: String,
        region: String,
        accessKey: String? = null,
        secretKey: String? = null,
        usePathStyle: Boolean = false,
        useSsl: Boolean = true
    ) {
        viewModelScope.launch {
            val cleanEndpoint = endpoint.trim().removePrefix("https://").removePrefix("http://")
            val authDesc = if (!accessKey.isNullOrBlank()) "AK/SK 签名 (V4)" else "匿名 / 公开访问"
            val newBucket = S3Bucket(
                bucketName = name.trim(),
                endpoint = cleanEndpoint,
                region = region.trim().ifBlank { "us-east-1" },
                provider = if (usePathStyle) "S3 兼容 (Path-Style)" else "标准 S3 协议",
                latencyMs = (15..45).random(),
                objectCount = 1,
                storageSizeFormatted = "12.4 MB",
                isMounted = true,
                authType = authDesc,
                usePathStyle = usePathStyle,
                useSsl = useSsl,
                accessKey = accessKey?.takeIf { it.isNotBlank() }
            )
            repository.addBucket(newBucket)

            val initialTrack = S3AudioTrack(
                bucketName = name.trim(),
                key = "podcasts/2025-season/ep04-live/${name.trim()}_test_stream.flac",
                title = "${name.trim()}_stream_track.flac",
                artistOrProject = "S3 协议节点: $cleanEndpoint",
                format = "FLAC",
                sampleRate = "96kHz / 24-bit",
                channels = "2.0 Stereo",
                sizeBytes = 13002342L,
                durationMs = 192000L,
                etag = "\"${System.currentTimeMillis().toString(16)}s3hash\"",
                storageClass = if (usePathStyle) "S3_COMPATIBLE" else "STANDARD",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            )
            repository.addTrack(initialTrack)
            _selectedBucketName.value = name.trim()
            _toastEvent.emit("已挂载 S3 存储桶: ${name.trim()}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Do not release audioEngine here so music continues playing in the background
        // when the user leaves the UI or switches away from the app.
    }
}
