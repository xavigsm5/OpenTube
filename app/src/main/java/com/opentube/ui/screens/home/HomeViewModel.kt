package com.opentube.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opentube.data.models.Video
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
enum class HomeCategory(val displayName: String) {
    TRENDING("Tendencias"),
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
    data class Success(val videos: List<Video>, val selectedCategory: HomeCategory) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * ViewModel for Home screen (Trending videos)
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private var currentCategory = HomeCategory.TRENDING
    
    init {
        android.util.Log.d("HomeViewModel", "=== HomeViewModel INIT ===")
        loadTrending()
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
    
    private fun loadTrending() {
        android.util.Log.d("HomeViewModel", "loadTrending() called")
        loadContentForCategory(HomeCategory.TRENDING)
    }
    
    private fun loadContentForCategory(category: HomeCategory) {
        android.util.Log.d("HomeViewModel", "loadContentForCategory: ${category.displayName}")
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            android.util.Log.d("HomeViewModel", "State set to Loading")
            
            val result = when (category) {
                HomeCategory.TRENDING -> {
                    android.util.Log.d("HomeViewModel", "Calling videoRepository.getTrending()")
                    videoRepository.getTrending()
                }
                HomeCategory.GAMING -> videoRepository.getGamingVideos()
                HomeCategory.MUSIC -> videoRepository.getMusicVideos()
                HomeCategory.SPORTS -> videoRepository.getSportsVideos()
                HomeCategory.LIVE -> videoRepository.getLiveVideos()
            }
            
            result.collect { videoResult ->
                android.util.Log.d("HomeViewModel", "Received result: ${videoResult.isSuccess}")
                _uiState.value = videoResult.fold(
                    onSuccess = { videos ->
                        android.util.Log.d("HomeViewModel", "Success with ${videos.size} videos")
                        HomeUiState.Success(videos, currentCategory)
                    },
                    onFailure = { exception ->
                        android.util.Log.e("HomeViewModel", "Error loading videos", exception)
                        val errorMessage = when {
                            exception.message?.contains("Could not get ytInitialData") == true -> 
                                "YouTube cambió su estructura. Por favor:\n• Espera 5-10 minutos\n• Cambia a datos móviles\n• Usa una VPN\n\nEsto es temporal."
                            exception.message?.contains("blocked this IP") == true -> 
                                "Tu IP fue bloqueada temporalmente por YouTube.\n• Espera 15-30 minutos\n• Cambia de red WiFi\n• Usa datos móviles"
                            exception.message?.contains("LOGIN_REQUIRED") == true ->
                                "YouTube requiere inicio de sesión.\n• Intenta en unos minutos\n• Cambia de red"
                            exception.message?.contains("429") == true ->
                                "Demasiadas peticiones.\n• Espera 10 minutos\n• Cambia de red"
                            else -> "Error: ${exception.message ?: "Desconocido"}\n\nIntenta:\n• Esperar unos minutos\n• Cambiar de red"
                        }
                        HomeUiState.Error(errorMessage)
                    }
                )
            }
        }
    }
    
    fun loadTrending(region: String = "US") {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            videoRepository.getTrending(region).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { videos ->
                        HomeUiState.Success(videos, currentCategory)
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("Could not get ytInitialData") == true -> 
                                "YouTube cambió su estructura. Por favor:\n• Espera 5-10 minutos\n• Cambia a datos móviles\n• Usa una VPN"
                            exception.message?.contains("blocked this IP") == true -> 
                                "Tu IP fue bloqueada temporalmente.\n• Espera 15-30 minutos\n• Cambia de red"
                            exception.message?.contains("LOGIN_REQUIRED") == true ->
                                "YouTube requiere inicio de sesión.\n• Intenta en unos minutos"
                            else -> "Error: ${exception.message ?: "Desconocido"}\n\n• Espera unos minutos\n• Cambia de red"
                        }
                        HomeUiState.Error(errorMessage)
                    }
                )
            }
        }
    }
    
    fun retry() {
        loadContentForCategory(currentCategory)
    }
}
