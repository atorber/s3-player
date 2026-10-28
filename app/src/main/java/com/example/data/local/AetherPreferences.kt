package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class AetherPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aether_s3_prefs", Context.MODE_PRIVATE)

    var currentBucketName: String
        get() = prefs.getString("currentBucketName", "xtrader") ?: "xtrader"
        set(value) = prefs.edit().putString("currentBucketName", value).apply()

    var basePrefixPath: String
        get() = prefs.getString("basePrefixPath", "/") ?: "/"
        set(value) = prefs.edit().putString("basePrefixPath", value).apply()

    var endpoint: String
        get() = prefs.getString("endpoint", "xtrader.oss.cn-north-3.inspurcloudoss.com") ?: "xtrader.oss.cn-north-3.inspurcloudoss.com"
        set(value) = prefs.edit().putString("endpoint", value).apply()

    var region: String
        get() = prefs.getString("region", "cn-north-3") ?: "cn-north-3"
        set(value) = prefs.edit().putString("region", value).apply()

    var accessKeyId: String
        get() = prefs.getString("accessKeyId", "") ?: ""
        set(value) = prefs.edit().putString("accessKeyId", value).apply()

    var secretAccessKey: String
        get() = prefs.getString("secretAccessKey", "") ?: ""
        set(value) = prefs.edit().putString("secretAccessKey", value).apply()

    var sessionToken: String
        get() = prefs.getString("sessionToken", "") ?: ""
        set(value) = prefs.edit().putString("sessionToken", value).apply()

    var usePathStyle: Boolean
        get() = prefs.getBoolean("usePathStyle", false)
        set(value) = prefs.edit().putBoolean("usePathStyle", value).apply()

    var isTlsEncrypted: Boolean
        get() = prefs.getBoolean("isTlsEncrypted", true)
        set(value) = prefs.edit().putBoolean("isTlsEncrypted", value).apply()

    var isPublicAccess: Boolean
        get() = prefs.getBoolean("isPublicAccess", false)
        set(value) = prefs.edit().putBoolean("isPublicAccess", value).apply()

    var isSubdirMonitoring: Boolean
        get() = prefs.getBoolean("isSubdirMonitoring", true)
        set(value) = prefs.edit().putBoolean("isSubdirMonitoring", value).apply()

    var pollIntervalSec: Int
        get() = prefs.getInt("pollIntervalSec", 3)
        set(value) = prefs.edit().putInt("pollIntervalSec", value).apply()

    var isSnsNotification: Boolean
        get() = prefs.getBoolean("isSnsNotification", false)
        set(value) = prefs.edit().putBoolean("isSnsNotification", value).apply()

    var enqueueRule: String
        get() = prefs.getString("enqueueRule", "append") ?: "append"
        set(value) = prefs.edit().putString("enqueueRule", value).apply()

    var monitoredPlaylistPrefix: String?
        get() = prefs.getString("monitoredPlaylistPrefix", null)
        set(value) = prefs.edit().putString("monitoredPlaylistPrefix", value).apply()

    var enabledFormats: Set<String>
        get() = prefs.getStringSet("enabledFormats", setOf(".flac", ".wav", ".mp3", ".m4a", ".aac", ".ogg", ".opus"))
            ?: setOf(".flac", ".wav", ".mp3", ".m4a", ".aac", ".ogg", ".opus")
        set(value) = prefs.edit().putStringSet("enabledFormats", value).apply()

    var isAutoPlayNewEnabled: Boolean
        get() = prefs.getBoolean("isAutoPlayNewEnabled", true)
        set(value) = prefs.edit().putBoolean("isAutoPlayNewEnabled", value).apply()

    var isPitchCorrectionEnabled: Boolean
        get() = prefs.getBoolean("isPitchCorrectionEnabled", true)
        set(value) = prefs.edit().putBoolean("isPitchCorrectionEnabled", value).apply()

    var isSilenceTrimmingEnabled: Boolean
        get() = prefs.getBoolean("isSilenceTrimmingEnabled", false)
        set(value) = prefs.edit().putBoolean("isSilenceTrimmingEnabled", value).apply()

    var isGridView: Boolean
        get() = prefs.getBoolean("isGridView", false)
        set(value) = prefs.edit().putBoolean("isGridView", value).apply()

    var sortModeName: String
        get() = prefs.getString("sortMode", "NAME_ASC") ?: "NAME_ASC"
        set(value) = prefs.edit().putString("sortMode", value).apply()

    var discoveredFolders: Set<String>
        get() = prefs.getStringSet("discovered_folders", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("discovered_folders", value).apply()
}
