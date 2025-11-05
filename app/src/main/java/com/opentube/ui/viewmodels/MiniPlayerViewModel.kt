package com.opentube.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.opentube.ui.components.MiniPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class MiniPlayerViewModel @Inject constructor() : ViewModel() {
    private val _miniPlayerState = MutableStateFlow(MiniPlayerState())
    val miniPlayerState: StateFlow<MiniPlayerState> = _miniPlayerState.asStateFlow()
    
    fun showMiniPlayer(
        videoId: String,
        title: String,
        channelName: String,
        isPlaying: Boolean = true,
        player: ExoPlayer? = null
    ) {
        _miniPlayerState.value = MiniPlayerState(
            videoId = videoId,
            title = title,
            channelName = channelName,
            isPlaying = isPlaying,
            isVisible = true,
            player = player
        )
    }
    
    // Solo oculta visualmente el mini player (cuando se expande a pantalla completa)
    fun hideMiniPlayerOnly() {
        _miniPlayerState.value = _miniPlayerState.value.copy(isVisible = false)
    }
    
    // Cierra completamente y libera el player (cuando se presiona el botón X)
    fun closeMiniPlayer() {
        val currentState = _miniPlayerState.value
        currentState.player?.let { player ->
            android.util.Log.d("MiniPlayerViewModel", "Liberando player desde mini reproductor")
            player.release()
        }
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
    
    override fun onCleared() {
        super.onCleared()
        // No liberar el player aquí, se maneja en VideoPlayerScreen
    }
}
