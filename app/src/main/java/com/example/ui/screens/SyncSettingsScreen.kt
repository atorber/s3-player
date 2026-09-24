package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudCircle
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SyncSettingsState
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.StatusRed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

@Composable
fun SyncSettingsScreen(
    settings: SyncSettingsState,
    onUpdatePollInterval: (Int) -> Unit,
    onSetEnqueueRule: (String) -> Unit,
    onTogglePublicAccess: () -> Unit,
    onToggleSubdirMonitoring: () -> Unit,
    onToggleSnsNotification: () -> Unit,
    onToggleFormatEnabled: (String) -> Unit,
    onRunPingTest: (endpoint: String, bucket: String) -> Unit,
    onClearCache: () -> Unit,
    onExportM3u8: () -> Unit,
    onSaveAndRestart: (
        bucketName: String,
        prefix: String,
        endpoint: String,
        region: String,
        accessKeyId: String,
        secretAccessKey: String,
        sessionToken: String,
        usePathStyle: Boolean,
        isTlsEncrypted: Boolean,
        isPublicAccess: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var bucketInput by remember { mutableStateOf(settings.currentBucketName) }
    var prefixInput by remember { mutableStateOf(settings.basePrefixPath) }
    var endpointInput by remember { mutableStateOf(settings.endpoint) }
    var regionInput by remember { mutableStateOf(settings.region) }
    var accessKeyInput by remember { mutableStateOf(settings.accessKeyId) }
    var secretKeyInput by remember { mutableStateOf(settings.secretAccessKey) }
    var sessionTokenInput by remember { mutableStateOf(settings.sessionToken) }
    var showSecretKey by remember { mutableStateOf(false) }
    var usePathStyle by remember { mutableStateOf(settings.usePathStyle) }
    var useSsl by remember { mutableStateOf(settings.isTlsEncrypted) }
    var isPublicAccess by remember { mutableStateOf(settings.isPublicAccess) }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Top Telemetry Diagnostic Pill & Heartbeat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(12.dp)
                .testTag("telemetry_heartbeat_card"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                        .alpha(pulseAlpha)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "S3 兼容协议源",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyanDim
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceContainerHigh)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = regionInput.ifBlank { "cn-north-3" },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AwsAmber
                            )
                        }
                    }
                    Text(
                        text = "${if (useSsl) "https://" else "http://"}${endpointInput.ifBlank { "xtrader.oss.cn-north-3.inspurcloudoss.com" }} · ${if (usePathStyle) "Path-Style" else "Virtual-Host"}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TextLowContrast,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerHigh)
                    .clickable { onRunPingTest(endpointInput, bucketInput) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("ping_test_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (settings.isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = ElectricCyan,
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ElectricCyanDim,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "连通测速",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyanDim
                    )
                }
            }
        }

        // SECTION 1: Target S3 Bucket & Protocol Configuration (不限制提供商，遵循 S3 协议规范)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = AwsAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "目标存储桶与目录 (S3 兼容协议)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHighest)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "通用 S3 规范",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyanDim
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ElectricCyanDim,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "不限制云厂商或自建服务：遵循标准 S3 协议规范，仅需填写符合 S3 协议的存储桶配置信息即可直连推流与监听。",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = TextMediumContrast,
                        lineHeight = 14.sp
                    )
                }

                // 1. S3 接入端点 (Endpoint / Host URL)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "S3 接入端点 (Endpoint / Host URL)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLowContrast
                        )
                        Text(
                            text = if (useSsl) "TLSv1.3 加密传输" else "HTTP 明文协议",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = if (useSsl) ElectricCyan else AwsAmber
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = AwsAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (useSsl) "https://" else "http://",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (useSsl) ElectricCyan else AwsAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = endpointInput,
                            onValueChange = { endpointInput = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextHighContrast
                            ),
                            cursorBrush = SolidColor(ElectricCyan),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("s3_endpoint_input")
                        )
                        if (endpointInput.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ElectricCyanDim,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Quick helper chips (Templates for quick typing, not restricting any providers)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val endpointPresets = listOf(
                            "xtrader.oss.cn-north-3.inspurcloudoss.com" to "浪潮云 Inspur OSS (当前)",
                            "s3.amazonaws.com" to "标准 AWS 端点",
                            "127.0.0.1:9000" to "本地 MinIO/Ceph",
                            "s3.us-west-2.amazonaws.com" to "美西端点",
                            "auto.r2.cloudflarestorage.com" to "R2 格式",
                            "oss-cn-hangzhou.aliyuncs.com" to "阿里云 OSS"
                        )
                        endpointPresets.forEach { (host, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .clickable {
                                        endpointInput = host
                                        if (host.contains("9000") || host.contains("127.0.0.1")) {
                                            usePathStyle = true
                                            useSsl = false
                                        }
                                    }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = host,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = TextMediumContrast
                                )
                            }
                        }
                    }
                }

                // 2. 存储桶名称 (Bucket Name)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "存储桶名称 (Bucket Name)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLowContrast
                        )
                        Text(
                            text = "RFC DNS 命名标准",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TextLowContrast
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "s3://",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AwsAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = bucketInput,
                            onValueChange = { bucketInput = it.trim() },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextHighContrast
                            ),
                            cursorBrush = SolidColor(ElectricCyan),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("s3_bucket_input")
                        )
                        if (bucketInput.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ElectricCyanDim,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // 3. 监听前缀根子目录 (Prefix Path)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "监听前缀 / 根子目录 (Prefix)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLowContrast
                        )
                        Text(
                            text = "默认根目录 / (整桶监听)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = AwsAmber
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AwsAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = prefixInput,
                            onValueChange = { prefixInput = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextHighContrast
                            ),
                            cursorBrush = SolidColor(ElectricCyan),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("s3_prefix_input"),
                            decorationBox = { innerTextField ->
                                if (prefixInput.isBlank()) {
                                    Text(
                                        text = "/ (根目录 - 整桶监听)",
                                        color = TextLowContrast,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceContainerHigh)
                                .clickable { prefixInput = "/" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "根目录 /",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }

                    // Composite S3 Virtual URI Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        val fullUri = "s3://${bucketInput.ifBlank { "<bucket>" }}${if (prefixInput.startsWith("/")) prefixInput else if (prefixInput.isBlank()) "/" else "/$prefixInput"}"
                        Text(
                            text = "当前监听 S3 URI: $fullUri",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = AwsAmber
                        )
                    }
                }

                // 4. 所属区域 (Region)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "所属区域代码 (Region)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLowContrast
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = regionInput,
                            onValueChange = { regionInput = it.trim() },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextHighContrast
                            ),
                            cursorBrush = SolidColor(ElectricCyan),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("s3_region_input")
                        )

                        // Quick region chips
                        val regions = listOf("us-east-1", "auto", "ap-northeast-1", "eu-west-1")
                        regions.forEach { reg ->
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (regionInput == reg) AwsAmber.copy(alpha = 0.25f) else SurfaceContainerHigh)
                                    .clickable { regionInput = reg }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = reg,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = if (regionInput == reg) AwsAmber else TextLowContrast
                                )
                            }
                        }
                    }
                }

                // 5. S3 认证授权与访问策略 (Credentials / Authentication)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (isPublicAccess) ElectricCyanDim else AwsAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "匿名 / 公开访问 (免密钥)",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHighContrast
                                )
                            }
                            Text(
                                text = "适用于公开广播的音频存储桶，跳过 SigV4 认证标头",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 9.sp,
                                color = TextLowContrast
                            )
                        }

                        Switch(
                            checked = isPublicAccess,
                            onCheckedChange = { isPublicAccess = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwsAmber,
                                checkedTrackColor = AwsAmber.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextLowContrast,
                                uncheckedTrackColor = SurfaceContainerHighest
                            ),
                            modifier = Modifier.size(36.dp, 24.dp)
                        )
                    }

                    if (!isPublicAccess) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(SurfaceContainerHighest.copy(alpha = 0.5f))
                        )

                        // Access Key ID
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Access Key ID (AK)",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLowContrast
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerLowest)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextLowContrast,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BasicTextField(
                                    value = accessKeyInput,
                                    onValueChange = { accessKeyInput = it.trim() },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextHighContrast
                                    ),
                                    cursorBrush = SolidColor(ElectricCyan),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("s3_ak_input")
                                )
                            }
                        }

                        // Secret Access Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Secret Access Key (SK)",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLowContrast
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerLowest)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextLowContrast,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BasicTextField(
                                    value = secretKeyInput,
                                    onValueChange = { secretKeyInput = it.trim() },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextHighContrast
                                    ),
                                    visualTransformation = if (showSecretKey) VisualTransformation.None else PasswordVisualTransformation(),
                                    cursorBrush = SolidColor(ElectricCyan),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("s3_sk_input")
                                )
                                IconButton(
                                    onClick = { showSecretKey = !showSecretKey },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showSecretKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextLowContrast,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Optional Session Token
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Session Token (可选临时安全凭据)",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 9.sp,
                                color = TextLowContrast
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerLowest)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = sessionTokenInput,
                                    onValueChange = { sessionTokenInput = it.trim() },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = TextHighContrast
                                    ),
                                    cursorBrush = SolidColor(ElectricCyan),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 6. S3 协议规范与传输配置 (S3 Protocol Spec & Addressing Options)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ElectricCyanDim,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "S3 协议寻址与安全传输",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHighContrast
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceContainerHighest)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AWS SigV4",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyanDim
                            )
                        }
                    }

                    // Path Style Addressing Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "强制路径样式寻址 (Path-Style)",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = TextHighContrast
                            )
                            Text(
                                text = if (usePathStyle) "格式: https://endpoint/bucket/... (MinIO/Ceph/内网服务)" else "格式: https://bucket.endpoint/... (标准云端 S3)",
                                fontFamily = FontFamily.Monospace,
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
                                uncheckedTrackColor = SurfaceContainerHighest
                            ),
                            modifier = Modifier.size(36.dp, 24.dp)
                        )
                    }

                    // TLS / SSL Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "启用 SSL / TLS 加密传输",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = TextHighContrast
                            )
                            Text(
                                text = if (useSsl) "使用 HTTPS 443 端口与证书校验" else "使用 HTTP 明文传输 (通常用于本地内网测试)",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 9.sp,
                                color = if (useSsl) ElectricCyan else AwsAmber
                            )
                        }

                        Switch(
                            checked = useSsl,
                            onCheckedChange = { useSsl = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricCyan,
                                checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextLowContrast,
                                uncheckedTrackColor = SurfaceContainerHighest
                            ),
                            modifier = Modifier.size(36.dp, 24.dp)
                        )
                    }
                }
            }
        }

        // SECTION 2: Real-Time Directory Polling & Auto-Discovery Engine
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = AwsAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "自动发现引擎",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AwsAmber)
                            .alpha(pulseAlpha)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "监听已激活",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AwsAmber
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Master Monitoring Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHigh.copy(alpha = 0.6f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AwsAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = AwsAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "子目录监听",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHighContrast
                            )
                            Text(
                                text = "自动识别新上传的 S3 PutObject 音频文件",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 9.sp,
                                color = TextLowContrast
                            )
                        }
                    }

                    Switch(
                        checked = settings.isSubdirMonitoring,
                        onCheckedChange = { onToggleSubdirMonitoring() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AwsAmber,
                            checkedTrackColor = AwsAmber.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextLowContrast,
                            uncheckedTrackColor = SurfaceContainerHighest
                        ),
                        modifier = Modifier.size(36.dp, 24.dp)
                    )
                }

                // Frequency Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "目录轮询间隔",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLowContrast
                        )

                        val modeDesc = when {
                            settings.pollIntervalSec <= 3 -> " (极速模式)"
                            settings.pollIntervalSec <= 10 -> " (快速模式)"
                            settings.pollIntervalSec <= 30 -> " (平衡模式)"
                            else -> " (省电模式)"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceContainerHighest)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${settings.pollIntervalSec}秒$modeDesc",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyanDim
                            )
                        }
                    }

                    Slider(
                        value = settings.pollIntervalSec.toFloat(),
                        onValueChange = { onUpdatePollInterval(it.toInt()) },
                        valueRange = 1f..60f,
                        colors = SliderDefaults.colors(
                            thumbColor = AwsAmber,
                            activeTrackColor = AwsAmber,
                            inactiveTrackColor = SurfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "1秒 (瞬时)", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextLowContrast)
                        Text(text = "15秒", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextLowContrast)
                        Text(text = "30秒", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextLowContrast)
                        Text(text = "60秒 (省电)", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextLowContrast)
                    }
                }

                // Webhook / SNS Hook
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ElectricCyanDim,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "S3 事件通知 / SNS",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextHighContrast
                            )
                        }
                        Text(
                            text = "基于 WebSocket 边缘节点的零延迟推送捕获",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 9.sp,
                            color = TextLowContrast
                        )
                    }

                    Switch(
                        checked = settings.isSnsNotification,
                        onCheckedChange = { onToggleSnsNotification() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextLowContrast,
                            uncheckedTrackColor = SurfaceContainerHighest
                        ),
                        modifier = Modifier.size(36.dp, 24.dp)
                    )
                }

                // Auto-Enqueue Radio Options
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "新音频入队行为",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLowContrast
                    )

                    val options = listOf(
                        "append" to ("追加至队列末尾" to "保持当前播放顺序不中断"),
                        "upnext" to ("插队至下一首播放 (立即)" to "优先在当前曲目结束后立即播放新上传的音频分轨"),
                        "ask" to ("轻量弹窗提醒确认" to "加入队列前在顶部弹出横幅提示操作")
                    )

                    options.forEach { (ruleKey, info) ->
                        val isSelected = settings.enqueueRule == ruleKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceContainerHigh else SurfaceContainerLow)
                                .clickable { onSetEnqueueRule(ruleKey) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSetEnqueueRule(ruleKey) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AwsAmber,
                                    unselectedColor = TextLowContrast
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = info.first,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AwsAmber else TextHighContrast
                                    )
                                    if (ruleKey == "upnext") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Bolt, null, tint = AwsAmber, modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(
                                    text = info.second,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 9.sp,
                                    color = TextLowContrast
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECTION 3: Audio Extension Recognition Filters
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "音频编码与扩展名过滤",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )
                Text(
                    text = "${settings.enabledFormats.size}/7 已启用",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = ElectricCyanDim
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "符合选中扩展名的文件将通过 HTTP Range 快速解析流式元数据。",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 10.sp,
                    color = TextLowContrast
                )

                // Chips Grid
                val formats = listOf(
                    ".flac" to "24-bit",
                    ".wav" to "PCM",
                    ".mp3" to "VBR",
                    ".m4a" to "ALAC",
                    ".aac" to "",
                    ".ogg" to "Vorbis",
                    ".opus" to "空间音频"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    formats.forEach { (fmt, tag) ->
                        val isChecked = settings.enabledFormats.contains(fmt)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerHighest)
                                .clickable { onToggleFormatEnabled(fmt) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleFormatEnabled(fmt) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AwsAmber,
                                        uncheckedColor = TextLowContrast
                                    ),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = fmt,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) AwsAmber else TextLowContrast
                                )
                                if (tag.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tag,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 8.sp,
                                        color = TextLowContrast
                                    )
                                }
                            }
                        }
                    }
                }

                // HTTP Range Streaming Limit Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stream,
                            contentDescription = null,
                            tint = ElectricCyanDim,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "单文件大小限制",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHighContrast
                            )
                            Text(
                                text = "动态 HTTP 分块流式切片 (不设限)",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 9.sp,
                                color = TextLowContrast
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = ElectricCyanDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // SECTION 4: Storage & Buffer Management
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本地缓存与边缘缓冲",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )
                Text(
                    text = "1.2 GB / 5.0 GB",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = ElectricCyan
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "本地离线缓存分配",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLowContrast
                    )
                    Text(
                        text = "已占用 24%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AwsAmber
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceContainerLowest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.24f)
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ElectricCyan, AwsAmber)
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "已缓存: 1.2 GB (14 首音频)", fontFamily = FontFamily.SansSerif, fontSize = 9.sp, color = TextLowContrast)
                    Text(text = "上限: 5.0 GB", fontFamily = FontFamily.SansSerif, fontSize = 9.sp, color = TextLowContrast)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onClearCache,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteSweep, null, tint = StatusRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除缓存", fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                        }
                    }

                    Button(
                        onClick = onExportM3u8,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, null, tint = ElectricCyanDim, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出 M3U8 列表", fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyanDim)
                        }
                    }
                }
            }
        }

        // SECTION 5: Primary Sticky Action Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    onSaveAndRestart(
                        bucketInput,
                        prefixInput,
                        endpointInput,
                        regionInput,
                        accessKeyInput,
                        secretKeyInput,
                        sessionTokenInput,
                        usePathStyle,
                        useSsl,
                        isPublicAccess
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AwsAmber),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(16.dp, RoundedCornerShape(12.dp), spotColor = AwsAmber.copy(alpha = 0.5f))
                    .testTag("save_restart_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (settings.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AetherVoid,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PublishedWithChanges,
                            contentDescription = null,
                            tint = AetherVoid,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "保存并重启实时监听",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AetherVoid
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                )
                Text(
                    text = "后台监听工作线程将热重载，音频播放不会中断",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 10.sp,
                    color = TextLowContrast
                )
            }
        }

        Spacer(modifier = Modifier.height(90.dp)) // clearance for dock & nav
    }
}
