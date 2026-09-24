package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket

@Database(
    entities = [S3AudioTrack::class, S3Bucket::class],
    version = 2,
    exportSchema = false
)
abstract class AetherDatabase : RoomDatabase() {
    abstract fun trackDao(): S3TrackDao
    abstract fun bucketDao(): S3BucketDao

    companion object {
        @Volatile
        private var INSTANCE: AetherDatabase? = null

        fun getInstance(context: Context): AetherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AetherDatabase::class.java,
                    "aether_s3_audio.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
