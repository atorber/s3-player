package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket
import com.example.ui.TrackSortMode
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.AwsAmber
import com.example.ui.theme.BorderAmberGlow
import com.example.ui.theme.BorderCyanGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast
import com.example.ui.theme.TextMediumContrast

/**
 * Dialog displaying full S3 Object Metadata, Audio Specs, and direct action triggers.
 */
@Composable
fun ObjectDetailsDialog(
    track: S3AudioTrack,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleCache: () -> Unit,
    onCopyPresignedUrl: () -> Unit,
    onCopyS3Uri: () -> Unit
) {
    val context = LocalContext.current
    var copiedField by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("object_details_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyanGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricCyanDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "S3 对象详情 / 元数据",
                                color = TextHighContrast,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = track.format + " · " + track.storageClass,
                                color = AwsAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = TextLowContrast,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Object Key & URI Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "OBJECT KEY (前缀路径)",
                            color = TextLowContrast,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = track.key,
                            color = TextHighContrast,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = track.fullS3Uri,
                                color = ElectricCyanDim,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("S3 URI", track.fullS3Uri))
                                    copiedField = "s3uri"
                                    onCopyS3Uri()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (copiedField == "s3uri") Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "复制 S3 URI",
                                    tint = if (copiedField == "s3uri") StatusGreen else ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // S3 Specs Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(label = "所属存储桶", value = track.bucketName)
                    DetailRow(label = "文件体积", value = "${track.sizeFormatted} (${track.sizeBytes} 字节)")
                    DetailRow(label = "音频时长", value = "${track.durationFormatted} (${track.durationMs / 1000} 秒)")
                    DetailRow(label = "采样率 / 位深", value = track.sampleRate)
                    DetailRow(label = "音频声道", value = track.channels)
                    DetailRow(label = "ETag (MD5 Checksum)", value = track.etag, isMono = true)
                    DetailRow(label = "存储分级 (Storage Class)", value = track.storageClass)
                    DetailRow(label = "最后修改时间", value = track.lastModified)
                    DetailRow(
                        label = "离线缓存状态",
                        value = if (track.isCachedLocally) "已缓存到本地块存储" else "云端流式点播 (未缓存)",
                        valueColor = if (track.isCachedLocally) StatusGreen else TextMediumContrast
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onPlay()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("dialog_play_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwsAmber,
                            contentColor = AetherVoid
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("立即播放", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onAddToQueue()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("dialog_queue_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyanDim)
                    ) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("加入队列", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onToggleCache()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (track.isCachedLocally) StatusGreen else TextMediumContrast
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Icon(
                            imageVector = if (track.isCachedLocally) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (track.isCachedLocally) "已缓存" else "缓存到本地", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Presigned URL", track.streamUrl))
                            copiedField = "url"
                            onCopyPresignedUrl()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyanGlow)
                    ) {
                        Icon(
                            imageVector = if (copiedField == "url") Icons.Default.Check else Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (copiedField == "url") "直链已复制" else "复制预签名直链", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isMono: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = TextHighContrast
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = TextLowContrast,
            fontSize = 11.sp,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            modifier = Modifier.weight(0.58f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Dialog for uploading a new audio track to current S3 directory prefix (PutObject).
 */
@Composable
fun UploadTrackDialog(
    targetPrefix: String,
    bucketName: String,
    onDismiss: () -> Unit,
    onUpload: (title: String, format: String, durationSec: Long, sizeMb: Double, storageClass: String) -> Unit
) {
    var title by remember { mutableStateOf("new_master_take_01") }
    var format by remember { mutableStateOf("FLAC") }
    var storageClass by remember { mutableStateOf("STANDARD") }
    var durationSec by remember { mutableLongStateOf(180L) }
    var sizeMb by remember { mutableDoubleStateOf(32.5) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("upload_track_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderAmberGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AwsAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = AwsAmber, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("上传音频对象 (PutObject)", color = TextHighContrast, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("目标: s3://$bucketName/${targetPrefix.ifBlank { "" }}", color = TextLowContrast, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = TextLowContrast, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File title input
                Text("音频文件标识 (File Name / Key)", color = TextMediumContrast, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth().testTag("upload_file_name_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest,
                        focusedBorderColor = AwsAmber,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextHighContrast,
                        unfocusedTextColor = TextHighContrast
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Format selector chips
                Text("音频封装格式", color = TextMediumContrast, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("FLAC", "WAV", "MP3", "M4A").forEach { fmt ->
                        val isSelected = format == fmt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AwsAmber else SurfaceContainerHigh)
                                .clickable { format = fmt }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = fmt,
                                color = if (isSelected) AetherVoid else TextHighContrast,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Storage Class
                Text("S3 存储类型 (Storage Class)", color = TextMediumContrast, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("STANDARD", "INTELLIGENT_TIERING", "EXPRESS_ONEZONE").forEach { sc ->
                        val isSelected = storageClass == sc
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ElectricCyan else SurfaceContainerHigh)
                                .clickable { storageClass = sc }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = when (sc) {
                                    "STANDARD" -> "标准"
                                    "INTELLIGENT_TIERING" -> "智能分层"
                                    else -> "单区极速"
                                },
                                color = if (isSelected) AetherVoid else TextMediumContrast,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration and Size sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("预估时长: ${durationSec / 60}m ${durationSec % 60}s", color = TextLowContrast, fontSize = 11.sp)
                    Text("预估体积: ${String.format("%.1f", sizeMb)} MB", color = TextLowContrast, fontSize = 11.sp)
                }
                Slider(
                    value = sizeMb.toFloat(),
                    onValueChange = {
                        sizeMb = it.toDouble()
                        durationSec = (it * 6).toLong().coerceAtLeast(30L)
                    },
                    valueRange = 2f..120f,
                    colors = SliderDefaults.colors(
                        thumbColor = AwsAmber,
                        activeTrackColor = AwsAmber,
                        inactiveTrackColor = SurfaceContainerHigh
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onUpload(title, format, durationSec, sizeMb, storageClass)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_upload_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AwsAmber, contentColor = AetherVoid),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("写入存储桶 (PutObject)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * Dialog for creating a new S3 folder / directory prefix.
 */
@Composable
fun NewFolderDialog(
    targetPrefix: String,
    bucketName: String,
    onDismiss: () -> Unit,
    onCreate: (folderName: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("new_folder_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyanGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("新建 S3 目录前缀", color = TextHighContrast, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = TextLowContrast, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "基准路径: s3://$bucketName/${targetPrefix.ifBlank { "" }}",
                    color = TextLowContrast,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it.replace(" ", "_") },
                    placeholder = { Text("例如: live-stems, raw-mixes, archive", color = TextLowContrast, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("folder_name_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextHighContrast,
                        unfocusedTextColor = TextHighContrast
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            onCreate(folderName)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_create_folder_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = AetherVoid),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("确认创建目录", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Dialog to select track sort mode.
 */
@Composable
fun SortModeDialog(
    currentSort: TrackSortMode,
    onDismiss: () -> Unit,
    onSelectSort: (TrackSortMode) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("sort_mode_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sort, contentDescription = null, tint = AwsAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("文件排序规则", color = TextHighContrast, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = TextLowContrast, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TrackSortMode.values().forEach { mode ->
                    val isSelected = mode == currentSort
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceContainerHigh else AetherSurface)
                            .clickable {
                                onSelectSort(mode)
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onSelectSort(mode)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AwsAmber,
                                unselectedColor = TextLowContrast
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.label,
                            color = if (isSelected) AwsAmber else TextHighContrast,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to select active bucket or switch storage bucket.
 */
@Composable
fun BucketSelectorDialog(
    currentBucket: String,
    buckets: List<S3Bucket>,
    onDismiss: () -> Unit,
    onSelectBucket: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("bucket_selector_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyanGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("切换 S3 目标存储桶", color = TextHighContrast, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = TextLowContrast, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                buckets.forEach { b ->
                    val isSelected = b.bucketName.equals(currentBucket, ignoreCase = true)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectBucket(b.bucketName)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SurfaceContainerHigh else SurfaceContainerLowest
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BorderCyanGlow else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = b.bucketName,
                                        color = if (isSelected) ElectricCyan else TextHighContrast,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ElectricCyanDark)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("当前已挂载", color = ElectricCyan, fontSize = 9.sp)
                                        }
                                    }
                                }
                                Text(
                                    text = "${b.provider} · ${b.endpoint}",
                                    color = TextLowContrast,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${b.latencyMs}ms",
                                color = StatusGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
