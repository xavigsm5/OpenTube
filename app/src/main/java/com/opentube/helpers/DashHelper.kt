package com.opentube.helpers

import android.content.Context
import android.util.Base64
import com.opentube.data.models.AudioStream
import com.opentube.data.models.VideoStream
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Helper para crear manifiestos DASH - Copia exacta de LibreTube
 */
object DashHelper {
    
    private val builderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    private val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
    
    private data class AdapSetInfo(
        val mimeType: String,
        val formats: MutableList<Any> = mutableListOf()
    )
    
    /**
     * Crea un manifiesto DASH - exactamente como LibreTube
     */
    fun createManifest(
        videoStreams: List<VideoStream>,
        audioStreams: List<AudioStream>,
        duration: Long
    ): String {
        android.util.Log.d("DashHelper", "Creating manifest with ${videoStreams.size} video streams and ${audioStreams.size} audio streams")
        android.util.Log.d("DashHelper", "Duration received: $duration seconds")
        
        val builder = builderFactory.newDocumentBuilder()
        val doc = builder.newDocument()
        
        val mpd = doc.createElement("MPD")
        mpd.setAttribute("xmlns", "urn:mpeg:dash:schema:mpd:2011")
        mpd.setAttribute("profiles", "urn:mpeg:dash:profile:full:2011")
        mpd.setAttribute("minBufferTime", "PT1.5S")
        mpd.setAttribute("type", "static")
        
        // Solo incluir mediaPresentationDuration si la duración es válida (> 0)
        // Si duration es -1 o 0, ExoPlayer puede inferir la duración de los segmentos
        if (duration > 0) {
            mpd.setAttribute("mediaPresentationDuration", "PT${duration}S")
            android.util.Log.d("DashHelper", "Set mediaPresentationDuration to PT${duration}S")
        } else {
            android.util.Log.w("DashHelper", "Duration is $duration, skipping mediaPresentationDuration (ExoPlayer will infer it)")
        }
        
        val period = doc.createElement("Period")
        
        val adapSetInfos = ArrayList<AdapSetInfo>()
        
        // Procesar video streams
        for (stream in videoStreams) {
            android.util.Log.d("DashHelper", "Video: ${stream.quality}, videoOnly=${stream.videoOnly}, indexEnd=${stream.indexEnd}")
            
            // ignore dual format and OTF streams
            if (!stream.videoOnly || (stream.indexEnd ?: 0) <= 0) {
                android.util.Log.d("DashHelper", "Skipping video stream: videoOnly=${stream.videoOnly}, indexEnd=${stream.indexEnd}")
                continue
            }
            
            val adapSetInfo = adapSetInfos.find { it.mimeType == stream.mimeType }
            if (adapSetInfo != null) {
                adapSetInfo.formats.add(stream)
                continue
            }
            adapSetInfos.add(
                AdapSetInfo(
                    stream.mimeType,
                    mutableListOf(stream)
                )
            )
        }
        
        // Procesar audio streams
        for (stream in audioStreams) {
            if ((stream.indexEnd ?: 0) <= 0) continue
            
            val adapSetInfo = adapSetInfos.find { 
                it.mimeType == stream.mimeType
            }
            if (adapSetInfo != null) {
                adapSetInfo.formats.add(stream)
                continue
            }
            
            adapSetInfos.add(
                AdapSetInfo(
                    stream.mimeType,
                    mutableListOf(stream)
                )
            )
        }
        
        for (adapSet in adapSetInfos) {
            val adapSetElement = doc.createElement("AdaptationSet")
            adapSetElement.setAttribute("mimeType", adapSet.mimeType)
            adapSetElement.setAttribute("startWithSAP", "1")
            adapSetElement.setAttribute("subsegmentAlignment", "true")
            
            val isVideo = adapSet.mimeType.contains("video")
            
            if (isVideo) {
                adapSetElement.setAttribute("scanType", "progressive")
            }
            
            for (format in adapSet.formats) {
                val rep = if (isVideo) {
                    createVideoRepresentation(doc, format as VideoStream)
                } else {
                    createAudioRepresentation(doc, format as AudioStream)
                }
                adapSetElement.appendChild(rep)
            }
            
            period.appendChild(adapSetElement)
        }
        
        mpd.appendChild(period)
        doc.appendChild(mpd)
        
        val domSource = DOMSource(doc)
        val writer = StringWriter()
        val transformer = transformerFactory.newTransformer()
        transformer.transform(domSource, StreamResult(writer))
        
        val manifest = writer.toString()
        android.util.Log.d("DashHelper", "Manifest created successfully, length: ${manifest.length}")
        android.util.Log.d("DashHelper", "Manifest content:\n$manifest")
        
        return manifest
    }
    
    private fun createVideoRepresentation(doc: Document, stream: VideoStream): Element {
        val representation = doc.createElement("Representation")
        representation.setAttribute("codecs", stream.codec ?: "")
        representation.setAttribute("bandwidth", (stream.bitrate ?: 0).toString())
        representation.setAttribute("width", (stream.width ?: 0).toString())
        representation.setAttribute("height", (stream.height ?: 0).toString())
        representation.setAttribute("maxPlayoutRate", "1")
        representation.setAttribute("frameRate", (stream.fps ?: 30).toString())
        
        val baseUrl = doc.createElement("BaseURL")
        baseUrl.appendChild(doc.createTextNode(stream.url))
        
        val segmentBase = doc.createElement("SegmentBase")
        segmentBase.setAttribute("indexRange", "${stream.indexStart}-${stream.indexEnd}")
        
        val initialization = doc.createElement("Initialization")
        initialization.setAttribute("range", "${stream.initStart}-${stream.initEnd}")
        segmentBase.appendChild(initialization)
        
        representation.appendChild(baseUrl)
        representation.appendChild(segmentBase)
        
        return representation
    }
    
    private fun createAudioRepresentation(doc: Document, stream: AudioStream): Element {
        val representation = doc.createElement("Representation")
        representation.setAttribute("bandwidth", (stream.bitrate ?: 0).toString())
        representation.setAttribute("codecs", stream.codec ?: "")
        representation.setAttribute("mimeType", stream.mimeType)
        
        val audioChannelConfiguration = doc.createElement("AudioChannelConfiguration")
        audioChannelConfiguration.setAttribute(
            "schemeIdUri",
            "urn:mpeg:dash:23003:3:audio_channel_configuration:2011"
        )
        audioChannelConfiguration.setAttribute("value", "2")
        
        val baseUrl = doc.createElement("BaseURL")
        baseUrl.appendChild(doc.createTextNode(stream.url))
        
        val segmentBase = doc.createElement("SegmentBase")
        segmentBase.setAttribute("indexRange", "${stream.indexStart}-${stream.indexEnd}")
        
        val initialization = doc.createElement("Initialization")
        initialization.setAttribute("range", "${stream.initStart}-${stream.initEnd}")
        segmentBase.appendChild(initialization)
        
        representation.appendChild(audioChannelConfiguration)
        representation.appendChild(baseUrl)
        representation.appendChild(segmentBase)
        
        return representation
    }
    
    /**
     * Crea URI base64 del manifest DASH - exactamente como LibreTube
     */
    fun createDashSource(
        videoStreams: List<VideoStream>,
        audioStreams: List<AudioStream>,
        duration: Long
    ): android.net.Uri {
        val manifest = createManifest(videoStreams, audioStreams, duration)
        val encoded = Base64.encodeToString(manifest.toByteArray(), Base64.DEFAULT)
        return android.net.Uri.parse("data:application/dash+xml;charset=utf-8;base64,$encoded")
    }
}
