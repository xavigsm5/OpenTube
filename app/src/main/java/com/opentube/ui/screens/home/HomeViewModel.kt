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
 * UI State for Home screen
 */
sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val videos: List<Video>) : HomeUiState
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
    
    init {
        loadTrending()
    }
    
    fun loadTrending(region: String = "US") {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            videoRepository.getTrending(region).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { videos ->
                        HomeUiState.Success(videos)
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
        loadTrending()
    }
}
