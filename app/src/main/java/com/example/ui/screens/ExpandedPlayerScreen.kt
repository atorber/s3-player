package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.example.ui.components.AetherPeakMeter
import com.example.ui.components.AetherStatusChip
import com.example.ui.components.AetherWaveform
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun ExpandedPlayerScreen(
    playerState: PlayerState,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onScrub: (Boolean, Long) -> Unit,
    onSkipForward10s: () -> Unit,
    onSkipBackward10s: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onToggleLoopMode: () -> Unit,
    onSetAbPointA: () -> Unit,
    onSetAbPointB: () -> Unit,
    onClearAbLoop: () -> Unit,
    onOpenTelemetry: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack ?: return
    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val isBuffering = playerState.status == PlaybackStatus.BUFFERING || playerState.status == PlaybackStatus.CONNECTING

    // Outer radiant pulse ring animation for 56px play button when streaming/buffering
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying || isBuffering) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val currentDisplayPos = if (playerState.isScrubbing) playerState.scrubPositionMs else playerState.currentPositionMs

    fun formatTimecode(posMs: Long): String {
        val totalSec = posMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val ms = posMs % 1000
        return String.format("%02d:%02d.%03d", min, sec, ms)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
            .testTag("expanded_player_sheet")
    ) {
        // Drag Handle & Top Inspection Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextLowContrast.copy(alpha = 0.5f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AetherSurfaceTier1)
                    .testTag("player_collapse_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "收起播放器",
                    tint = TextHighContrast,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "S3 流媒体控制台",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElectricCyan
                )
                Text(
                    text = "${playerState.telemetry.s3RegionEdge} • ${playerState.telemetry.latencyMs}ms",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TextLowContrast
                )
            }

            Row {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AetherSurfaceTier1)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "收藏",
                        tint = if (track.isFavorite) AwsAmber else TextLowContrast,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenTelemetry,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AetherSurfaceTier1)
                        .testTag("player_inspect_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "音频遥测与S3响应头",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Central Visualizer: Cosmic Isometric 3D Audio Cube Artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Under-Glow
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = if (isPlaying) AwsAmber.copy(alpha = 0.35f) else ElectricCyan.copy(alpha = 0.2f),
                        ambientColor = ElectricCyan.copy(alpha = 0.15f)
                    )
                    .background(AetherSurfaceTier1)
                    .border(1.dp, if (isPlaying) BorderCyanGlow else BorderSubtle, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_aether_cube),
                    contentDescription = "Aether 3D Audio Telemetry Core",
                    modifier = Modifier
                        .size(150.dp)
                        .scale(if (isPlaying) 1.03f else 1.0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stereo Peak dB Meters
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            AetherPeakMeter(
                leftDb = playerState.telemetry.leftChannelDb,
                rightDb = playerState.telemetry.rightChannelDb
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Track Title & S3 Object Key Path
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = track.title,
                fontFamily = FontFamily.SansSerif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = track.artistOrProject,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = TextMediumContrast
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.fullS3Uri,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = ElectricCyan,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Technical Telemetry Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AetherStatusChip(
                label = track.sampleRate,
                borderColor = ElectricCyan.copy(alpha = 0.4f),
                textColor = ElectricCyan
            )
            AetherStatusChip(
                label = track.channels,
                borderColor = AwsAmber.copy(alpha = 0.4f),
                textColor = AwsAmber
            )
            AetherStatusChip(
                label = "缓冲 ${playerState.bufferPercentage}%",
                indicatorColor = if (playerState.bufferPercentage > 50) StatusGreen else AwsAmber,
                borderColor = BorderSubtle,
                textColor = TextMediumContrast
            )
            AetherStatusChip(
                label = track.storageClass,
                borderColor = BorderSubtle,
                textColor = TextLowContrast
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Dynamic Waveform Visualizer & Seek Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            AetherWaveform(
                amplitudes = track.getWaveformPoints(),
                currentPositionMs = playerState.currentPositionMs,
                durationMs = playerState.durationMs,
                bufferPercent = playerState.bufferPercentage,
                isScrubbing = playerState.isScrubbing,
                scrubPositionMs = playerState.scrubPositionMs,
                onScrub = onScrub,
                heightDp = 84
            )
        }

        // Millisecond Timecode Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTimecode(currentDisplayPos),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AwsAmber
            )
            Text(
                text = formatTimecode(playerState.durationMs),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextMediumContrast
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Master Transport Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Loop Mode Button (Ghost/Tertiary: shifts to Amber when active)
            IconButton(
                onClick = onToggleLoopMode,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("player_loop_btn")
            ) {
                val loopIcon = when (playerState.loopMode) {
                    LoopMode.SINGLE -> Icons.Default.RepeatOne
                    LoopMode.ALL, LoopMode.AB_REPEAT -> Icons.Default.Repeat
                    LoopMode.OFF -> Icons.Default.Repeat
                }
                val loopTint = if (playerState.loopMode != LoopMode.OFF) AwsAmber else TextLowContrast
                Icon(
                    imageVector = loopIcon,
                    contentDescription = "循环模式",
                    tint = loopTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Skip Backward 10s
            IconButton(
                onClick = onSkipBackward10s,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("player_rewind_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "后退10秒",
                    tint = TextHighContrast,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Master 56px Circular Tactile Play/Pause Button
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                // Radiant pulse ring
                if (isPlaying || isBuffering) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(AwsAmber.copy(alpha = 0.25f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AwsAmber)
                        .shadow(12.dp, CircleShape, spotColor = AwsAmber)
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("master_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AetherVoid,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = AetherVoid,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Skip Forward 10s
            IconButton(
                onClick = onSkipForward10s,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("player_forward_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "快进10秒",
                    tint = TextHighContrast,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Skip Next Track
            IconButton(
                onClick = onPlayNext,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("player_skip_next_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    tint = TextHighContrast,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AB Looper Bar & Playback Speed Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AB Repeat Region Looper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "A-B 循环:",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLowContrast
                )

                // Point A button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (playerState.abPointA != null) AwsAmber else AetherSurfaceTier2)
                        .border(1.dp, if (playerState.abPointA != null) AwsAmber else BorderSubtle, RoundedCornerShape(4.dp))
                        .clickable(onClick = onSetAbPointA)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (playerState.abPointA != null) "A: ${(playerState.abPointA / 1000)}s" else "[A点]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (playerState.abPointA != null) AetherVoid else TextHighContrast
                    )
                }

                // Point B button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (playerState.abPointB != null) AwsAmber else AetherSurfaceTier2)
                        .border(1.dp, if (playerState.abPointB != null) AwsAmber else BorderSubtle, RoundedCornerShape(4.dp))
                        .clickable(onClick = onSetAbPointB)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (playerState.abPointB != null) "B: ${(playerState.abPointB / 1000)}s" else "[B点]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (playerState.abPointB != null) AetherVoid else TextHighContrast
                    )
                }

                if (playerState.abPointA != null || playerState.abPointB != null) {
                    Text(
                        text = "清除",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier
                            .clickable(onClick = onClearAbLoop)
                            .padding(4.dp)
                    )
                }
            }

            // Speed Selector Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(0.8f, 1.0f, 1.25f, 1.5f).forEach { spd ->
                    val isSelected = playerState.playbackSpeed == spd
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else AetherSurfaceTier2)
                            .border(1.dp, if (isSelected) ElectricCyan else BorderSubtle, RoundedCornerShape(4.dp))
                            .clickable { onSetPlaybackSpeed(spd) }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${spd}x",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ElectricCyan else TextLowContrast
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Action: Open Telemetry & Headers Inspector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AetherSurfaceTier2)
                .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenTelemetry)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("inspect_headers_btn")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "查看 S3 响应头与 EQ 调音台",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Text(
                    text = "MD5校验 // 5段EQ >",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMediumContrast
                )
            }
        }
    }
}
