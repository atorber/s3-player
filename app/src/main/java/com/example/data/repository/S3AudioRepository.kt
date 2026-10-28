package com.example.data.repository

import com.example.data.SampleData
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
        // Production repository starts clean; buckets and tracks are populated via user S3 sync or mounting.
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
     * Reads real S3 objects and common prefixes from the remote server with automatic pagination
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
        allowedExtensions: Set<String>? = null,
        maxPages: Int = 100
    ): Result<S3ListResult> = withContext(Dispatchers.IO) {
        val allTracks = mutableListOf<S3AudioTrack>()
        val allCommonPrefixes = mutableSetOf<String>()
        var currentToken: String? = continuationToken
        var isTruncated = false
        var pagesFetched = 0

        try {
            do {
                pagesFetched++
                val pageResult = remoteService.listObjectsV2(
                    endpoint = endpoint,
                    bucketName = bucketName,
                    prefix = prefix,
                    delimiter = delimiter,
                    region = region,
                    accessKeyId = accessKeyId,
                    secretAccessKey = secretAccessKey,
                    sessionToken = sessionToken,
                    continuationToken = currentToken,
                    usePathStyle = usePathStyle,
                    useTls = useTls,
                    isPublicAccess = isPublicAccess,
                    allowedExtensions = allowedExtensions
                )

                if (pageResult.isFailure) {
                    if (allTracks.isEmpty() && allCommonPrefixes.isEmpty()) {
                        return@withContext pageResult
                    } else {
                        break
                    }
                }

                val listResult = pageResult.getOrThrow()
                allTracks.addAll(listResult.tracks)
                allCommonPrefixes.addAll(listResult.commonPrefixes)

                isTruncated = listResult.isTruncated
                currentToken = if (isTruncated && !listResult.nextContinuationToken.isNullOrBlank()) {
                    listResult.nextContinuationToken
                } else {
                    null
                }
            } while (currentToken != null && pagesFetched < maxPages)

            // Save real tracks into local database, preserving local status like favorite and cached
            if (allTracks.isNotEmpty()) {
                val existingMap = trackDao.getAllTracksList().associateBy { "${it.bucketName}:${it.key}" }
                val merged = allTracks.map { remoteTrack ->
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

            Result.success(
                S3ListResult(
                    tracks = allTracks,
                    commonPrefixes = allCommonPrefixes.toList().distinct(),
                    isTruncated = isTruncated,
                    nextContinuationToken = currentToken
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
