package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Speed
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
import com.example.data.model.S3Bucket
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
fun BucketsManagerScreen(
    buckets: List<S3Bucket>,
    selectedBucketName: String?,
    onSelectBucket: (String) -> Unit,
    onOpenMountDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .padding(horizontal = 16.dp)
            .testTag("buckets_manager_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CLOUD S3 BUCKET MOUNTS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )
                Text(
                    text = "ACTIVE S3 / R2 / MINIO REPOSITORIES",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TextLowContrast
                )
            }

            Button(
                onClick = onOpenMountDialog,
                colors = ButtonDefaults.buttonColors(containerColor = AwsAmber),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("mount_new_bucket_action_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = AetherVoid,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "MOUNT S3",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherVoid
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(buckets, key = { it.bucketName }) { bucket ->
                val isSelected = bucket.bucketName == selectedBucketName

                BucketItemCard(
                    bucket = bucket,
                    isSelected = isSelected,
                    onSelect = { onSelectBucket(bucket.bucketName) }
                )
            }
        }
    }
}

@Composable
private fun BucketItemCard(
    bucket: S3Bucket,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) ElectricCyan else BorderSubtle
    val bgColor = if (isSelected) AetherSurfaceTier2 else AetherSurfaceTier1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .padding(14.dp)
            .testTag("bucket_card_${bucket.bucketName}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AwsAmber.copy(alpha = 0.2f) else AetherSurfaceTier2)
                        .border(1.dp, if (isSelected) AwsAmber else BorderSubtle, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (isSelected) AwsAmber else TextLowContrast,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "s3://${bucket.bucketName}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ElectricCyan else TextHighContrast
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${bucket.provider} • ${bucket.endpoint}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TextLowContrast
                    )
                }
            }

            AetherStatusChip(
                label = "${bucket.latencyMs} ms",
                indicatorColor = StatusGreen,
                borderColor = BorderSubtle,
                textColor = TextHighContrast
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AetherStatusChip(
                    label = bucket.region,
                    borderColor = BorderSubtle,
                    textColor = TextMediumContrast
                )
                AetherStatusChip(
                    label = bucket.authType,
                    borderColor = BorderSubtle,
                    textColor = TextLowContrast
                )
            }

            Text(
                text = "${bucket.objectCount} Objects • ${bucket.storageSizeFormatted}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ElectricCyan
            )
        }
    }
}
