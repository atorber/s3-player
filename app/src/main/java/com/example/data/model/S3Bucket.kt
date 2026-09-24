package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "s3_buckets")
data class S3Bucket(
    @PrimaryKey val bucketName: String,
    val region: String,
    val provider: String,
    val endpoint: String,
    val latencyMs: Int = 24,
    val objectCount: Int = 0,
    val storageSizeFormatted: String = "0 MB",
    val isMounted: Boolean = true,
    val authType: String = "IAM Role / Public Read"
)
