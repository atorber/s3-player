package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.audio.EqPreset
import com.example.audio.PlayerState
import com.example.ui.components.AetherStatusChip
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
fun TelemetryInspectorScreen(
    playerState: PlayerState,
    onClose: () -> Unit,
    onSetEqPreset: (EqPreset) -> Unit,
    onSetBand: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack
    val telemetry = playerState.telemetry

    val eqFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("telemetry_inspector_screen")
    ) {
        // Modal Header
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
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "S3 遥测与均衡器引擎",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                    Text(
                        text = "实时边缘传输指标与音频分析",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = TextLowContrast
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AetherSurfaceTier1)
                    .testTag("telemetry_close_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = TextHighContrast,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Network Telemetry Cards
        Text(
            text = "实时 S3 边缘传输遥测 //",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AwsAmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryMetricCard(
                title = "往返延迟 (RTT)",
                value = "${telemetry.latencyMs} ms",
                subtext = "TLS 1.3 // 边缘节点",
                accentColor = StatusGreen,
                modifier = Modifier.weight(1f)
            )
            TelemetryMetricCard(
                title = "实时传输吞吐率",
                value = String.format("%.2f MB/s", telemetry.throughputMBs),
                subtext = "分块实时传输",
                accentColor = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
            TelemetryMetricCard(
                title = "数据包抖动",
                value = String.format("%.2f ms", telemetry.jitterMs),
                subtext = "丢包率 0.00%",
                accentColor = AwsAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Raw HTTP S3 Headers Table
        Text(
            text = "S3 鉴权元数据响应头 //",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AetherSurfaceTier1)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            HeaderRow("x-amz-request-id", "7H9A2Q0K1P4R8T9S")
            HeaderRow("ETag (MD5 Checksum)", track?.etag ?: "\"e4d909c29af5731b8d234a91f421c002\"")
            HeaderRow("Content-Type", "audio/${track?.format?.lowercase() ?: "flac"}")
            HeaderRow("Content-Length", "${track?.sizeBytes ?: 48291040} 字节 (${track?.sizeFormatted ?: "48.2 MB"})")
            HeaderRow("x-amz-server-side-encryption", "AES256")
            HeaderRow("x-amz-storage-class", track?.storageClass ?: "EXPRESS_ONEZONE")
            HeaderRow("Accept-Ranges", "bytes 0-48291039")
            HeaderRow("Cache-Control", "public, max-age=31536000, immutable")
            HeaderRow("Last-Modified", track?.lastModified ?: "2026-09-18 14:22:04 UTC")
            HeaderRow("x-amz-version-id", "3/L4kqtJlcpXroDTDmJ+rmSpXd3dIbrHY")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5-Band Equalizer & Audio Engine
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "5段均衡器与分轨调音 //",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AwsAmber
            )
            Text(
                text = "当前预设: ${playerState.eqPreset.label}",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                color = ElectricCyan
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Equalizer Presets Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EqPreset.values().forEach { preset ->
                val isSelected = playerState.eqPreset == preset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AwsAmber else AetherSurfaceTier2)
                        .border(1.dp, if (isSelected) AwsAmber else BorderSubtle, RoundedCornerShape(6.dp))
                        .clickable { onSetEqPreset(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AetherVoid else TextHighContrast
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sliders for 5 bands
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AetherSurfaceTier1)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            eqFrequencies.forEachIndexed { index, freq ->
                val currentDb = playerState.eqCustomBands.getOrElse(index) { 0f }
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = freq,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = String.format("%+.1f dB", currentDb),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentDb > 0) AwsAmber else if (currentDb < 0) ElectricCyan else TextMediumContrast
                        )
                    }

                    Slider(
                        value = currentDb,
                        onValueChange = { onSetBand(index, it) },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(
                            thumbColor = AwsAmber,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = AetherSurfaceTier2
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AetherSurfaceTier1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextLowContrast
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtext,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = TextMediumContrast
        )
    }
}

@Composable
private fun HeaderRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TextLowContrast,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TextHighContrast,
            modifier = Modifier.weight(1f)
        )
    }
}
