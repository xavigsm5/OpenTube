package com.opentube.ui.screens.shorts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortsScreen(
    onNavigateBack: () -> Unit,
    onChannelClick: ((String) -> Unit)? = null,
    viewModel: ShortsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is ShortsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is ShortsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = { viewModel.loadShorts() }) {
                        Text("Reintentar")
                    }
                }
            }
        }
        
        is ShortsUiState.Success -> {
            val pagerState = rememberPagerState(pageCount = { state.shorts.size })
            
            // Detectar cuando se acerca al final para cargar más shorts
            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage >= state.shorts.size - 3) {
                    viewModel.loadMoreShorts()
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    ShortItem(
                        short = state.shorts[page],
                        onChannelClick = onChannelClick,
                        isCurrentPage = pagerState.currentPage == page,
                        viewModel = viewModel
                    )
                }
                
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Refresh button
                IconButton(
                    onClick = { viewModel.refreshShorts() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShortItem(
    short: com.opentube.data.models.Video,
    @Suppress("UNUSED_PARAMETER") onChannelClick: ((String) -> Unit)?,
    isCurrentPage: Boolean,
    viewModel: ShortsViewModel
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showPauseIcon by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false
            
            // Listener para saber cuándo está listo
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isReady = true
                    }
                }
            })
        }
    }
    
    // Controlar reproducción basado en si es la página actual Y si está listo
    LaunchedEffect(isCurrentPage, isReady) {
        if (isCurrentPage && isReady) {
            exoPlayer.play()
            isPlaying = true
        } else {
            exoPlayer.pause()
            isPlaying = false
        }
    }
    
    // Cargar el video usando NewPipe para obtener el stream real
    LaunchedEffect(short.url) {
        try {
            isLoading = true
            android.util.Log.d("ShortsScreen", "Loading short: ${short.title}")
            
            // Extraer videoId de la URL
            val videoId = short.url.substringAfter("watch?v=").substringBefore("&")
            android.util.Log.d("ShortsScreen", "Video ID: $videoId")
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val streamInfo = org.schabi.newpipe.extractor.stream.StreamInfo.getInfo(
                    org.schabi.newpipe.extractor.ServiceList.YouTube,
                    "https://www.youtube.com/watch?v=$videoId"
                )
                
                android.util.Log.d("ShortsScreen", "Stream info loaded")
                android.util.Log.d("ShortsScreen", "HLS URL: ${streamInfo.hlsUrl}")
                android.util.Log.d("ShortsScreen", "VideoStreams: ${streamInfo.videoStreams.size}")
                android.util.Log.d("ShortsScreen", "VideoOnlyStreams: ${streamInfo.videoOnlyStreams.size}")
                android.util.Log.d("ShortsScreen", "AudioStreams: ${streamInfo.audioStreams.size}")
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    var loaded = false
                    
                    // OPCIÓN 1: Progressive stream (video+audio juntos) - MEJOR para Shorts
                    if (!loaded && streamInfo.videoStreams.isNotEmpty()) {
                        try {
                            // Ordenar por calidad: primero por height
                            val bestProgressive = streamInfo.videoStreams
                                .filter { it.height >= 360 } // Mínimo 360p
                                .maxByOrNull { it.height }
                                ?: streamInfo.videoStreams.firstOrNull()
                            
                            if (bestProgressive != null) {
                                android.util.Log.d("ShortsScreen", "Trying progressive ${bestProgressive.height}p...")
                                val mediaItem = MediaItem.fromUri(bestProgressive.content)
                                exoPlayer.setMediaItem(mediaItem)
                                exoPlayer.prepare()
                                isLoading = false
                                loaded = true
                                android.util.Log.d("ShortsScreen", "✅ Progressive loaded: ${bestProgressive.height}p")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ShortsScreen", "❌ Progressive failed", e)
                        }
                    }
                    
                    // OPCIÓN 2: HLS (más confiable para videos largos, pero shorts no lo tienen)
                    if (!loaded && streamInfo.hlsUrl != null && streamInfo.hlsUrl.isNotEmpty()) {
                        try {
                            android.util.Log.d("ShortsScreen", "Trying HLS...")
                            val mediaItem = MediaItem.fromUri(streamInfo.hlsUrl)
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                            isLoading = false
                            loaded = true
                            android.util.Log.d("ShortsScreen", "✅ HLS loaded successfully")
                        } catch (e: Exception) {
                            android.util.Log.e("ShortsScreen", "❌ HLS failed", e)
                        }
                    }
                    
                    if (!loaded) {
                        android.util.Log.e("ShortsScreen", "❌ No stream could be loaded!")
                        isLoading = false
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ShortsScreen", "Error loading short", e)
            isLoading = false
        }
    }
    
    // Ocultar el ícono de pausa automáticamente
    LaunchedEffect(showPauseIcon) {
        if (showPauseIcon) {
            kotlinx.coroutines.delay(800)
            showPauseIcon = false
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }
    
    // Animación para reducir el video cuando se muestran comentarios (estilo Instagram)
    val videoHeightFraction by animateFloatAsState(
        targetValue = if (showComments) 0.55f else 1f, // 55% cuando hay comentarios
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "videoHeight"
    )
    
    val videoScale by animateFloatAsState(
        targetValue = if (showComments) 0.85f else 1f, // Se reduce un poco
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "videoScale"
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Video Player - se reduce cuando hay comentarios (estilo Instagram)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(videoHeightFraction)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = videoScale
                        scaleY = videoScale
                    }
                    .clickable {
                        if (isPlaying) {
                            exoPlayer.pause()
                            isPlaying = false
                            showPauseIcon = true
                        } else {
                            exoPlayer.play()
                            isPlaying = true
                            showPauseIcon = false
                        }
                    }
            ) {
                // Video player
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Loading indicator
                if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
        
        // Pause/Play icon overlay
        if (showPauseIcon) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPlaying) "Play" else "Pause",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
        
        // Right side buttons (smaller icons)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Like button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Me gusta",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = formatViews(short.views),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            // Dislike button
            IconButton(
                onClick = { },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbDown,
                    contentDescription = "No me gusta",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Comment button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { showComments = !showComments },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comentarios",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Comentarios",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            // Share button
            IconButton(
                onClick = { },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.7f)
        ) {
            // Channel info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = short.uploaderName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Suscribirse", fontSize = 12.sp)
                }
            }
            
            // Title/Description
            Text(
                text = short.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
            } // Cierra Box interno (con graphicsLayer)
        } // Cierra Box externo (con videoHeightFraction)
        
        // Panel de Comentarios - Estilo Instagram (debajo del video, no encima)
        if (showComments) {
            val videoId = short.url.substringAfter("watch?v=").substringBefore("&")
            val currentState = (viewModel.uiState.collectAsState().value as? ShortsUiState.Success)
            val comments = currentState?.comments?.get(videoId) ?: emptyList()
            val isLoadingComments = currentState?.loadingComments?.contains(videoId) ?: false
            
            // Cargar comentarios si no están cargados
            LaunchedEffect(videoId) {
                viewModel.loadCommentsForShort(videoId)
            }
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header de comentarios
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comentarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        IconButton(onClick = { showComments = false }) {
                            Icon(Icons.Default.Close, "Cerrar")
                        }
                    }
                    
                    Divider()
                    
                    // Lista de comentarios
                    if (isLoadingComments) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (comments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay comentarios",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(comments.size) { index ->
                                RealCommentItem(comments[index])
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealCommentItem(comment: com.opentube.ui.screens.player.Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        coil.compose.AsyncImage(
            model = comment.authorAvatar,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Nombre del autor
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.author,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                if (comment.isVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificado",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Texto del comentario
            Text(
                text = android.text.Html.fromHtml(comment.text, android.text.Html.FROM_HTML_MODE_COMPACT).toString(),
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Información adicional (tiempo y likes)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = comment.publishedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (comment.likes > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCount(comment.likes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatViews(views: Long): String {
    return when {
        views >= 1_000_000 -> "${views / 1_000_000}M"
        views >= 1_000 -> "${views / 1_000}K"
        else -> views.toString()
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}
