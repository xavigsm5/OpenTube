package com.opentube.utils

import kotlin.math.ln
import kotlin.math.pow

/**
 * Format duration from seconds to MM:SS or HH:MM:SS
 */
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

/**
 * Format view count to compact format (e.g., 1.2K, 3.4M)
 */
fun formatViews(views: Long): String {
    if (views < 1000) return "$views vistas"
    
    val exp = (ln(views.toDouble()) / ln(1000.0)).toInt()
    val suffix = "KMBT"[exp - 1]
    val value = views / 1000.0.pow(exp.toDouble())
    
    return String.format("%.1f%c vistas", value, suffix)
}

/**
 * Format subscriber count
 */
fun formatSubscribers(count: Long): String {
    if (count < 1000) return "$count suscriptores"
    
    val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
    val suffix = "KMBT"[exp - 1]
    val value = count / 1000.0.pow(exp.toDouble())
    
    return String.format("%.1f%c suscriptores", value, suffix)
}

/**
 * Extract video ID from URL
 */
fun extractVideoId(url: String): String? {
    val patterns = listOf(
        "(?<=watch\\?v=)[^&#]*",
        "(?<=youtu.be/)[^?&#]*",
        "(?<=embed/)[^?&#]*"
    )
    
    for (pattern in patterns) {
        val regex = Regex(pattern)
        val match = regex.find(url)
        if (match != null) {
            return match.value
        }
    }
    
    return null
}

/**
 * Extract channel ID from URL
 */
fun extractChannelId(url: String): String? {
    val patterns = listOf(
        "(?<=channel/)[^?&#/]*",
        "(?<=c/)[^?&#/]*",
        "(?<=user/)[^?&#/]*",
        "(?<=@)[^?&#/]*"
    )
    
    for (pattern in patterns) {
        val regex = Regex(pattern)
        val match = regex.find(url)
        if (match != null) {
            return match.value
        }
    }
    
    return null
}
