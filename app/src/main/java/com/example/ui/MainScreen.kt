package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AetherDockPlayer
import com.example.ui.components.AetherNavBar
import com.example.ui.components.AetherTopBar
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.ExpandedPlayerScreen
import com.example.ui.screens.MountBucketDialog
import com.example.ui.screens.PlaylistScreen
import com.example.ui.screens.SyncSettingsScreen
import com.example.ui.screens.TelemetryInspectorScreen
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AetherViewModel) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val browserTracks by viewModel.browserTracks.collectAsStateWithLifecycle()
    val browserFolders by viewModel.browserFolders.collectAsStateWithLifecycle()
    val formatCounts by viewModel.formatCounts.collectAsStateWithLifecycle()
    val allBuckets by viewModel.allBuckets.collectAsStateWithLifecycle()
    val selectedBucketName by viewModel.selectedBucketName.collectAsStateWithLifecycle()
    val currentPrefix by viewModel.currentPrefix.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedTrackIds by viewModel.selectedTrackIds.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val playlistQueue by viewModel.playlistQueue.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFormat by viewModel.selectedFormatFilter.collectAsStateWithLifecycle()
    val isRealtimeBannerVisible by viewModel.isRealtimeBannerVisible.collectAsStateWithLifecycle()
    val realtimeBannerTrack by viewModel.realtimeBannerTrack.collectAsStateWithLifecycle()
    val isRecursiveScan by viewModel.isRecursiveScan.collectAsStateWithLifecycle()
    val directoryTiers by viewModel.directoryTiers.collectAsStateWithLifecycle()
    val syncSettings by viewModel.syncSettings.collectAsStateWithLifecycle()
    val isAutoPlayNewEnabled by viewModel.isAutoPlayNewEnabled.collectAsStateWithLifecycle()

    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Collect Toast notifications
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            toastMessage = msg
            delay(3500)
            toastMessage = null
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherVoid),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AetherTopBar(
                activeTab = navState.activeTab,
                currentUri = "s3://${syncSettings.currentBucketName}${if (syncSettings.basePrefixPath.startsWith("/")) syncSettings.basePrefixPath else if (syncSettings.basePrefixPath.isBlank()) "/" else "/${syncSettings.basePrefixPath}"}",
                onOpenSyncStatus = { viewModel.setTelemetryInspectorVisible(true) },
                onProfileClick = { viewModel.setActiveTab(2) },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent Floating Dock Player
                if (playerState.currentTrack != null) {
                    AetherDockPlayer(
                        playerState = playerState,
                        onExpandPlayer = { viewModel.setExpandedPlayerVisible(true) },
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSkipBackward10 = viewModel::skipBackward10s,
                        onSkipForward30 = viewModel::skipForward30s
                    )
                }

                // 3-Tab Bottom Navigation Bar
                AetherNavBar(
                    activeTab = navState.activeTab,
                    onTabSelected = viewModel::setActiveTab
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (navState.activeTab) {
                0 -> {
                    // Screen 1: 文件浏览 (Browser)
                    BrowserScreen(
                        currentBucket = selectedBucketName ?: "xtrader",
                        allBuckets = allBuckets,
                        currentPrefix = currentPrefix,
                        folders = browserFolders,
                        tracks = browserTracks,
                        playerState = playerState,
                        searchQuery = searchQuery,
                        selectedFormat = selectedFormat,
                        formatCounts = formatCounts,
                        sortMode = sortMode,
                        isGridView = isGridView,
                        isSelectionMode = isSelectionMode,
                        selectedTrackIds = selectedTrackIds,
                        isRecursiveScan = isRecursiveScan,
                        isScanning = isScanning,
                        onSelectBucket = viewModel::selectBucket,
                        onNavigateToPrefix = viewModel::navigateToPrefix,
                        onNavigateUp = viewModel::navigateUp,
                        onNavigateToRoot = viewModel::navigateToRoot,
                        onSearchChange = viewModel::setSearchQuery,
                        onSelectFormat = viewModel::setFormatFilter,
                        onToggleRecursiveScan = viewModel::toggleRecursiveScan,
                        onSetSortMode = viewModel::setSortMode,
                        onToggleGridView = viewModel::toggleGridView,
                        onToggleSelectionMode = viewModel::toggleSelectionMode,
                        onToggleTrackSelection = viewModel::toggleTrackSelection,
                        onSelectAll = viewModel::selectAll,
                        onClearSelection = viewModel::clearSelection,
                        onBatchPlay = viewModel::batchPlaySelected,
                        onBatchAddToQueue = viewModel::batchAddSelectedToQueue,
                        onBatchCache = viewModel::batchCacheSelected,
                        onBatchPresign = viewModel::batchPresignSelected,
                        onBatchDelete = viewModel::batchDeleteSelected,
                        onPlayTrack = viewModel::playTrackFromBrowser,
                        onPlayAll = viewModel::playAllInDirectory,
                        onForceScan = viewModel::forceScanNow,
                        onAddToQueue = viewModel::addToQueue,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onToggleCache = viewModel::toggleCacheLocally,
                        onDeleteTrack = viewModel::deleteTrack,
                        onCopyPresignedUrl = viewModel::copyTrackPresignedUrl,
                        onCopyS3Uri = viewModel::copyTrackS3Uri,
                        onShowTrackDetails = viewModel::showTrackDetails,
                        onCreateFolder = viewModel::createNewFolder,
                        onUploadTrack = viewModel::uploadNewTrack,
                        directoryTiers = directoryTiers,
                        onLocatePlayingTrack = viewModel::locateCurrentPlayingTrackDirectory,
                        onAddFolderToPlaylist = viewModel::addFolderToPlaylist,
                        onPlayFolder = viewModel::playFolder,
                        onAddCurrentDirectoryToPlaylist = viewModel::addCurrentDirectoryToPlaylist,
                        syncBasePrefix = syncSettings.basePrefixPath,
                        onNavigateToSyncBase = viewModel::navigateToSyncBase
                    )
                }
                1 -> {
                    // Screen 2: 播放列表 (Playlist)
                    val activePlaylistPath by viewModel.activePlaylistPath.collectAsStateWithLifecycle()
                    val activeBucket = selectedBucketName?.takeIf { it != "ALL" } ?: syncSettings.currentBucketName
                    val displayLoc = "s3://$activeBucket/${activePlaylistPath.removePrefix("/")}"
                    PlaylistScreen(
                        tracks = playlistQueue,
                        playerState = playerState,
                        isBannerVisible = isRealtimeBannerVisible,
                        isAutoPlayNewEnabled = isAutoPlayNewEnabled,
                        monitoredLocation = displayLoc,
                        bannerTrack = realtimeBannerTrack,
                        onDismissBanner = viewModel::dismissRealtimeBanner,
                        onPlayTrack = viewModel::playTrackFromPlaylist,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onStopPlayback = viewModel::stopPlayback,
                        onPlayNewArrival = {
                            val newArrival = realtimeBannerTrack ?: playlistQueue.firstOrNull()
                            newArrival?.let { viewModel.playTrackFromPlaylist(it) }
                            viewModel.dismissRealtimeBanner()
                        },
                        onToggleAutoPlayNew = viewModel::toggleAutoPlayNew,
                        onToggleRepeat = viewModel::toggleLoopMode,
                        onClearPlayed = viewModel::clearPlayedQueue,
                    )
                }
                2 -> {
                    // Screen 3: 同步设置 (Sync Settings)
                    SyncSettingsScreen(
                        settings = syncSettings,
                        onUpdatePollInterval = viewModel::updatePollInterval,
                        onSetEnqueueRule = viewModel::setEnqueueRule,
                        onTogglePublicAccess = viewModel::togglePublicAccess,
                        onToggleSubdirMonitoring = viewModel::toggleSubdirMonitoring,
                        onToggleSnsNotification = viewModel::toggleSnsNotification,
                        onToggleFormatEnabled = viewModel::toggleFormatEnabled,
                        onRunPingTest = { ep, bk, reg, ak, sk, tok, ps, ssl, pub ->
                            viewModel.runPingTest(ep, bk, reg, ak, sk, tok, ps, ssl, pub)
                        },
                        onClearCache = viewModel::clearCache,
                        onExportM3u8 = viewModel::exportM3u8,
                        onSaveAndRestart = { b, p, ep, reg, ak, sk, tok, ps, ssl, pub ->
                            viewModel.saveAndRestartSync(b, p, ep, reg, ak, sk, tok, ps, ssl, pub)
                        }
                    )
                }
            }

            // Floating Dynamic Toast Banner
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            ) {
                toastMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ElectricCyanDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyanDark
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = ElectricCyanDark,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { toastMessage = null }
                        )
                    }
                }
            }

            // Expanded Full Player Modal
            if (navState.isExpandedPlayerVisible && playerState.currentTrack != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.setExpandedPlayerVisible(false) },
                    containerColor = AetherVoid
                ) {
                    ExpandedPlayerScreen(
                        playerState = playerState,
                        onCollapse = { viewModel.setExpandedPlayerVisible(false) },
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSeekTo = viewModel::seekTo,
                        onScrub = viewModel::setScrubbing,
                        onSkipForward10s = viewModel::skipForward10s,
                        onSkipBackward10s = viewModel::skipBackward10s,
                        onPlayNext = viewModel::playNext,
                        onPlayPrevious = viewModel::playPrevious,
                        onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
                        onToggleLoopMode = viewModel::toggleLoopMode,
                        onSetAbPointA = viewModel::setAbPointA,
                        onSetAbPointB = viewModel::setAbPointB,
                        onClearAbLoop = viewModel::clearAbLoop,
                        onOpenTelemetry = { viewModel.setTelemetryInspectorVisible(true) },
                        onToggleFavorite = { playerState.currentTrack?.let { viewModel.toggleFavorite(it) } },
                        onToggleCache = { playerState.currentTrack?.let { viewModel.toggleCacheLocally(it) } }
                    )
                }
            }

            // Standalone Telemetry Inspector Modal
            if (navState.isTelemetryInspectorVisible) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.setTelemetryInspectorVisible(false) },
                    containerColor = AetherVoid
                ) {
                    TelemetryInspectorScreen(
                        playerState = playerState,
                        onClose = { viewModel.setTelemetryInspectorVisible(false) },
                        onSetEqPreset = viewModel::setEqPreset,
                        onSetBand = viewModel::setCustomEqBand
                    )
                }
            }

            // Mount Bucket Dialog
            if (navState.isMountBucketDialogVisible) {
                MountBucketDialog(
                    onDismiss = { viewModel.setMountBucketDialogVisible(false) },
                    onMount = { name, endpoint, reg, ak, sk, usePathStyle, useSsl ->
                        viewModel.mountNewBucket(name, endpoint, reg, ak, sk, usePathStyle, useSsl)
                    }
                )
            }
        }
    }
}
