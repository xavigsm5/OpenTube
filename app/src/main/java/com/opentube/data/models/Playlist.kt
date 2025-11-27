package com.opentube.data.models

data class Playlist(
    val url: String,
    val name: String,
    val thumbnail: String,
    val uploaderName: String,
    val videoCount: Long,
    val streamCount: Long = 0
)
