package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.AwsAmberLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
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
    val context = LocalContext.current
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedTrackForDetails by remember { mutableStateOf<S3AudioTrack?>(null) }

    // Breadcrumb path segments
    val pathSegments = remember(currentPrefix) {
        currentPrefix.split("/").filter { it.isNotBlank() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // 1. 简洁顶部路径导航条 (Breadcrumb & Level Navigation)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("browser_path_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AetherSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Bucket name & Up/Root navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AwsAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = AwsAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "存储桶: $currentBucket",
                                    color = TextHighContrast,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (currentPrefix.isBlank()) "位置: 根目录 /" else "位置: /$currentPrefix",
                                    color = TextLowContrast,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Navigation buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentPrefix.isNotBlank()) {
                                IconButton(
                                    onClick = onNavigateUp,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("btn_navigate_up")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "返回上一级",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onNavigateToRoot,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("btn_navigate_root")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "返回根目录",
                                        tint = AwsAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { isSearchExpanded = !isSearchExpanded },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_toggle_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索",
                                    tint = if (isSearchExpanded || searchQuery.isNotBlank()) ElectricCyan else TextLowContrast,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Row 2: 面包屑级联路径 (可逐级直接点击跳转)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "路径: ",
                            color = TextLowContrast,
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentPrefix.isBlank()) ElectricCyan.copy(alpha = 0.2f) else SurfaceContainerLow)
                                .clickable { onNavigateToRoot() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
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

                            Text(text = " > ", color = TextLowContrast, fontSize = 11.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isLast) ElectricCyan.copy(alpha = 0.2f) else SurfaceContainerLow)
                                    .clickable { onNavigateToPrefix(targetPath) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
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

                    // 搜索框展开
                    AnimatedVisibility(visible = isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("搜索当前目录音频文件名...", fontSize = 12.sp, color = TextLowContrast) },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = TextLowContrast, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerLow
                            )
                        )
                    }

                    // Row 3: 当前目录一键播放与一键入队快捷操作
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPlayAll,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_play_all_directory"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("播放此目录", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onAddCurrentDirectoryToPlaylist,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_add_all_directory_to_playlist"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("全部加入列表", color = TextHighContrast, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. 当前路径下的子目录列表 (逐级递归下钻 & 选中添加至播放列表)
        val currentDisplayPath = if (currentPrefix.isBlank()) "根目录 /" else "/${currentPrefix.trim().trim('/')}/"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = AwsAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "当前路径子目录 (${folders.size})",
                        color = TextHighContrast,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "位置: $currentDisplayPath",
                    color = ElectricCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (folders.isNotEmpty()) {
            items(folders, key = { it.fullPrefix }) { folder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPrefix(folder.fullPrefix) }
                        .testTag("folder_card_${folder.name.removeSuffix("/")}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AetherSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Folder details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AwsAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = AwsAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = folder.name,
                                    color = TextHighContrast,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (folder.itemCount > 0) "${folder.itemCount} 首音频 · ${folder.sizeFormatted}" else "s3://$currentBucket/${folder.fullPrefix}",
                                    color = TextLowContrast,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Actions: 选中子目录添加到播放列表 / 播放 / 进入
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 选中子目录添加到播放列表
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .clickable { onAddFolderToPlaylist(folder.fullPrefix) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("btn_add_folder_${folder.name.removeSuffix("/")}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistAdd,
                                        contentDescription = "加到播放列表",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "加到列表",
                                        color = ElectricCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // 播放此目录
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .clickable { onPlayFolder(folder.fullPrefix) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("btn_play_folder_${folder.name.removeSuffix("/")}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "播放",
                                        tint = AwsAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "播放",
                                        color = AwsAmberLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // 进入图标提示
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "进入目录",
                                tint = TextLowContrast,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(90f)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AetherSurface.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "正在检索当前路径下的子目录...",
                                    color = TextLowContrast,
                                    fontSize = 11.sp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = TextLowContrast,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "当前路径无下级子目录",
                                    color = TextLowContrast,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        if (!isScanning) {
                            Text(
                                text = "刷新目录",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onForceScan() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. 当前目录直属音频文件列表 (Audio Tracks)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "音频文件 (${tracks.size})",
                        color = TextHighContrast,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (tracks.isNotEmpty()) {
                    Text(
                        text = "即点即播 · 单曲加列表",
                        color = TextLowContrast,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (tracks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AetherSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = TextLowContrast,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (folders.isNotEmpty()) "当前层级暂无直属音频文件，请点击上方子目录查看" else "此目录下暂无音频文件",
                            color = TextMediumContrast,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (currentPrefix.isNotBlank()) {
                            OutlinedButton(
                                onClick = onNavigateUp,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Text("返回上一级", color = ElectricCyan, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            items(tracks, key = { it.id }) { track ->
                val isPlayingThis = playerState.currentTrack?.id == track.id && playerState.status == PlaybackStatus.PLAYING
                var isMenuExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTrack(track) }
                        .testTag("track_row_${track.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlayingThis) ElectricCyan.copy(alpha = 0.08f) else AetherSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPlayingThis) ElectricCyan.copy(alpha = 0.4f) else BorderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Play state indicator & Track metadata
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlayingThis) ElectricCyan else SurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPlayingThis) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "播放中",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "播放",
                                        tint = TextHighContrast,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = if (isPlayingThis) ElectricCyan else TextHighContrast,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${track.artistOrProject} · ${track.durationFormatted} · ${track.sizeFormatted}",
                                        color = TextLowContrast,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Right action buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // ➕ 加入播放列表
                            IconButton(
                                onClick = { onAddToQueue(track) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_enqueue_${track.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = "加入播放列表",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 收藏切换
                            IconButton(
                                onClick = { onToggleFavorite(track) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (track.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "收藏",
                                    tint = if (track.isFavorite) AwsAmber else TextLowContrast,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 更多菜单
                            Box {
                                IconButton(
                                    onClick = { isMenuExpanded = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "更多",
                                        tint = TextLowContrast,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("查看 S3 对象详情", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            isMenuExpanded = false
                                            selectedTrackForDetails = track
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("复制 S3 URI", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            isMenuExpanded = false
                                            onCopyS3Uri(track)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("下载 / 缓存至本地", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) },
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
    }

    // 对象详情对话框
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
