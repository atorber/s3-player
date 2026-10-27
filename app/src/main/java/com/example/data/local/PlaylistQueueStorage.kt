package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.audio.LoopMode
import com.example.data.model.S3AudioTrack
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persists and restores the playback queue, current active track, and player settings
 * across application restarts and process terminations.
 */
class PlaylistQueueStorage(private val context: Context) {
    private val queueFile = File(context.filesDir, "aether_playback_queue.json")
    private val prefs = context.getSharedPreferences("aether_playback_state", Context.MODE_PRIVATE)

    data class RestoredPlaybackState(
        val queue: List<S3AudioTrack>,
        val currentTrack: S3AudioTrack?,
        val positionMs: Long,
        val loopMode: LoopMode,
        val playbackSpeed: Float
    )

    /**
     * Atomically saves the current playlist queue and player snapshot to disk.
     */
    fun saveQueue(
        queue: List<S3AudioTrack>,
        currentTrack: S3AudioTrack?,
        currentPositionMs: Long,
        loopMode: LoopMode = LoopMode.OFF,
        playbackSpeed: Float = 1.0f
    ) {
        try {
            val jsonArray = JSONArray()
            queue.forEach { track ->
                jsonArray.put(trackToJson(track))
            }

            // Write atomically to temporary file then rename to avoid corruption
            val tempFile = File(context.filesDir, "aether_playback_queue.tmp")
            tempFile.writeText(jsonArray.toString())
            if (tempFile.exists()) {
                if (queueFile.exists()) queueFile.delete()
                tempFile.renameTo(queueFile)
            }

            prefs.edit()
                .putString("lastTrackKey", currentTrack?.key ?: "")
                .putString("lastTrackBucket", currentTrack?.bucketName ?: "")
                .putLong("lastPositionMs", currentPositionMs.coerceAtLeast(0L))
                .putString("lastLoopMode", loopMode.name)
                .putFloat("lastPlaybackSpeed", playbackSpeed)
                .apply()
        } catch (e: Exception) {
            Log.e("PlaylistQueueStorage", "Failed to save playback queue: ${e.message}", e)
        }
    }

    /**
     * Loads the persisted queue and active track state on startup.
     */
    fun loadQueue(): RestoredPlaybackState {
        val tracks = mutableListOf<S3AudioTrack>()
        try {
            if (queueFile.exists()) {
                val content = queueFile.readText()
                if (content.isNotBlank()) {
                    val jsonArray = JSONArray(content)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        tracks.add(jsonToTrack(obj))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PlaylistQueueStorage", "Failed to load playback queue: ${e.message}", e)
        }

        val lastKey = prefs.getString("lastTrackKey", "") ?: ""
        val lastBucket = prefs.getString("lastTrackBucket", "") ?: ""
        val lastPos = prefs.getLong("lastPositionMs", 0L)
        val loopModeStr = prefs.getString("lastLoopMode", LoopMode.OFF.name) ?: LoopMode.OFF.name
        val loopMode = try {
            LoopMode.valueOf(loopModeStr)
        } catch (_: Exception) {
            LoopMode.OFF
        }
        val playbackSpeed = prefs.getFloat("lastPlaybackSpeed", 1.0f)

        val matchedTrack = if (lastKey.isNotBlank()) {
            tracks.firstOrNull { it.key == lastKey && it.bucketName == lastBucket } ?: tracks.firstOrNull()
        } else {
            tracks.firstOrNull()
        }

        return RestoredPlaybackState(
            queue = tracks,
            currentTrack = matchedTrack,
            positionMs = lastPos,
            loopMode = loopMode,
            playbackSpeed = playbackSpeed
        )
    }

    /**
     * Clears all persisted queue records from storage.
     */
    fun clear() {
        try {
            if (queueFile.exists()) queueFile.delete()
            prefs.edit().clear().apply()
        } catch (_: Exception) {}
    }

    private fun trackToJson(track: S3AudioTrack): JSONObject {
        val json = JSONObject()
        json.put("id", track.id)
        json.put("bucketName", track.bucketName)
        json.put("key", track.key)
        json.put("title", track.title)
        json.put("artistOrProject", track.artistOrProject)
        json.put("format", track.format)
        json.put("sampleRate", track.sampleRate)
        json.put("channels", track.channels)
        json.put("sizeBytes", track.sizeBytes)
        json.put("durationMs", track.durationMs)
        json.put("etag", track.etag)
        json.put("storageClass", track.storageClass)
        json.put("streamUrl", track.streamUrl)
        json.put("isCachedLocally", track.isCachedLocally)
        json.put("waveformPointsRaw", track.waveformPointsRaw)
        json.put("isFavorite", track.isFavorite)
        json.put("lastModified", track.lastModified)
        return json
    }

    private fun jsonToTrack(json: JSONObject): S3AudioTrack {
        return S3AudioTrack(
            id = json.optLong("id", 0L),
            bucketName = json.optString("bucketName", ""),
            key = json.optString("key", ""),
            title = json.optString("title", "Unknown"),
            artistOrProject = json.optString("artistOrProject", "S3 Storage"),
            format = json.optString("format", "FLAC"),
            sampleRate = json.optString("sampleRate", "48kHz / 24-bit"),
            channels = json.optString("channels", "2.0 Stereo"),
            sizeBytes = json.optLong("sizeBytes", 0L),
            durationMs = json.optLong("durationMs", 0L),
            etag = json.optString("etag", ""),
            storageClass = json.optString("storageClass", "STANDARD"),
            streamUrl = json.optString("streamUrl", ""),
            isCachedLocally = json.optBoolean("isCachedLocally", false),
            waveformPointsRaw = json.optString("waveformPointsRaw", ""),
            isFavorite = json.optBoolean("isFavorite", false),
            lastModified = json.optString("lastModified", "")
        )
    }
}
