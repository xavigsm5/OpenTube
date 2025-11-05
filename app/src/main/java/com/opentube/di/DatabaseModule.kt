package com.opentube.di

import android.content.Context
import androidx.room.Room
import com.opentube.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): OpenTubeDatabase {
        return Room.databaseBuilder(
            context,
            OpenTubeDatabase::class.java,
            OpenTubeDatabase.DATABASE_NAME
        )
            .addMigrations(
                OpenTubeDatabase.MIGRATION_1_2,
                OpenTubeDatabase.MIGRATION_2_3
            )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideWatchHistoryDao(database: OpenTubeDatabase): WatchHistoryDao {
        return database.watchHistoryDao()
    }
    
    @Provides
    @Singleton
    fun provideFavoriteDao(database: OpenTubeDatabase): FavoriteDao {
        return database.favoriteDao()
    }
    
    @Provides
    @Singleton
    fun provideSubscriptionDao(database: OpenTubeDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
    
    @Provides
    @Singleton
    fun providePlaylistDao(database: OpenTubeDatabase): PlaylistDao {
        return database.playlistDao()
    }
    
    @Provides
    @Singleton
    fun provideLikedCommentsDao(database: OpenTubeDatabase): LikedCommentsDao {
        return database.likedCommentsDao()
    }
}
