package com.opentube.data.models

data class Album(
    val url: String,
    val name: String,
    val thumbnail: String,
    val artist: String,
    val year: String? = null
)
