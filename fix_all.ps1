$file = "app\src\main\java\com\opentube\ui\screens\player\VideoPlayerScreen.kt"
$content = Get-Content $file -Raw -Encoding UTF8

# 1. Agregar import graphicsLayer
if ($content -notmatch "import androidx.compose.ui.graphics.graphicsLayer") {
    $content = $content -replace "import androidx.compose.ui.draw.alpha", "import androidx.compose.ui.draw.alpha`r`nimport androidx.compose.ui.graphics.graphicsLayer"
}

# 2. Actualizar firma de onMinimize
$content = $content -replace "onMinimize: \(\(title: String, channel: String, isPlaying: Boolean, player: androidx.media3.exoplayer.ExoPlayer\?\) -> Unit\)\? = null,", "onMinimize: ((title: String, channel: String, thumbnailUrl: String, isPlaying: Boolean, player: androidx.media3.exoplayer.ExoPlayer?) -> Unit)? = null,"

# 3. Actualizar llamadas a onMinimize (todas las ocurrencias)
# Usamos regex específico para cada caso o uno genérico si el formato es consistente
# Caso 1: BackHandler
$content = $content -replace "(onMinimize\(\s+state\.videoDetails\.title,\s+state\.videoDetails\.uploader,\s+)(isPlaying,)", "$1state.videoDetails.thumbnailUrl,`r`n                $2"

# Caso 2: Swipe gesture (original)
$content = $content -replace "(onMinimize\(\s+state\.videoDetails\.title,\s+state\.videoDetails\.uploader,\s+)(isPlaying,)", "$1state.videoDetails.thumbnailUrl,`r`n                                                $2"

# Caso 3: Controls back click
$content = $content -replace "(onMinimize\?\.invoke\(\s+videoDetails\.title,\s+videoDetails\.uploader,\s+)(isPlaying,)", "$1videoDetails.thumbnailUrl,`r`n                                    $2"

# 4. Actualizar PlayerSettingsSheet
$settingsParams = @"
                PlayerSettingsSheet(
                    videoStreams = videoDetails.videoStreams,
                    audioStreams = videoDetails.audioStreams,
                    subtitleStreams = videoDetails.subtitleStreams,
                    currentQuality = playerSettings.selectedQuality,
                    currentAudioTrack = playerSettings.selectedAudioTrack,
                    currentSubtitleTrack = null,
                    currentSpeed = playerSettings.playbackSpeed,
                    subtitlesEnabled = false,
                    musicModeEnabled = false,
                    onQualitySelected = { viewModel.selectQuality(it) },
                    onAudioSelected = { viewModel.selectAudioTrack(it) },
                    onSubtitleSelected = { },
                    onSpeedSelected = { viewModel.setPlaybackSpeed(it) },
                    onSubtitlesToggle = { },
                    onMusicModeToggle = { },
                    onDismiss = { viewModel.hideSettingsSheet() }
                )
"@

# Reemplazar la llamada antigua (buscamos un patrón que coincida con la llamada incompleta)
# Esto es más difícil con regex simple, así que buscaremos el bloque
$oldSettingsPattern = "(?s)PlayerSettingsSheet\(\s+videoStreams = videoDetails\.videoStreams,.*?onDismiss = \{ viewModel\.hideSettingsSheet\(\) \}\s+\)"
$content = $content -replace $oldSettingsPattern, $settingsParams

# 5. Mejorar Swipe Gesture
# Reemplazamos el bloque detectVerticalDragGestures completo
$swipeLogic = @"
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    // Si desliza más de 200px hacia abajo, minimizar
                                    if (dragOffsetY > 200f && !isFullscreen) {
                                        val state = uiState
                                        if (state is VideoPlayerUiState.Success && onMinimize != null) {
                                            // Marcar que estamos minimizando
                                            isMinimizing = true
                                            onMinimize(
                                                state.videoDetails.title,
                                                state.videoDetails.uploader,
                                                state.videoDetails.thumbnailUrl,
                                                isPlaying,
                                                exoPlayer
                                            )
                                            // Navegar hacia atrás DESPUÉS de minimizar
                                            onNavigateBack()
                                        }
                                    }
                                    // Reset drag offset con animación
                                    dragOffsetY = 0f
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    if (!isFullscreen) {
                                        // Solo permitir drag hacia abajo
                                        dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f).coerceAtMost(300f)
                                    }
                                }
                            )
                        }
                        .offset(y = dragOffsetY.dp)
                        .graphicsLayer {
                            // Reducir escala ligeramente mientras se arrastra
                            val scale = 1f - (dragOffsetY / 1000f).coerceIn(0f, 0.1f)
                            scaleX = scale
                            scaleY = scale
                            // Reducir opacidad ligeramente
                            alpha = 1f - (dragOffsetY / 800f).coerceIn(0f, 0.3f)
                        }
"@

$oldSwipePattern = "(?s)detectVerticalDragGestures\(\s+onDragEnd = \{.*?dragOffsetY = 0f\s+\},\s+onVerticalDrag = \{ _, dragAmount ->.*?\}\s+\)\s+\}"

$content = $content -replace $oldSwipePattern, $swipeLogic

$content | Set-Content $file -NoNewline -Encoding UTF8
Write-Host "✅ All fixes applied"
