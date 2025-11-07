package com.opentube.data.extractor

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.VideoStream
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import com.opentube.data.models.Video
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class para usar NewPipe Extractor y obtener videos de YouTube
 */
@Singleton
class NewPipeHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Obtener información de un video por su ID
     */
    suspend fun getVideoInfo(videoId: String): Result<Video> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, url)
            
            Result.success(
                Video(
                    url = url,
                    title = streamInfo.name,
                    thumbnail = streamInfo.thumbnails.maxByOrNull { it.height }?.url ?: "",
                    uploaderName = streamInfo.uploaderName,
                    uploaderUrl = streamInfo.uploaderUrl,
                    uploaderAvatar = streamInfo.uploaderAvatars.maxByOrNull { it.height }?.url,
                    uploadedDate = streamInfo.uploadDate?.offsetDateTime()?.toString(),
                    duration = streamInfo.duration,
                    views = streamInfo.viewCount,
                    uploaderVerified = streamInfo.isUploaderVerified,
                    isShort = streamInfo.duration < 60,
                    isLive = streamInfo.streamType == StreamType.LIVE_STREAM
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener la URL del stream de video en la mejor calidad disponible
     */
    suspend fun getVideoStreamUrl(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, url)
            
            // Obtener el stream de mejor calidad
            val bestStream = streamInfo.videoStreams
                .filter { !it.isVideoOnly } // Streams con audio
                .maxByOrNull { it.height } 
                ?: streamInfo.videoOnlyStreams.maxByOrNull { it.height }
            
            if (bestStream != null) {
                Result.success(bestStream.content ?: "")
            } else {
                Result.failure(Exception("No se encontraron streams disponibles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Buscar videos por query
     */
    suspend fun searchVideos(query: String): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val searchInfo = SearchInfo.getInfo(
                ServiceList.YouTube,
                ServiceList.YouTube.searchQHFactory.fromQuery(query)
            )
            
            val videos = searchInfo.relatedItems
                .filterIsInstance<org.schabi.newpipe.extractor.stream.StreamInfoItem>()
                .map { item ->
                    Video(
                        url = item.url,
                        title = item.name,
                        thumbnail = item.thumbnails.maxByOrNull { it.height }?.url ?: "",
                        uploaderName = item.uploaderName ?: "",
                        uploaderUrl = item.uploaderUrl,
                        uploaderAvatar = item.uploaderAvatars.maxByOrNull { it.height }?.url,
                        uploadedDate = item.uploadDate?.offsetDateTime()?.toString(),
                        duration = item.duration,
                        views = item.viewCount,
                        uploaderVerified = item.isUploaderVerified,
                        isShort = item.duration < 60,
                        isLive = item.streamType == StreamType.LIVE_STREAM
                    )
                }
            
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener videos trending/populares
     */
    suspend fun getTrendingVideos(): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val kiosk = ServiceList.YouTube.getKioskList().defaultKioskExtractor
            val kioskInfo = org.schabi.newpipe.extractor.kiosk.KioskInfo.getInfo(ServiceList.YouTube, kiosk.url)
            
            val videos = kioskInfo.relatedItems
                .filterIsInstance<org.schabi.newpipe.extractor.stream.StreamInfoItem>()
                .map { item ->
                    Video(
                        url = item.url,
                        title = item.name,
                        thumbnail = item.thumbnails.maxByOrNull { it.height }?.url ?: "",
                        uploaderName = item.uploaderName ?: "",
                        uploaderUrl = item.uploaderUrl,
                        uploaderAvatar = item.uploaderAvatars.maxByOrNull { it.height }?.url,
                        uploadedDate = item.uploadDate?.offsetDateTime()?.toString(),
                        duration = item.duration,
                        views = item.viewCount,
                        uploaderVerified = item.isUploaderVerified,
                        isShort = item.duration > 0 && item.duration < 60,
                        isLive = item.streamType == StreamType.LIVE_STREAM
                    )
                }
            
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener videos de Deportes
     */
    suspend fun getSportsVideos(): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val searchResults = searchVideos("sports highlights soccer football basketball")
            searchResults.getOrNull()?.take(30)?.let { videos ->
                Result.success(videos.filter { it.views > 10000 })
            } ?: Result.failure(Exception("No se pudieron cargar videos de deportes"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener videos de Gaming
     */
    suspend fun getGamingVideos(): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            // Buscar videos de gaming populares
            val searchResults = searchVideos("gaming highlights gameplay")
            searchResults.getOrNull()?.take(30)?.let { videos ->
                Result.success(videos.filter { it.views > 10000 })
            } ?: Result.failure(Exception("No se pudieron cargar videos de gaming"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener videos de Música
     */
    suspend fun getMusicVideos(): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            // Buscar videos musicales populares
            val searchResults = searchVideos("music video official")
            searchResults.getOrNull()?.take(30)?.let { videos ->
                Result.success(videos.filter { it.views > 10000 })
            } ?: Result.failure(Exception("No se pudieron cargar videos de música"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener videos en vivo
     */
    suspend fun getLiveVideos(): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val searchResults = searchVideos("live")
            searchResults.getOrNull()?.let { videos ->
                // Filtrar solo videos en vivo
                Result.success(videos.filter { it.isLive })
            } ?: Result.failure(Exception("No se pudieron cargar videos en vivo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener Shorts (videos cortos de YouTube)
     */
    suspend fun getShorts(page: Int = 0): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val searchQueries = listOf(
                "shorts viral",
                "shorts funny",
                "shorts memes",
                "shorts amazing",
                "shorts pets",
                "shorts food",
                "shorts sports",
                "shorts music",
                "shorts comedy",
                "shorts gaming",
                "shorts dance",
                "shorts talent",
                "shorts magic",
                "shorts fails"
            )
            
            val query = searchQueries[page % searchQueries.size]
            
            val searchResults = searchVideos(query)
            val shorts = searchResults.getOrNull()?.filter { it.duration > 0 && it.duration < 61 }
                ?: throw Exception("No se pudieron cargar shorts")
            
            Result.success(shorts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener sugerencias de búsqueda
     */
    suspend fun getSearchSuggestions(query: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val suggestionExtractor = ServiceList.YouTube.suggestionExtractor
            val suggestions = suggestionExtractor.suggestionList(query)
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
