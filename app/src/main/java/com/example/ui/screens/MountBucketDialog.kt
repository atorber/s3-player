package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AetherSurfaceTier1
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun MountBucketDialog(
    onDismiss: () -> Unit,
    onMount: (name: String, region: String, provider: String, endpoint: String) -> Unit
) {
    var bucketName by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("us-east-1") }
    var selectedProvider by remember { mutableStateOf("AWS S3") }
    var endpoint by remember { mutableStateOf("s3.us-east-1.amazonaws.com") }

    val regions = listOf("us-east-1", "us-west-2", "eu-west-1", "ap-northeast-1")
    val providers = listOf("AWS S3", "Cloudflare R2", "MinIO", "Wasabi")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AetherSurfaceTier1)
                .border(1.dp, BorderCyanGlow, RoundedCornerShape(12.dp))
                .padding(18.dp)
                .testTag("mount_bucket_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MOUNT S3 BUCKET",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextLowContrast,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "BUCKET NAME //",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMediumContrast
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = bucketName,
                onValueChange = { bucketName = it },
                placeholder = {
                    Text(
                        "my-audio-stems-2026",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextLowContrast
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mount_bucket_name_input"),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextHighContrast
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = AetherVoid,
                    unfocusedContainerColor = AetherVoid
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cloud Provider selection
            Text(
                text = "CLOUD S3 PROVIDER //",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMediumContrast
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                providers.forEach { prov ->
                    val isSel = selectedProvider == prov
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) AwsAmber.copy(alpha = 0.2f) else AetherSurfaceTier2)
                            .border(1.dp, if (isSel) AwsAmber else BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable {
                                selectedProvider = prov
                                endpoint = when (prov) {
                                    "AWS S3" -> "s3.$selectedRegion.amazonaws.com"
                                    "Cloudflare R2" -> "r2.cloudflarestorage.com"
                                    "MinIO" -> "minio.internal:9000"
                                    "Wasabi" -> "s3.wasabisys.com"
                                    else -> "s3.amazonaws.com"
                                }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prov,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) AwsAmber else TextLowContrast
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Region selection
            Text(
                text = "AWS CLOUD REGION //",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMediumContrast
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                regions.forEach { reg ->
                    val isSel = selectedRegion == reg
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) ElectricCyan.copy(alpha = 0.2f) else AetherSurfaceTier2)
                            .border(1.dp, if (isSel) ElectricCyan else BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable {
                                selectedRegion = reg
                                if (selectedProvider == "AWS S3") {
                                    endpoint = "s3.$reg.amazonaws.com"
                                }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reg,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) ElectricCyan else TextLowContrast
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (bucketName.isNotBlank()) {
                        onMount(bucketName.trim(), selectedRegion, selectedProvider, endpoint)
                        onDismiss()
                    }
                },
                enabled = bucketName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AwsAmber),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("confirm_mount_btn")
            ) {
                Text(
                    text = "CONFIRM S3 MOUNT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherVoid
                )
            }
        }
    }
}
