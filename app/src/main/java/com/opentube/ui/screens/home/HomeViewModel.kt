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
    
    private var currentCategory = HomeCategory.TRENDING
    
    init {
        loadTrending()
    }
    
    fun selectCategory(category: HomeCategory) {
        if (currentCategory == category) return
        currentCategory = category
        loadContentForCategory(category)
    }
    
    private fun loadContentForCategory(category: HomeCategory) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            // Por ahora todas cargan trending, pero puedes extenderlo
            val region = when (category) {
                HomeCategory.TRENDING -> "US"
                HomeCategory.GAMING -> "US" // Podrías filtrar por gaming
                HomeCategory.MUSIC -> "US"  // Podrías filtrar por música
                HomeCategory.LIVE -> "US"   // Podrías filtrar por en vivo
            }
            
            videoRepository.getTrending(region).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { videos ->
                        HomeUiState.Success(videos, currentCategory)
                    },
                    onFailure = { exception ->
                        HomeUiState.Error(
                            exception.message ?: "Error desconocido"
                        )
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
                        HomeUiState.Error(
                            exception.message ?: "Error desconocido"
                        )
                    }
                )
            }
        }
    }
    
    fun retry() {
        loadContentForCategory(currentCategory)
    }
}
