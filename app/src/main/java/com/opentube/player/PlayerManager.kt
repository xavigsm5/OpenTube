package com.opentube.player

import android.content.Context
import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private var bufferingStartTime = 0L
    private var bufferingCheckRunnable: Runnable? = null
    
    val player: ExoPlayer
        get() {
            if (_player == null) {
                val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        5000,  // minBufferMs (reduced from 15s for faster start)
                        30000, // maxBufferMs (reduced from 50s)
                        250,   // bufferForPlaybackMs (very low for instant start)
                        1000   // bufferForPlaybackAfterRebufferMs (reduced from 1500)
                    )
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()

                _player = ExoPlayer.Builder(context)
                    .setLoadControl(loadControl)
                    .build()
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
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        // Start tracking buffering duration
                        bufferingStartTime = System.currentTimeMillis()
                        startBufferingCheck()
                    }
                    Player.STATE_READY, Player.STATE_ENDED, Player.STATE_IDLE -> {
                        // Stop tracking
                        stopBufferingCheck()
                        bufferingStartTime = 0L
                    }
                }
            }
        })
    }
    
    private fun startBufferingCheck() {
        stopBufferingCheck()
        bufferingCheckRunnable = object : Runnable {
            override fun run() {
                val player = _player ?: return
                if (player.playbackState == Player.STATE_BUFFERING && bufferingStartTime > 0) {
                    val bufferingDuration = System.currentTimeMillis() - bufferingStartTime
                    if (bufferingDuration > 8000) {
                        // Buffering for more than 8 seconds - try to recover
                        android.util.Log.w("PlayerManager", "Buffering stall detected (${bufferingDuration}ms). Attempting recovery by seeking forward.")
                        val currentPos = player.currentPosition
                        player.seekTo(currentPos + 1000) // Seek forward 1 second
                        bufferingStartTime = System.currentTimeMillis() // Reset timer
                    }
                }
                handler.postDelayed(this, 2000) // Check every 2 seconds
            }
        }
        handler.postDelayed(bufferingCheckRunnable!!, 2000)
    }
    
    private fun stopBufferingCheck() {
        bufferingCheckRunnable?.let { handler.removeCallbacks(it) }
        bufferingCheckRunnable = null
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
        stopBufferingCheck()
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
