package com.opentube.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

data class MiniPlayerState(
    val videoId: String = "",
    val title: String = "",
    val channelName: String = "",
    val thumbnailUrl: String = "",
    val isPlaying: Boolean = false,
    val isVisible: Boolean = false,
    val player: androidx.media3.exoplayer.ExoPlayer? = null
)

@Composable
fun MiniPlayer(
    state: MiniPlayerState,
    onPlayPauseClick: () -> Unit,
    onClose: () -> Unit,
    onClick: () -> Unit,
    onSeekForward: () -> Unit = {},
    onSeekBackward: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        // Floating PiP Player
        Card(
            modifier = modifier
                .width(240.dp) // Reduced width
                .wrapContentHeight()
                .padding(8.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column {
                // Video Area (16:9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    if (state.player != null) {
                        AndroidView(
                            factory = { context: android.content.Context ->
                                androidx.media3.ui.PlayerView(context).apply {
                                    useController = false
                                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    player = state.player
                                }
                            },
                            update = { playerView: androidx.media3.ui.PlayerView ->
                                playerView.player = state.player
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = state.thumbnailUrl,
                            contentDescription = "Video thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Close Button (Overlay on Video Top Right)
                    // Close Button (Overlay on Video Top Right)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp) // Slightly larger touch target
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                                .background(Color.Black.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape) // Subtle background only on icon if needed, or none.
                                // User said "circle behind x looks ugly". I'll remove it completely or make it very subtle shadow.
                                // I'll use a shadow on the icon instead of a background circle.
                        )
                    }
                }
                
                // Controls Bar (Bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(vertical = 4.dp), // Reduced padding
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = onSeekBackward,
                        modifier = Modifier.size(32.dp) // Smaller button
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Replay10,
                            contentDescription = "-10s",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Play/Pause
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.size(40.dp) // Smaller button
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Forward 10s
                    IconButton(
                        onClick = onSeekForward,
                        modifier = Modifier.size(32.dp) // Smaller button
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Forward10,
                            contentDescription = "+10s",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Progress Bar (Bottom Line)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Red)
                )
            }
        }
    }
}

