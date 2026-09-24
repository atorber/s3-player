package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextHighContrast

@Composable
fun AetherWaveform(
    amplitudes: List<Float>,
    currentPositionMs: Long,
    durationMs: Long,
    bufferPercent: Int,
    isScrubbing: Boolean,
    scrubPositionMs: Long,
    onScrub: (Boolean, Long) -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 80
) {
    val displayPosition = if (isScrubbing) scrubPositionMs else currentPositionMs
    val playFraction = if (durationMs > 0) (displayPosition.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val bufferFraction = (bufferPercent / 100f).coerceIn(0f, 1f)

    var componentWidthPx by remember { mutableFloatStateOf(1f) }
    var tooltipOffsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val unplayedColor = AetherSurfaceTier2
    val bufferedColor = ElectricCyan.copy(alpha = 0.35f)
    val playedColor = ElectricCyan
    val thumbColor = AwsAmber

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .testTag("waveform_visualizer")
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val targetFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (targetFraction * durationMs).toLong()
                        onScrub(false, targetMs)
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val targetFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            val targetMs = (targetFraction * durationMs).toLong()
                            tooltipOffsetX = offset.x
                            onScrub(true, targetMs)
                        },
                        onDragEnd = {
                            isDragging = false
                            val targetFraction = (tooltipOffsetX / componentWidthPx).coerceIn(0f, 1f)
                            val targetMs = (targetFraction * durationMs).toLong()
                            onScrub(false, targetMs)
                        },
                        onDragCancel = {
                            isDragging = false
                            onScrub(false, displayPosition)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            tooltipOffsetX = change.position.x.coerceIn(0f, componentWidthPx)
                            val targetFraction = (tooltipOffsetX / componentWidthPx).coerceIn(0f, 1f)
                            val targetMs = (targetFraction * durationMs).toLong()
                            onScrub(true, targetMs)
                        }
                    )
                }
        ) {
            componentWidthPx = size.width
            val width = size.width
            val height = size.height

            // Render amplitude bars: 2px bar width with 1.5px gap
            val barWidth = 2.5.dp.toPx()
            val barGap = 1.8.dp.toPx()
            val totalBarStride = barWidth + barGap
            val totalBars = (width / totalBarStride).toInt().coerceAtLeast(1)

            val safeAmplitudes = if (amplitudes.isEmpty()) {
                List(totalBars) { 0.4f }
            } else {
                amplitudes
            }

            for (i in 0 until totalBars) {
                val ampIndex = ((i.toFloat() / totalBars) * safeAmplitudes.size).toInt().coerceIn(0, safeAmplitudes.size - 1)
                val amp = safeAmplitudes[ampIndex].coerceIn(0.08f, 1.0f)
                val barHeight = (height * 0.85f * amp).coerceAtLeast(4.dp.toPx())

                val x = i * totalBarStride
                val y = (height - barHeight) / 2f
                val barFraction = (x / width).coerceIn(0f, 1f)

                val barColor = when {
                    barFraction <= playFraction -> playedColor
                    barFraction <= bufferFraction -> bufferedColor
                    else -> unplayedColor
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }

            // Draw Amber scrub thumb at active position
            val thumbX = (playFraction * width).coerceIn(0f, width)
            drawLine(
                color = thumbColor,
                start = Offset(thumbX, 2f),
                end = Offset(thumbX, height - 2f),
                strokeWidth = 2.5.dp.toPx()
            )
            drawCircle(
                color = thumbColor,
                radius = 5.dp.toPx(),
                center = Offset(thumbX, height / 2f)
            )
        }

        // Millisecond Precision Scrub Tooltip
        if (isScrubbing || isDragging) {
            val ms = displayPosition % 1000
            val totalSec = displayPosition / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            val formattedTime = String.format("%02d:%02d.%03d", min, sec, ms)

            val tooltipWidth = 84.dp
            val targetOffset = (playFraction * componentWidthPx) - (tooltipWidth.value * 1.5f)

            Box(
                modifier = Modifier
                    .offset { IntOffset(targetOffset.toInt().coerceAtLeast(8), -32) }
                    .clip(RoundedCornerShape(4.dp))
                    .background(AetherVoid)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formattedTime,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AwsAmber
                )
            }
        }
    }
}
