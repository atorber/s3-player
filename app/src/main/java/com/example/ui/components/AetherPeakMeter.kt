package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun AetherPeakMeter(
    leftDb: Float,
    rightDb: Float,
    modifier: Modifier = Modifier
) {
    // Map -60dB .. 0dB to fraction 0f .. 1f
    val animatedLeft by animateFloatAsState(
        targetValue = ((leftDb + 60f) / 60f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 60),
        label = "peakL"
    )
    val animatedRight by animateFloatAsState(
        targetValue = ((rightDb + 60f) / 60f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 60),
        label = "peakR"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
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
            Text(
                text = "立体声峰值电平 // L/R",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = TextMediumContrast
            )
            Text(
                text = String.format("L: %.1f dB | R: %.1f dB", leftDb, rightDb),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = ElectricCyan
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Left Channel Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "L",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextLowContrast,
                modifier = Modifier.width(14.dp)
            )
            MeterBar(fraction = animatedLeft)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Right Channel Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "R",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextLowContrast,
                modifier = Modifier.width(14.dp)
            )
            MeterBar(fraction = animatedRight)
        }
    }
}

@Composable
private fun MeterBar(fraction: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(AetherSurfaceTier2)
    ) {
        val totalWidth = size.width
        val barWidth = totalWidth * fraction

        val gradient = Brush.horizontalGradient(
            colors = listOf(
                StatusGreen,
                ElectricCyan,
                AwsAmber,
                StatusRed
            )
        )

        drawRect(
            brush = gradient,
            topLeft = Offset.Zero,
            size = Size(barWidth, size.height)
        )

        // Draw fine 3dB segment dividers
        val segments = 20
        for (i in 1 until segments) {
            val segX = (totalWidth / segments) * i
            drawLine(
                color = Color.Black.copy(alpha = 0.4f),
                start = Offset(segX, 0f),
                end = Offset(segX, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
