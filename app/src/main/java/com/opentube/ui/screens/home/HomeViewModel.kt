package com.opentube.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opentube.data.models.Video
import com.opentube.data.extractor.PagedResult
import com.opentube.data.models.Playlist
import com.opentube.data.models.Album
import com.opentube.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Categorías disponibles en la pantalla de inicio
 */
/**
 * Categorías disponibles en la pantalla de inicio
 */
enum class HomeCategory(val displayName: String) {
    ALL("Todos"),
    GAMING("Gaming"),
    MUSIC("Música"),
    SPORTS("Deportes"),
    LIVE("En vivo")
}

/**
 * UI State for Home screen
 */
sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val videos: List<Video>, 
        val selectedCategory: HomeCategory,
        val playlists: List<Playlist> = emptyList(),
        val albums: List<Album> = emptyList(),
        val nextPageUrl: String? = null,
        val isLoadingMore: Boolean = false
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * ViewModel for Home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private var currentCategory = HomeCategory.ALL
    
    init {
        android.util.Log.d("HomeViewModel", "=== HomeViewModel INIT ===")
        loadContentForCategory(HomeCategory.ALL)
    }
    
    fun refresh() {
        android.util.Log.d("HomeViewModel", "refresh() called")
        viewModelScope.launch {
            _isRefreshing.value = true
            loadContentForCategory(currentCategory)
            _isRefreshing.value = false
        }
    }
    
    fun selectCategory(category: HomeCategory) {
        android.util.Log.d("HomeViewModel", "selectCategory: ${category.displayName}")
        if (currentCategory == category) return
        currentCategory = category
        loadContentForCategory(category)
    }
    
    fun loadMore() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success && !currentState.isLoadingMore && currentState.nextPageUrl != null) {
            android.util.Log.d("HomeViewModel", "loadMore() called. Next page: ${currentState.nextPageUrl}")
            viewModelScope.launch {
                _uiState.value = currentState.copy(isLoadingMore = true)
                
                // Determine which method to call based on category
                // Currently only Trending supports pagination via getTrendingPaged
                // TODO: Implement pagination for other categories
                if (currentCategory != HomeCategory.ALL && currentCategory != HomeCategory.MUSIC) {
                     videoRepository.getTrendingPaged(currentState.nextPageUrl).collect { result ->
                        result.fold(
                            onSuccess = { pagedResult ->
                                _uiState.value = currentState.copy(
                                    videos = currentState.videos + pagedResult.items,
                                    nextPageUrl = pagedResult.nextPageUrl,
                                    isLoadingMore = false
                                )
                            },
                            onFailure = {
                                _uiState.value = currentState.copy(isLoadingMore = false)
                            }
                        )
                     }
                } else {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }
    
    private fun loadContentForCategory(category: HomeCategory) {
        android.util.Log.d("HomeViewModel", "loadContentForCategory: ${category.displayName}")
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            if (category == HomeCategory.ALL) {
                loadAllRandomContent()
            } else if (category == HomeCategory.MUSIC) {
                loadMusicContent()
            } else {
                // Use getTrendingPaged for other categories (Gaming, Sports, Live are currently just filtered searches, 
                // but for now we'll use Trending as base or implement specific paged searches later)
                // For simplicity, let's use getTrendingPaged for now if it's not specific
                
                // Actually, Gaming/Sports/Live use specific repository methods. 
                // We should update those to be paged too.
                // For now, let's just use getTrendingPaged for the default case.
                
                val resultFlow = if (category == HomeCategory.GAMING) videoRepository.getGamingVideos()
                                 else if (category == HomeCategory.SPORTS) videoRepository.getSportsVideos()
                                 else if (category == HomeCategory.LIVE) videoRepository.getLiveVideos()
                                 else videoRepository.getTrendingPaged(null) // Use Paged for default/Trending
                
                resultFlow.collect { result ->
                    // Handle both List<Video> and PagedResult<Video>
                    // This is a bit hacky because of type erasure/generics, but let's see
                    
                    // Since we know what we called, we can handle it.
                    if (category == HomeCategory.GAMING || category == HomeCategory.SPORTS || category == HomeCategory.LIVE) {
                         // These return Result<List<Video>>
                         val listResult = result as Result<List<Video>> // Unsafe cast warning, but we know
                         handleListResult(listResult, category)
                    } else {
                        // Trending returns Result<PagedResult<Video>>
                        val pagedResult = result as Result<PagedResult<Video>>
                        handlePagedResult(pagedResult, category)
                    }
                }
            }
        }
    }

    private fun handleListResult(result: Result<List<Video>>, category: HomeCategory) {
        _uiState.value = result.fold(
            onSuccess = { videos ->
                HomeUiState.Success(videos, category)
            },
            onFailure = { exception ->
                HomeUiState.Error(exception.message ?: "Error desconocido")
            }
        )
    }

    private fun handlePagedResult(result: Result<PagedResult<Video>>, category: HomeCategory) {
        _uiState.value = result.fold(
            onSuccess = { pagedResult ->
                HomeUiState.Success(
                    videos = pagedResult.items,
                    selectedCategory = category,
                    nextPageUrl = pagedResult.nextPageUrl
                ) as HomeUiState
            },
            onFailure = { exception ->
                HomeUiState.Error(exception.message ?: "Error desconocido") as HomeUiState
            }
        )
    }

    private suspend fun loadMusicContent() {
        try {
            // Cargar videos, playlists y álbumes en paralelo (o secuencial por ahora)
            var videos: List<Video> = emptyList()
            var playlists: List<Playlist> = emptyList()
            var albums: List<Album> = emptyList()
            
            // 1. Videos de música
            videoRepository.getMusicVideos().collect { result ->
                result.onSuccess { videos = it }
            }
            
            // 2. Playlists (Buscamos "Music Playlists")
            videoRepository.getPlaylists("Music").collect { result ->
                result.onSuccess { playlists = it }
            }
            
            // 3. Álbumes (Buscamos "Full Album")
            videoRepository.getAlbums("Full Album").collect { result ->
                result.onSuccess { albums = it }
            }
            
            if (videos.isNotEmpty() || playlists.isNotEmpty() || albums.isNotEmpty()) {
                _uiState.value = HomeUiState.Success(
                    videos = videos,
                    selectedCategory = HomeCategory.MUSIC,
                    playlists = playlists,
                    albums = albums
                )
            } else {
                _uiState.value = HomeUiState.Error("No se encontró contenido musical")
            }
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error("Error cargando música: ${e.message}")
        }
    }
    
    private suspend fun loadAllRandomContent() {
        // Para "Todos", cargamos contenido variado y lo mezclamos
        try {
            val trendingFlow = videoRepository.getTrending()
            val musicFlow = videoRepository.getMusicVideos()
            val gamingFlow = videoRepository.getGamingVideos()
            val sportsFlow = videoRepository.getSportsVideos()
            
            var allVideos = mutableListOf<Video>()
            
            // Recolectar resultados (esto podría optimizarse con async/await si los flujos fueran suspend functions directas)
            // Como son flujos, los recolectamos secuencialmente por simplicidad, o podríamos usar combine
            
            trendingFlow.collect { result -> result.getOrNull()?.let { allVideos.addAll(it) } }
            musicFlow.collect { result -> result.getOrNull()?.let { allVideos.addAll(it) } }
            gamingFlow.collect { result -> result.getOrNull()?.let { allVideos.addAll(it) } }
            sportsFlow.collect { result -> result.getOrNull()?.let { allVideos.addAll(it) } }
            
            if (allVideos.isNotEmpty()) {
                // Mezclar aleatoriamente
                allVideos.shuffle()
                // Eliminar duplicados por URL
                val distinctVideos = allVideos.distinctBy { it.url }
                _uiState.value = HomeUiState.Success(distinctVideos, HomeCategory.ALL)
            } else {
                _uiState.value = HomeUiState.Error("No se pudo cargar contenido. Verifica tu conexión.")
            }
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error("Error cargando contenido: ${e.message}")
        }
    }
    
    fun retry() {
        loadContentForCategory(currentCategory)
    }
}
