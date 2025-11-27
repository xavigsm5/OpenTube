package com.opentube.ui.screens.album

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

sealed interface AlbumUiState {
    object Loading : AlbumUiState
    data class Success(val videos: List<Video>) : AlbumUiState
    data class Error(val message: String) : AlbumUiState
}

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumUiState>(AlbumUiState.Loading)
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    fun loadAlbum(url: String) {
        viewModelScope.launch {
            _uiState.value = AlbumUiState.Loading
            videoRepository.getPlaylistDetails(url).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { videos -> AlbumUiState.Success(videos) },
                    onFailure = { AlbumUiState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }
}
