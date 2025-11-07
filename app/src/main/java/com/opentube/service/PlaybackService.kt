package com.opentube.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.opentube.MainActivity
import com.opentube.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var currentVideoTitle: String = "OpenTube"
    private var currentVideoUploader: String = ""
    private var currentVideoThumbnail: String = ""
    
    companion object {
        const val EXTRA_VIDEO_TITLE = "video_title"
        const val EXTRA_VIDEO_UPLOADER = "video_uploader"
        const val EXTRA_VIDEO_THUMBNAIL = "video_thumbnail"
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_PLAYER = "player"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "playback_channel"
        
        // Referencia global al player actual
        var currentPlayer: ExoPlayer? = null
    }
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("PlaybackService", "onCreate llamado")
        
        // Esperar a que se establezca el player desde MediaServiceManager
        // La MediaSession se creará en onStartCommand cuando tengamos el player
    }
    
    private fun initializeMediaSession() {
        val player = currentPlayer
        if (player == null) {
            android.util.Log.e("PlaybackService", "No hay player disponible para crear MediaSession")
            return
        }
        
        if (mediaSession != null) {
            android.util.Log.d("PlaybackService", "MediaSession ya existe")
            return
        }
        
        android.util.Log.d("PlaybackService", "Creando MediaSession con player compartido")
        
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val connectionResult = super.onConnect(session, controller)
                    val sessionCommands = connectionResult.availableSessionCommands.buildUpon()
                    return MediaSession.ConnectionResult.accept(
                        sessionCommands.build(),
                        connectionResult.availablePlayerCommands
                    )
                }
            })
            .build()
            
        // Configurar listener para eventos del player
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                android.util.Log.d("PlaybackService", "Estado de reproducción cambiado: $playbackState")
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        android.util.Log.d("PlaybackService", "Reproducción terminada")
                        stopSelf()
                    }
                    Player.STATE_READY, Player.STATE_BUFFERING -> {
                        updateNotification()
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                android.util.Log.d("PlaybackService", "isPlaying cambiado: $isPlaying")
                updateNotification()
            }
        })
        
        android.util.Log.d("PlaybackService", "MediaSession creada exitosamente")
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentVideoTitle)
            .setContentText(currentVideoUploader)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(currentPlayer?.isPlaying == true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        
        // Agregar estilo de media usando MediaSession
        mediaSession?.let { session ->
            try {
                builder.setStyle(
                    androidx.media3.session.MediaStyleNotificationHelper.MediaStyle(session)
                )
            } catch (e: Exception) {
                android.util.Log.e("PlaybackService", "Error setting media style", e)
            }
        }
        
        // Agregar acciones básicas
        currentPlayer?.let { p ->
            val playPauseIntent = if (p.isPlaying) {
                PendingIntent.getService(
                    this,
                    1,
                    Intent(this, PlaybackService::class.java).apply {
                        action = "PAUSE"
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getService(
                    this,
                    1,
                    Intent(this, PlaybackService::class.java).apply {
                        action = "PLAY"
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            
            val icon = if (p.isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            
            val label = if (p.isPlaying) "Pausar" else "Reproducir"
            
            builder.addAction(
                NotificationCompat.Action(icon, label, playPauseIntent)
            )
        }
        
        return builder.build()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("PlaybackService", "onStartCommand llamado")
        
        intent?.let {
            // Manejar acciones de la notificación
            when (it.action) {
                "PLAY" -> {
                    android.util.Log.d("PlaybackService", "Acción PLAY")
                    currentPlayer?.play()
                }
                "PAUSE" -> {
                    android.util.Log.d("PlaybackService", "Acción PAUSE")
                    currentPlayer?.pause()
                }
                else -> {
                    // Actualizar metadata del video
                    currentVideoTitle = it.getStringExtra(EXTRA_VIDEO_TITLE) ?: "OpenTube"
                    currentVideoUploader = it.getStringExtra(EXTRA_VIDEO_UPLOADER) ?: ""
                    currentVideoThumbnail = it.getStringExtra(EXTRA_VIDEO_THUMBNAIL) ?: ""
                    val videoId = it.getStringExtra(EXTRA_VIDEO_ID) ?: ""
                    
                    android.util.Log.d("PlaybackService", "Metadata recibida: $currentVideoTitle")
                    
                    // Inicializar MediaSession si tenemos player y aún no existe
                    if (currentPlayer != null && mediaSession == null) {
                        initializeMediaSession()
                    }
                    
                    // Actualizar metadata
                    updateMediaMetadata(currentVideoTitle, currentVideoUploader, currentVideoThumbnail, videoId)
                }
            }
            
            // Actualizar notificación
            updateNotification()
        }
        
        return super.onStartCommand(intent, flags, startId)
    }
    
    private fun updateMediaMetadata(
        title: String,
        artist: String,
        thumbnailUrl: String,
        videoId: String
    ) {
        // Cargar thumbnail de forma asíncrona
        serviceScope.launch {
            val bitmap = loadThumbnail(thumbnailUrl)
            
            val metadata = MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(android.net.Uri.parse(thumbnailUrl))
                .also { builder ->
                    bitmap?.let { 
                        // Convertir Bitmap a ByteArray
                        val stream = java.io.ByteArrayOutputStream()
                        it.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                        builder.setArtworkData(stream.toByteArray(), MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    }
                }
                .setExtras(Bundle().apply {
                    putString("videoId", videoId)
                })
                .build()
            
            currentPlayer?.let { p ->
                // Actualizar metadata del MediaItem actual o crear uno nuevo
                val mediaItem = MediaItem.Builder()
                    .setMediaId(videoId)
                    .setMediaMetadata(metadata)
                    .build()
                
                // Solo actualizar metadata si el player ya tiene contenido
                if (p.currentMediaItem != null) {
                    p.replaceMediaItem(p.currentMediaItemIndex, mediaItem)
                }
            }
        }
    }
    
    private suspend fun loadThumbnail(url: String): Bitmap? {
        if (url.isEmpty()) return null
        
        return try {
            val imageLoader = ImageLoader(this)
            val request = ImageRequest.Builder(this)
                .data(url)
                .allowHardware(false)
                .build()
            
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaybackService", "Error loading thumbnail", e)
            null
        }
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        currentPlayer = null
        super.onDestroy()
    }
}
