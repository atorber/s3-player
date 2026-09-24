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
import com.example.ui.screens.MountBucketDialog
import com.example.ui.screens.PlayerScreen
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
    val tracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
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

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFormat by viewModel.selectedFormatFilter.collectAsStateWithLifecycle()
    val isRealtimeBannerVisible by viewModel.isRealtimeBannerVisible.collectAsStateWithLifecycle()
    val isRecursiveScan by viewModel.isRecursiveScan.collectAsStateWithLifecycle()
    val directoryTiers by viewModel.directoryTiers.collectAsStateWithLifecycle()
    val syncSettings by viewModel.syncSettings.collectAsStateWithLifecycle()
    val isAutoPlayNewEnabled by viewModel.isAutoPlayNewEnabled.collectAsStateWithLifecycle()
    val isPitchCorrectionEnabled by viewModel.isPitchCorrectionEnabled.collectAsStateWithLifecycle()
    val isSilenceTrimmingEnabled by viewModel.isSilenceTrimmingEnabled.collectAsStateWithLifecycle()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isObjectKeyDialogVisible by remember { mutableStateOf(false) }

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
                onProfileClick = { viewModel.setActiveTab(3) },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent Floating Dock Player (visible on tabs other than full player)
                if (navState.activeTab != 2 && playerState.currentTrack != null) {
                    AetherDockPlayer(
                        playerState = playerState,
                        onExpandPlayer = { viewModel.setActiveTab(2) },
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSkipBackward10 = viewModel::skipBackward10s,
                        onSkipForward30 = viewModel::skipForward30s
                    )
                }

                // 4-Tab Bottom Navigation Bar
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
                        onPlayTrack = viewModel::playTrack,
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
                    val activeBucket = selectedBucketName?.takeIf { it != "ALL" } ?: syncSettings.currentBucketName
                    val displayLoc = "s3://$activeBucket/${currentPrefix.removePrefix("/")}"
                    PlaylistScreen(
                        tracks = tracks,
                        playerState = playerState,
                        isBannerVisible = isRealtimeBannerVisible,
                        isAutoPlayNewEnabled = isAutoPlayNewEnabled,
                        monitoredLocation = displayLoc,
                        onDismissBanner = viewModel::dismissRealtimeBanner,
                        onPlayTrack = viewModel::playTrack,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onStopPlayback = viewModel::stopPlayback,
                        onPlayNewArrival = {
                            val newArrival = tracks.firstOrNull { it.id == 2L } ?: tracks.firstOrNull()
                            newArrival?.let { viewModel.playTrack(it) }
                            viewModel.dismissRealtimeBanner()
                        },
                        onToggleAutoPlayNew = viewModel::toggleAutoPlayNew,
                        onToggleRepeat = viewModel::toggleLoopMode,
                        onClearPlayed = { /* Cleared */ },
                    )
                }
                2 -> {
                    // Screen 3: 正在播放 / 沉浸式播放器 (Player)
                    PlayerScreen(
                        playerState = playerState,
                        fallbackTrack = tracks.firstOrNull(),
                        isPitchCorrectionEnabled = isPitchCorrectionEnabled,
                        isSilenceTrimmingEnabled = isSilenceTrimmingEnabled,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSeekTo = viewModel::seekTo,
                        onSkipBackward15 = viewModel::skipBackward15s,
                        onSkipForward30 = viewModel::skipForward30s,
                        onPlayNext = viewModel::playNext,
                        onPlayPrevious = viewModel::playPrevious,
                        onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
                        onTogglePitchCorrection = viewModel::togglePitchCorrection,
                        onToggleSilenceTrimming = viewModel::toggleSilenceTrimming,
                        onInspectObjectKey = { isObjectKeyDialogVisible = true },
                        onQuickListenNewArrival = {
                            val arrival = tracks.firstOrNull { it.id == 2L }
                            arrival?.let { viewModel.playTrack(it) }
                        },
                        onToggleRepeat = viewModel::toggleLoopMode,
                        onStopPlayback = viewModel::stopPlayback
                    )
                }
                3 -> {
                    // Screen 4: 同步设置 (Sync Settings)
                    SyncSettingsScreen(
                        settings = syncSettings,
                        onUpdatePollInterval = viewModel::updatePollInterval,
                        onSetEnqueueRule = viewModel::setEnqueueRule,
                        onTogglePublicAccess = viewModel::togglePublicAccess,
                        onToggleSubdirMonitoring = viewModel::toggleSubdirMonitoring,
                        onToggleSnsNotification = viewModel::toggleSnsNotification,
                        onToggleFormatEnabled = viewModel::toggleFormatEnabled,
                        onRunPingTest = { ep, bk -> viewModel.runPingTest(ep, bk) },
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

            // S3 Object Key Details Dialog
            if (isObjectKeyDialogVisible) {
                AlertDialog(
                    onDismissRequest = { isObjectKeyDialogVisible = false },
                    containerColor = SurfaceContainer,
                    title = {
                        Text(
                            text = "S3 对象详细元数据",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "URI: s3://xtrader/podcasts/2025-season/guest_recording_track_02.flac",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = AwsAmber
                            )
                            Text(
                                text = "文件大小: 67,108,864 字节 (64.0 MB)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextLowContrast
                            )
                            Text(
                                text = "存储类型: INTELLIGENT_TIERING",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextLowContrast
                            )
                            Text(
                                text = "ETag: \"4d19a008c234a91f421e4d909c29af57\"",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextLowContrast
                            )
                            Text(
                                text = "服务端: S3 Lambda Transcoder",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextLowContrast
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { isObjectKeyDialogVisible = false }) {
                            Text("关闭", color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                )
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
