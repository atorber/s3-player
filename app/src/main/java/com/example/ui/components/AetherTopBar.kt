package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AetherSurfaceTier1
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
fun AetherTopBar(
    currentBucketName: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenMountDialog: () -> Unit,
    onOpenTelemetryInspector: () -> Unit,
    latencyMs: Int = 28,
    activeRegion: String = "us-east-1",
    modifier: Modifier = Modifier
) {
    var isSearchFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AetherVoid)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Brand Bar with Logo & Telemetry Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Aether Cube Vector Icon
                Image(
                    painter = painterResource(id = R.drawable.ic_aether_cube),
                    contentDescription = "Aether S3 Audio Player",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AETHER",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextHighContrast
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "// S3 AUDIO",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AwsAmber
                        )
                    }
                    Text(
                        text = "HIGH-PRECISION CLOUD STREAMING",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = TextLowContrast
                    )
                }
            }

            // Right header actions: Telemetry Inspector button & Mount Bucket button
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live Edge Telemetry Pill
                AetherStatusChip(
                    label = "$activeRegion • ${latencyMs}ms",
                    indicatorColor = StatusGreen,
                    borderColor = ElectricCyan.copy(alpha = 0.35f),
                    textColor = ElectricCyan,
                    modifier = Modifier.clickable(onClick = onOpenTelemetryInspector)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenTelemetryInspector,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AetherSurfaceTier1)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .testTag("open_telemetry_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Open Telemetry Inspector",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenMountDialog,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AwsAmber)
                        .testTag("mount_bucket_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Mount S3 Bucket",
                        tint = AetherVoid,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // S3 URI / Prefix Lookup Bar
        val searchBorder = if (isSearchFocused) BorderCyanGlow else BorderSubtle

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AetherVoid)
                .border(1.dp, searchBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Anchored s3:// prefix in JetBrains Mono muted slate
                Text(
                    text = "s3://",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )

                if (currentBucketName != null && currentBucketName != "ALL") {
                    Text(
                        text = "$currentBucketName/",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AwsAmber
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "filter prefix or track key...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextLowContrast
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isSearchFocused = it.isFocused }
                            .testTag("s3_uri_search_input"),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextHighContrast
                        ),
                        cursorBrush = SolidColor(ElectricCyan),
                        singleLine = true
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = TextLowContrast,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextLowContrast,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
