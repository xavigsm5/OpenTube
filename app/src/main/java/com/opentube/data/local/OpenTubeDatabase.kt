package com.opentube.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for OpenTube
 */
@Database(
    entities = [
        WatchHistoryEntity::class,
        FavoriteEntity::class,
        SubscriptionEntity::class,
        PlaylistEntity::class,
        PlaylistVideoEntity::class,
        LikedCommentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class OpenTubeDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun likedCommentsDao(): LikedCommentsDao
    
    companion object {
        const val DATABASE_NAME = "opentube_database"
        
        /**
         * Migration from version 1 to 2
         * Adds subscriberCount and isVerified columns to subscriptions table
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add subscriberCount column with default value 0
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN subscriberCount INTEGER NOT NULL DEFAULT 0")
                
                // Add isVerified column with default value 0 (false)
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        /**
         * Migration from version 2 to 3
         * Adds liked_comments table
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS liked_comments (
                        commentId TEXT NOT NULL PRIMARY KEY,
                        videoId TEXT NOT NULL,
                        likedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}