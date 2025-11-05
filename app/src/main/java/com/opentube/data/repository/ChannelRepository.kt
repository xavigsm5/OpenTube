package com.opentube.data.repository

import com.opentube.data.models.Channel
import com.opentube.data.models.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for channel operations using NewPipe
 */
@Singleton
class ChannelRepository @Inject constructor() {
    
    /**
     * Get latest videos from channel using tabs
     */
    private suspend fun getLatestVideos(channelInfo: ChannelInfo): Pair<List<Video>, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val videosTab = channelInfo.tabs.find { 
                    it.contentFilters.contains(ChannelTabs.VIDEOS) 
                }
                
                if (videosTab != null) {
                    android.util.Log.d("ChannelRepository", "Found videos tab, loading...")
                    
                    val tabInfo = ChannelTabInfo.getInfo(
                        org.schabi.newpipe.extractor.ServiceList.YouTube,
                        videosTab
                    )
                    
                    val videos = tabInfo.relatedItems.mapNotNull { item ->
                        try {
                            if (item is org.schabi.newpipe.extractor.stream.StreamInfoItem) {
                                Video(
                                    url = item.url,
                                    title = item.name,
                                    thumbnail = item.thumbnails.firstOrNull()?.url ?: "",
                                    duration = item.duration,
                                    views = item.viewCount,
                                    uploadedDate = item.uploadDate?.offsetDateTime()?.toString(),
                                    uploaderName = item.uploaderName ?: "",
                                    uploaderAvatar = item.uploaderAvatars.firstOrNull()?.url,
                                    uploaderUrl = item.uploaderUrl,
                                    uploaderVerified = item.isUploaderVerified,
                                    isLive = item.streamType == org.schabi.newpipe.extractor.stream.StreamType.LIVE_STREAM
                                )
                            } else null
                        } catch (e: Exception) {
                            android.util.Log.e("ChannelRepository", "Error parsing video item", e)
                            null
                        }
                    }
                    
                    android.util.Log.d("ChannelRepository", "Loaded ${videos.size} videos")
                    return@withContext videos to tabInfo.nextPage?.url
                }
                
                android.util.Log.w("ChannelRepository", "No videos tab found")
                return@withContext emptyList<Video>() to null
            } catch (e: Exception) {
                android.util.Log.e("ChannelRepository", "Error getting latest videos", e)
                return@withContext emptyList<Video>() to null
            }
        }
    }
    
    /**
     * Get channel information usando NewPipe
     */
    fun getChannel(channelId: String): Flow<Result<Channel>> = flow {
        try {
            val url = "https://www.youtube.com/channel/$channelId"
            android.util.Log.d("ChannelRepository", "Fetching channel: $channelId")
            
            val channelInfo = withContext(Dispatchers.IO) {
                org.schabi.newpipe.extractor.channel.ChannelInfo.getInfo(
                    org.schabi.newpipe.extractor.ServiceList.YouTube,
                    url
                )
            }
            
            android.util.Log.d("ChannelRepository", "Channel fetched: ${channelInfo.name}")
            
            // Obtener videos del canal
            val (videos, nextPage) = getLatestVideos(channelInfo)
            
            // Crear modelo Channel
            val channel = Channel(
                id = channelId,
                name = channelInfo.name,
                avatarUrl = channelInfo.avatars.firstOrNull()?.url ?: "",
                bannerUrl = channelInfo.banners.firstOrNull()?.url,
                description = channelInfo.description ?: "",
                subscriberCount = channelInfo.subscriberCount,
                verified = channelInfo.isVerified,
                videos = videos,
                nextPage = nextPage
            )
            
            emit(Result.success(channel))
        } catch (e: Exception) {
            android.util.Log.e("ChannelRepository", "Error fetching channel", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Load next page of channel videos
     */
    fun getChannelNextPage(
        channelId: String,
        nextPage: String
    ): Flow<Result<Channel>> = flow {
        try {
            // TODO: Implementar carga de siguiente página con NewPipe
            android.util.Log.d("ChannelRepository", "Next page not implemented yet")
            emit(Result.failure(NotImplementedError("Próxima página no implementada aún")))
        } catch (e: Exception) {
            android.util.Log.e("ChannelRepository", "Error loading next page", e)
            emit(Result.failure(e))
        }
    }
}
