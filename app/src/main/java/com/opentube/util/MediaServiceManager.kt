package com.opentube.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.opentube.service.PlaybackService

object MediaServiceManager {
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var serviceInstance: PlaybackService? = null
    
    fun startService(
        context: Context,
        videoTitle: String,
        videoUploader: String,
        videoThumbnail: String,
        videoId: String,
        player: androidx.media3.exoplayer.ExoPlayer
    ) {
        // Almacenar el player en el servicio
        PlaybackService.currentPlayer = player
        
        val intent = Intent(context, PlaybackService::class.java).apply {
            putExtra(PlaybackService.EXTRA_VIDEO_TITLE, videoTitle)
            putExtra(PlaybackService.EXTRA_VIDEO_UPLOADER, videoUploader)
            putExtra(PlaybackService.EXTRA_VIDEO_THUMBNAIL, videoThumbnail)
            putExtra(PlaybackService.EXTRA_VIDEO_ID, videoId)
        }
        
        // Iniciar como foreground service
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        
        // Conectar al MediaController si no está conectado
        if (mediaController == null) {
            connectToMediaController(context, player)
        }
    }
    
    
    private fun connectToMediaController(context: Context, player: androidx.media3.exoplayer.ExoPlayer) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                android.util.Log.d("MediaServiceManager", "MediaController conectado")
                
                // Obtener la instancia del servicio y actualizar la MediaSession con el player
                // Esto se hará a través del companion object
            } catch (e: Exception) {
                android.util.Log.e("MediaServiceManager", "Error conectando MediaController", e)
            }
        }, MoreExecutors.directExecutor())
    }
    
    
    fun updateMetadata(
        context: Context,
        videoTitle: String,
        videoUploader: String,
        videoThumbnail: String,
        videoId: String,
        player: androidx.media3.exoplayer.ExoPlayer
    ) {
        PlaybackService.currentPlayer = player
        
        val intent = Intent(context, PlaybackService::class.java).apply {
            putExtra(PlaybackService.EXTRA_VIDEO_TITLE, videoTitle)
            putExtra(PlaybackService.EXTRA_VIDEO_UPLOADER, videoUploader)
            putExtra(PlaybackService.EXTRA_VIDEO_THUMBNAIL, videoThumbnail)
            putExtra(PlaybackService.EXTRA_VIDEO_ID, videoId)
        }
        context.startService(intent)
    }
    
    fun release() {
        mediaController?.release()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
        controllerFuture = null
    }
}
