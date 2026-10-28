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
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
    val currentTrack = playerState.currentTrack ?: tracks.firstOrNull()
    val currentIndex = remember(tracks, playerState.currentTrack) {
        val active = playerState.currentTrack
        if (active == null) -1
        else tracks.indexOfFirst { it.key == active.key && it.bucketName == active.bucketName }
    }

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }

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

    val speedOptions = listOf(1.0f, 1.25f, 1.5f, 2.0f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        // 1. 实时新增 S3 音频到达横幅 (Real-time Stream Notification Banner)
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
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                }
                                Text(
                                    text = bannerTrack?.title ?: "新增音频对象",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighContrast,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${bannerTrack?.format ?: "FLAC"} · ${bannerTrack?.sizeFormatted ?: "--"}",
                                    fontSize = 10.sp,
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

        // 2. 主控播放器中枢卡片 (Master Player Console Hero Card)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AetherSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("playlist_hero_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Telemetry & Utility Row (状态指示 + 延迟/吞吐量 + 倍速切换 + EQ遥测)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        val isStreaming = playerState.status == PlaybackStatus.PLAYING ||
                                playerState.status == PlaybackStatus.BUFFERING
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (isStreaming) ElectricCyan else AwsAmber,
                            modifier = Modifier
                                .size(14.dp)
                                .alpha(if (isStreaming) pulseAlpha else 1f)
                        )
                        Text(
                            text = when (playerState.status) {
                                PlaybackStatus.PLAYING -> "正在推流"
                                PlaybackStatus.BUFFERING, PlaybackStatus.CONNECTING -> "正在缓冲..."
                                PlaybackStatus.PAUSED -> "已暂停"
                                PlaybackStatus.ERROR -> "流异常"
                                PlaybackStatus.IDLE -> "就绪待命"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStreaming) ElectricCyan else AwsAmber,
                            maxLines = 1
                        )
                        Text(
                            text = "· ${playerState.telemetry.latencyMs}ms · ${
                                String.format(
                                    java.util.Locale.US,
                                    "%.1f",
                                    playerState.telemetry.throughputMBs
                                )
                            } MB/s",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextLowContrast,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 右侧：倍速切换胶丸 + 音频遥测/EQ 按钮
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (playerState.playbackSpeed != 1.0f) AwsAmber.copy(alpha = 0.2f)
                                    else SurfaceContainerHigh
                                )
                                .border(
                                    1.dp,
                                    if (playerState.playbackSpeed != 1.0f) AwsAmber.copy(alpha = 0.6f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    val nextIdx = (speedOptions.indexOf(playerState.playbackSpeed) + 1) % speedOptions.size
                                    onSetPlaybackSpeed(speedOptions[nextIdx])
                                }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("hero_btn_speed")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "倍速",
                                    tint = if (playerState.playbackSpeed != 1.0f) AwsAmber else TextMediumContrast,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${playerState.playbackSpeed}x",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (playerState.playbackSpeed != 1.0f) AwsAmber else TextHighContrast
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerHigh)
                                .clickable { onOpenTelemetry() }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("hero_btn_telemetry")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Equalizer,
                                    contentDescription = "音效与遥测",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "EQ/遥测",
                                    fontSize = 10.sp,
                                    color = ElectricCyanDim,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Track Artwork & Metadata Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_aether_cube),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (playerState.status == PlaybackStatus.PLAYING) ElectricCyan.copy(alpha = 0.15f)
                                    else AwsAmber.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (playerState.status == PlaybackStatus.PLAYING) ElectricCyan else AwsAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val folderDisplay = currentTrack?.key?.trim()?.removePrefix("/")?.let { k ->
                        if (k.contains("/")) "📁 " + k.substringBeforeLast("/") + "/" else "📁 根目录 /"
                    } ?: "📁 暂未选择目录"

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceContainerHighest)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = currentTrack?.format ?: "S3 STREAM",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AwsAmber
                                )
                            }
                            Text(
                                text = folderDisplay,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextLowContrast,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = currentTrack?.title ?: "队列暂无播放曲目",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val bufferPct = playerState.bufferPercentage.coerceIn(0, 100)
                        Text(
                            text = if (currentTrack != null) {
                                "${currentTrack.sampleRate} · ${currentTrack.sizeFormatted} · 缓冲 $bufferPct%"
                            } else {
                                "在「文件浏览」中点击任意目录或单曲即可开始流式播放"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = ElectricCyanDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Interactive Seek Slider & Timestamps
                val duration = if (playerState.durationMs > 0) playerState.durationMs else (currentTrack?.durationMs ?: 0L)
                val currentPos = playerState.currentPositionMs.coerceAtMost(duration.coerceAtLeast(1L))
                val actualFraction = if (duration > 0) (currentPos.toFloat() / duration).coerceIn(0f, 1f) else 0f
                val sliderValue = if (isUserScrubbing) scrubFraction else actualFraction
                val displayPositionMs = if (isUserScrubbing) (scrubFraction * duration).toLong() else currentPos

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerLowest.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Slider(
                        value = sliderValue,
                        onValueChange = { frac ->
                            isUserScrubbing = true
                            scrubFraction = frac
                        },
                        onValueChangeFinished = {
                            if (duration > 0) {
                                onSeekTo((scrubFraction * duration).toLong())
                            }
                            isUserScrubbing = false
                        },
                        enabled = currentTrack != null && duration > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("hero_seek_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = SurfaceContainerHighest,
                            disabledThumbColor = TextLowContrast,
                            disabledActiveTrackColor = TextLowContrast
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatPlaylistMs(displayPositionMs),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUserScrubbing) AwsAmber else ElectricCyan
                        )
                        if (currentIndex >= 0 && tracks.isNotEmpty()) {
                            Text(
                                text = "第 ${currentIndex + 1} / ${tracks.size} 首",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextLowContrast
                            )
                        }
                        Text(
                            text = formatPlaylistMs(duration),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextLowContrast
                        )
                    }
                }

                // 辅助控制栏：循环模式切换 + 停止播放按钮 (独立行，空间自适应)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val loopIcon = when (playerState.loopMode) {
                        LoopMode.SINGLE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    val loopLabel = when (playerState.loopMode) {
                        LoopMode.ALL -> "列表循环"
                        LoopMode.SINGLE -> "单曲循环"
                        LoopMode.OFF -> "顺序播放"
                        LoopMode.AB_REPEAT -> "AB循环"
                    }
                    val isLoopOn = playerState.loopMode != LoopMode.OFF
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isLoopOn) AwsAmber.copy(alpha = 0.16f)
                                else SurfaceContainerHigh
                            )
                            .border(
                                1.dp,
                                if (isLoopOn) AwsAmber.copy(alpha = 0.5f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleRepeat() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("playlist_toggle_loop_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = loopIcon,
                                contentDescription = loopLabel,
                                tint = if (isLoopOn) AwsAmber else TextMediumContrast,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = loopLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isLoopOn) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLoopOn) AwsAmber else TextMediumContrast
                            )
                        }
                    }

                    // 停止按钮
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh)
                            .clickable { onStopPlayback() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("hero_btn_stop"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "停止播放",
                                tint = if (playerState.status == PlaybackStatus.PLAYING) StatusRed else TextMediumContrast,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "停止",
                                fontSize = 11.sp,
                                color = if (playerState.status == PlaybackStatus.PLAYING) StatusRed else TextMediumContrast
                            )
                        }
                    }
                }

                // 核心控制按钮行：上一首 / 快退10s / 播放暂停(Hero 52dp) / 快进30s / 下一首
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 上一首
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .clickable(enabled = tracks.isNotEmpty()) { onPlayPrevious() }
                            .testTag("hero_btn_prev"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "上一首",
                            tint = if (tracks.isNotEmpty()) TextHighContrast else TextLowContrast,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 快退10s
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .clickable(enabled = currentTrack != null) { onSkipBackward10() }
                            .testTag("hero_btn_replay10"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "快退10秒",
                            tint = if (currentTrack != null) TextHighContrast else TextLowContrast,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 主播放/暂停 (突出的大圆形高光按钮)
                    val isPlaying = playerState.status == PlaybackStatus.PLAYING ||
                            playerState.status == PlaybackStatus.BUFFERING
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) AwsAmber else ElectricCyan)
                            .clickable(enabled = currentTrack != null || tracks.isNotEmpty()) { onTogglePlayPause() }
                            .testTag("hero_btn_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = AetherVoid,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 快进30s
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .clickable(enabled = currentTrack != null) { onSkipForward30() }
                            .testTag("hero_btn_forward30"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward30,
                            contentDescription = "快进30秒",
                            tint = if (currentTrack != null) TextHighContrast else TextLowContrast,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 下一首
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .clickable(enabled = tracks.isNotEmpty()) { onPlayNext() }
                            .testTag("hero_btn_next"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "下一首",
                            tint = if (tracks.isNotEmpty()) TextHighContrast else TextLowContrast,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3. 播放队列标题栏与快捷工具栏 (Queue Header & Action Bar - 响应式双行架构)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: 队列标题与统计 (左侧) + 监听路径指示 (右侧)
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
                            text = "播放队列 (${tracks.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        if (currentIndex > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "已播 $currentIndex 首",
                                    fontSize = 10.sp,
                                    color = TextMediumContrast
                                )
                            }
                        }
                    }

                    Text(
                        text = "监听: $monitoredLocation",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TextLowContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Row 2: 独立全宽横向滚动胶丸栏 (窄屏下绝不挤占标题，手势滑动流畅)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 0. 目录监听开关
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                if (isDirectoryMonitoringEnabled) AwsAmber.copy(alpha = 0.16f)
                                else SurfaceContainer
                            )
                            .border(
                                1.dp,
                                if (isDirectoryMonitoringEnabled) AwsAmber.copy(alpha = 0.5f) else BorderSubtle,
                                RoundedCornerShape(7.dp)
                            )
                            .clickable { onToggleDirectoryMonitoring() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("playlist_toggle_monitoring_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "目录监听",
                                tint = if (isDirectoryMonitoringEnabled) AwsAmber else TextMediumContrast,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isDirectoryMonitoringEnabled) "监听已开" else "监听已关",
                                fontSize = 10.sp,
                                fontWeight = if (isDirectoryMonitoringEnabled) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDirectoryMonitoringEnabled) AwsAmber else TextMediumContrast
                            )
                        }
                    }

                    // 0.1 立即检测新增音频
                    if (isDirectoryMonitoringEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(SurfaceContainer)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(7.dp))
                                .clickable { onForceCheckNewUploads() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("playlist_force_check_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "立即检测",
                                    tint = TextMediumContrast,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "立即检测",
                                    fontSize = 10.sp,
                                    color = TextMediumContrast
                                )
                            }
                        }
                    }

                    // 1. 新增音频自动播放开关
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                if (isAutoPlayNewEnabled) ElectricCyan.copy(alpha = 0.16f)
                                else SurfaceContainer
                            )
                            .border(
                                1.dp,
                                if (isAutoPlayNewEnabled) ElectricCyan.copy(alpha = 0.5f) else BorderSubtle,
                                RoundedCornerShape(7.dp)
                            )
                            .clickable { onToggleAutoPlayNew() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("playlist_toggle_autoplay_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAutoPlayNewEnabled) Icons.Default.MotionPhotosAuto else Icons.Default.PlayCircle,
                                contentDescription = "新音频自动播放",
                                tint = if (isAutoPlayNewEnabled) ElectricCyan else TextMediumContrast,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isAutoPlayNewEnabled) "新曲自动播" else "手动触发",
                                fontSize = 10.sp,
                                fontWeight = if (isAutoPlayNewEnabled) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAutoPlayNewEnabled) ElectricCyan else TextMediumContrast
                            )
                        }
                    }

                    // 2. 清理已播曲目
                    if (tracks.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(SurfaceContainer)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(7.dp))
                                .clickable { onClearPlayed() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("playlist_clear_played_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = "清理已播",
                                    tint = TextMediumContrast,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "清理已播",
                                    fontSize = 10.sp,
                                    color = TextHighContrast
                                )
                            }
                        }

                        // 3. 清空全部队列
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(SurfaceContainer)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(7.dp))
                                .clickable { onClearAll() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("playlist_clear_all_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "清空队列",
                                    tint = StatusRed.copy(alpha = 0.85f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "清空",
                                    fontSize = 10.sp,
                                    color = TextMediumContrast
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. 播放队列曲目列表或空队列引导卡片
        if (tracks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AetherSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TextLowContrast,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "当前播放队列为空",
                        color = TextHighContrast,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "您可以前往「文件浏览」直接点击目录播放，或将喜欢的子目录与单曲加入队列",
                        color = TextLowContrast,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onNavigateToBrowser,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "前往文件浏览添加音频",
                            color = Color.Black,
                            fontSize = 12.sp,
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
                        .alpha(if (isPlayed) 0.62f else 1f)
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
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("queue_item_${track.id}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：序号或播放状态指示器 + 曲目元数据
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
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
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                isPausedThis -> {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "已暂停",
                                        tint = AetherVoid,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%02d", index + 1),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
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
                                fontSize = 13.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                color = when {
                                    isPlayingThis -> ElectricCyan
                                    isPausedThis -> AwsAmber
                                    else -> TextHighContrast
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

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
                                        fontSize = 9.sp,
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
                                                fontSize = 9.sp,
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
                                                fontSize = 9.sp,
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
                                                fontSize = 9.sp,
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
                                                fontSize = 9.sp,
                                                color = TextLowContrast
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${track.durationFormatted} · ${track.sizeFormatted}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = TextLowContrast,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }

                    // 右侧：紧凑型队列项控制按钮组 (替换默认 48dp 的 IconButton，窄屏下绝不挤压曲目标题)
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
                                        contentDescription = "下一首播放 (优先插队)",
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

private fun formatPlaylistMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}
