package com.opentube.data.models

import com.google.gson.annotations.SerializedName

/**
 * Video model from Piped API
 */
data class Video(
    @SerializedName("url")
    val url: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("thumbnail")
    val thumbnail: String,
    
    @SerializedName("uploaderName")
    val uploaderName: String,
    
    @SerializedName("uploaderUrl")
    val uploaderUrl: String?,
    
    @SerializedName("uploaderAvatar")
    val uploaderAvatar: String?,
    
    @SerializedName("uploadedDate")
    val uploadedDate: String?,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("views")
    val views: Long,
    
    @SerializedName("uploaderVerified")
    val uploaderVerified: Boolean = false,
    
    @SerializedName("isShort")
    val isShort: Boolean = false,
    
    @SerializedName("isLive")
    val isLive: Boolean = false
) {
    val videoId: String
        get() {
            return when {
                url.contains("/watch?v=") -> url.substringAfter("/watch?v=").substringBefore("&")
                url.contains("/shorts/") -> url.substringAfter("/shorts/").substringBefore("?")
                url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
                else -> url.substringAfter("/watch?v=").substringBefore("&")
            }
        }
}

/**
 * Detailed video information
 */
data class VideoDetails(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("uploadDate")
    val uploadDate: String,
    
    @SerializedName("uploader")
    val uploader: String,
    
    @SerializedName("uploaderUrl")
    val uploaderUrl: String,
    
    @SerializedName("uploaderAvatar")
    val uploaderAvatar: String,
    
    @SerializedName("uploaderVerified")
    val uploaderVerified: Boolean,
    
    @SerializedName("uploaderSubscriberCount")
    val subscriberCount: Long,
    
    @SerializedName("likes")
    val likes: Long,
    
    @SerializedName("dislikes")
    val dislikes: Long,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("views")
    val views: Long,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String,
    
    @SerializedName("videoStreams")
    val videoStreams: List<VideoStream>,
    
    @SerializedName("audioStreams")
    val audioStreams: List<AudioStream>,
    
    @SerializedName("relatedStreams")
    val relatedStreams: List<Video>,
    
    @SerializedName("liveNow")
    val liveNow: Boolean = false,
    
    @SerializedName("hlsUrl")
    val hlsUrl: String? = null
)

/**
 * Video stream quality option
 */
data class VideoStream(
    @SerializedName("url")
    val url: String,
    
    @SerializedName("format")
    val format: String,
    
    @SerializedName("quality")
    val quality: String,
    
    @SerializedName("mimeType")
    val mimeType: String,
    
    @SerializedName("codec")
    val codec: String?,
    
    @SerializedName("videoOnly")
    val videoOnly: Boolean,
    
    @SerializedName("bitrate")
    val bitrate: Int?,
    
    @SerializedName("initStart")
    val initStart: Int?,
    
    @SerializedName("initEnd")
    val initEnd: Int?,
    
    @SerializedName("indexStart")
    val indexStart: Int?,
    
    @SerializedName("indexEnd")
    val indexEnd: Int?,
    
    @SerializedName("width")
    val width: Int?,
    
    @SerializedName("height")
    val height: Int?,
    
    @SerializedName("fps")
    val fps: Int?
)

/**
 * Audio stream option
 */
data class AudioStream(
    @SerializedName("url")
    val url: String,
    
    @SerializedName("format")
    val format: String,
    
    @SerializedName("quality")
    val quality: String,
    
    @SerializedName("mimeType")
    val mimeType: String,
    
    @SerializedName("codec")
    val codec: String?,
    
    @SerializedName("audioTrackId")
    val audioTrackId: String?,
    
    @SerializedName("audioTrackName")
    val audioTrackName: String?,
    
    @SerializedName("bitrate")
    val bitrate: Int?,
    
    @SerializedName("initStart")
    val initStart: Int?,
    
    @SerializedName("initEnd")
    val initEnd: Int?,
    
    @SerializedName("indexStart")
    val indexStart: Int?,
    
    @SerializedName("indexEnd")
    val indexEnd: Int?
)
