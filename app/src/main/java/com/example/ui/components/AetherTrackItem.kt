package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.S3AudioTrack
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast

@Composable
fun AetherTrackItem(
    track: S3AudioTrack,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    onTrackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighlighted = isCurrentTrack
    val containerBg = if (isHighlighted) AetherSurfaceTier2 else AetherSurfaceTier1
    val borderColor = if (isHighlighted) ElectricCyan.copy(alpha = 0.5f) else BorderSubtle

    val badgeBorderColor = when (track.format.uppercase()) {
        "FLAC" -> ElectricCyan
        "WAV" -> AwsAmber
        "MP3" -> Color(0xFF38BDF8)
        else -> Color(0xFFA855F7)
    }

    // Live transmission indicator dot: Green for Cached, Cyan for S3 Streaming, Amber for Fetching
    val transmissionDotColor = when {
        track.isCachedLocally -> StatusGreen
        isCurrentTrack && isPlaying -> ElectricCyan
        isCurrentTrack -> StatusAmber
        else -> ElectricCyan.copy(alpha = 0.4f)
    }

    val transmissionLabel = when {
        track.isCachedLocally -> "已缓存"
        isCurrentTrack && isPlaying -> "串流中"
        isCurrentTrack -> "等待中"
        else -> "S3节点"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onTrackClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("track_item_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Monospaced file extension badge in dark capsule with cyan/amber accent border
        Box(
            modifier = Modifier
                .size(44.dp, 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AetherSurfaceTier2)
                .border(1.dp, badgeBorderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = track.format.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeBorderColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center: Track title above truncated S3 Object Key path
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isHighlighted) ElectricCyan else TextHighContrast
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.fullS3Uri,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextLowContrast
            )
        }

        // Right: File size payload + live transmission indicator dot
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.sizeFormatted,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(transmissionDotColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = transmissionLabel,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = transmissionDotColor
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Quick favorite toggle button
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .size(36.dp)
                .testTag("track_fav_${track.id}")
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (track.isFavorite) "取消收藏" else "加入收藏",
                tint = if (track.isFavorite) AwsAmber else TextLowContrast,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
