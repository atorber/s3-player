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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.LoopMode
import com.example.audio.PlaybackStatus
import com.example.audio.PlayerState
import com.example.ui.components.AetherStatusChip
import com.example.ui.components.AetherWaveform
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
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
    onSkipForward30s: () -> Unit,
    onSkipBackward10s: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onToggleLoopMode: () -> Unit,
    onSetAbPointA: () -> Unit = {},
    onSetAbPointB: () -> Unit = {},
    onClearAbLoop: () -> Unit = {},
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
        targetValue = if (isPlaying || isBuffering) 1.22f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Subtle vinyl rotation animation when playing
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotate"
    )

    val currentDisplayPos = if (playerState.isScrubbing) playerState.scrubPositionMs else playerState.currentPositionMs

    fun formatTimecode(posMs: Long): String {
        val totalSec = (posMs / 1000).coerceAtLeast(0)
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format(java.util.Locale.US, "%02d:%02d", min, sec)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 36.dp)
            .testTag("expanded_player_sheet")
    ) {
        // Drag Handle & Top Inspection Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
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
                    .size(38.dp)
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
                    text = "正在播放",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Text(
                    text = track.bucketName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextLowContrast
                )
            }

            Row {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AetherSurfaceTier1)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "收藏",
                        tint = if (track.isFavorite) AwsAmber else TextLowContrast,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onOpenTelemetry,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AetherSurfaceTier1)
                        .testTag("player_inspect_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "音频均衡器",
                        tint = ElectricCyan,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Central Visualizer: Elegant Vinyl Record Artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Under-Glow
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = CircleShape,
                        spotColor = if (isPlaying) AwsAmber.copy(alpha = 0.35f) else ElectricCyan.copy(alpha = 0.2f),
                        ambientColor = ElectricCyan.copy(alpha = 0.15f)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF22262E),
                                Color(0xFF14171D),
                                Color(0xFF0C0E12)
                            )
                        )
                    )
                    .border(2.dp, if (isPlaying) BorderCyanGlow else BorderSubtle, CircleShape)
                    .rotate(if (isPlaying) rotationAngle else 0f),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl Grooves Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radiusStep = size.minDimension / 14f
                    for (i in 2..5) {
                        drawCircle(
                            color = Color(0xFF2E3440).copy(alpha = 0.4f),
                            radius = i * radiusStep,
                            style = Stroke(width = 1f)
                        )
                    }
                }

                // Vinyl Center Label
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AwsAmber, Color(0xFFE65100))
                            )
                        )
                        .border(2.dp, ElectricCyan.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AetherVoid,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Track Title & S3 Object Key Path
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        ) {
            Text(
                text = track.title,
                fontFamily = FontFamily.SansSerif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artistOrProject.ifBlank { "S3 音频对象" },
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                color = TextMediumContrast
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.fullS3Uri,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = ElectricCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Technical Audio Badges Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AetherStatusChip(
                label = track.format.uppercase(),
                borderColor = AwsAmber.copy(alpha = 0.5f),
                textColor = AwsAmber
            )
            AetherStatusChip(
                label = track.sampleRate,
                borderColor = ElectricCyan.copy(alpha = 0.4f),
                textColor = ElectricCyan
            )
            AetherStatusChip(
                label = track.channels,
                borderColor = BorderSubtle,
                textColor = TextMediumContrast
            )
            AetherStatusChip(
                label = "缓冲 ${playerState.bufferPercentage}%",
                indicatorColor = if (playerState.bufferPercentage > 50) StatusGreen else AwsAmber,
                borderColor = BorderSubtle,
                textColor = TextMediumContrast
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Dynamic Waveform Visualizer & Seek Bar
        Box(modifier = Modifier.padding(horizontal = 18.dp)) {
            AetherWaveform(
                amplitudes = track.getWaveformPoints(),
                currentPositionMs = playerState.currentPositionMs,
                durationMs = playerState.durationMs,
                bufferPercent = playerState.bufferPercentage,
                isScrubbing = playerState.isScrubbing,
                scrubPositionMs = playerState.scrubPositionMs,
                onScrub = onScrub,
                heightDp = 80
            )
        }

        // Millisecond Timecode Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp),
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

        Spacer(modifier = Modifier.height(14.dp))

        // Master Transport Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Loop Mode Button
            IconButton(
                onClick = onToggleLoopMode,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("player_loop_btn")
            ) {
                val loopIcon = when (playerState.loopMode) {
                    LoopMode.SINGLE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                val loopTint = if (playerState.loopMode != LoopMode.OFF) AwsAmber else TextLowContrast
                Icon(
                    imageVector = loopIcon,
                    contentDescription = "循环模式",
                    tint = loopTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Skip Backward 10s
            IconButton(
                onClick = onSkipBackward10s,
                modifier = Modifier
                    .size(46.dp)
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

            // Skip Forward 30s (Harmonized standard)
            IconButton(
                onClick = onSkipForward30s,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("player_forward_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "快进30秒",
                    tint = TextHighContrast,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Skip Next Track
            IconButton(
                onClick = onPlayNext,
                modifier = Modifier
                    .size(46.dp)
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

        Spacer(modifier = Modifier.height(18.dp))

        // Playback Speed Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "播放倍速",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMediumContrast
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                    val isSelected = playerState.playbackSpeed == spd
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else SurfaceContainerHigh)
                            .border(
                                1.dp,
                                if (isSelected) ElectricCyan else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSetPlaybackSpeed(spd) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${spd}x",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ElectricCyan else TextMediumContrast
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Equalizer Action Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AetherSurfaceTier2)
                .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenTelemetry)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("inspect_headers_btn")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "专业音频均衡器 (EQ) 调音台",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Text(
                    text = "${playerState.eqPreset.label} >",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = AwsAmber
                )
            }
        }
    }
}
