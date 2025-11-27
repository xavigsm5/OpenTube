package com.opentube.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.opentube.ui.components.MiniPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val playerManager: com.opentube.player.PlayerManager
) : ViewModel() {
    private val _miniPlayerState = MutableStateFlow(MiniPlayerState())
    val miniPlayerState: StateFlow<MiniPlayerState> = _miniPlayerState.asStateFlow()
    
    fun showMiniPlayer(
        videoId: String,
        title: String,
        channelName: String,
        thumbnailUrl: String,
        isPlaying: Boolean = true,
        player: ExoPlayer? = null
    ) {
        _miniPlayerState.value = MiniPlayerState(
            videoId = videoId,
            title = title,
            channelName = channelName,
            thumbnailUrl = thumbnailUrl,
            isPlaying = isPlaying,
            isVisible = true,
            player = playerManager.player // Use shared player
        )
    }
    
    // Solo oculta visualmente el mini player (cuando se expande a pantalla completa)
    fun hideMiniPlayerOnly() {
        _miniPlayerState.value = _miniPlayerState.value.copy(isVisible = false)
    }
    
    // Cierra completamente (cuando se presiona el botón X)
    fun closeMiniPlayer() {
        val currentState = _miniPlayerState.value
        // Pause player instead of releasing
        playerManager.pause()
        
        _miniPlayerState.value = currentState.copy(
            isVisible = false,
            player = null
        )
    }
    
    // Compatibilidad: cierra el mini player
    fun hideMiniPlayer() {
        closeMiniPlayer()
    }
    
    fun togglePlayPause() {
        val currentState = _miniPlayerState.value
        currentState.player?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
        _miniPlayerState.value = currentState.copy(
            isPlaying = !currentState.isPlaying
        )
    }
    
    fun updatePlayState(isPlaying: Boolean) {
        _miniPlayerState.value = _miniPlayerState.value.copy(isPlaying = isPlaying)
    }
    
    fun seekForward() {
        _miniPlayerState.value.player?.let { player ->
            player.seekTo(player.currentPosition + 10000)
        }
    }
    
    fun seekBackward() {
        _miniPlayerState.value.player?.let { player ->
            player.seekTo((player.currentPosition - 10000).coerceAtLeast(0))
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // No liberar el player aquí, se maneja en VideoPlayerScreen
    }
}
