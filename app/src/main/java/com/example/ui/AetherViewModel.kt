package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AetherAudioEngine
import com.example.audio.EqPreset
import com.example.audio.LoopMode
import com.example.audio.PlayerState
import com.example.data.local.AetherDatabase
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket
import com.example.data.repository.S3AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiNavigationState(
    val isExpandedPlayerVisible: Boolean = false,
    val isTelemetryInspectorVisible: Boolean = false,
    val isMountBucketDialogVisible: Boolean = false,
    val isBucketListDrawerOpen: Boolean = false,
    val activeTab: Int = 0 // 0: Explorer, 1: Buckets, 2: Favorites, 3: Telemetry
)

class AetherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AetherDatabase.getInstance(application)
    private val repository = S3AudioRepository(db.trackDao(), db.bucketDao(), viewModelScope)
    val audioEngine = AetherAudioEngine(application, viewModelScope)

    val playerState: StateFlow<PlayerState> = audioEngine.playerState

    val allBuckets: StateFlow<List<S3Bucket>> = repository.allBuckets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBucketName = MutableStateFlow<String?>("prod-audio-stems")
    val selectedBucketName: StateFlow<String?> = _selectedBucketName.asStateFlow()

    private val _selectedFormatFilter = MutableStateFlow<String?>("ALL")
    val selectedFormatFilter: StateFlow<String?> = _selectedFormatFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _navState = MutableStateFlow(UiNavigationState())
    val navState: StateFlow<UiNavigationState> = _navState.asStateFlow()

    val filteredTracks: StateFlow<List<S3AudioTrack>> = combine(
        repository.allTracks,
        _selectedBucketName,
        _selectedFormatFilter,
        _searchQuery,
        _navState
    ) { tracks, bucketName, formatFilter, query, nav ->
        var list = tracks

        if (nav.activeTab == 2) {
            // Favorites tab
            list = list.filter { it.isFavorite }
        } else if (bucketName != null && bucketName != "ALL") {
            list = list.filter { it.bucketName.equals(bucketName, ignoreCase = true) }
        }

        if (formatFilter != null && formatFilter != "ALL") {
            list = list.filter { it.format.equals(formatFilter, ignoreCase = true) }
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

    init {
        // Keep audio engine queue in sync
        viewModelScope.launch {
            filteredTracks.collect { tracks ->
                audioEngine.updateQueue(tracks)
            }
        }
    }

    fun selectBucket(bucketName: String?) {
        _selectedBucketName.value = bucketName
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

    // Audio Engine Actions
    fun playTrack(track: S3AudioTrack) {
        audioEngine.playTrack(track, filteredTracks.value)
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

    fun playNext() {
        audioEngine.playNext()
    }

    fun playPrevious() {
        audioEngine.playPrevious()
    }

    fun setPlaybackSpeed(speed: Float) {
        audioEngine.setPlaybackSpeed(speed)
    }

    fun toggleLoopMode() {
        audioEngine.toggleLoopMode()
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

    fun mountNewBucket(name: String, region: String, provider: String, endpoint: String) {
        viewModelScope.launch {
            val newBucket = S3Bucket(
                bucketName = name,
                region = region,
                provider = provider,
                endpoint = endpoint,
                latencyMs = (15..45).random(),
                objectCount = 0,
                storageSizeFormatted = "0 MB",
                isMounted = true
            )
            repository.addBucket(newBucket)
            _selectedBucketName.value = name
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
