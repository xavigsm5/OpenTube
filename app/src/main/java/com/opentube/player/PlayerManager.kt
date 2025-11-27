package com.opentube.player

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _player: ExoPlayer? = null
    val player: ExoPlayer
        get() {
            if (_player == null) {
                _player = ExoPlayer.Builder(context).build()
            }
            return _player!!
        }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentVideoId = MutableStateFlow<String?>(null)
    val currentVideoId: StateFlow<String?> = _currentVideoId.asStateFlow()
    
    // Cache video details to avoid reloading when transitioning from mini-player
    private var _cachedVideoDetails: com.opentube.data.models.VideoDetails? = null
    val cachedVideoDetails: com.opentube.data.models.VideoDetails?
        get() = _cachedVideoDetails

    init {
        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun prepare(mediaItem: androidx.media3.common.MediaItem) {
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun release() {
        _player?.release()
        _player = null
        _cachedVideoDetails = null
        _currentVideoId.value = null
    }

    fun setVideoId(videoId: String) {
        _currentVideoId.value = videoId
    }
    
    fun cacheVideoDetails(details: com.opentube.data.models.VideoDetails) {
        _cachedVideoDetails = details
    }
    
    fun clearCache() {
        _cachedVideoDetails = null
    }
}
