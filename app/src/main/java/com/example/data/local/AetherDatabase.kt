package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.S3AudioTrack
import com.example.data.model.S3Bucket

@Database(
    entities = [S3AudioTrack::class, S3Bucket::class],
    version = 3,
    exportSchema = false
)
abstract class AetherDatabase : RoomDatabase() {
    abstract fun trackDao(): S3TrackDao
    abstract fun bucketDao(): S3BucketDao

    companion object {
        @Volatile
        private var INSTANCE: AetherDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN secretKey TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN sessionToken TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN basePrefix TEXT NOT NULL DEFAULT '/'")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_s3_audio_tracks_bucketName_key ON s3_audio_tracks(bucketName, key)")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN secretKey TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN sessionToken TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN basePrefix TEXT NOT NULL DEFAULT '/'")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN secretKey TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN sessionToken TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE s3_buckets ADD COLUMN basePrefix TEXT NOT NULL DEFAULT '/'")
                } catch (_: Exception) {}
                try {
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_s3_audio_tracks_bucketName_key ON s3_audio_tracks(bucketName, key)")
                } catch (_: Exception) {}
            }
        }

        fun getInstance(context: Context): AetherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AetherDatabase::class.java,
                    "aether_s3_audio.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
