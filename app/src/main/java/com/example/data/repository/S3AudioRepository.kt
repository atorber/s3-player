package com.example.data.repository

import com.example.data.local.S3BucketDao
import com.example.data.local.S3TrackDao
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket
import com.example.data.remote.S3ListResult
import com.example.data.remote.S3RemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class S3AudioRepository(
    private val trackDao: S3TrackDao,
    private val bucketDao: S3BucketDao,
    private val externalScope: CoroutineScope,
    val remoteService: S3RemoteService = S3RemoteService()
) {
    val allTracks: Flow<List<S3AudioTrack>> = trackDao.getAllTracks()
    val allBuckets: Flow<List<S3Bucket>> = bucketDao.getAllBuckets()
    val favoriteTracks: Flow<List<S3AudioTrack>> = trackDao.getFavoriteTracks()

    init {
        externalScope.launch(Dispatchers.IO) {
            // Initialize active bucket configuration if not present
            val existingBuckets = bucketDao.getAllBuckets().first()
            if (existingBuckets.isEmpty()) {
                bucketDao.insert(
                    S3Bucket(
                        bucketName = "xtrader",
                        region = "cn-north-3",
                        provider = "S3 兼容协议 (浪潮云 OSS)",
                        endpoint = "xtrader.oss.cn-north-3.inspurcloudoss.com",
                        latencyMs = 0,
                        objectCount = 0,
                        storageSizeFormatted = "0 B",
                        isMounted = true,
                        authType = "S3 SigV4 认证",
                        usePathStyle = false,
                        useSsl = true,
                        accessKey = ""
                    )
                )
            }
        }
    }

    fun getTracksForBucket(bucketName: String): Flow<List<S3AudioTrack>> =
        trackDao.getTracksForBucket(bucketName)

    fun searchTracks(query: String): Flow<List<S3AudioTrack>> =
        trackDao.searchTracks(query)

    suspend fun getTrackById(id: Long): S3AudioTrack? = withContext(Dispatchers.IO) {
        trackDao.getTrackById(id)
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) = withContext(Dispatchers.IO) {
        trackDao.setFavorite(id, !current)
    }

    suspend fun toggleCacheLocally(id: Long, current: Boolean) = withContext(Dispatchers.IO) {
        trackDao.setCachedLocally(id, !current)
    }

    suspend fun addBucket(bucket: S3Bucket) = withContext(Dispatchers.IO) {
        bucketDao.insert(bucket)
    }

    suspend fun addTrack(track: S3AudioTrack) = withContext(Dispatchers.IO) {
        trackDao.insert(track)
    }

    suspend fun insertTracks(tracks: List<S3AudioTrack>) = withContext(Dispatchers.IO) {
        trackDao.insertAll(tracks)
    }

    suspend fun clearTracksForBucket(bucketName: String) = withContext(Dispatchers.IO) {
        trackDao.clearBucketTracks(bucketName)
    }

    suspend fun clearAllTracks() = withContext(Dispatchers.IO) {
        trackDao.clearAllTracks()
    }

    suspend fun deleteTrack(id: Long) = withContext(Dispatchers.IO) {
        trackDao.deleteById(id)
    }

    /**
     * Reads real S3 objects and common prefixes from the remote server
     */
    suspend fun syncRemoteS3(
        endpoint: String,
        bucketName: String,
        prefix: String,
        delimiter: String = "/",
        region: String = "cn-north-3",
        accessKeyId: String = "",
        secretAccessKey: String = "",
        sessionToken: String = "",
        continuationToken: String? = null,
        usePathStyle: Boolean = false,
        useTls: Boolean = true,
        isPublicAccess: Boolean = false,
        allowedExtensions: Set<String>? = null
    ): Result<S3ListResult> = withContext(Dispatchers.IO) {
        val result = remoteService.listObjectsV2(
            endpoint = endpoint,
            bucketName = bucketName,
            prefix = prefix,
            delimiter = delimiter,
            region = region,
            accessKeyId = accessKeyId,
            secretAccessKey = secretAccessKey,
            sessionToken = sessionToken,
            continuationToken = continuationToken,
            usePathStyle = usePathStyle,
            useTls = useTls,
            isPublicAccess = isPublicAccess,
            allowedExtensions = allowedExtensions
        )

        result.onSuccess { listResult ->
            // Save real tracks into local database, preserving local status like favorite and cached
            if (listResult.tracks.isNotEmpty()) {
                val existingMap = trackDao.getAllTracksList().associateBy { "${it.bucketName}:${it.key}" }
                val merged = listResult.tracks.map { remoteTrack ->
                    val existing = existingMap["${remoteTrack.bucketName}:${remoteTrack.key}"]
                    if (existing != null) {
                        remoteTrack.copy(
                            id = existing.id,
                            isFavorite = existing.isFavorite,
                            isCachedLocally = existing.isCachedLocally
                        )
                    } else {
                        remoteTrack
                    }
                }
                trackDao.insertAll(merged)
            }
        }

        result
    }
}
