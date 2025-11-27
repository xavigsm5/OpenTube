package com.opentube.ui.screens.playlist

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

sealed interface PlaylistUiState {
    object Loading : PlaylistUiState
    data class Success(val videos: List<Video>) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    fun loadPlaylist(url: String) {
        viewModelScope.launch {
            _uiState.value = PlaylistUiState.Loading
            videoRepository.getPlaylistDetails(url).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { videos -> PlaylistUiState.Success(videos) },
                    onFailure = { PlaylistUiState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }
}
