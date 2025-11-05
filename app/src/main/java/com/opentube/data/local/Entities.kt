package com.opentube.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Watch history entry
 */
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val uploaderName: String,
    val thumbnail: String,
    val duration: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val position: Long = 0 // Last watched position in milliseconds
)

/**
 * Favorite video
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val uploaderName: String,
    val thumbnail: String,
    val duration: Long,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Subscribed channel
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val channelId: String,
    val channelName: String,
    val channelAvatar: String,
    val subscriberCount: Long = 0,
    val isVerified: Boolean = false,
    val subscribedAt: Long = System.currentTimeMillis()
)

/**
 * Local playlist
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Video in playlist (junction table)
 */
@Entity(
    tableName = "playlist_videos",
    primaryKeys = ["playlistId", "videoId"]
)
data class PlaylistVideoEntity(
    val playlistId: Long,
    val videoId: String,
    val title: String,
    val uploaderName: String,
    val thumbnail: String,
    val duration: Long,
    val position: Int = 0, // Order in playlist
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Liked comments
 */
@Entity(tableName = "liked_comments")
data class LikedCommentEntity(
    @PrimaryKey
    val commentId: String,
    val videoId: String,
    val likedAt: Long = System.currentTimeMillis()
)
