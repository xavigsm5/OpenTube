package com.opentube.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

data class MiniPlayerState(
    val videoId: String = "",
    val title: String = "",
    val channelName: String = "",
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
    modifier: Modifier = Modifier
) {
    // Log para debug
    LaunchedEffect(state.player) {
        android.util.Log.d("MiniPlayer", "Player actualizado: ${state.player != null}, isPlaying: ${state.isPlaying}")
    }
    
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(
            initialOffsetY = { it }, // Desliza desde abajo
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it }, // Desliza hacia abajo
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 16.dp // Aumentado de 8dp a 16dp para mejor visibilidad
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Video player en miniatura
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (state.player != null) {
                        AndroidView(
                            factory = { context ->
                                PlayerView(context).apply {
                                    useController = false
                                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { playerView ->
                                // Actualizar el player cada vez que cambia el estado
                                playerView.player = state.player
                                playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Información del video
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = state.channelName,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Botón Play/Pause
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pausar" else "Reproducir",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Botón Cerrar
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

