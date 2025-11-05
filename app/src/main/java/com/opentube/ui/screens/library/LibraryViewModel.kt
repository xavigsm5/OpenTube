package com.opentube.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opentube.data.local.FavoriteDao
import com.opentube.data.local.FavoriteEntity
import com.opentube.data.local.WatchHistoryDao
import com.opentube.data.local.WatchHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LibraryTab {
    object History : LibraryTab
    object Favorites : LibraryTab
    object Playlists : LibraryTab
}

data class LibraryUiState(
    val historyVideos: List<WatchHistoryEntity> = emptyList(),
    val favoriteVideos: List<FavoriteEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao,
    private val favoriteDao: FavoriteDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    
    init {
        loadLibrary()
    }
    
    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load history
                watchHistoryDao.getAllHistory().collect { history ->
                    _uiState.value = _uiState.value.copy(
                        historyVideos = history,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
        
        viewModelScope.launch {
            // Load favorites
            favoriteDao.getAllFavorites().collect { favorites ->
                _uiState.value = _uiState.value.copy(
                    favoriteVideos = favorites
                )
            }
        }
    }
    
    fun deleteFromHistory(videoId: String) {
        viewModelScope.launch {
            watchHistoryDao.deleteHistoryById(videoId)
        }
    }
    
    fun deleteFromFavorites(videoId: String) {
        viewModelScope.launch {
            favoriteDao.deleteFavoriteById(videoId)
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            watchHistoryDao.clearAllHistory()
        }
    }
}
