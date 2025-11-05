package com.opentube.ui.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.opentube.ui.screens.player.components.PlayerControls
import com.opentube.ui.screens.player.components.PlayerSettingsSheet
import com.opentube.ui.components.VideoCard
import com.opentube.helpers.DashHelper
import com.opentube.util.PictureInPictureUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    onNavigateBack: () -> Unit,
    onChannelClick: ((String) -> Unit)? = null,
    onVideoClick: ((String) -> Unit)? = null,
    onMinimize: ((title: String, channel: String, isPlaying: Boolean, player: androidx.media3.exoplayer.ExoPlayer?) -> Unit)? = null,
    existingPlayer: androidx.media3.exoplayer.ExoPlayer? = null,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    android.util.Log.d("VideoPlayerScreen", "=== VideoPlayerScreen COMPOSABLE STARTED for videoId: $videoId ===")
    
    val uiState by viewModel.uiState.collectAsState()
    val showSettingsSheet by viewModel.showSettingsSheet.collectAsState()
    val context = LocalContext.current
    
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isMinimizing by remember { mutableStateOf(false) }
    
    // Manage fullscreen
    val activity = context as? Activity
    val isFullscreen = (uiState as? VideoPlayerUiState.Success)?.playerSettings?.isFullscreen ?: false
    
    // Gestionar orientación y barras del sistema según modo pantalla completa
    LaunchedEffect(isFullscreen) {
        activity?.requestedOrientation = if (isFullscreen) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        
        // Ocultar/mostrar barras del sistema en pantalla completa
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, !isFullscreen)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController?.apply {
                if (isFullscreen) {
                    hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                }
            }
        }
    }
    
    // Ocultar controles automáticamente después de 3 segundos
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }
    
    // Actualizar posición de reproducción cada segundo
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            exoPlayer?.let { player ->
                currentPosition = player.currentPosition
                duration = player.duration.coerceAtLeast(0)
                bufferedPosition = player.bufferedPosition
                isPlaying = player.isPlaying
                viewModel.updatePlaybackPosition(currentPosition)
            }
            delay(100)
        }
    }
    
    // Handle back button - Minimize video player
    BackHandler(enabled = !isFullscreen) {
        val state = uiState
        if (state is VideoPlayerUiState.Success && onMinimize != null) {
            // Log para debug
            android.util.Log.d("VideoPlayerScreen", "Minimizando - Player: ${exoPlayer != null}, isPlaying: $isPlaying")
            // Marcar que estamos minimizando para NO liberar el player
            isMinimizing = true
            // Minimizar el reproductor
            onMinimize(
                state.videoDetails.title,
                state.videoDetails.uploader,
                isPlaying,
                exoPlayer
            )
            // Navegar hacia atrás DESPUÉS de minimizar
            onNavigateBack()
        } else {
            onNavigateBack()
        }
    }
    
    // Handle fullscreen back button
    BackHandler(enabled = isFullscreen) {
        viewModel.toggleFullscreen()
    }
    
    when (val state = uiState) {
        is VideoPlayerUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is VideoPlayerUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Volver")
                    }
                }
            }
        }
        
        is VideoPlayerUiState.Success -> {
            val videoDetails = state.videoDetails
            val playerSettings = state.playerSettings
            
            // Inicializar ExoPlayer
            DisposableEffect(videoId) {
                // Reutilizar player del mini reproductor si existe
                val player = if (existingPlayer != null && exoPlayer == null) {
                    android.util.Log.d("VideoPlayerScreen", "Usando player del mini reproductor")
                    existingPlayer
                } else if (exoPlayer != null) {
                    android.util.Log.d("VideoPlayerScreen", "Reutilizando player actual")
                    exoPlayer!!
                } else {
                    android.util.Log.d("VideoPlayerScreen", "Creando nuevo player")
                    val dataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(context)
                    ExoPlayer.Builder(context)
                        .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory))
                        .build()
                }
                
                android.util.Log.d("VideoPlayerScreen", "Inicializando player para video: $videoId")
                android.util.Log.d("VideoPlayerScreen", "Streams de video: ${videoDetails.videoStreams.size}")
                android.util.Log.d("VideoPlayerScreen", "Streams de audio: ${videoDetails.audioStreams.size}")
                
                // Solo preparar el stream si es un player nuevo
                val shouldPrepareStream = (existingPlayer == null || player.currentMediaItem == null)
                
                if (shouldPrepareStream) {
                    android.util.Log.d("VideoPlayerScreen", "Preparando nuevo stream")
                
                // Verificar si hay streams DASH válidos (con indexEnd > 0)
                val validDashStreams = videoDetails.videoStreams.filter { 
                    it.videoOnly && (it.indexEnd ?: 0) > 0 
                }
                
                // Usar streams progresivos si no hay DASH válidos
                val progressiveStreams = videoDetails.videoStreams.filter { !it.videoOnly }
                
                android.util.Log.d("VideoPlayerScreen", "Streams DASH válidos: ${validDashStreams.size}")
                android.util.Log.d("VideoPlayerScreen", "Streams progresivos: ${progressiveStreams.size}")
                android.util.Log.d("VideoPlayerScreen", "HLS disponible: ${videoDetails.hlsUrl != null}")
                
                if (videoDetails.videoStreams.isNotEmpty()) {
                    try {
                        // Usar la calidad seleccionada desde el inicio
                        val selectedStream = state.playerSettings.selectedQuality
                        
                        if (selectedStream != null && selectedStream.url.isNotEmpty()) {
                            android.util.Log.d("VideoPlayerScreen", "Using selected quality: ${selectedStream.quality} (${selectedStream.height}p)")
                            
                            if (selectedStream.videoOnly) {
                                // DASH stream - necesita audio separado
                                val selectedAudio = state.playerSettings.selectedAudioTrack 
                                    ?: videoDetails.audioStreams.maxByOrNull { it.bitrate ?: 0 }
                                
                                if (selectedAudio != null) {
                                    android.util.Log.d("VideoPlayerScreen", "Creating DASH manifest for ${selectedStream.quality}")
                                    
                                    val dashUri = DashHelper.createDashSource(
                                        videoStreams = listOf(selectedStream),
                                        audioStreams = listOf(selectedAudio),
                                        duration = videoDetails.duration
                                    )
                                    
                                    val mediaItem = androidx.media3.common.MediaItem.Builder()
                                        .setUri(dashUri)
                                        .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_MPD)
                                        .build()
                                    
                                    player.setMediaItem(mediaItem)
                                    player.prepare()
                                    player.playWhenReady = true
                                    
                                    android.util.Log.d("VideoPlayerScreen", "Player prepared with DASH stream")
                                }
                            } else {
                                // Progressive stream (video + audio juntos)
                                android.util.Log.d("VideoPlayerScreen", "Using progressive stream")
                                
                                val mediaItem = androidx.media3.common.MediaItem.Builder()
                                    .setUri(selectedStream.url)
                                    .build()
                                
                                player.setMediaItem(mediaItem)
                                player.prepare()
                                player.playWhenReady = true
                                
                                android.util.Log.d("VideoPlayerScreen", "Player prepared with progressive stream")
                            }
                        }
                        // Fallback: HLS stream
                        else if (videoDetails.hlsUrl != null) {
                            android.util.Log.d("VideoPlayerScreen", "Using HLS stream: ${videoDetails.hlsUrl}")
                            
                            // Use HlsMediaSource with YouTubeHlsPlaylistParser like LibreTube
                            val hlsMediaSourceFactory = androidx.media3.exoplayer.hls.HlsMediaSource.Factory(
                                androidx.media3.datasource.DefaultDataSource.Factory(context)
                            ).setPlaylistParserFactory(com.opentube.util.YouTubeHlsPlaylistParser.Factory())
                            
                            val mediaItem = androidx.media3.common.MediaItem.Builder()
                                .setUri(videoDetails.hlsUrl)
                                .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                                .build()
                            
                            val mediaSource = hlsMediaSourceFactory.createMediaSource(mediaItem)
                            player.setMediaSource(mediaSource)
                            player.prepare()
                            player.playWhenReady = true
                            
                            android.util.Log.d("VideoPlayerScreen", "Player prepared with HLS stream")
                        }
                        
                        // Restore playback position
                        if (state.currentPosition > 0) {
                            player.seekTo(state.currentPosition)
                        }
                        
                        // Set playback speed
                        player.setPlaybackSpeed(playerSettings.playbackSpeed)
                    } catch (e: Exception) {
                        android.util.Log.e("VideoPlayerScreen", "Error setting up player", e)
                        e.printStackTrace()
                    }
                } else {
                    android.util.Log.e("VideoPlayerScreen", "No video streams available!")
                }
                } else {
                    android.util.Log.d("VideoPlayerScreen", "Usando stream del mini reproductor")
                    player.playWhenReady = true
                }
                
                // Auto-reproducir siempre
                player.playWhenReady = true
                player.prepare()
                
                exoPlayer = player
                
                onDispose {
                    // Solo liberar el player cuando no estamos minimizando
                    if (!isMinimizing) {
                        android.util.Log.d("VideoPlayerScreen", "Liberando player (no minimizado)")
                        player.release()
                    } else {
                        android.util.Log.d("VideoPlayerScreen", "Manteniendo player activo (minimizado)")
                    }
                }
            }
            
            // Actualizar velocidad de reproducción cuando cambia
            LaunchedEffect(playerSettings.playbackSpeed) {
                exoPlayer?.setPlaybackSpeed(playerSettings.playbackSpeed)
            }
            
            // Change quality/audio when changed - exactamente como LibreTube
            LaunchedEffect(playerSettings.selectedQuality, playerSettings.selectedAudioTrack) {
                exoPlayer?.let { player ->
                    if (videoDetails.videoStreams.isEmpty()) return@let
                    
                    val savedPosition = player.currentPosition
                    val wasPlaying = player.isPlaying
                    
                    try {
                        // Usar la calidad seleccionada por el usuario
                        val selectedStream = playerSettings.selectedQuality
                        
                        if (selectedStream != null && selectedStream.url.isNotEmpty()) {
                            android.util.Log.d("VideoPlayerScreen", "Changing to quality: ${selectedStream.quality} (${selectedStream.height}p)")
                            
                            if (selectedStream.videoOnly) {
                                // Video DASH - necesita audio separado
                                val selectedAudio = playerSettings.selectedAudioTrack 
                                    ?: videoDetails.audioStreams.maxByOrNull { it.bitrate ?: 0 }
                                
                                if (selectedAudio != null) {
                                    val dashUri = DashHelper.createDashSource(
                                        videoStreams = listOf(selectedStream),
                                        audioStreams = listOf(selectedAudio),
                                        duration = videoDetails.duration
                                    )
                                    
                                    val mediaItem = androidx.media3.common.MediaItem.Builder()
                                        .setUri(dashUri)
                                        .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_MPD)
                                        .build()
                                    
                                    player.setMediaItem(mediaItem)
                                    player.prepare()
                                    player.seekTo(savedPosition)
                                    player.playWhenReady = wasPlaying
                                }
                            } else {
                                // Progressive stream (video + audio juntos)
                                val mediaItem = androidx.media3.common.MediaItem.Builder()
                                    .setUri(selectedStream.url)
                                    .build()
                                
                                player.setMediaItem(mediaItem)
                                player.prepare()
                                player.seekTo(savedPosition)
                                player.playWhenReady = wasPlaying
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("VideoPlayerScreen", "Error changing quality", e)
                        e.printStackTrace()
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Video player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isFullscreen) 16f / 9f else 16f / 9f)
                        .background(androidx.compose.ui.graphics.Color.Black)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    // Si desliza más de 150px hacia abajo, minimizar
                                    if (dragOffsetY > 150f && !isFullscreen) {
                                        val state = uiState
                                        if (state is VideoPlayerUiState.Success && onMinimize != null) {
                                            // Marcar que estamos minimizando
                                            isMinimizing = true
                                            onMinimize(
                                                state.videoDetails.title,
                                                state.videoDetails.uploader,
                                                isPlaying,
                                                exoPlayer
                                            )
                                            // Navegar hacia atrás DESPUÉS de minimizar
                                            onNavigateBack()
                                        }
                                    }
                                    dragOffsetY = 0f
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    if (!isFullscreen) {
                                        dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                    }
                                }
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showControls = !showControls
                        }
                ) {
                    // ExoPlayer view
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = false
                                // Modo FIT: mantiene proporción del video sin recortar
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        update = { playerView ->
                            playerView.player = exoPlayer
                            // Forzar FIT siempre, especialmente en pantalla completa
                            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Controles personalizados
                    PlayerControls(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        bufferedPosition = bufferedPosition,
                        onPlayPauseClick = {
                            exoPlayer?.let { player ->
                                if (player.isPlaying) player.pause() else player.play()
                            }
                        },
                        onSeek = { position ->
                            exoPlayer?.seekTo(position)
                        },
                        onRewind = {
                            exoPlayer?.let { player ->
                                player.seekTo((player.currentPosition - 10000).coerceAtLeast(0))
                            }
                        },
                        onForward = {
                            exoPlayer?.let { player ->
                                player.seekTo((player.currentPosition + 10000).coerceAtMost(player.duration))
                            }
                        },
                        onFullscreenClick = {
                            viewModel.toggleFullscreen()
                        },
                        onSettingsClick = {
                            viewModel.showSettingsSheet()
                        },
                        isFullscreen = isFullscreen,
                        visible = showControls,
                        videoTitle = videoDetails.title
                    )
                }
                
                // Video details (only in portrait mode)
                if (!isFullscreen) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // Video info
                        item {
                            com.opentube.ui.screens.player.components.VideoInfoSection(
                                title = videoDetails.title,
                                uploader = videoDetails.uploader,
                                uploaderAvatar = videoDetails.uploaderAvatar,
                                uploaderVerified = videoDetails.uploaderVerified,
                                views = videoDetails.views,
                                likes = videoDetails.likes,
                                uploadDate = videoDetails.uploadDate,
                                description = videoDetails.description,
                                subscriberCount = videoDetails.subscriberCount,
                                isFavorite = state.isFavorite,
                                isSubscribed = state.isSubscribed,
                                onFavoriteClick = { viewModel.toggleFavorite() },
                                onSubscribeClick = { viewModel.toggleSubscription() },
                                onChannelClick = { 
                                    val channelId = videoDetails.uploaderUrl.substringAfterLast("/")
                                    onChannelClick?.invoke(channelId)
                                },
                                onShareClick = {
                                    // TODO: Implement share functionality
                                }
                            )
                        }
                        
                        // Comments section
                        item {
                            CommentsSection(
                                comments = state.comments,
                                isLoading = state.isLoadingComments,
                                onLoadMore = { viewModel.loadMoreComments() }
                            )
                        }
                        
                        // Related videos
                        item {
                            Text(
                                text = "Videos relacionados",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        items(videoDetails.relatedStreams) { relatedVideo ->
                            VideoCard(
                                video = relatedVideo,
                                onClick = { 
                                    onVideoClick?.invoke(relatedVideo.videoId)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Settings sheet
            if (showSettingsSheet) {
                PlayerSettingsSheet(
                    videoStreams = videoDetails.videoStreams,
                    audioStreams = videoDetails.audioStreams,
                    currentQuality = playerSettings.selectedQuality,
                    currentAudioTrack = playerSettings.selectedAudioTrack,
                    currentSpeed = playerSettings.playbackSpeed,
                    subtitlesEnabled = playerSettings.subtitlesEnabled,
                    onQualitySelected = { viewModel.selectQuality(it) },
                    onAudioSelected = { viewModel.selectAudioTrack(it) },
                    onSpeedSelected = { viewModel.setPlaybackSpeed(it) },
                    onSubtitlesToggle = { viewModel.toggleSubtitles() },
                    onDismiss = { viewModel.hideSettingsSheet() }
                )
            }
        }
    }
}
