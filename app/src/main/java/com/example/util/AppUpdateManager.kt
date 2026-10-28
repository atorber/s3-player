package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val currentVersionName: String,
    val currentVersionCode: Int,
    val latestVersionName: String = "",
    val releaseTitle: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val apkSize: Long = 0L,
    val isUpdateAvailable: Boolean = false
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class Available(val info: AppUpdateInfo) : UpdateStatus()
    data class UpToDate(val info: AppUpdateInfo) : UpdateStatus()
    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val info: AppUpdateInfo
    ) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File, val info: AppUpdateInfo) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

class AppUpdateManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    private var activeCall: Call? = null
    private var downloadJob: Job? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun checkForUpdates(owner: String = "atorber", repo: String = "s3-player") {
        if (_status.value is UpdateStatus.Checking || _status.value is UpdateStatus.Downloading) return

        _status.value = UpdateStatus.Checking
        scope.launch(Dispatchers.IO) {
            try {
                val updateInfo = fetchLatestRelease(owner, repo)
                val isNewer = isNewerVersion(updateInfo.latestVersionName, BuildConfig.VERSION_NAME)
                val finalInfo = updateInfo.copy(isUpdateAvailable = isNewer)
                withContext(Dispatchers.Main) {
                    if (isNewer) {
                        _status.value = UpdateStatus.Available(finalInfo)
                    } else {
                        _status.value = UpdateStatus.UpToDate(finalInfo)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _status.value = UpdateStatus.Error(e.message ?: "检查更新失败，请稍后重试")
                }
            }
        }
    }

    private fun fetchLatestRelease(owner: String, repo: String): AppUpdateInfo {
        val currentName = BuildConfig.VERSION_NAME
        val currentCode = BuildConfig.VERSION_CODE

        // Strategy 1: GitHub REST API
        try {
            val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val req = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AetherS3Player/$currentName")
                .build()

            val response = okHttpClient.newCall(req).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string().orEmpty()
                val json = JSONObject(bodyStr)
                val tagName = json.optString("tag_name", "").trim()
                val title = json.optString("name", tagName).trim()
                val notes = json.optString("body", "").trim()

                var apkDownloadUrl = ""
                var apkSize = 0L

                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", "")
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                if (apkDownloadUrl.isBlank() && tagName.isNotBlank()) {
                    apkDownloadUrl = "https://github.com/$owner/$repo/releases/download/$tagName/s3-player-$tagName.apk"
                }

                if (tagName.isNotBlank()) {
                    return AppUpdateInfo(
                        currentVersionName = currentName,
                        currentVersionCode = currentCode,
                        latestVersionName = tagName.removePrefix("v").removePrefix("V"),
                        releaseTitle = if (title.isNotBlank()) title else "Release $tagName",
                        releaseNotes = notes,
                        downloadUrl = apkDownloadUrl,
                        apkSize = apkSize
                    )
                }
            }
        } catch (_: Exception) {
            // Fall through to Strategy 2
        }

        // Strategy 2: Web 302 Redirect Check (No rate limits)
        val noRedirectClient = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val webReq = Request.Builder()
            .url("https://github.com/$owner/$repo/releases/latest")
            .header("User-Agent", "Mozilla/5.0")
            .build()

        val webResp = noRedirectClient.newCall(webReq).execute()
        val location = webResp.header("Location").orEmpty()
        val tag = location.substringAfterLast("/tag/").substringAfterLast("/").trim()

        if (tag.isNotBlank()) {
            val apkUrl = "https://github.com/$owner/$repo/releases/download/$tag/s3-player-$tag.apk"
            return AppUpdateInfo(
                currentVersionName = currentName,
                currentVersionCode = currentCode,
                latestVersionName = tag.removePrefix("v").removePrefix("V"),
                releaseTitle = "Release $tag",
                releaseNotes = "发现 GitHub Release 最新版本 $tag",
                downloadUrl = apkUrl,
                apkSize = 0L
            )
        }

        throw IOException("无法获取最新版本信息，请检查网络连接")
    }

    fun startDownload(info: AppUpdateInfo) {
        if (_status.value is UpdateStatus.Downloading) return
        val downloadUrl = info.downloadUrl.ifBlank {
            "https://github.com/atorber/s3-player/releases/download/v${info.latestVersionName}/s3-player-v${info.latestVersionName}.apk"
        }

        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val destFile = File(destDir, "s3-player-v${info.latestVersionName}.apk")
            if (destFile.exists()) {
                destFile.delete()
            }

            withContext(Dispatchers.Main) {
                _status.value = UpdateStatus.Downloading(0f, 0L, info.apkSize, info)
            }

            try {
                val req = Request.Builder()
                    .url(downloadUrl)
                    .header("User-Agent", "AetherS3Player/${BuildConfig.VERSION_NAME}")
                    .build()

                val call = okHttpClient.newCall(req)
                activeCall = call
                val response = call.execute()

                if (!response.isSuccessful) {
                    throw IOException("下载请求失败: HTTP ${response.code}")
                }

                val body = response.body ?: throw IOException("响应内容为空")
                val totalLength = if (body.contentLength() > 0) body.contentLength() else info.apkSize
                var downloaded = 0L

                body.byteStream().use { input ->
                    destFile.outputStream().use { output ->
                        val buffer = ByteArray(32 * 1024)
                        var read: Int
                        var lastReportTime = 0L

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read

                            val now = System.currentTimeMillis()
                            if (now - lastReportTime > 120 || downloaded == totalLength) {
                                lastReportTime = now
                                val progress = if (totalLength > 0) {
                                    downloaded.toFloat() / totalLength.toFloat()
                                } else 0f

                                withContext(Dispatchers.Main) {
                                    _status.value = UpdateStatus.Downloading(
                                        progress.coerceIn(0f, 1f),
                                        downloaded,
                                        totalLength,
                                        info
                                    )
                                }
                            }
                        }
                        output.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    _status.value = UpdateStatus.ReadyToInstall(destFile, info)
                    // Trigger installation automatically
                    installApk(destFile)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    withContext(Dispatchers.Main) {
                        _status.value = UpdateStatus.Idle
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _status.value = UpdateStatus.Error("下载更新包失败: ${e.message}")
                    }
                }
            } finally {
                activeCall = null
            }
        }
    }

    fun cancelDownload() {
        activeCall?.cancel()
        downloadJob?.cancel()
        _status.value = UpdateStatus.Idle
    }

    fun dismissUpdate() {
        _status.value = UpdateStatus.Idle
    }

    fun installApk(apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return false
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            return true
        } catch (e: Exception) {
            _status.value = UpdateStatus.Error("调起系统安装器失败: ${e.message}")
            return false
        }
    }

    companion object {
        fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
            val rParts = remoteVersion.trim().removePrefix("v").removePrefix("V")
                .substringBefore("-").split(".")
                .mapNotNull { it.toIntOrNull() }
            val cParts = currentVersion.trim().removePrefix("v").removePrefix("V")
                .substringBefore("-").split(".")
                .mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(rParts.size, cParts.size)
            for (i in 0 until maxLen) {
                val r = rParts.getOrElse(i) { 0 }
                val c = cParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        }
    }
}
