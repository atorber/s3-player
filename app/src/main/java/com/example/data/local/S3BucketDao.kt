package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.S3Bucket
import kotlinx.coroutines.flow.Flow

@Dao
interface S3BucketDao {
    @Query("SELECT * FROM s3_buckets ORDER BY bucketName ASC")
    fun getAllBuckets(): Flow<List<S3Bucket>>

    @Query("SELECT * FROM s3_buckets WHERE bucketName = :name LIMIT 1")
    suspend fun getBucketByName(name: String): S3Bucket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(buckets: List<S3Bucket>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: S3Bucket)

    @Update
    suspend fun update(bucket: S3Bucket)

    @Query("UPDATE s3_buckets SET isMounted = :isMounted WHERE bucketName = :name")
    suspend fun setMounted(name: String, isMounted: Boolean)

    @Query("DELETE FROM s3_buckets WHERE bucketName = :name")
    suspend fun deleteByName(name: String)
}
