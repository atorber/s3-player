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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
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
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
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
            .padding(18.dp)
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
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "专业音频均衡器与流媒体信息",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                    Text(
                        text = "硬件 5 段均衡器调音与真实连接指标",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextLowContrast
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AetherSurfaceTier1)
                    .testTag("telemetry_close_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = TextHighContrast,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 1. 5-Band Equalizer Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "5 段硬件均衡器",
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AwsAmber
            )
            Text(
                text = "当前预设: ${playerState.eqPreset.label}",
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElectricCyan
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Equalizer Presets Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EqPreset.values().forEach { preset ->
                val isSelected = playerState.eqPreset == preset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AwsAmber else AetherSurfaceTier2)
                        .border(1.dp, if (isSelected) AwsAmber else BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onSetEqPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = preset.label,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
                .clip(RoundedCornerShape(12.dp))
                .background(AetherSurfaceTier1)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%+.1f dB", currentDb),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
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

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Real Cloud Audio Attributes & Connection Info
        Text(
            text = "云端流媒体真实属性",
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AetherSurfaceTier1)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow("音频名称", track?.title ?: "未加载")
            InfoRow("S3 存储桶", track?.bucketName ?: telemetry.bucketName.ifBlank { "默认存储桶" })
            InfoRow("网络延迟 (RTT)", "${telemetry.latencyMs} ms")
            InfoRow("音频格式", track?.format?.uppercase() ?: "FLAC")
            InfoRow("音频采样率", track?.sampleRate ?: "44.1 kHz")
            InfoRow("声 道", track?.channels ?: "Stereo 双声道")
            InfoRow("文件大小", track?.sizeFormatted ?: "--")
            InfoRow("存储类型", track?.storageClass ?: "STANDARD")
            if (!track?.etag.isNullOrBlank()) {
                InfoRow("ETag 校验码", track?.etag ?: "")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            color = TextLowContrast,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextHighContrast,
            modifier = Modifier.weight(1f)
        )
    }
}
