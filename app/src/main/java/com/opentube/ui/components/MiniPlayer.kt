package com.opentube.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
    val isExpanded: Boolean = false,
    val player: androidx.media3.exoplayer.ExoPlayer? = null,
    val sourceRect: androidx.compose.ui.geometry.Rect? = null
)

@Composable
fun MiniPlayer(
    state: MiniPlayerState,
    onPlayPauseClick: () -> Unit,
    onClose: () -> Unit,
    onClick: () -> Unit,
    onExpandedChange: (Boolean) -> Unit = {},
    onSeekForward: () -> Unit = {},
    onSeekBackward: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Track expanded local state: first click expands, second click opens full player
    var isLocalExpanded by remember { mutableStateOf(false) }
    
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
        Box(
            modifier = modifier
                .animateContentSize(animationSpec = tween(300))
                .then(
                    if (isLocalExpanded) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.width(180.dp)
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .clickable {
                    if (isLocalExpanded) {
                        // Second click: open full player
                        onClick()
                        isLocalExpanded = false
                        onExpandedChange(false)
                    } else {
                        // First click: expand mini player
                        isLocalExpanded = true
                        onExpandedChange(true)
                    }
                }
        ) {
            // Video area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                if (state.player != null) {
                    AndroidView(
                        factory = { context: android.content.Context ->
                            androidx.media3.ui.PlayerView(context).apply {
                                useController = false
                                resizeMode = if (isLocalExpanded) {
                                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                } else {
                                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }
                                player = state.player
                            }
                        },
                        update = { playerView: androidx.media3.ui.PlayerView ->
                            playerView.player = state.player
                            playerView.resizeMode = if (isLocalExpanded) {
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                            } else {
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            }
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

                // Overlay buttons - Play/Pause at bottom left, Close at top right
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .size(if (isLocalExpanded) 44.dp else 36.dp)
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(if (isLocalExpanded) 32.dp else 24.dp)
                    )
                }

                IconButton(
                    onClick = {
                        isLocalExpanded = false
                        onExpandedChange(false)
                        onClose()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(if (isLocalExpanded) 40.dp else 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(if (isLocalExpanded) 28.dp else 20.dp)
                    )
                }
            }
        }
    }
}
