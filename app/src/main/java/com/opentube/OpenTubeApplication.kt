package com.opentube

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import org.schabi.newpipe.extractor.NewPipe
import com.opentube.data.extractor.NewPipeDownloaderImpl

@HiltAndroidApp
class OpenTubeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar NewPipe Extractor con el downloader correcto
        NewPipe.init(NewPipeDownloaderImpl())
        
        // Crear canal de notificaciones para reproducción de media
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "playback_channel",
                "Reproducción de Media",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción de video"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
