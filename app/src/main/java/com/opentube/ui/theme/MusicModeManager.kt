package com.opentube.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager global para el estado del Modo Música
 * Este ViewModel mantiene el estado de si la app está en "Modo Música" (Spotify-like)
 * o en "Modo Video" (YouTube-like)
 */
@Singleton
class MusicModeManager @Inject constructor() {
    var isMusicModeGlobal by mutableStateOf(false)
        private set
    
    fun toggleMusicMode() {
        isMusicModeGlobal = !isMusicModeGlobal
    }
    
    fun setMusicMode(enabled: Boolean) {
        isMusicModeGlobal = enabled
    }
}
