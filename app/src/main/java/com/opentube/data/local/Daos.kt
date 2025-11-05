package com.opentube.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for watch history
 */
@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>
    
    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun getHistoryEntry(videoId: String): WatchHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WatchHistoryEntity)
    
    @Delete
    suspend fun deleteHistory(history: WatchHistoryEntity)
    
    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteHistoryById(videoId: String)
    
    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM watch_history WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}

/**
 * DAO for favorites
 */
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT * FROM favorites WHERE videoId = :videoId LIMIT 1")
    suspend fun getFavorite(videoId: String): FavoriteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE videoId = :videoId")
    suspend fun deleteFavoriteById(videoId: String)
}

/**
 * DAO for subscriptions
 */
@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE channelId = :channelId LIMIT 1")
    suspend fun getSubscription(channelId: String): SubscriptionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)
    
    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)
    
    @Query("DELETE FROM subscriptions WHERE channelId = :channelId")
    suspend fun deleteSubscriptionById(channelId: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelId = :channelId)")
    fun isSubscribed(channelId: String): Flow<Boolean>
}

/**
 * DAO for playlists
 */
@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylist(playlistId: Long): PlaylistEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Query("SELECT * FROM playlist_videos WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getPlaylistVideos(playlistId: Long): Flow<List<PlaylistVideoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistVideo(video: PlaylistVideoEntity)
    
    @Delete
    suspend fun deletePlaylistVideo(video: PlaylistVideoEntity)
    
    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId")
    suspend fun deleteAllPlaylistVideos(playlistId: Long)
}

/**
 * DAO for liked comments
 */
@Dao
interface LikedCommentsDao {
    
    @Query("SELECT * FROM liked_comments WHERE videoId = :videoId")
    fun getLikedCommentsByVideo(videoId: String): Flow<List<LikedCommentEntity>>
    
    @Query("SELECT * FROM liked_comments WHERE commentId = :commentId")
    suspend fun isCommentLiked(commentId: String): LikedCommentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeComment(comment: LikedCommentEntity)
    
    @Query("DELETE FROM liked_comments WHERE commentId = :commentId")
    suspend fun unlikeComment(commentId: String)
}
