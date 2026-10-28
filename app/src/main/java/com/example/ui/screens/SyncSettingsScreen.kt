package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SyncSettingsState
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
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
    onRunPingTest: (
        endpoint: String,
        bucket: String,
        region: String,
        accessKeyId: String,
        secretAccessKey: String,
        sessionToken: String,
        usePathStyle: Boolean,
        isTlsEncrypted: Boolean,
        isPublicAccess: Boolean
    ) -> Unit,
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
    var prefixInput by remember { mutableStateOf(settings.basePrefixPath.removePrefix("/")) }
    var endpointInput by remember { mutableStateOf(settings.endpoint) }
    var regionInput by remember { mutableStateOf(settings.region) }
    var accessKeyInput by remember { mutableStateOf(settings.accessKeyId) }
    var secretKeyInput by remember { mutableStateOf(settings.secretAccessKey) }
    var sessionTokenInput by remember { mutableStateOf(settings.sessionToken) }
    var showSecretKey by remember { mutableStateOf(false) }
    var usePathStyle by remember { mutableStateOf(settings.usePathStyle) }
    var useSsl by remember { mutableStateOf(settings.isTlsEncrypted) }
    var isPublicAccess by remember { mutableStateOf(settings.isPublicAccess) }

    var isAdvancedExpanded by remember { mutableStateOf(false) }
    var isFormatsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        bucketInput = settings.currentBucketName
        prefixInput = settings.basePrefixPath.removePrefix("/")
        endpointInput = settings.endpoint
        regionInput = settings.region
        accessKeyInput = settings.accessKeyId
        secretKeyInput = settings.secretAccessKey
        sessionTokenInput = settings.sessionToken
        usePathStyle = settings.usePathStyle
        useSsl = settings.isTlsEncrypted
        isPublicAccess = settings.isPublicAccess
    }

    val normalizedPrefix = if (prefixInput.trim().isBlank()) "/" else "/${prefixInput.trim().removePrefix("/")}"
    val isDirty = bucketInput.trim() != settings.currentBucketName ||
            normalizedPrefix != (if (settings.basePrefixPath.startsWith("/")) settings.basePrefixPath else "/${settings.basePrefixPath}") ||
            endpointInput.trim() != settings.endpoint ||
            regionInput.trim() != settings.region ||
            accessKeyInput.trim() != settings.accessKeyId ||
            secretKeyInput.trim() != settings.secretAccessKey ||
            sessionTokenInput.trim() != settings.sessionToken ||
            usePathStyle != settings.usePathStyle ||
            useSsl != settings.isTlsEncrypted ||
            isPublicAccess != settings.isPublicAccess

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // =========================================================================
        // 1. S3 连接核心配置卡片 (一屏紧凑布局)
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainer)
                .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Header: Title + Auth Mode Quick Pill
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "S3 存储连接",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                }

                // Segmented Auth Mode Toggle: [密钥认证] vs [公开免密]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLowest)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val authModes = listOf(false to "AK/SK 密钥", true to "公开免密")
                    authModes.forEach { (pubMode, label) ->
                        val active = isPublicAccess == pubMode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) SurfaceContainerHigh else Color.Transparent)
                                .clickable { isPublicAccess = pubMode }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (pubMode) Icons.Default.LockOpen else Icons.Default.Key,
                                    contentDescription = null,
                                    tint = if (active) AwsAmber else TextLowContrast,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 10.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) AwsAmber else TextLowContrast
                                )
                            }
                        }
                    }
                }
            }

            // Input 1: Endpoint (with clickable protocol toggle badge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "接入端点 (Endpoint)",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 10.sp,
                    color = TextLowContrast
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLowest)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clickable HTTPS / HTTP pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (useSsl) ElectricCyan.copy(alpha = 0.15f) else AwsAmber.copy(alpha = 0.15f))
                            .clickable { useSsl = !useSsl }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (useSsl) "https://" else "http://",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (useSsl) ElectricCyan else AwsAmber
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = endpointInput,
                        onValueChange = {
                            endpointInput = it.trim().removePrefix("https://").removePrefix("http://")
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextHighContrast
                        ),
                        cursorBrush = SolidColor(ElectricCyan),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("s3_endpoint_input"),
                        decorationBox = { inner ->
                            if (endpointInput.isEmpty()) {
                                Text(
                                    text = "如 s3.amazonaws.com",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextLowContrast
                                )
                            }
                            inner()
                        }
                    )
                    if (endpointInput.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "清空",
                            tint = TextLowContrast,
                            modifier = Modifier
                                .size(15.dp)
                                .clickable { endpointInput = "" }
                        )
                    }
                }
            }

            // Input 2: Bucket Name & Prefix Path (Side-by-side in one compact row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bucket Name
                Column(
                    modifier = Modifier.weight(0.46f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "存储桶 (Bucket)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = TextLowContrast
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "s3://",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AwsAmber
                        )
                        Spacer(modifier = Modifier.width(3.dp))
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
                                .testTag("s3_bucket_input"),
                            decorationBox = { inner ->
                                if (bucketInput.isEmpty()) {
                                    Text(
                                        text = "bucket",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextLowContrast
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }

                // Prefix Path
                Column(
                    modifier = Modifier.weight(0.54f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "默认目录前缀 (Prefix)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = TextLowContrast
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AwsAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "/",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMediumContrast
                        )
                        BasicTextField(
                            value = prefixInput,
                            onValueChange = { prefixInput = it.removePrefix("/") },
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
                            decorationBox = { inner ->
                                if (prefixInput.isEmpty()) {
                                    Text(
                                        text = "根目录 (整桶)",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 11.sp,
                                        color = TextLowContrast
                                    )
                                }
                                inner()
                            }
                        )
                        if (prefixInput.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "重置为根目录",
                                tint = TextLowContrast,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { prefixInput = "" }
                            )
                        }
                    }
                }
            }

            // Input 3: AK / SK Credentials (Visible only when !isPublicAccess)
            AnimatedVisibility(
                visible = !isPublicAccess,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Access Key ID
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AK",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AwsAmber,
                            modifier = Modifier.width(24.dp)
                        )
                        BasicTextField(
                            value = accessKeyInput,
                            onValueChange = { accessKeyInput = it.trim() },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextHighContrast
                            ),
                            cursorBrush = SolidColor(ElectricCyan),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("s3_ak_input"),
                            decorationBox = { inner ->
                                if (accessKeyInput.isEmpty()) {
                                    Text(
                                        text = "Access Key ID",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextLowContrast
                                    )
                                }
                                inner()
                            }
                        )
                        if (accessKeyInput.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "清空 AK",
                                tint = TextLowContrast,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { accessKeyInput = "" }
                            )
                        }
                    }

                    // Secret Access Key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SK",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyanDim,
                            modifier = Modifier.width(24.dp)
                        )
                        BasicTextField(
                            value = secretKeyInput,
                            onValueChange = { secretKeyInput = it.trim() },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextHighContrast
                            ),
                            visualTransformation = if (showSecretKey) VisualTransformation.None else PasswordVisualTransformation(),
                            cursorBrush = SolidColor(ElectricCyan),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("s3_sk_input"),
                            decorationBox = { inner ->
                                if (secretKeyInput.isEmpty()) {
                                    Text(
                                        text = "Secret Access Key",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextLowContrast
                                    )
                                }
                                inner()
                            }
                        )
                        IconButton(
                            onClick = { showSecretKey = !showSecretKey },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (showSecretKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "切换密钥可见",
                                tint = TextLowContrast,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Collapsible Advanced Protocol Settings (Region, Path-Style, SSL, Session Token)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAdvancedExpanded = !isAdvancedExpanded }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = ElectricCyanDim,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "高级协议项",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMediumContrast
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${regionInput.ifBlank { "cn-north-3" }} · ${if (usePathStyle) "Path-Style" else "Virtual-Host"} · ${if (useSsl) "HTTPS" else "HTTP"}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextLowContrast,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextLowContrast,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isAdvancedExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Region Input + Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerLowest)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Region",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextLowContrast,
                                modifier = Modifier.width(46.dp)
                            )
                            BasicTextField(
                                value = regionInput,
                                onValueChange = { regionInput = it.trim() },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextHighContrast
                                ),
                                cursorBrush = SolidColor(ElectricCyan),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("s3_region_input")
                            )
                            listOf("cn-north-3", "us-east-1", "auto").forEach { reg ->
                                val isRegSelected = regionInput == reg
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isRegSelected) AwsAmber.copy(alpha = 0.22f) else SurfaceContainerHigh)
                                        .clickable { regionInput = reg }
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = reg,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = if (isRegSelected) AwsAmber else TextLowContrast
                                    )
                                }
                            }
                        }

                        // Addressing & SSL toggles in a single compact row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Path-Style Toggle Chip
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (usePathStyle) AwsAmber.copy(alpha = 0.15f) else SurfaceContainerLowest)
                                    .border(
                                        1.dp,
                                        if (usePathStyle) AwsAmber.copy(alpha = 0.5f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { usePathStyle = !usePathStyle }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Path-Style 寻址",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (usePathStyle) AwsAmber else TextMediumContrast
                                )
                                Text(
                                    text = if (usePathStyle) "开启" else "关闭",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (usePathStyle) AwsAmber else TextLowContrast
                                )
                            }

                            // HTTPS Toggle Chip
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (useSsl) ElectricCyan.copy(alpha = 0.15f) else SurfaceContainerLowest)
                                    .border(
                                        1.dp,
                                        if (useSsl) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { useSsl = !useSsl }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HTTPS 加密",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (useSsl) ElectricCyan else TextMediumContrast
                                )
                                Text(
                                    text = if (useSsl) "开启" else "HTTP",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (useSsl) ElectricCyan else AwsAmber
                                )
                            }
                        }

                        // Optional Session Token
                        if (!isPublicAccess) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerLowest)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Token",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = TextLowContrast,
                                    modifier = Modifier.width(46.dp)
                                )
                                BasicTextField(
                                    value = sessionTokenInput,
                                    onValueChange = { sessionTokenInput = it.trim() },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextHighContrast
                                    ),
                                    cursorBrush = SolidColor(ElectricCyan),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { inner ->
                                        if (sessionTokenInput.isEmpty()) {
                                            Text(
                                                text = "Session Token (可选临时凭据)",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 10.sp,
                                                color = TextLowContrast
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 2. 主操作按钮栏（紧跟在 S3 连接配置卡片下方，第一屏内直接可见！）
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Ping Connectivity Test Button
            Button(
                onClick = {
                    onRunPingTest(
                        endpointInput,
                        bucketInput,
                        regionInput,
                        accessKeyInput,
                        secretKeyInput,
                        sessionTokenInput,
                        usePathStyle,
                        useSsl,
                        isPublicAccess
                    )
                },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(0.38f)
                    .height(44.dp)
                    .testTag("ping_test_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (settings.isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = ElectricCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "连通测速",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Right: Save & Restart Sync Button
            Button(
                onClick = {
                    onSaveAndRestart(
                        bucketInput,
                        normalizedPrefix,
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
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AwsAmber),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(0.62f)
                    .height(44.dp)
                    .shadow(10.dp, RoundedCornerShape(10.dp), spotColor = AwsAmber.copy(alpha = 0.4f))
                    .testTag("save_restart_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (settings.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AetherVoid,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PublishedWithChanges,
                            contentDescription = null,
                            tint = AetherVoid,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDirty) "保存修改并生效 *" else "保存并重启监听",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AetherVoid,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // =========================================================================
        // 3. 自动发现与入队规则卡片 (紧凑单行交互)
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainer)
                .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Auto-Monitoring Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = if (settings.isSubdirMonitoring) ElectricCyan else TextLowContrast,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "后台自动扫描新音频",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "定期轮询监听目录并自动入队",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
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

            // Poll Interval Quick-Select Pills (Single Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "轮询间隔",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = TextMediumContrast
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLowest)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val intervals = listOf(3, 5, 10, 30, 60)
                    intervals.forEach { sec ->
                        val selected = settings.pollIntervalSec == sec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) AwsAmber else Color.Transparent)
                                .clickable { onUpdatePollInterval(sec) }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${sec}s",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) AetherVoid else TextLowContrast
                            )
                        }
                    }
                }
            }

            // Enqueue Rule Segmented Bar (Single Row)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "发现新音频时",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = TextMediumContrast
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLowest)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val rules = listOf(
                        "upnext" to "插队下一首",
                        "append" to "追加末尾",
                        "ask" to "仅横幅提醒"
                    )
                    rules.forEach { (key, label) ->
                        val isSelected = settings.enqueueRule == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SurfaceContainerHigh else Color.Transparent)
                                .clickable { onSetEnqueueRule(key) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AwsAmber else TextLowContrast
                            )
                        }
                    }
                }
            }

            // Collapsible Audio Format Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFormatsExpanded = !isFormatsExpanded }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "音频格式过滤 (${settings.enabledFormats.size}/7 已启用)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMediumContrast
                    )
                    Icon(
                        imageVector = if (isFormatsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextLowContrast,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isFormatsExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val allFormats = listOf(".flac", ".wav", ".mp3", ".m4a", ".aac", ".ogg", ".opus")
                        allFormats.forEach { fmt ->
                            val enabled = settings.enabledFormats.contains(fmt)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (enabled) AwsAmber.copy(alpha = 0.18f) else SurfaceContainerLowest)
                                    .border(
                                        1.dp,
                                        if (enabled) AwsAmber.copy(alpha = 0.6f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onToggleFormatEnabled(fmt) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (enabled) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = AwsAmber,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = fmt.removePrefix(".").uppercase(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal,
                                        color = if (enabled) AwsAmber else TextLowContrast
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}
