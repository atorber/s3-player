package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlaybackStatus
import com.example.audio.PlayerState
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket
import com.example.ui.components.AetherStatusChip
import com.example.ui.components.AetherTrackItem
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
fun ExplorerScreen(
    tracks: List<S3AudioTrack>,
    buckets: List<S3Bucket>,
    selectedBucketName: String?,
    selectedFormatFilter: String?,
    playerState: PlayerState,
    onSelectBucket: (String?) -> Unit,
    onSelectFormat: (String?) -> Unit,
    onTrackClick: (S3AudioTrack) -> Unit,
    onToggleFavorite: (S3AudioTrack) -> Unit,
    onToggleCache: (S3AudioTrack) -> Unit,
    onOpenMountDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBucket = buckets.firstOrNull { it.bucketName == selectedBucketName }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
    ) {
        // Horizontal Bucket Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "ALL BUCKETS" Chip
            val isAllSelected = selectedBucketName == null || selectedBucketName == "ALL"
            FilterPill(
                label = "全部 S3 存储桶",
                isSelected = isAllSelected,
                onClick = { onSelectBucket("ALL") },
                testTag = "filter_bucket_all"
            )

            buckets.forEach { b ->
                val isSelected = selectedBucketName == b.bucketName
                FilterPill(
                    label = b.bucketName,
                    isSelected = isSelected,
                    indicatorColor = if (b.isMounted) StatusGreen else null,
                    onClick = { onSelectBucket(b.bucketName) },
                    testTag = "filter_bucket_${b.bucketName}"
                )
            }
        }

        // Format Filter Pills (FLAC, WAV, MP3, OGG)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "格式筛选 //",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextLowContrast,
                modifier = Modifier.padding(end = 4.dp)
            )

            listOf("ALL", "FLAC", "WAV", "MP3", "OGG").forEach { fmt ->
                val isSelected = (selectedFormatFilter ?: "ALL") == fmt
                FormatPill(
                    label = if (fmt == "ALL") "全部" else fmt,
                    isSelected = isSelected,
                    onClick = { onSelectFormat(fmt) },
                    testTag = "filter_format_$fmt"
                )
            }
        }

        // Active Bucket Telemetry Bar
        if (activeBucket != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AetherSurfaceTier1)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = AwsAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "s3://${activeBucket.bucketName}/",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${activeBucket.provider} • ${activeBucket.endpoint}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextLowContrast
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${tracks.size} 个音频对象",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeBucket.storageSizeFormatted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextMediumContrast
                        )
                    }
                }
            }
        }

        // Tracks List or Empty State
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = TextLowContrast,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "未找到 S3 音频对象",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMediumContrast
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "请调整筛选前缀或挂载新的 S3 存储桶。",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextLowContrast
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenMountDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = AwsAmber)
                    ) {
                        Text(
                            text = "挂载存储桶",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AetherVoid
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tracks, key = { it.id }) { track ->
                    val isCurrent = playerState.currentTrack?.id == track.id
                    val isPlaying = isCurrent && playerState.status == PlaybackStatus.PLAYING

                    AetherTrackItem(
                        track = track,
                        isPlaying = isPlaying,
                        isCurrentTrack = isCurrent,
                        onTrackClick = { onTrackClick(track) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onToggleCache = { onToggleCache(track) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    indicatorColor: Color? = null,
    testTag: String = ""
) {
    val bg = if (isSelected) AwsAmber.copy(alpha = 0.2f) else AetherSurfaceTier1
    val border = if (isSelected) AwsAmber else BorderSubtle
    val textColor = if (isSelected) AwsAmber else TextMediumContrast

    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(9999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (indicatorColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun FormatPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val bg = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else AetherSurfaceTier2
    val border = if (isSelected) ElectricCyan else BorderSubtle
    val textColor = if (isSelected) ElectricCyan else TextLowContrast

    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
