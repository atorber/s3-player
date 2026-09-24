package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.AwsAmberLight
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun PlayerScreen(
    playerState: PlayerState,
    fallbackTrack: S3AudioTrack?,
    isPitchCorrectionEnabled: Boolean,
    isSilenceTrimmingEnabled: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipBackward15: () -> Unit,
    onSkipForward30: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onTogglePitchCorrection: () -> Unit,
    onToggleSilenceTrimming: () -> Unit,
    onInspectObjectKey: () -> Unit,
    onQuickListenNewArrival: () -> Unit,
    onToggleRepeat: () -> Unit = {},
    onStopPlayback: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack ?: fallbackTrack
    val isPlaying = playerState.status == PlaybackStatus.PLAYING

    // Vinyl disc rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_orbit")
    val rotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 16000 else 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    // Pulse animation for badges & live glow
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val currentMs = if (playerState.isScrubbing) playerState.scrubPositionMs else playerState.currentPositionMs
    val totalMs = if (playerState.durationMs > 0) playerState.durationMs else 2892000L // 48:12 default
    val progressFraction = (currentMs.toFloat() / totalMs).coerceIn(0f, 1f)

    val currentMin = (currentMs / 1000) / 60
    val currentSec = (currentMs / 1000) % 60
    val formattedCurrent = String.format("%02d:%02d", currentMin, currentSec)

    val totalMin = (totalMs / 1000) / 60
    val totalSec = (totalMs / 1000) % 60
    val formattedTotal = String.format("%02d:%02d", totalMin, totalSec)

    val remainingMs = (totalMs - currentMs).coerceAtLeast(0L)
    val remMin = (remainingMs / 1000) / 60
    val remSec = (remainingMs / 1000) % 60
    val formattedRemaining = String.format("-%02d:%02d", remMin, remSec)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // 1. Top Bar Context Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerHigh.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = AwsAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "/podcasts/2025-season/ep04-live/",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TextHighContrast
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = ElectricCyanDim,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "etag:9b2d8...f4a",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = ElectricCyanDim
                    )
                }
            }
        }

        // 2. Holographic Audio Stage & Vinyl / Spatial Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(SurfaceContainer, SurfaceContainerLowest)
                    )
                )
                .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("audio_stage_box"),
            contentAlignment = Alignment.Center
        ) {
            // Stage Top: S3 Storage Class & Latency Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHighest.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AwsAmber)
                                .alpha(pulseAlpha)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "S3 标准存储 · ap-northeast-1",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHighest.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "18ms 边缘延迟",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }

            // Center: Holographic Vinyl Disc
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                // Outer Orbit Ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .rotate(rotationDeg)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    ElectricCyan.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    AwsAmber.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Concentric Grooves Disc
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest)
                        .border(2.dp, SurfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .border(1.dp, SurfaceContainerHighest, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            // Center Hub
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(8.dp, CircleShape, spotColor = AwsAmber)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(AwsAmber, ElectricCyan)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = AetherVoid,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Left (L) & Right (R) Channel indicators
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "L", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = ElectricCyan)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ElectricCyan.copy(alpha = 0.6f))
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "R", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = AwsAmber)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AwsAmber.copy(alpha = 0.6f))
                    )
                }
            }

            // Bottom of Stage: Real-time Stereo Waveform Visualizer Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(
                    0.51f, 0.94f, 0.26f, 0.50f, 0.83f, 0.89f, 0.96f, 0.58f,
                    0.61f, 0.47f, 0.88f, 0.48f, 0.94f, 0.37f, 0.32f, 0.49f,
                    0.36f, 0.83f, 0.41f, 0.96f
                )
                heights.forEachIndexed { i, fraction ->
                    val color = if (i % 2 == 0) ElectricCyan else AwsAmber
                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height((36 * fraction).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
            }
        }

        // 3. Track Title & Metadata Stack
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "ep04_final_master_v2.flac",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.artistOrProject ?: "Aether Tech Podcast · Ep 04: Edge Cloud Audio",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextLowContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = "收藏",
                        tint = TextMediumContrast,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Tech Telemetry Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FLAC 96kHz / 24-bit",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyanDim
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "无损 1411kbps",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AwsAmber
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "bytes=0-1048576",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TextLowContrast
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                                .alpha(pulseAlpha)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "直链直连",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyanDim
                        )
                    }
                }
            }
        }

        // 4. Waveform Scrubbing Engine & Dynamic Buffering Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                    Text(
                        text = "S3 分块: 68/120 已缓存",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyanDim
                    )
                }

                Text(
                    text = "当前分块 #42",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextLowContrast
                )
            }

            // Waveform scrubber bar with thumb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeekTo((fraction * totalMs).toLong())
                        }
                    }
                    .testTag("waveform_scrubber"),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background waveform bars simulation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .alpha(0.2f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rawAmplitudes = listOf(
                        4, 8, 6, 10, 7, 11, 5, 9, 10, 6, 8, 11, 9, 5, 8, 10, 7, 6, 4, 9, 7, 5, 8, 10, 6, 7, 4, 6
                    )
                    rawAmplitudes.forEach { amp ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((amp * 3.2).dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TextHighContrast)
                        )
                    }
                }

                // S3 Loaded Buffer Segment (68%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SurfaceContainerHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(6.dp)
                            .background(ElectricCyan.copy(alpha = 0.35f))
                    )
                }

                // Active Played Timeline Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricCyan, AwsAmber)
                            )
                        )
                )

                // Scrubber Thumb
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterEnd)
                            .shadow(8.dp, CircleShape, spotColor = AwsAmber)
                            .clip(CircleShape)
                            .background(AwsAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AetherVoid)
                        )
                    }
                }
            }

            // Timestamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedCurrent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyanDim
                )
                Text(
                    text = "$formattedTotal 总时长",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextLowContrast
                )
                Text(
                    text = formattedRemaining,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AwsAmber
                )
            }
        }

        // 5. Primary Playback Controls & Skip Clusters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainer)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip 15s Back
                IconButton(
                    onClick = onSkipBackward15,
                    modifier = Modifier.size(44.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "快退 15 秒",
                            tint = TextMediumContrast,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(text = "15s", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = TextLowContrast)
                    }
                }

                // Previous Track
                IconButton(
                    onClick = onPlayPrevious,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "上一曲",
                        tint = TextHighContrast,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Stop Button
                IconButton(
                    onClick = onStopPlayback,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .testTag("player_stop_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "停止播放",
                        tint = if (isPlaying) com.example.ui.theme.StatusRed else TextMediumContrast,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Master Primary Play/Pause Tactical Amber Button
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(16.dp, CircleShape, spotColor = AwsAmber.copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .background(AwsAmber)
                        .clickable { onTogglePlayPause() }
                        .testTag("player_play_pause_master_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "播放或暂停",
                        tint = AetherVoid,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Next Track
                IconButton(
                    onClick = onPlayNext,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "下一曲",
                        tint = TextHighContrast,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Skip 30s Forward
                IconButton(
                    onClick = onSkipForward30,
                    modifier = Modifier.size(44.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "快进 30 秒",
                            tint = TextMediumContrast,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(text = "30s", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = TextLowContrast)
                    }
                }
            }

            // Playback Tweaks: Speed Toggles & Audio Processing Switchers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed Selector Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)
                    speeds.forEach { spd ->
                        val isSpeedActive = (playerState.playbackSpeed == spd)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSpeedActive) AwsAmber else Color.Transparent)
                                .clickable { onSetPlaybackSpeed(spd) }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${spd}x",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (isSpeedActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSpeedActive) AetherVoid else TextLowContrast
                            )
                        }
                    }
                }

                // Sound Processing Switches
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Loop Mode Toggle
                    val loopText = when (playerState.loopMode) {
                        LoopMode.ALL -> "列表循环"
                        LoopMode.SINGLE -> "单曲循环"
                        LoopMode.OFF -> "顺序播放"
                        LoopMode.AB_REPEAT -> "区间循环"
                    }
                    val loopIcon = when (playerState.loopMode) {
                        LoopMode.SINGLE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    val isLoopActive = playerState.loopMode != LoopMode.OFF
                    val loopColor = if (playerState.loopMode == LoopMode.SINGLE) ElectricCyan else AwsAmber

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLoopActive) loopColor.copy(alpha = 0.18f) else SurfaceContainerLow)
                            .clickable { onToggleRepeat() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("player_toggle_loop_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = loopIcon,
                                contentDescription = loopText,
                                tint = if (isLoopActive) loopColor else TextMediumContrast,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = loopText,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                fontWeight = if (isLoopActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLoopActive) loopColor else TextLowContrast
                            )
                        }
                    }

                    // Pitch Preservation Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPitchCorrectionEnabled) SurfaceContainerHigh else SurfaceContainerLow)
                            .clickable { onTogglePitchCorrection() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (isPitchCorrectionEnabled) ElectricCyanDim else TextLowContrast,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "音调调整",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPitchCorrectionEnabled) ElectricCyanDim else TextLowContrast
                            )
                        }
                    }

                    // Silence Trimming Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSilenceTrimmingEnabled) SurfaceContainerHigh else SurfaceContainerLow)
                            .clickable { onToggleSilenceTrimming() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SpaceBar,
                                contentDescription = null,
                                tint = if (isSilenceTrimmingEnabled) AwsAmber else TextLowContrast,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "静音裁剪",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                color = if (isSilenceTrimmingEnabled) AwsAmber else TextLowContrast
                            )
                        }
                    }
                }
            }
        }

        // 6. Bottom Live S3 Ingestion Mini Drawer Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(SurfaceContainerHigh, SurfaceContainer)
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .alpha(pulseAlpha)
                    )
                    Text(
                        text = "实时入队动态",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyanDim
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onInspectObjectKey() }
                ) {
                    Text(
                        text = "查看对象键",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TextLowContrast
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = TextLowContrast,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Queue Item Info Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerLowest.copy(alpha = 0.7f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = ElectricCyanDim,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "guest_recording_track_02.flac",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHighContrast,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "通过 S3 PutObject 事件自动添加",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 9.sp,
                            color = TextLowContrast
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan.copy(alpha = 0.2f))
                        .clickable { onQuickListenNewArrival() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "快速试听",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // clearance for dock & nav
    }
}
