package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.S3AudioTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface S3TrackDao {
    @Query("SELECT * FROM s3_audio_tracks ORDER BY id ASC")
    fun getAllTracks(): Flow<List<S3AudioTrack>>

    @Query("SELECT * FROM s3_audio_tracks WHERE bucketName = :bucketName ORDER BY key ASC")
    fun getTracksForBucket(bucketName: String): Flow<List<S3AudioTrack>>

    @Query("SELECT * FROM s3_audio_tracks WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteTracks(): Flow<List<S3AudioTrack>>

    @Query("SELECT * FROM s3_audio_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): S3AudioTrack?

    @Query("SELECT * FROM s3_audio_tracks WHERE title LIKE '%' || :query || '%' OR `key` LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<S3AudioTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<S3AudioTrack>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: S3AudioTrack): Long

    @Update
    suspend fun update(track: S3AudioTrack)

    @Query("UPDATE s3_audio_tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE s3_audio_tracks SET isCachedLocally = :isCached WHERE id = :id")
    suspend fun setCachedLocally(id: Long, isCached: Boolean)

    @Query("DELETE FROM s3_audio_tracks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
