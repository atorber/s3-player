package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.LoopMode
import com.example.audio.PlaybackStatus
import com.example.audio.PlayerState
import com.example.data.model.S3AudioTrack
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun PlaylistScreen(
    tracks: List<S3AudioTrack>,
    playerState: PlayerState,
    isBannerVisible: Boolean,
    isAutoPlayNewEnabled: Boolean = true,
    isDirectoryMonitoringEnabled: Boolean = true,
    monitoredLocation: String = "s3://xtrader/",
    bannerTrack: S3AudioTrack? = null,
    onDismissBanner: () -> Unit,
    onPlayTrack: (S3AudioTrack) -> Unit,
    onTogglePlayPause: () -> Unit = {},
    onStopPlayback: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onSkipBackward10: () -> Unit = {},
    onSkipForward30: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onPlayPrevious: () -> Unit = {},
    onSetPlaybackSpeed: (Float) -> Unit = {},
    onPlayNewArrival: () -> Unit,
    onToggleAutoPlayNew: () -> Unit = {},
    onToggleDirectoryMonitoring: () -> Unit = {},
    onForceCheckNewUploads: () -> Unit = {},
    onToggleRepeat: () -> Unit,
    onClearPlayed: () -> Unit,
    onClearAll: () -> Unit = {},
    onRemoveTrack: (S3AudioTrack) -> Unit = {},
    onEnqueueNext: (S3AudioTrack) -> Unit = {},
    onOpenTelemetry: () -> Unit = {},
    onNavigateToBrowser: () -> Unit = {},
    onShuffle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentIndex = remember(tracks, playerState.currentTrack) {
        val active = playerState.currentTrack
        if (active == null) -1
        else tracks.indexOfFirst { it.key == active.key && it.bucketName == active.bucketName }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
    ) {
        // 1. 实时新增 S3 音频到达通知横幅
        if (isBannerVisible) {
            item {
                AnimatedVisibility(
                    visible = isBannerVisible,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh)
                            .border(1.dp, BorderCyanGlow, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .testTag("realtime_stream_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ElectricCyan)
                                            .alpha(pulseAlpha)
                                    )
                                    Text(
                                        text = "检测到新增 S3 音频 · ${bannerTrack?.lastModified ?: "刚刚"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                }
                                Text(
                                    text = bannerTrack?.title ?: "新增音频对象",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighContrast,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${bannerTrack?.format ?: "FLAC"} · ${bannerTrack?.sizeFormatted ?: "--"}",
                                    fontSize = 11.sp,
                                    color = TextLowContrast
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHighest)
                                        .clickable { onDismissBanner() }
                                        .padding(horizontal = 9.dp, vertical = 6.dp)
                                        .testTag("banner_dismiss_btn")
                                ) {
                                    Text(
                                        text = "忽略",
                                        fontSize = 11.sp,
                                        color = TextMediumContrast
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricCyan)
                                        .clickable { onPlayNewArrival() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("banner_play_btn")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "立即播放",
                                            tint = AetherVoid,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "立即播放",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AetherVoid
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. 播放队列头部控制栏 (Queue Header & Streamlined Action Bar)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AetherSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("playlist_hero_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Row: 队列标题与数量 (左侧) + 监听目录徽标 (右侧)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "播放队列",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerHigh)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${tracks.size} 首",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AwsAmber
                            )
                        }
                        if (currentIndex >= 0 && currentIndex < tracks.size) {
                            Text(
                                text = "正在播第 ${currentIndex + 1} 首",
                                fontSize = 11.sp,
                                color = ElectricCyan
                            )
                        }
                    }

                    // 监听路径指示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isDirectoryMonitoringEnabled) StatusGreen else TextLowContrast)
                        )
                        Text(
                            text = monitoredLocation.removePrefix("s3://").ifBlank { "未监听目录" },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextMediumContrast,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 快捷操作栏 (2行3列全显布局，一屏直达无需横向滑动)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 第 1 行：监听状态与刷新相关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. 目录监听开关
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    if (isDirectoryMonitoringEnabled) ElectricCyan.copy(alpha = 0.16f)
                                    else SurfaceContainerHigh
                                )
                                .border(
                                    1.dp,
                                    if (isDirectoryMonitoringEnabled) ElectricCyan.copy(alpha = 0.5f) else BorderSubtle,
                                    RoundedCornerShape(7.dp)
                                )
                                .clickable { onToggleDirectoryMonitoring() }
                                .padding(vertical = 7.dp, horizontal = 2.dp)
                                .testTag("playlist_toggle_monitoring_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = if (isDirectoryMonitoringEnabled) ElectricCyan else TextMediumContrast,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isDirectoryMonitoringEnabled) "监听中" else "监听关闭",
                                    fontSize = 11.sp,
                                    fontWeight = if (isDirectoryMonitoringEnabled) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDirectoryMonitoringEnabled) ElectricCyan else TextMediumContrast,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 2. 刷新检测
                        val canForceCheck = isDirectoryMonitoringEnabled
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (canForceCheck) SurfaceContainerHigh else SurfaceContainerLow)
                                .border(
                                    1.dp,
                                    if (canForceCheck) BorderSubtle else BorderSubtle.copy(alpha = 0.4f),
                                    RoundedCornerShape(7.dp)
                                )
                                .clickable(enabled = canForceCheck) { onForceCheckNewUploads() }
                                .padding(vertical = 7.dp, horizontal = 2.dp)
                                .testTag("playlist_force_check_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = if (canForceCheck) TextMediumContrast else TextLowContrast,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "刷新检测",
                                    fontSize = 11.sp,
                                    color = if (canForceCheck) TextHighContrast else TextLowContrast,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 3. 新增音频自动播放开关
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    if (isAutoPlayNewEnabled) AwsAmber.copy(alpha = 0.16f)
                                    else SurfaceContainerHigh
                                )
                                .border(
                                    1.dp,
                                    if (isAutoPlayNewEnabled) AwsAmber.copy(alpha = 0.5f) else BorderSubtle,
                                    RoundedCornerShape(7.dp)
                                )
                                .clickable { onToggleAutoPlayNew() }
                                .padding(vertical = 7.dp, horizontal = 2.dp)
                                .testTag("playlist_toggle_autoplay_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isAutoPlayNewEnabled) Icons.Default.MotionPhotosAuto else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = if (isAutoPlayNewEnabled) AwsAmber else TextMediumContrast,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isAutoPlayNewEnabled) "新曲自动播" else "新曲手动播",
                                    fontSize = 11.sp,
                                    fontWeight = if (isAutoPlayNewEnabled) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAutoPlayNewEnabled) AwsAmber else TextMediumContrast,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // 第 2 行：播放模式与队列管理
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 4. 循环模式切换
                        val loopLabel = when (playerState.loopMode) {
                            LoopMode.ALL -> "列表循环"
                            LoopMode.SINGLE -> "单曲循环"
                            LoopMode.OFF -> "顺序播放"
                            LoopMode.AB_REPEAT -> "列表循环"
                        }
                        val isLoopOn = playerState.loopMode != LoopMode.OFF
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isLoopOn) SurfaceContainerHighest else SurfaceContainerHigh)
                                .border(
                                    1.dp,
                                    if (isLoopOn) AwsAmber.copy(alpha = 0.5f) else BorderSubtle,
                                    RoundedCornerShape(7.dp)
                                )
                                .clickable { onToggleRepeat() }
                                .padding(vertical = 7.dp, horizontal = 2.dp)
                                .testTag("playlist_toggle_loop_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (playerState.loopMode == LoopMode.SINGLE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = if (isLoopOn) AwsAmber else TextMediumContrast,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = loopLabel,
                                    fontSize = 11.sp,
                                    color = if (isLoopOn) AwsAmber else TextMediumContrast,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 5. 清理已播曲目
                        val canClearPlayed = currentIndex > 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (canClearPlayed) SurfaceContainerHigh else SurfaceContainerLow)
                                .border(
                                    1.dp,
                                    if (canClearPlayed) BorderSubtle else BorderSubtle.copy(alpha = 0.4f),
                                    RoundedCornerShape(7.dp)
                                )
                                .clickable(enabled = canClearPlayed) { onClearPlayed() }
                                .padding(vertical = 7.dp, horizontal = 2.dp)
                                .testTag("playlist_clear_played_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    tint = if (canClearPlayed) TextMediumContrast else TextLowContrast,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (currentIndex > 0) "清理已播 ($currentIndex)" else "清理已播",
                                    fontSize = 11.sp,
                                    color = if (canClearPlayed) TextHighContrast else TextLowContrast,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 6. 清空全部队列
                        val canClearAll = tracks.isNotEmpty()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (canClearAll) SurfaceContainerHigh else SurfaceContainerLow)
                                .border(
                                    1.dp,
                                    if (canClearAll) BorderSubtle else BorderSubtle.copy(alpha = 0.4f),
                                    RoundedCornerShape(7.dp)
                                )
                                .clickable(enabled = canClearAll) { onClearAll() }
                                .padding(vertical = 7.dp, horizontal = 2.dp)
                                .testTag("playlist_clear_all_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = if (canClearAll) StatusRed.copy(alpha = 0.85f) else StatusRed.copy(alpha = 0.3f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "清空全部",
                                    fontSize = 11.sp,
                                    color = if (canClearAll) TextMediumContrast else TextLowContrast,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 播放队列曲目列表或空队列引导卡片
        if (tracks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AetherSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TextLowContrast,
                        modifier = Modifier.size(42.dp)
                    )
                    Text(
                        text = "播放队列为空",
                        color = TextHighContrast,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "前往「文件浏览」轻触任意目录即可整组播放，或将单个音频加入队列",
                        color = TextLowContrast,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onNavigateToBrowser,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "前往文件浏览添加音频",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            itemsIndexed(tracks, key = { index, t -> "${t.id}_$index" }) { index, track ->
                val isCurrent = index == currentIndex
                val isPlayingThis = isCurrent && (
                        playerState.status == PlaybackStatus.PLAYING ||
                                playerState.status == PlaybackStatus.BUFFERING ||
                                playerState.status == PlaybackStatus.CONNECTING
                        )
                val isPausedThis = isCurrent && playerState.status == PlaybackStatus.PAUSED
                val isPlayed = currentIndex > 0 && index < currentIndex
                val isNextUp = currentIndex >= 0 && index == currentIndex + 1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isPlayed) 0.55f else 1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isPlayingThis -> ElectricCyan.copy(alpha = 0.09f)
                                isPausedThis -> AwsAmber.copy(alpha = 0.08f)
                                else -> AetherSurface
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isPlayingThis -> ElectricCyan.copy(alpha = 0.45f)
                                isPausedThis -> AwsAmber.copy(alpha = 0.45f)
                                else -> BorderSubtle
                            },
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (isPlayingThis || isPausedThis) {
                                onTogglePlayPause()
                            } else {
                                onPlayTrack(track)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("queue_item_${track.id}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：序号或状态指示 + 曲目信息
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
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
                                        contentDescription = "正在播放",
                                        tint = AetherVoid,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                isPausedThis -> {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "已暂停",
                                        tint = AetherVoid,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%02d", index + 1),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNextUp) ElectricCyan else TextMediumContrast
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                color = when {
                                    isPlayingThis -> ElectricCyan
                                    isPausedThis -> AwsAmber
                                    else -> TextHighContrast
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 格式徽标
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SurfaceContainerHigh)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = track.format.uppercase(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AwsAmber
                                    )
                                }

                                // 真实队列位置状态标签
                                when {
                                    isPlayingThis -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ElectricCyan.copy(alpha = 0.18f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "正在播放",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricCyan
                                            )
                                        }
                                    }
                                    isPausedThis -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AwsAmber.copy(alpha = 0.18f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "已暂停",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AwsAmber
                                            )
                                        }
                                    }
                                    isNextUp -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ElectricCyan.copy(alpha = 0.14f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "下一首",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricCyanDim
                                            )
                                        }
                                    }
                                    isPlayed -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SurfaceContainer)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "已播",
                                                fontSize = 10.sp,
                                                color = TextLowContrast
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${track.durationFormatted} · ${track.sizeFormatted}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextLowContrast,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }

                    // 右侧：紧凑控制按键
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isPlayingThis) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh)
                                    .clickable { onStopPlayback() }
                                    .testTag("btn_stop_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "停止",
                                    tint = StatusRed,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(AwsAmber)
                                    .clickable { onTogglePlayPause() }
                                    .testTag("btn_pause_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "暂停",
                                    tint = AetherVoid,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (isPausedThis) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh)
                                    .clickable { onStopPlayback() }
                                    .testTag("btn_stop_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "停止",
                                    tint = TextMediumContrast,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan)
                                    .clickable { onTogglePlayPause() }
                                    .testTag("btn_resume_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "继续播放",
                                    tint = AetherVoid,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            if (!isNextUp) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh.copy(alpha = 0.5f))
                                        .clickable { onEnqueueNext(track) }
                                        .testTag("btn_play_next_${track.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueuePlayNext,
                                        contentDescription = "优先插队",
                                        tint = ElectricCyanDim,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh)
                                    .clickable { onPlayTrack(track) }
                                    .testTag("btn_play_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "立即播放",
                                    tint = TextHighContrast,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh.copy(alpha = 0.5f))
                                    .clickable { onRemoveTrack(track) }
                                    .testTag("btn_remove_${track.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "从队列移除",
                                    tint = TextLowContrast,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
