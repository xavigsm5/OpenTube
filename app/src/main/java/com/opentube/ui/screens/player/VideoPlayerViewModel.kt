package com.opentube.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opentube.data.local.FavoriteDao
import com.opentube.data.local.FavoriteEntity
import com.opentube.data.local.WatchHistoryDao
import com.opentube.data.local.WatchHistoryEntity
import com.opentube.data.local.SubscriptionDao
import com.opentube.data.local.SubscriptionEntity
import com.opentube.data.models.VideoDetails
import com.opentube.data.models.VideoStream
import com.opentube.data.models.AudioStream
import com.opentube.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerSettings(
    val selectedQuality: VideoStream? = null,
    val selectedAudioTrack: AudioStream? = null,
    val playbackSpeed: Float = 1.0f,
    val subtitlesEnabled: Boolean = false,
    val isFullscreen: Boolean = false,
    val showControls: Boolean = true
)

sealed interface VideoPlayerUiState {
    object Loading : VideoPlayerUiState
    data class Success(
        val videoDetails: VideoDetails,
        val playerSettings: PlayerSettings = PlayerSettings(),
        val isFavorite: Boolean = false,
        val isSubscribed: Boolean = false,
        val currentPosition: Long = 0,
        val comments: List<Comment> = emptyList(),
        val isLoadingComments: Boolean = false
    ) : VideoPlayerUiState
    data class Error(val message: String) : VideoPlayerUiState
}

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val watchHistoryDao: WatchHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val subscriptionDao: SubscriptionDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val videoId: String = checkNotNull(savedStateHandle["videoId"])
    
    private val _uiState = MutableStateFlow<VideoPlayerUiState>(VideoPlayerUiState.Loading)
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()
    
    private val _showSettingsSheet = MutableStateFlow(false)
    val showSettingsSheet: StateFlow<Boolean> = _showSettingsSheet.asStateFlow()
    
    init {
        android.util.Log.d("VideoPlayerViewModel", "=== ViewModel INIT for videoId: $videoId ===")
        loadVideo()
        checkIfFavorite()
        checkIfSubscribed()
        loadComments()
    }
    
    private fun loadVideo() {
        android.util.Log.d("VideoPlayerViewModel", "loadVideo() called for videoId: $videoId")
        viewModelScope.launch {
            videoRepository.getVideoDetails(videoId).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { details ->
                        // Guardar en historial
                        saveToHistory(details)
                        
                        // Similar to LibreTube: Try DASH first (videoOnly), fallback to Progressive
                        val dashStreams = details.videoStreams.filter { it.videoOnly && (it.indexEnd ?: 0) > 0 }
                        val bestVideo = if (dashStreams.isNotEmpty()) {
                            // DASH available - use video-only stream
                            dashStreams.maxByOrNull { it.height ?: 0 }
                        } else {
                            // No DASH - use progressive stream (video+audio combined)
                            details.videoStreams
                                .filter { !it.videoOnly }
                                .maxByOrNull { it.height ?: 0 }
                        }
                        
                        // Only need audio for DASH streams (videoOnly = true)
                        val bestAudio = if (bestVideo?.videoOnly == true) {
                            details.audioStreams
                                .filter { (it.indexEnd ?: 0) > 0 }
                                .maxByOrNull { it.bitrate ?: 0 }
                        } else {
                            null  // Progressive stream already has audio
                        }
                        
                        VideoPlayerUiState.Success(
                            videoDetails = details,
                            playerSettings = PlayerSettings(
                                selectedQuality = bestVideo,
                                selectedAudioTrack = bestAudio
                            )
                        )
                    },
                    onFailure = { exception ->
                        VideoPlayerUiState.Error(
                            exception.message ?: "Error al cargar el video"
                        )
                    }
                )
            }
        }
    }
    
    private fun checkIfFavorite() {
        viewModelScope.launch {
            val favorite = favoriteDao.getFavorite(videoId)
            val currentState = _uiState.value
            if (currentState is VideoPlayerUiState.Success) {
                _uiState.value = currentState.copy(isFavorite = favorite != null)
            }
        }
    }
    
    private fun checkIfSubscribed() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is VideoPlayerUiState.Success) {
                val channelId = currentState.videoDetails.uploaderUrl.substringAfterLast("/")
                val subscription = subscriptionDao.getSubscription(channelId)
                _uiState.value = currentState.copy(isSubscribed = subscription != null)
            }
        }
    }
    
    private suspend fun saveToHistory(details: VideoDetails) {
        watchHistoryDao.insertHistory(
            WatchHistoryEntity(
                videoId = videoId,
                title = details.title,
                uploaderName = details.uploader,
                thumbnail = details.thumbnailUrl,
                duration = details.duration
            )
        )
    }
    
    fun selectQuality(videoStream: VideoStream) {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            _uiState.value = currentState.copy(
                playerSettings = currentState.playerSettings.copy(
                    selectedQuality = videoStream
                )
            )
        }
        hideSettingsSheet()
    }
    
    fun selectAudioTrack(audioStream: AudioStream) {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            _uiState.value = currentState.copy(
                playerSettings = currentState.playerSettings.copy(
                    selectedAudioTrack = audioStream
                )
            )
        }
        hideSettingsSheet()
    }
    
    fun setPlaybackSpeed(speed: Float) {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            _uiState.value = currentState.copy(
                playerSettings = currentState.playerSettings.copy(
                    playbackSpeed = speed
                )
            )
        }
        hideSettingsSheet()
    }
    
    fun toggleSubtitles() {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            _uiState.value = currentState.copy(
                playerSettings = currentState.playerSettings.copy(
                    subtitlesEnabled = !currentState.playerSettings.subtitlesEnabled
                )
            )
        }
    }
    
    fun toggleFullscreen() {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            _uiState.value = currentState.copy(
                playerSettings = currentState.playerSettings.copy(
                    isFullscreen = !currentState.playerSettings.isFullscreen
                )
            )
        }
    }
    
    fun toggleControls() {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            _uiState.value = currentState.copy(
                playerSettings = currentState.playerSettings.copy(
                    showControls = !currentState.playerSettings.showControls
                )
            )
        }
    }
    
    fun updatePlaybackPosition(position: Long) {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            _uiState.value = currentState.copy(currentPosition = position)
            
            // Guardar posición en historial
            viewModelScope.launch {
                watchHistoryDao.getHistoryEntry(videoId)?.let { entry ->
                    watchHistoryDao.insertHistory(
                        entry.copy(position = position)
                    )
                }
            }
        }
    }
    
    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            viewModelScope.launch {
                if (currentState.isFavorite) {
                    favoriteDao.deleteFavoriteById(videoId)
                    _uiState.value = currentState.copy(isFavorite = false)
                } else {
                    favoriteDao.insertFavorite(
                        FavoriteEntity(
                            videoId = videoId,
                            title = currentState.videoDetails.title,
                            uploaderName = currentState.videoDetails.uploader,
                            thumbnail = currentState.videoDetails.thumbnailUrl,
                            duration = currentState.videoDetails.duration
                        )
                    )
                    _uiState.value = currentState.copy(isFavorite = true)
                }
            }
        }
    }
    
    fun toggleSubscription() {
        val currentState = _uiState.value
        if (currentState is VideoPlayerUiState.Success) {
            viewModelScope.launch {
                val channelId = currentState.videoDetails.uploaderUrl.substringAfterLast("/")
                if (currentState.isSubscribed) {
                    subscriptionDao.deleteSubscriptionById(channelId)
                    _uiState.value = currentState.copy(isSubscribed = false)
                } else {
                    subscriptionDao.insertSubscription(
                        SubscriptionEntity(
                            channelId = channelId,
                            channelName = currentState.videoDetails.uploader,
                            channelAvatar = currentState.videoDetails.uploaderAvatar,
                            subscriberCount = currentState.videoDetails.subscriberCount,
                            isVerified = currentState.videoDetails.uploaderVerified
                        )
                    )
                    _uiState.value = currentState.copy(isSubscribed = true)
                }
            }
        }
    }
    
    fun showSettingsSheet() {
        _showSettingsSheet.value = true
    }
    
    fun hideSettingsSheet() {
        _showSettingsSheet.value = false
    }
    
    private fun loadComments() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is VideoPlayerUiState.Success) {
                _uiState.value = currentState.copy(isLoadingComments = true)
            }
            
            videoRepository.getComments(videoId).collect { result ->
                val state = _uiState.value
                if (state is VideoPlayerUiState.Success) {
                    result.fold(
                        onSuccess = { comments ->
                            _uiState.value = state.copy(
                                comments = comments,
                                isLoadingComments = false
                            )
                        },
                        onFailure = { exception ->
                            android.util.Log.e("VideoPlayerViewModel", "Error loading comments", exception)
                            _uiState.value = state.copy(
                                comments = emptyList(),
                                isLoadingComments = false
                            )
                        }
                    )
                }
            }
        }
    }
    
    fun loadMoreComments() {
        // TODO: Implementar paginación de comentarios
        android.util.Log.d("VideoPlayerViewModel", "loadMoreComments() not implemented yet")
    }
}
