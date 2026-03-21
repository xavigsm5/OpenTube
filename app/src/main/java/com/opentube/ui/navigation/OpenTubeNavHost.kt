package com.opentube.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.opentube.ui.screens.home.HomeScreen
import com.opentube.ui.screens.splash.SplashScreen
import com.opentube.ui.screens.search.SearchScreen
import com.opentube.ui.screens.player.VideoPlayerScreen
import com.opentube.ui.screens.channel.ChannelScreen
import com.opentube.ui.screens.library.LibraryScreen
import com.opentube.ui.screens.subscriptions.SubscriptionsScreen
import com.opentube.ui.screens.settings.SettingsScreen
import com.opentube.ui.components.MiniPlayer
import com.opentube.ui.viewmodels.MiniPlayerViewModel

/**
 * Main navigation host for the app
 */
@Composable
fun OpenTubeNavHost(
    navController: NavHostController = rememberNavController(),
    miniPlayerViewModel: MiniPlayerViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val miniPlayerState by miniPlayerViewModel.miniPlayerState.collectAsState()
    
    // Show bottom bar only on main screens
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Home.route,
        Screen.Subscriptions.route,
        Screen.Library.route,
        Screen.Settings.route
    ) && !miniPlayerState.isExpanded
    
    // Hide mini player when on video player screen
    val isOnVideoPlayer = currentDestination?.route?.startsWith("video/") == true
    
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        // Píldora principal
                    Row(
                        modifier = Modifier
                            .height(68.dp)
                            .clip(CircleShape)
                            .hazeChild(state = hazeState, shape = CircleShape)
                            .background(Color(0x661A1A1A)) // Glassmorphism translúcido
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassNavItem(
                            icon = Icons.Default.Home,
                            label = "Inicio",
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        
                        LiquidGlassNavItem(
                            icon = Icons.Default.Subscriptions,
                            label = "Suscripciones",
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Subscriptions.route } == true,
                            onClick = {
                                navController.navigate(Screen.Subscriptions.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        
                        LiquidGlassNavItem(
                            icon = Icons.Default.VideoLibrary,
                            label = "Biblioteca",
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Library.route } == true,
                            onClick = {
                                navController.navigate(Screen.Library.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Botón Buscar separado
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .hazeChild(state = hazeState, shape = CircleShape)
                            .background(Color(0x661A1A1A)) // Mismo estilo glass
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .clickable {
                                navController.navigate(Screen.Search.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().haze(state = hazeState, backgroundColor = MaterialTheme.colorScheme.background)) {
            // NavHost - Sin bottom padding para que el contenido pase por detrás del blur
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 0.dp, // No double padding, rely on inner Scaffold TopAppBars
                        bottom = 0.dp // No bottom padding to allow blur
                    ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onVideoClick = { videoId ->
                        android.util.Log.d("Navigation", "Home: Expanding video: $videoId")
                        miniPlayerViewModel.showPlayer(videoId = videoId)
                    },

                    onSearchClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            
            composable(Screen.Shorts.route) {
                com.opentube.ui.screens.shorts.ShortsScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onChannelClick = { channelId ->
                        navController.navigate(Screen.Channel.createRoute(channelId))
                    }
                )
            }
            
            composable(
                route = Screen.Search.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(500))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(500)
                    ) + fadeOut(animationSpec = tween(500))
                }
            ) {
                SearchScreen(
                    onBackClick = { navController.navigateUp() },
                    onVideoClick = { videoId ->
                        android.util.Log.d("Navigation", "Search: Expanding video: $videoId")
                        miniPlayerViewModel.showPlayer(videoId = videoId)
                    },
                    onChannelClick = { channelId ->
                        navController.navigate(Screen.Channel.createRoute(channelId))
                    }
                )
            }

            composable(
                route = Screen.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
                com.opentube.ui.screens.playlist.PlaylistScreen(
                    playlistId = playlistId,
                    onBackClick = { navController.navigateUp() },
                    onVideoClick = { videoId ->
                        miniPlayerViewModel.showPlayer(videoId = videoId)
                    }
                )
            }

            composable(
                route = Screen.AlbumDetail.route,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType })
            ) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
                com.opentube.ui.screens.album.AlbumScreen(
                    albumId = albumId,
                    onBackClick = { navController.navigateUp() },
                    onVideoClick = { videoId ->
                        miniPlayerViewModel.showPlayer(videoId = videoId)
                    }
                )
            }
            
            composable(
                route = Screen.Channel.route,
                arguments = listOf(
                    navArgument("channelId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val channelId = backStackEntry.arguments?.getString("channelId") ?: return@composable
                ChannelScreen(
                    channelId = channelId,
                    onNavigateBack = { navController.navigateUp() },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.VideoPlayer.createRoute(videoId))
                    }
                )
            }
            
            composable(Screen.Subscriptions.route) {
                SubscriptionsScreen(
                    onChannelClick = { channelId ->
                        navController.navigate(Screen.Channel.createRoute(channelId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
            
            composable(Screen.Library.route) {
                LibraryScreen(
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.VideoPlayer.createRoute(videoId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                com.opentube.ui.screens.settings.SettingsScreen()
            }
        } // Fin del NavHost
            
        // Shared Player Overlay
        if (miniPlayerState.isVisible) {
            var dragOffset by remember { mutableFloatStateOf(0f) }
            val isExpanded = miniPlayerState.isExpanded

            if (isExpanded) {
                // Expanded Full Player with Drag support
                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val screenHeight = with(androidx.compose.ui.platform.LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
                
                // Calculate alpha based on drag (1.0f when full, 0f when fully dragged down)
                val alpha = (1f - (dragOffset / screenHeight)).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = dragOffset
                        }
                        .background(MaterialTheme.colorScheme.background.copy(alpha = alpha))
                ) {
                    VideoPlayerScreen(
                        videoId = miniPlayerState.videoId,
                        existingPlayer = miniPlayerState.player,
                        onDrag = { dragY -> dragOffset = dragY },
                        onNavigateBack = {
                            dragOffset = 0f
                            miniPlayerViewModel.showMiniPlayer(
                                videoId = miniPlayerState.videoId,
                                title = miniPlayerState.title,
                                channelName = miniPlayerState.channelName,
                                thumbnailUrl = miniPlayerState.thumbnailUrl,
                                isPlaying = miniPlayerState.isPlaying,
                                player = miniPlayerState.player
                            )
                        },
                        onChannelClick = { channelId ->
                            dragOffset = 0f
                            miniPlayerViewModel.hideMiniPlayer()
                            navController.navigate(Screen.Channel.createRoute(channelId))
                        },
                        onVideoClick = { newVideoId ->
                            dragOffset = 0f
                            miniPlayerViewModel.showPlayer(newVideoId)
                        },
                        onMinimize = { title, channel, thumbnailUrl, isPlaying, player ->
                            dragOffset = 0f
                            miniPlayerViewModel.showMiniPlayer(
                                videoId = miniPlayerState.videoId,
                                title = title,
                                channelName = channel,
                                thumbnailUrl = thumbnailUrl,
                                isPlaying = isPlaying,
                                player = player
                            )
                        }
                    )
                }
            } else {
                // Mini Player (Floating PiP)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            bottom = if (showBottomBar) paddingValues.calculateBottomPadding() + 16.dp else 16.dp,
                            end = 16.dp
                        )
                ) {
                    MiniPlayer(
                        state = miniPlayerState,
                        onPlayPauseClick = { miniPlayerViewModel.togglePlayPause() },
                        onClose = { miniPlayerViewModel.closeMiniPlayer() },
                        onClick = { miniPlayerViewModel.expandPlayer() },
                        onSeekForward = { miniPlayerViewModel.seekForward() },
                        onSeekBackward = { miniPlayerViewModel.seekBackward() }
                    )
                }
            }
        }
    } // Fin del Box padre
    } // Fin del Scaffold
} // Fin de la función OpenTubeNavHost

@Composable
private fun LiquidGlassNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Anima el color entre blanco (seleccionado) y gris (no seleccionado)
    val tintColor by animateColorAsState(if (selected) Color.White else Color(0xFFAAAAAA), label = "icon_color")
    
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(26.dp)
            )
            // Mostrar texto siempre, no solo cuando está seleccionado
            Text(
                text = label,
                color = tintColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
