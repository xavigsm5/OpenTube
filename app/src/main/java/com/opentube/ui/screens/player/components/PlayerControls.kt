package com.opentube.ui.screens.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onFullscreenClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isFullscreen: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier,
    videoTitle: String = "",
    resizeMode: Int = 0,
    onResizeModeClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            if (isFullscreen) {
                // Fullscreen mode
                
                // Barra superior con back y título
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Video title
                    Text(
                        text = videoTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                }
                
                // Top right buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Aspect ratio button
                    IconButton(
                        onClick = onResizeModeClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        val resizeIcon = when(resizeMode) {
                            0 -> "FIT"
                            1 -> "FILL"
                            2 -> "ZOOM"
                            3 -> "W"
                            4 -> "H"
                            else -> "FIT"
                        }
                        Text(
                            text = resizeIcon,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                    
                    // Settings
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
            } else {
                // Vertical mode
                
                // Back button top left
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 8.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Top right buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Aspect ratio button
                    IconButton(
                        onClick = onResizeModeClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        val resizeIcon = when(resizeMode) {
                            0 -> "FIT"
                            1 -> "FILL"
                            2 -> "ZOOM"
                            3 -> "W"
                            4 -> "H"
                            else -> "FIT"
                        }
                        Text(
                            text = resizeIcon,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                    }
                    
                    // Settings
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Center controls
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind 10s
                IconButton(
                    onClick = onRewind,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Retroceder 10s",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Main play/pause button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                // Forward 10s
                IconButton(
                    onClick = onForward,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Adelantar 10s",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Bottom bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isFullscreen) 0.7f else 0.5f)
                            )
                        )
                    )
                    .then(if (isFullscreen) Modifier.navigationBarsPadding() else Modifier)
                    .padding(
                        start = if (isFullscreen) 16.dp else 12.dp,
                        end = if (isFullscreen) 16.dp else 12.dp,
                        bottom = if (isFullscreen) 8.dp else 4.dp,
                        top = 0.dp
                    )
            ) {
                // Time and fullscreen button FIRST
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current time / duration
                    Text(
                        text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = if (isFullscreen) 14.sp else 12.sp
                    )
                    
                    // Fullscreen toggle
                    IconButton(
                        onClick = onFullscreenClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Salir de pantalla completa" else "Pantalla completa",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Progress bar BELOW the time
                VideoProgressBar(
                    currentPosition = currentPosition,
                    duration = duration,
                    bufferedPosition = bufferedPosition,
                    onSeek = onSeek
                )
            }
        }
    }
}

@Composable
private fun VideoProgressBar(
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember(currentPosition) { 
        mutableFloatStateOf(currentPosition.toFloat()) 
    }
    var isUserSeeking by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = if (isUserSeeking) sliderPosition else currentPosition.toFloat(),
            onValueChange = { value ->
                isUserSeeking = true
                sliderPosition = value
            },
            onValueChangeFinished = {
                isUserSeeking = false
                onSeek(sliderPosition.toLong())
            },
            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
