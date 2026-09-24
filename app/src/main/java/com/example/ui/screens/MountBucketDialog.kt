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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun MountBucketDialog(
    onDismiss: () -> Unit,
    onMount: (
        name: String,
        endpoint: String,
        region: String,
        accessKey: String?,
        secretKey: String?,
        usePathStyle: Boolean,
        useSsl: Boolean
    ) -> Unit
) {
    var bucketName by remember { mutableStateOf("xtrader") }
    var endpoint by remember { mutableStateOf("xtrader.oss.cn-north-3.inspurcloudoss.com") }
    var region by remember { mutableStateOf("cn-north-3") }
    var accessKey by remember { mutableStateOf("YjNmNjhkOWMtODE5My00MjM5LTgxZGYtNWQ3MzFlNDA4NTlm") }
    var secretKey by remember { mutableStateOf("NGE4NjQzZmItNGViNy00NDY0LWFjNjYtYjZiZDA1MTdmOGJj") }
    var showSecretKey by remember { mutableStateOf(false) }
    var usePathStyle by remember { mutableStateOf(false) }
    var useSsl by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AetherSurfaceTier1)
                .border(1.dp, BorderCyanGlow, RoundedCornerShape(12.dp))
                .padding(18.dp)
                .verticalScroll(rememberScrollState())
                .testTag("mount_bucket_dialog")
        ) {
            // Dialog Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "挂载 S3 协议存储",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "通用 S3 协议适配 • 支持云端与自建私有存储",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            color = ElectricCyan
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = TextLowContrast,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Protocol compatibility banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AetherSurfaceTier2)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AwsAmber,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "基于标准 S3 REST 协议：不绑定特定云厂商，任意兼容 S3 API 的公有云服务或自建集群（如 MinIO、Ceph、Rust S3、LocalStack 等）均可直接接入音频流播放。",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = TextMediumContrast,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. 存储桶名称
            Text(
                text = "存储桶名称 (BUCKET NAME) *",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = bucketName,
                onValueChange = { bucketName = it },
                placeholder = {
                    Text(
                        "如 audio-master-stems",
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

            // 2. S3 协议终端节点 Endpoint
            Text(
                text = "S3 终端节点 (ENDPOINT) *",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = endpoint,
                onValueChange = { endpoint = it },
                placeholder = {
                    Text(
                        "如 192.168.1.100:9000 或 s3.us-east-1.amazonaws.com",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextLowContrast
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mount_endpoint_input"),
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

            // 3. 区域 Region
            Text(
                text = "区域 (REGION) [可选，自建填 auto 或默认]",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMediumContrast
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                placeholder = {
                    Text(
                        "us-east-1 或 auto",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextLowContrast
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mount_region_input"),
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

            // 4. 访问凭证 Access Key & Secret Key (可选)
            Text(
                text = "S3 访问凭证 (选填，公开读桶可留空)",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMediumContrast
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = accessKey,
                onValueChange = { accessKey = it },
                placeholder = {
                    Text(
                        "Access Key ID (AK)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextLowContrast
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextHighContrast
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = AetherVoid,
                    unfocusedContainerColor = AetherVoid
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = secretKey,
                onValueChange = { secretKey = it },
                placeholder = {
                    Text(
                        "Secret Access Key (SK)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextLowContrast
                    )
                },
                singleLine = true,
                visualTransformation = if (showSecretKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showSecretKey = !showSecretKey }) {
                        Icon(
                            imageVector = if (showSecretKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "切换密码可见",
                            tint = TextLowContrast,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextHighContrast
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = AetherVoid,
                    unfocusedContainerColor = AetherVoid
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. 协议与寻址开关 (Path-Style & SSL)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AetherSurfaceTier2)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Path-Style switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "路径样式访问 (Path-Style)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = if (usePathStyle) "开启: http://endpoint/bucket (自建MinIO/Ceph推荐)" else "关闭: http://bucket.endpoint (公有云默认)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 9.sp,
                            color = if (usePathStyle) AwsAmber else TextLowContrast
                        )
                    }

                    Switch(
                        checked = usePathStyle,
                        onCheckedChange = { usePathStyle = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AwsAmber,
                            checkedTrackColor = AwsAmber.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextLowContrast,
                            uncheckedTrackColor = AetherVoid
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // SSL / HTTPS switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "SSL / HTTPS 加密传输",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = if (useSsl) "使用 https:// 安全串流" else "使用 http:// (用于局域网或私有内网测试)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 9.sp,
                            color = if (useSsl) StatusGreen else TextLowContrast
                        )
                    }

                    Switch(
                        checked = useSsl,
                        onCheckedChange = { useSsl = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextLowContrast,
                            uncheckedTrackColor = AetherVoid
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Confirm Mount Button
            Button(
                onClick = {
                    if (bucketName.isNotBlank() && endpoint.isNotBlank()) {
                        onMount(
                            bucketName.trim(),
                            endpoint.trim(),
                            region.trim().ifBlank { "us-east-1" },
                            accessKey.trim().takeIf { it.isNotBlank() },
                            secretKey.trim().takeIf { it.isNotBlank() },
                            usePathStyle,
                            useSsl
                        )
                        onDismiss()
                    }
                },
                enabled = bucketName.isNotBlank() && endpoint.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AwsAmber),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("confirm_mount_btn")
            ) {
                Text(
                    text = "确认接入 S3 存储桶",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherVoid
                )
            }
        }
    }
}
