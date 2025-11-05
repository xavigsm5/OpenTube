package com.opentube

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.schabi.newpipe.extractor.NewPipe
import com.opentube.data.extractor.NewPipeDownloaderImpl

@HiltAndroidApp
class OpenTubeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar NewPipe Extractor con el downloader correcto
        NewPipe.init(NewPipeDownloaderImpl())
    }
}
