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
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
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
    onResizeModeClick: () -> Unit = {}
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
            // Barra superior en pantalla completa
            if (isFullscreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        // Degradado oscuro para que el título se lea bien sobre el video
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding() // Respeta la barra de estado del sistema
                        .padding(top = 40.dp, start = 28.dp, end = 28.dp, bottom = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Título del video
                    Text(
                        text = videoTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Botón de aspect ratio / resize mode
                    IconButton(
                        onClick = onResizeModeClick,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    ) {
                        // Icono según el modo actual
                        val resizeIcon = when(resizeMode) {
                            0 -> "FIT" // Fit
                            1 -> "FILL" // Fill
                            2 -> "ZOOM" // Zoom
                            3 -> "W" // Fixed width
                            4 -> "H" // Fixed height
                            else -> "FIT"
                        }
                        Text(
                            text = resizeIcon,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Botón de ajustes
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
            
            // Controles centrales: retroceder, play/pause, adelantar
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Retroceder 10 segundos
                IconButton(
                    onClick = onRewind,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Play/Pause (botón grande)
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
                
                // Adelantar 10 segundos
                IconButton(
                    onClick = onForward,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            // Barra inferior: barra de progreso, tiempo y botones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    // Degradado de abajo para que se vea bien sobre el video
                    .then(
                        if (isFullscreen) Modifier.background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        else Modifier
                    )
                    .then(
                        if (isFullscreen) Modifier.navigationBarsPadding()
                        else Modifier
                    )
                    .padding(
                        top = if (isFullscreen) 56.dp else 0.dp,
                        start = if (isFullscreen) 36.dp else 16.dp,
                        end = if (isFullscreen) 36.dp else 16.dp,
                        bottom = if (isFullscreen) 40.dp else 16.dp
                    )
            ) {
                // Barra de progreso
                VideoProgressBar(
                    currentPosition = currentPosition,
                    duration = duration,
                    bufferedPosition = bufferedPosition,
                    onSeek = onSeek
                )
                
                Spacer(modifier = Modifier.height(if (isFullscreen) 12.dp else 6.dp))
                
                // Fila con el tiempo del video y botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tiempo: posición actual / duración total
                    Text(
                        text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                        color = Color.White,
                        style = if (isFullscreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall
                    )
                    
                    if (isFullscreen) {
                        // En pantalla completa: solo botón para salir
                        IconButton(
                            onClick = onFullscreenClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Salir de pantalla completa",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // En vertical: botones de ajustes y pantalla completa
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = onFullscreenClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Pantalla completa",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
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
