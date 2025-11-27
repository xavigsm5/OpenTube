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
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import com.opentube.data.models.Video
import com.opentube.data.models.Playlist
import com.opentube.data.models.Album
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
import com.opentube.data.local.dataStore
import kotlinx.coroutines.flow.collect
// import org.schabi.newpipe.extractor.Localization
import org.schabi.newpipe.extractor.Page

/**
 * Helper class para usar NewPipe Extractor y obtener videos de YouTube
 */
@Singleton
class NewPipeHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
    private var contentCountry: String = "ES"
    private var contentLanguage: String = "es"
    
    init {
        // Observar cambios en configuración de idioma/país
        scope.launch {
            context.dataStore.data.collect { preferences ->
                contentLanguage = preferences[androidx.datastore.preferences.core.stringPreferencesKey("content_language")] ?: "es"
                contentCountry = preferences[androidx.datastore.preferences.core.stringPreferencesKey("content_country")] ?: "ES"
                
                try {
                    // TODO: Fix Localization import and usage
                    // val localization = Localization(contentCountry, contentLanguage)
                    // ServiceList.YouTube.setLocalization(localization)
                    android.util.Log.d("NewPipeHelper", "Localization updated: $contentCountry - $contentLanguage")
                } catch (e: Exception) {
                    android.util.Log.e("NewPipeHelper", "Error updating localization", e)
                }
            }
        }
    }
    
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
     * Buscar videos por query con paginación
     */
    suspend fun searchVideosPaged(query: String, pageUrl: String? = null): Result<PagedResult<Video>> = withContext(Dispatchers.IO) {
        try {
            val itemsPage = if (pageUrl == null) {
                // Primera página - obtener SearchInfo
                val searchInfo = SearchInfo.getInfo(
                    ServiceList.YouTube,
                    ServiceList.YouTube.searchQHFactory.fromQuery(query)
                )
                // Crear InfoItemsPage manualmente desde SearchInfo
                org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage(
                    searchInfo.getRelatedItems(),
                    searchInfo.getNextPage(),
                    emptyList()
                )
            } else {
                // Páginas siguientes - obtener directamente InfoItemsPage
                SearchInfo.getMoreItems(
                    ServiceList.YouTube,
                    ServiceList.YouTube.searchQHFactory.fromQuery(query),
                    Page(pageUrl)
                )
            }
            
            val videos = itemsPage.items
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
            
            val nextPage = itemsPage.nextPage?.url
            Result.success(PagedResult(videos, nextPage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Buscar videos por query (legacy wrapper)
     */
    suspend fun searchVideos(query: String): Result<List<Video>> {
        return searchVideosPaged(query, null).map { it.items }
    }

    /**
     * Obtener videos trending/populares con paginación
     */
    suspend fun getTrendingVideosPaged(pageUrl: String? = null): Result<PagedResult<Video>> = withContext(Dispatchers.IO) {
        try {
            val kiosk = ServiceList.YouTube.getKioskList().defaultKioskExtractor
            val trendingUrl = if (contentCountry.isNotEmpty() && contentCountry != "GLOBAL") {
                "https://www.youtube.com/feed/trending?gl=$contentCountry"
            } else {
                kiosk.url
            }
            
            val itemsPage = if (pageUrl == null) {
                // Primera página - obtener KioskInfo
                val kioskInfo = org.schabi.newpipe.extractor.kiosk.KioskInfo.getInfo(ServiceList.YouTube, trendingUrl)
                // Crear InfoItemsPage manualmente desde KioskInfo
                org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage(
                    kioskInfo.getRelatedItems(),
                    kioskInfo.getNextPage(),
                    emptyList()
                )
            } else {
                // Páginas siguientes - obtener directamente InfoItemsPage
                org.schabi.newpipe.extractor.kiosk.KioskInfo.getMoreItems(
                    ServiceList.YouTube,
                    trendingUrl,
                    Page(pageUrl)
                )
            }
            
            val videos = itemsPage.items
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
                .filter { !it.isLive }
            
            val nextPage = itemsPage.nextPage?.url
            Result.success(PagedResult(videos, nextPage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener videos trending/populares (legacy wrapper)
     */
    suspend fun getTrendingVideos(): Result<List<Video>> {
        return getTrendingVideosPaged(null).map { it.items }
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
            // Queries personalizadas con mucha variedad
            val searchQueries = listOf(
                // Fútbol y deportes
                "shorts football goals",
                "shorts soccer skills",
                "shorts messi ronaldo",
                "shorts champions league",
                "shorts world cup moments",
                "shorts basketball dunks",
                "shorts nba highlights",
                "shorts tennis rallies",
                "shorts extreme sports",
                "shorts skateboarding tricks",
                "shorts surfing waves",
                "shorts snowboarding",
                "shorts parkour",
                "shorts martial arts",
                "shorts boxing knockouts",
                "shorts ufc highlights",
                
                // Música
                "shorts music viral",
                "shorts latest songs",
                "shorts music video",
                "shorts concert moments",
                "shorts guitar solo",
                "shorts piano cover",
                "shorts rap freestyle",
                "shorts singing talent",
                "shorts dance music",
                "shorts edm drops",
                "shorts rock performance",
                "shorts jazz improvisation",
                "shorts classical music",
                "shorts kpop dance",
                "shorts reggaeton",
                
                // Comedia y entretenimiento
                "shorts funny moments",
                "shorts comedy sketches",
                "shorts stand up comedy",
                "shorts pranks",
                "shorts fails compilation",
                "shorts memes",
                "shorts tiktok funny",
                "shorts vine energy",
                "shorts jokes",
                "shorts roasts",
                
                // Cine y películas
                "shorts movie trailers",
                "shorts film clips",
                "shorts behind the scenes",
                "shorts movie scenes",
                "shorts cinema moments",
                "shorts actor interviews",
                "shorts upcoming movies",
                "shorts superhero clips",
                "shorts action scenes",
                "shorts movie easter eggs",
                
                // Curiosidades y educación
                "shorts science facts",
                "shorts history facts",
                "shorts did you know",
                "shorts amazing facts",
                "shorts psychology facts",
                "shorts space universe",
                "shorts technology explained",
                "shorts life hacks",
                "shorts brain teasers",
                "shorts mystery solved",
                "shorts conspiracy theories",
                "shorts philosophy",
                "shorts quantum physics",
                "shorts biology facts",
                
                // Gaming
                "shorts gaming moments",
                "shorts gameplay highlights",
                "shorts esports plays",
                "shorts minecraft builds",
                "shorts fortnite wins",
                "shorts valorant clutch",
                "shorts league of legends",
                "shorts cod warzone",
                "shorts gta funny",
                "shorts roblox",
                "shorts among us",
                "shorts game reviews",
                "shorts speedrun",
                
                // Comida
                "shorts cooking recipes",
                "shorts street food",
                "shorts food review",
                "shorts dessert making",
                "shorts chef skills",
                "shorts food asmr",
                "shorts baking",
                "shorts restaurant food",
                "shorts food challenge",
                "shorts tasty recipes",
                
                // Viajes
                "shorts travel destinations",
                "shorts travel vlog",
                "shorts beautiful places",
                "shorts adventure travel",
                "shorts japan travel",
                "shorts europe travel",
                "shorts beach paradise",
                "shorts mountain hiking",
                "shorts city tours",
                "shorts travel tips",
                
                // Animales
                "shorts cute animals",
                "shorts funny cats",
                "shorts funny dogs",
                "shorts wildlife",
                "shorts animal facts",
                "shorts pets compilation",
                "shorts exotic animals",
                "shorts ocean life",
                "shorts bird videos",
                "shorts animal rescue",
                
                // Naturaleza y paisajes
                "shorts nature beautiful",
                "shorts satisfying video",
                "shorts oddly satisfying",
                "shorts relaxing nature",
                "shorts waterfalls",
                "shorts sunset timelapse",
                "shorts northern lights",
                "shorts weather phenomena",
                
                // Arte y creatividad
                "shorts art timelapse",
                "shorts drawing tutorial",
                "shorts painting process",
                "shorts digital art",
                "shorts sculpture making",
                "shorts graffiti art",
                "shorts pottery making",
                "shorts crafts diy",
                
                // Fitness y salud
                "shorts gym transformation",
                "shorts workout routine",
                "shorts calisthenics",
                "shorts yoga poses",
                "shorts fitness motivation",
                "shorts bodybuilding",
                "shorts running tips",
                "shorts healthy recipes",
                
                // Tecnología
                "shorts tech review",
                "shorts gadget unboxing",
                "shorts smartphone tips",
                "shorts ai technology",
                "shorts coding tips",
                "shorts app review",
                "shorts tech news",
                "shorts innovation",
                
                // Moda y estilo
                "shorts fashion trends",
                "shorts outfit ideas",
                "shorts makeup tutorial",
                "shorts hairstyle",
                "shorts style tips",
                "shorts fashion show",
                
                // Motivación
                "shorts motivation",
                "shorts inspirational",
                "shorts success stories",
                "shorts discipline",
                "shorts mindset",
                "shorts quotes",
                
                // Magia y trucos
                "shorts magic tricks",
                "shorts card tricks",
                "shorts illusions",
                "shorts street magic",
                
                // Automóviles
                "shorts car drift",
                "shorts supercar",
                "shorts motorcycle",
                "shorts racing",
                "shorts car review",
                
                // Aviación
                "shorts aviation",
                "shorts airplane landing",
                "shorts flight simulator",
                "shorts pilot view",
                
                // Fotografía y videografía
                "shorts photography tips",
                "shorts camera tricks",
                "shorts videography",
                "shorts editing tutorial"
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
    
    /**
     * Buscar playlists
     */
    suspend fun searchPlaylists(query: String): Result<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val searchInfo = SearchInfo.getInfo(
                ServiceList.YouTube,
                ServiceList.YouTube.searchQHFactory.fromQuery(query)
            )
            
            val playlists = searchInfo.getRelatedItems()
                .filterIsInstance<org.schabi.newpipe.extractor.playlist.PlaylistInfoItem>()
                .map { item ->
                    Playlist(
                        url = item.url,
                        name = item.name,
                        thumbnail = item.thumbnails.maxByOrNull { it.height }?.url ?: "",
                        uploaderName = item.uploaderName ?: "",
                        videoCount = item.streamCount
                    )
                }
            
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Buscar álbumes (simulado buscando playlists con "album")
     */
    suspend fun searchAlbums(query: String): Result<List<Album>> = withContext(Dispatchers.IO) {
        try {
            val searchInfo = SearchInfo.getInfo(
                ServiceList.YouTube,
                ServiceList.YouTube.searchQHFactory.fromQuery("$query album")
            )
            
            val albums = searchInfo.getRelatedItems()
                .filterIsInstance<org.schabi.newpipe.extractor.playlist.PlaylistInfoItem>()
                .map { item ->
                    Album(
                        url = item.url,
                        name = item.name,
                        thumbnail = item.thumbnails.maxByOrNull { it.height }?.url ?: "",
                        artist = item.uploaderName ?: ""
                    )
                }
            
            Result.success(albums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener detalles de una playlist (videos)
     */
    suspend fun getPlaylistDetails(url: String): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val playlistInfo = PlaylistInfo.getInfo(ServiceList.YouTube, url)
            
            val videos = playlistInfo.getRelatedItems()
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
}
