package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AetherDockPlayer
import com.example.ui.components.AetherTopBar
import com.example.ui.screens.BucketsManagerScreen
import com.example.ui.screens.ExpandedPlayerScreen
import com.example.ui.screens.ExplorerScreen
import com.example.ui.screens.MountBucketDialog
import com.example.ui.screens.TelemetryInspectorScreen
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AetherViewModel) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val tracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val buckets by viewModel.allBuckets.collectAsStateWithLifecycle()
    val selectedBucketName by viewModel.selectedBucketName.collectAsStateWithLifecycle()
    val selectedFormat by viewModel.selectedFormatFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherVoid),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AetherTopBar(
                currentBucketName = selectedBucketName,
                searchQuery = searchQuery,
                onSearchChange = viewModel::setSearchQuery,
                onOpenMountDialog = { viewModel.setMountBucketDialogVisible(true) },
                onOpenTelemetryInspector = { viewModel.setTelemetryInspectorVisible(true) },
                latencyMs = playerState.telemetry.latencyMs,
                activeRegion = buckets.firstOrNull { it.bucketName == selectedBucketName }?.region ?: "us-east-1",
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent Floating Dock Player (above bottom bar when track loaded)
                if (playerState.currentTrack != null) {
                    AetherDockPlayer(
                        playerState = playerState,
                        onExpandPlayer = { viewModel.setExpandedPlayerVisible(true) },
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onPlayNext = viewModel::playNext
                    )
                }

                // High-precision Industrial Bottom Navigation
                NavigationBar(
                    containerColor = AetherSurfaceTier1,
                    contentColor = TextHighContrast,
                    modifier = Modifier.border(1.dp, BorderSubtle)
                ) {
                    NavigationBarItem(
                        selected = navState.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "S3 Explorer"
                            )
                        },
                        label = {
                            Text(
                                "EXPLORER",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AwsAmber,
                            selectedTextColor = AwsAmber,
                            unselectedIconColor = TextLowContrast,
                            unselectedTextColor = TextLowContrast,
                            indicatorColor = AetherSurface
                        ),
                        modifier = Modifier.testTag("nav_tab_explorer")
                    )

                    NavigationBarItem(
                        selected = navState.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Cloud Buckets"
                            )
                        },
                        label = {
                            Text(
                                "BUCKETS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            selectedTextColor = ElectricCyan,
                            unselectedIconColor = TextLowContrast,
                            unselectedTextColor = TextLowContrast,
                            indicatorColor = AetherSurface
                        ),
                        modifier = Modifier.testTag("nav_tab_buckets")
                    )

                    NavigationBarItem(
                        selected = navState.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Bookmarked Tracks"
                            )
                        },
                        label = {
                            Text(
                                "BOOKMARKS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AwsAmber,
                            selectedTextColor = AwsAmber,
                            unselectedIconColor = TextLowContrast,
                            unselectedTextColor = TextLowContrast,
                            indicatorColor = AetherSurface
                        ),
                        modifier = Modifier.testTag("nav_tab_favorites")
                    )

                    NavigationBarItem(
                        selected = navState.activeTab == 3,
                        onClick = { viewModel.setActiveTab(3) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Telemetry & EQ"
                            )
                        },
                        label = {
                            Text(
                                "TELEMETRY",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            selectedTextColor = ElectricCyan,
                            unselectedIconColor = TextLowContrast,
                            unselectedTextColor = TextLowContrast,
                            indicatorColor = AetherSurface
                        ),
                        modifier = Modifier.testTag("nav_tab_telemetry")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (navState.activeTab) {
                0, 2 -> {
                    ExplorerScreen(
                        tracks = tracks,
                        buckets = buckets,
                        selectedBucketName = selectedBucketName,
                        selectedFormatFilter = selectedFormat,
                        playerState = playerState,
                        onSelectBucket = viewModel::selectBucket,
                        onSelectFormat = viewModel::setFormatFilter,
                        onTrackClick = viewModel::playTrack,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onToggleCache = viewModel::toggleCacheLocally,
                        onOpenMountDialog = { viewModel.setMountBucketDialogVisible(true) }
                    )
                }
                1 -> {
                    BucketsManagerScreen(
                        buckets = buckets,
                        selectedBucketName = selectedBucketName,
                        onSelectBucket = {
                            viewModel.selectBucket(it)
                            viewModel.setActiveTab(0)
                        },
                        onOpenMountDialog = { viewModel.setMountBucketDialogVisible(true) }
                    )
                }
                3 -> {
                    TelemetryInspectorScreen(
                        playerState = playerState,
                        onClose = { viewModel.setActiveTab(0) },
                        onSetEqPreset = viewModel::setEqPreset,
                        onSetBand = viewModel::setCustomEqBand
                    )
                }
            }

            // Expanded Player Bottom Sheet
            if (navState.isExpandedPlayerVisible && playerState.currentTrack != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.setExpandedPlayerVisible(false) },
                    sheetState = sheetState,
                    containerColor = AetherVoid,
                    dragHandle = null,
                    modifier = Modifier.fillMaxSize()
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
                        onOpenTelemetry = {
                            viewModel.setExpandedPlayerVisible(false)
                            viewModel.setTelemetryInspectorVisible(true)
                        },
                        onToggleFavorite = {
                            playerState.currentTrack?.let { viewModel.toggleFavorite(it) }
                        },
                        onToggleCache = {
                            playerState.currentTrack?.let { viewModel.toggleCacheLocally(it) }
                        }
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
                    onMount = { name, reg, prov, endpoint ->
                        viewModel.mountNewBucket(name, reg, prov, endpoint)
                    }
                )
            }
        }
    }
}
