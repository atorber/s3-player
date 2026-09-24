package com.example.data.repository

import com.example.data.SampleData
import com.example.data.local.S3BucketDao
import com.example.data.local.S3TrackDao
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class S3AudioRepository(
    private val trackDao: S3TrackDao,
    private val bucketDao: S3BucketDao,
    private val externalScope: CoroutineScope
) {
    val allTracks: Flow<List<S3AudioTrack>> = trackDao.getAllTracks()
    val allBuckets: Flow<List<S3Bucket>> = bucketDao.getAllBuckets()
    val favoriteTracks: Flow<List<S3AudioTrack>> = trackDao.getFavoriteTracks()

    init {
        externalScope.launch(Dispatchers.IO) {
            val existingBuckets = bucketDao.getAllBuckets().first()
            if (existingBuckets.isEmpty()) {
                bucketDao.insertAll(SampleData.initialBuckets)
            }
            val existingTracks = trackDao.getAllTracks().first()
            if (existingTracks.isEmpty()) {
                trackDao.insertAll(SampleData.getInitialTracks())
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

    suspend fun deleteTrack(id: Long) = withContext(Dispatchers.IO) {
        trackDao.deleteById(id)
    }
}
