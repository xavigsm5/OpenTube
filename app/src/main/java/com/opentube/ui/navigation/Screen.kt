package com.opentube.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Shorts : Screen("shorts")
    object Search : Screen("search")
    object VideoPlayer : Screen("video/{videoId}") {
        fun createRoute(videoId: String) = "video/$videoId"
    }
    object Channel : Screen("channel/{channelId}") {
        fun createRoute(channelId: String) = "channel/$channelId"
    }
    object Library : Screen("library")
    object Subscriptions : Screen("subscriptions")
    object History : Screen("history")
    object Favorites : Screen("favorites")
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object Settings : Screen("settings")
}
