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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.OutlineVariant
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
    monitoredLocation: String = "s3://xtrader/",
    onDismissBanner: () -> Unit,
    onPlayTrack: (S3AudioTrack) -> Unit,
    onTogglePlayPause: () -> Unit = {},
    onStopPlayback: () -> Unit = {},
    onPlayNewArrival: () -> Unit,
    onToggleAutoPlayNew: () -> Unit = {},
    onToggleRepeat: () -> Unit,
    onClearPlayed: () -> Unit,
    onShuffle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTrack = playerState.currentTrack ?: tracks.firstOrNull()
    var isAutoEnqueueOn by remember { mutableStateOf(true) }

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
            .background(AetherSurface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
            // 1. Real-time Stream Notification Banner (Dismissible Toast)
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
                        .padding(14.dp)
                        .testTag("realtime_stream_banner")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan)
                                        .alpha(pulseAlpha)
                                )
                                Text(
                                    text = "检测到新增 S3 音频文件",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyanDim
                                )
                            }

                            Text(
                                text = "18秒前",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextLowContrast
                            )
                        }

                        Column(modifier = Modifier.padding(start = 14.dp)) {
                            Text(
                                text = "guest_recording_track_02.flac",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                            Text(
                                text = "已通过 PutObject 事件自动入队至第 2 位",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = TextLowContrast
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerHighest)
                                    .clickable { onDismissBanner() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("banner_dismiss_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "忽略",
                                        tint = TextMediumContrast,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "忽略",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 11.sp,
                                        color = TextMediumContrast
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricCyan)
                                    .clickable { onPlayNewArrival() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("banner_play_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "立即播放",
                                        tint = AetherVoid,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "立即播放",
                                        fontFamily = FontFamily.SansSerif,
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

        item {
            // 2. Currently Playing Hero Card in Playlist
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = AwsAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "正在从 S3 节点实时推流",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AwsAmber
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "延迟 24ms",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = ElectricCyanDim
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .testTag("playlist_hero_card")
                ) {
                    // Top row: artwork + meta + volume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_aether_cube),
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(AwsAmber.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = AwsAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
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
                                            text = currentTrack?.format ?: "FLAC",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AwsAmber
                                        )
                                    }
                                    Text(
                                        text = "📁 ep04-live/",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = TextLowContrast
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = currentTrack?.title ?: "ep04_final_master_v2.flac",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighContrast,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "24bit · 96kHz · 1,411 kbps CBR",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = ElectricCyanDim
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onStopPlayback,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh)
                                    .testTag("hero_btn_stop")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "停止播放",
                                    tint = if (playerState.status == PlaybackStatus.PLAYING) StatusRed else TextMediumContrast,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (playerState.status == PlaybackStatus.PLAYING) AwsAmber else ElectricCyan)
                                    .testTag("hero_btn_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (playerState.status == PlaybackStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playerState.status == PlaybackStatus.PLAYING) "暂停" else "播放",
                                    tint = AetherVoid,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Waveform Snippet + timestamps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest.copy(alpha = 0.85f))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val barHeights = listOf(
                                8.dp, 14.dp, 22.dp, 12.dp, 24.dp, 18.dp, 26.dp, 20.dp,
                                10.dp, 16.dp, 22.dp, 12.dp, 26.dp, 16.dp, 20.dp, 14.dp,
                                18.dp, 10.dp, 22.dp, 16.dp, 8.dp, 18.dp, 12.dp
                            )
                            barHeights.forEachIndexed { idx, h ->
                                val isPlayed = idx <= 12
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(h)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isPlayed) (if (idx == 12) AwsAmber else ElectricCyan) else SurfaceContainerHighest)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "24:18",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AwsAmber
                            )
                            Text(
                                text = "48:12",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextLowContrast
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gain / Boost Quick Control & S3 Buffer
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
                                text = "S3 缓冲区:",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                color = TextLowContrast
                            )
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SurfaceContainerHighest)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.88f)
                                        .height(5.dp)
                                        .background(ElectricCyan)
                                )
                            }
                            Text(
                                text = "100%",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = ElectricCyanDim
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "音频增益:",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                color = TextLowContrast
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+1.5 dB",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AwsAmber
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // 3. Playlist Header & Quick Controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "实时动态播放队列",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHighest)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${tracks.size} 首曲目",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyanDim
                        )
                    }
                }

                Text(
                    text = "正在监听 $monitoredLocation",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextLowContrast
                )

                // Quick Controls Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. 自动播放开关 (替换原随机播放)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAutoPlayNewEnabled) ElectricCyan.copy(alpha = 0.18f) else SurfaceContainer)
                            .border(
                                1.dp,
                                if (isAutoPlayNewEnabled) ElectricCyan.copy(alpha = 0.6f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleAutoPlayNew() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("playlist_toggle_autoplay_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAutoPlayNewEnabled) Icons.Default.MotionPhotosAuto else Icons.Default.PlayCircle,
                                contentDescription = "自动播放",
                                tint = if (isAutoPlayNewEnabled) ElectricCyan else TextMediumContrast,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAutoPlayNewEnabled) "自动播放 [开]" else "自动播放 [关]",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = if (isAutoPlayNewEnabled) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAutoPlayNewEnabled) ElectricCyan else TextHighContrast
                            )
                        }
                    }

                    // 2. 循环播放模式切换 (已生效：列表循环 / 单曲循环 / 顺序播放)
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
                            .background(if (isLoopActive) loopColor.copy(alpha = 0.18f) else SurfaceContainer)
                            .border(
                                1.dp,
                                if (isLoopActive) loopColor.copy(alpha = 0.6f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleRepeat() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("playlist_toggle_loop_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = loopIcon,
                                contentDescription = loopText,
                                tint = if (isLoopActive) loopColor else TextMediumContrast,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = loopText,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = if (isLoopActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLoopActive) loopColor else TextHighContrast
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainer)
                            .clickable { onClearPlayed() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CleaningServices, null, tint = TextMediumContrast, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除已播", fontFamily = FontFamily.SansSerif, fontSize = 11.sp, color = TextHighContrast)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAutoEnqueueOn) AwsAmber else SurfaceContainer)
                            .clickable { isAutoEnqueueOn = !isAutoEnqueueOn }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudSync,
                                null,
                                tint = if (isAutoEnqueueOn) AetherVoid else TextMediumContrast,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isAutoEnqueueOn) "自动入队 [开]" else "自动入队 [关]",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoEnqueueOn) AetherVoid else TextHighContrast
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "接下来播放 · 智能排序",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextLowContrast
                )
                Text(
                    text = "长按拖动排序",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextLowContrast
                )
            }
        }

        // 4. Live Queue List
        itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
            val isCurrent = track.key == playerState.currentTrack?.key && track.bucketName == playerState.currentTrack?.bucketName
            val isPlayingThis = isCurrent && (playerState.status == PlaybackStatus.PLAYING ||
                    playerState.status == PlaybackStatus.BUFFERING ||
                    playerState.status == PlaybackStatus.CONNECTING)
            val isPausedThis = isCurrent && playerState.status == PlaybackStatus.PAUSED
            val isFresh = track.id == 2L
            val isNewJoin = track.id == 3L

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isPlayingThis -> ElectricCyan.copy(alpha = 0.08f)
                            isPausedThis -> AwsAmber.copy(alpha = 0.08f)
                            else -> SurfaceContainerLow
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isPlayingThis -> ElectricCyan.copy(alpha = 0.45f)
                            isPausedThis -> AwsAmber.copy(alpha = 0.45f)
                            else -> Color.Transparent
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
                    .padding(10.dp)
                    .testTag("queue_item_${track.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "排序",
                        tint = TextLowContrast,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = track.format,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlayingThis) ElectricCyan else if (isFresh) ElectricCyanDim else AwsAmber
                            )
                            Text(
                                text = if (track.format == "FLAC") "96k" else "48k",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = TextLowContrast
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = track.title,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isPlayingThis) ElectricCyan else if (isPausedThis) AwsAmber else TextHighContrast,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isPlayingThis) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "正在播放",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricCyanDim
                                        )
                                    }
                                }
                            } else if (isPausedThis) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AwsAmber.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "已暂停",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AwsAmber
                                    )
                                }
                            } else if (isFresh) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "实时新增 (PutObject)",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyanDim
                                    )
                                }
                            } else if (isNewJoin) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SurfaceContainerHighest)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "最新 · 自动入队",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AwsAmber
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SurfaceContainer)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "就绪",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 8.sp,
                                        color = TextLowContrast
                                    )
                                }
                            }

                            Text(
                                text = "${track.sizeFormatted} • ${track.durationFormatted}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextLowContrast
                            )
                        }
                    }
                }

                // 正在播放的音频在列表中的操作按钮显示为暂停、停止；未播放音频显示播放
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPlayingThis) {
                        // 1. 停止按钮 (Stop)
                        IconButton(
                            onClick = { onStopPlayback() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                                .testTag("btn_stop_${track.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "停止",
                                tint = StatusRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 2. 暂停按钮 (Pause)
                        IconButton(
                            onClick = { onTogglePlayPause() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AwsAmber)
                                .testTag("btn_pause_${track.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "暂停",
                                tint = AetherVoid,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (isPausedThis) {
                        // 1. 停止按钮 (Stop)
                        IconButton(
                            onClick = { onStopPlayback() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                                .testTag("btn_stop_${track.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "停止",
                                tint = TextMediumContrast,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 2. 继续播放按钮 (Resume Play)
                        IconButton(
                            onClick = { onTogglePlayPause() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                                .testTag("btn_resume_${track.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "继续播放",
                                tint = AetherVoid,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        // 普通播放按钮 (Play)
                        IconButton(
                            onClick = { onPlayTrack(track) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                                .testTag("btn_play_${track.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "播放",
                                tint = if (isFresh) ElectricCyan else TextHighContrast,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            // 5. Queue Policy Footer info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLowest)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = ElectricCyanDim,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "当前实时监听与入队策略",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "当检测到当前子目录有新音频上传时，立即插入下一首并预缓存音频头信息。基于 HTTP Range 请求实现零转码损耗即时流式播放。",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = TextLowContrast,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // clearance for dock & nav
        }
    }
}
