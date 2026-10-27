package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlaybackStatus
import com.example.audio.PlayerState
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket
import com.example.ui.DirectoryTier
import com.example.ui.S3FolderItem
import com.example.ui.TrackSortMode
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.AwsAmberLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun BrowserScreen(
    currentBucket: String,
    allBuckets: List<S3Bucket> = emptyList(),
    currentPrefix: String,
    folders: List<S3FolderItem>,
    tracks: List<S3AudioTrack>,
    playerState: PlayerState,
    searchQuery: String = "",
    selectedFormat: String? = null,
    formatCounts: Map<String, Int> = emptyMap(),
    sortMode: TrackSortMode = TrackSortMode.NAME_ASC,
    isGridView: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedTrackIds: Set<Long> = emptySet(),
    isRecursiveScan: Boolean = false,
    isScanning: Boolean = false,
    onSelectBucket: (String) -> Unit = {},
    onNavigateToPrefix: (String) -> Unit = {},
    onNavigateUp: () -> Unit = {},
    onNavigateToRoot: () -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onSelectFormat: (String) -> Unit = {},
    onToggleRecursiveScan: () -> Unit = {},
    onSetSortMode: (TrackSortMode) -> Unit = {},
    onToggleGridView: () -> Unit = {},
    onToggleSelectionMode: () -> Unit = {},
    onToggleTrackSelection: (Long) -> Unit = {},
    onSelectAll: (List<Long>) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onBatchPlay: () -> Unit = {},
    onBatchAddToQueue: () -> Unit = {},
    onBatchCache: (Boolean) -> Unit = {},
    onBatchPresign: () -> Unit = {},
    onBatchDelete: () -> Unit = {},
    onPlayTrack: (S3AudioTrack) -> Unit = {},
    onPlayAll: () -> Unit = {},
    onForceScan: () -> Unit = {},
    onAddToQueue: (S3AudioTrack) -> Unit = {},
    onEnqueueNext: (S3AudioTrack) -> Unit = {},
    onToggleFavorite: (S3AudioTrack) -> Unit = {},
    onToggleCache: (S3AudioTrack) -> Unit = {},
    onDeleteTrack: (S3AudioTrack) -> Unit = {},
    onCopyPresignedUrl: (S3AudioTrack) -> Unit = {},
    onCopyS3Uri: (S3AudioTrack) -> Unit = {},
    onShowTrackDetails: (S3AudioTrack) -> Unit = {},
    onCreateFolder: (String) -> Unit = {},
    onUploadTrack: (String, String, Long, Double, String) -> Unit = { _, _, _, _, _ -> },
    directoryTiers: List<DirectoryTier> = emptyList(),
    onLocatePlayingTrack: () -> Unit = {},
    onAddFolderToPlaylist: (String) -> Unit = {},
    onPlayFolder: (String) -> Unit = {},
    onAddCurrentDirectoryToPlaylist: () -> Unit = {},
    syncBasePrefix: String = "/",
    onNavigateToSyncBase: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSortDialogVisible by remember { mutableStateOf(false) }
    var selectedTrackForDetails by remember { mutableStateOf<S3AudioTrack?>(null) }

    val pathSegments = remember(currentPrefix) {
        currentPrefix.split("/").filter { it.isNotBlank() }
    }
    val hasActiveFilter = searchQuery.isNotBlank() || (selectedFormat != null && selectedFormat != "ALL")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
    ) {
        // 1. 顶部一体化面包屑路径与工具控制台 (Unified Navigation & Filter Header)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("browser_path_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AetherSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: 存储桶标识信息 + 右侧操作工具栏 (统一 34dp 矩形圆角 Box 按钮，杜绝 Material3 默认 48dp 挤压变形问题)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 左侧：存储桶图标与名称
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AwsAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = AwsAmber,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentBucket,
                                    color = TextHighContrast,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (currentPrefix.isBlank()) "根目录 /" else "/$currentPrefix",
                                    color = TextLowContrast,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 右侧：紧凑工具栏 (返回上级 / 定位播放 / 递归开关 / 搜索 / 刷新)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (currentPrefix.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHigh)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { onNavigateUp() }
                                        .testTag("btn_navigate_up"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "返回上一级",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (playerState.currentTrack != null) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHigh)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { onLocatePlayingTrack() }
                                        .testTag("btn_locate_playing"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "定位当前播放曲目所在目录",
                                        tint = AwsAmber,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isRecursiveScan) AwsAmber.copy(alpha = 0.2f) else SurfaceContainerHigh)
                                    .border(
                                        1.dp,
                                        if (isRecursiveScan) AwsAmber.copy(alpha = 0.6f) else BorderSubtle,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onToggleRecursiveScan() }
                                    .testTag("btn_toggle_recursive"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = if (isRecursiveScan) "已开启子目录穿透" else "仅显示当前层级",
                                    tint = if (isRecursiveScan) AwsAmber else TextMediumContrast,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSearchExpanded || hasActiveFilter) ElectricCyan.copy(alpha = 0.2f) else SurfaceContainerHigh)
                                    .border(
                                        1.dp,
                                        if (isSearchExpanded || hasActiveFilter) ElectricCyan.copy(alpha = 0.6f) else BorderSubtle,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { isSearchExpanded = !isSearchExpanded }
                                    .testTag("btn_toggle_search"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索与格式筛选",
                                    tint = if (isSearchExpanded || hasActiveFilter) ElectricCyan else TextMediumContrast,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerHigh)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onForceScan() }
                                    .testTag("btn_force_scan"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = ElectricCyan
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "刷新目录",
                                        tint = TextMediumContrast,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Row 2: 独立完整的全宽面包屑路径栏 (可点击层级回退)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 根目录 Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentPrefix.isBlank()) ElectricCyan.copy(alpha = 0.2f) else SurfaceContainerLow)
                                .border(
                                    1.dp,
                                    if (currentPrefix.isBlank()) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onNavigateToRoot() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("btn_navigate_root")
                        ) {
                            Text(
                                text = "根目录 /",
                                color = if (currentPrefix.isBlank()) ElectricCyan else TextMediumContrast,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (currentPrefix.isBlank()) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        var accumulatedPath = ""
                        pathSegments.forEachIndexed { index, segment ->
                            accumulatedPath += "$segment/"
                            val targetPath = accumulatedPath
                            val isLast = index == pathSegments.lastIndex

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextLowContrast,
                                modifier = Modifier.size(14.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isLast) ElectricCyan.copy(alpha = 0.2f) else SurfaceContainerLow)
                                    .border(
                                        1.dp,
                                        if (isLast) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onNavigateToPrefix(targetPath) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$segment/",
                                    color = if (isLast) ElectricCyan else TextMediumContrast,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Row 2: 可展开的搜索框与音频格式筛选胶丸 (Search & Format Chips)
                    AnimatedVisibility(
                        visible = isSearchExpanded || hasActiveFilter,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                placeholder = {
                                    Text(
                                        text = "输入关键词过滤文件名或目录...",
                                        fontSize = 12.sp,
                                        color = TextLowContrast
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { onSearchChange("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "清空搜索",
                                                tint = TextLowContrast,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedContainerColor = SurfaceContainerHigh,
                                    unfocusedContainerColor = SurfaceContainerLow
                                )
                            )

                            if (formatCounts.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "格式:",
                                        color = TextLowContrast,
                                        fontSize = 11.sp
                                    )
                                    val formats = listOf("ALL") + formatCounts.keys.filter { it != "ALL" }
                                    formats.forEach { fmt ->
                                        val isSelected = (selectedFormat == null && fmt == "ALL") || selectedFormat == fmt
                                        val count = formatCounts[fmt] ?: 0
                                        val label = if (fmt == "ALL") "全部 ($count)" else "$fmt ($count)"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSelected) ElectricCyan.copy(alpha = 0.2f)
                                                    else SurfaceContainerLow
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) ElectricCyan else BorderSubtle,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable { onSelectFormat(fmt) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) ElectricCyan else TextMediumContrast,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Row 3: 当当前目录包含可播放音频或开启递归时，提供快捷全目录操作栏
                    if (tracks.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onPlayAll,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("btn_play_all_directory"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "播放此目录 (${tracks.size})",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onAddCurrentDirectoryToPlaylist,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("btn_add_all_directory_to_playlist"),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "全部加入列表",
                                    color = TextHighContrast,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. 子目录列表区块 (仅在存在子目录时渲染，避免无子目录时的无效占位卡片)
        if (folders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = AwsAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "匹配的目录 (${folders.size})" else "下级目录 (${folders.size})",
                            color = TextHighContrast,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "点击进入目录 · 右侧直接播放/入队",
                        color = TextLowContrast,
                        fontSize = 10.sp
                    )
                }
            }

            itemsIndexed(folders, key = { _, folder -> folder.fullPrefix }) { _, folder ->
                val cleanName = folder.name.removeSuffix("/")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPrefix(folder.fullPrefix) }
                        .testTag("folder_card_$cleanName"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AetherSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 左侧：文件夹图标 + 名称 + 曲目统计
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AwsAmber.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = AwsAmber,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cleanName,
                                    color = TextHighContrast,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (folder.itemCount > 0) {
                                        "${folder.itemCount} 首音频 · ${folder.sizeFormatted}"
                                    } else {
                                        "s3://$currentBucket/${folder.fullPrefix}"
                                    },
                                    color = TextLowContrast,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 右侧：加到列表 / 立即播放 / 进入箭头
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .clickable { onAddFolderToPlaylist(folder.fullPrefix) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("btn_add_folder_$cleanName"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistAdd,
                                        contentDescription = "加到播放列表",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "入队",
                                        color = ElectricCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AwsAmber.copy(alpha = 0.16f))
                                    .clickable { onPlayFolder(folder.fullPrefix) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("btn_play_folder_$cleanName"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "播放目录",
                                        tint = AwsAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "播放",
                                        color = AwsAmberLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "进入目录",
                                tint = TextLowContrast,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. 音频文件列表区块 (Audio Tracks Section)
        if (tracks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "搜索结果 (${tracks.size})"
                                isRecursiveScan -> "音频文件 · 含子目录 (${tracks.size})"
                                else -> "音频文件 (${tracks.size})"
                            },
                            color = TextHighContrast,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 排序触发器胶丸
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceContainerLow)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable { isSortDialogVisible = true }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("btn_open_sort_dialog")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "排序方式",
                                tint = AwsAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortMode.label,
                                color = TextMediumContrast,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                val isCurrentTrack = playerState.currentTrack?.key == track.key &&
                        playerState.currentTrack?.bucketName == track.bucketName
                val isPlayingThis = isCurrentTrack && (
                        playerState.status == PlaybackStatus.PLAYING ||
                                playerState.status == PlaybackStatus.BUFFERING ||
                                playerState.status == PlaybackStatus.CONNECTING
                        )
                val isPausedThis = isCurrentTrack && playerState.status == PlaybackStatus.PAUSED
                var isMenuExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTrack(track) }
                        .testTag("track_row_${track.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isPlayingThis -> ElectricCyan.copy(alpha = 0.09f)
                            isPausedThis -> AwsAmber.copy(alpha = 0.08f)
                            else -> AetherSurface
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isPlayingThis -> ElectricCyan.copy(alpha = 0.45f)
                            isPausedThis -> AwsAmber.copy(alpha = 0.45f)
                            else -> BorderSubtle
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 左侧：序号/状态徽标 + 曲目信息
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isPlayingThis -> ElectricCyan
                                            isPausedThis -> AwsAmber
                                            else -> SurfaceContainerHigh
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isPlayingThis -> {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = "播放中",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    isPausedThis -> {
                                        Icon(
                                            imageVector = Icons.Default.Pause,
                                            contentDescription = "已暂停",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = String.format(java.util.Locale.US, "%02d", index + 1),
                                            color = TextMediumContrast,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = when {
                                        isPlayingThis -> ElectricCyan
                                        isPausedThis -> AwsAmber
                                        else -> TextHighContrast
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SurfaceContainerHigh)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = track.format.uppercase(),
                                            color = AwsAmberLight,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (track.isCachedLocally) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = "已缓存",
                                            tint = StatusGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    val folderPrefix = if (track.key.contains('/')) "📁 " + track.key.substringBeforeLast('/') + "/ · " else ""
                                    Text(
                                        text = "${if (searchQuery.isNotBlank() || isRecursiveScan) folderPrefix else ""}${track.durationFormatted} · ${track.sizeFormatted}" +
                                                if (isRecursiveScan) " · ${track.artistOrProject}" else "",
                                        color = TextLowContrast,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // 右侧快捷操作组：加入列表 + 收藏 + 更多菜单
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onAddToQueue(track) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("btn_enqueue_${track.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = "加入播放队列",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onToggleFavorite(track) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (track.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "收藏",
                                    tint = if (track.isFavorite) AwsAmber else TextLowContrast,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { isMenuExpanded = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "更多操作",
                                        tint = TextLowContrast,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("下一首播放 (优先插队)", fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.QueuePlayNext,
                                                contentDescription = null,
                                                tint = ElectricCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        onClick = {
                                            isMenuExpanded = false
                                            onEnqueueNext(track)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("查看 S3 对象元数据", fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        onClick = {
                                            isMenuExpanded = false
                                            selectedTrackForDetails = track
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("复制 S3 URI", fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        onClick = {
                                            isMenuExpanded = false
                                            onCopyS3Uri(track)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("复制 HTTP 预签名直链", fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        onClick = {
                                            isMenuExpanded = false
                                            onCopyPresignedUrl(track)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (track.isCachedLocally) "移除本地离线缓存" else "下载并缓存至本地",
                                                fontSize = 12.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (track.isCachedLocally) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        onClick = {
                                            isMenuExpanded = false
                                            onToggleCache(track)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. 当既没有子目录也没有直属音频文件时的统一空状态 / 扫描状态卡片
        if (folders.isEmpty() && tracks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AetherSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.5.dp,
                                color = ElectricCyan
                            )
                            Text(
                                text = "正在从 S3 节点拉取目录与音频索引...",
                                color = TextHighContrast,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = TextLowContrast,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                text = if (hasActiveFilter) "未找到匹配筛选条件的音频或目录" else "当前路径下暂无子目录或音频文件",
                                color = TextHighContrast,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (hasActiveFilter) {
                                    "可尝试清空搜索词或切换上方格式筛选"
                                } else {
                                    "您可以开启右上角「子目录穿透」检索深层文件，或点击刷新重新同步"
                                },
                                color = TextLowContrast,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (hasActiveFilter) {
                                    OutlinedButton(
                                        onClick = {
                                            onSearchChange("")
                                            onSelectFormat("ALL")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                                    ) {
                                        Text("清除筛选条件", color = ElectricCyan, fontSize = 11.sp)
                                    }
                                } else {
                                    if (currentPrefix.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = onNavigateUp,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text("返回上一级", color = ElectricCyan, fontSize = 11.sp)
                                        }
                                    }
                                    Button(
                                        onClick = onForceScan,
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("立即刷新", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 排序模式选择弹窗
    if (isSortDialogVisible) {
        SortModeDialog(
            currentSort = sortMode,
            onDismiss = { isSortDialogVisible = false },
            onSelectSort = { mode ->
                onSetSortMode(mode)
                isSortDialogVisible = false
            }
        )
    }

    // S3 对象元数据详情弹窗
    selectedTrackForDetails?.let { track ->
        ObjectDetailsDialog(
            track = track,
            onDismiss = { selectedTrackForDetails = null },
            onPlay = { onPlayTrack(track) },
            onAddToQueue = { onAddToQueue(track) },
            onToggleCache = { onToggleCache(track) },
            onCopyPresignedUrl = { onCopyPresignedUrl(track) },
            onCopyS3Uri = { onCopyS3Uri(track) }
        )
    }
}
