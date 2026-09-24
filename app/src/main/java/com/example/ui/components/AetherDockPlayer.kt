package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlaybackStatus
import com.example.audio.PlayerState
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast

@Composable
fun AetherDockPlayer(
    playerState: PlayerState,
    onExpandPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack ?: return

    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val isConnecting = playerState.status == PlaybackStatus.CONNECTING || playerState.status == PlaybackStatus.BUFFERING
    val playFraction = if (playerState.durationMs > 0) {
        (playerState.currentPositionMs.toFloat() / playerState.durationMs).coerceIn(0f, 1f)
    } else 0f

    val totalSec = playerState.currentPositionMs / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val timecode = String.format("%02d:%02d", min, sec)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = ElectricCyan.copy(alpha = 0.25f),
                ambientColor = ElectricCyan.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(AetherSurfaceTier1.copy(alpha = 0.94f))
            .border(1.dp, BorderCyanGlow, RoundedCornerShape(14.dp))
            .clickable(onClick = onExpandPlayer)
            .testTag("dock_player")
    ) {
        Column {
            // Live playback buffer line
            LinearProgressIndicator(
                progress = { playFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = ElectricCyan,
                trackColor = AetherSurfaceTier2
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Format / Status Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AetherSurfaceTier2)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = track.format.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Track details
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
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextHighContrast
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timecode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AwsAmber
                        )
                        Text(
                            text = " // " + track.sampleRate,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextLowContrast,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Skip Next button
                IconButton(
                    onClick = onPlayNext,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("dock_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = TextHighContrast,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Tactile Master Play/Pause button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AwsAmber)
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("dock_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AetherVoid,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = AetherVoid,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
