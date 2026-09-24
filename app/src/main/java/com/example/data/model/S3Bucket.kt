package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "s3_buckets")
data class S3Bucket(
    @PrimaryKey val bucketName: String,
    val endpoint: String,
    val region: String = "us-east-1",
    val provider: String = "S3 协议兼容",
    val latencyMs: Int = 24,
    val objectCount: Int = 0,
    val storageSizeFormatted: String = "0 MB",
    val isMounted: Boolean = true,
    val authType: String = "S3 协议 (签名/匿名)",
    val usePathStyle: Boolean = false,
    val useSsl: Boolean = true,
    val accessKey: String? = null
)
