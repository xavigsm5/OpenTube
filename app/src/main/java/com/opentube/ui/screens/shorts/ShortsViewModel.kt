package com.opentube.ui.screens.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opentube.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ShortsUiState {
    object Loading : ShortsUiState()
    data class Success(
        val shorts: List<com.opentube.data.models.Video>,
        val comments: Map<String, List<com.opentube.ui.screens.player.Comment>> = emptyMap(),
        val loadingComments: Set<String> = emptySet()
    ) : ShortsUiState()
    data class Error(val message: String) : ShortsUiState()
}

@HiltViewModel
class ShortsViewModel @Inject constructor(
    private val repository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ShortsUiState>(ShortsUiState.Loading)
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()
    
    private val allShorts = mutableListOf<com.opentube.data.models.Video>()
    private val seenVideoIds = mutableSetOf<String>() // Para evitar duplicados
    private var currentPage = 0
    private var isLoadingMore = false
    
    init {
        android.util.Log.d("ShortsViewModel", "=== ShortsViewModel INIT ===")
        loadShorts()
    }
    
    fun loadShorts() {
        android.util.Log.d("ShortsViewModel", "loadShorts() called - page: $currentPage")
        viewModelScope.launch {
            _uiState.value = ShortsUiState.Loading
            android.util.Log.d("ShortsViewModel", "State set to Loading")
            
            try {
                // Obtener shorts específicamente (videos de menos de 60 segundos)
                repository.getShorts(0).collect { result ->
                    android.util.Log.d("ShortsViewModel", "Received result: ${result.isSuccess}")
                    result.fold(
                        onSuccess = { videos ->
                            android.util.Log.d("ShortsViewModel", "Success with ${videos.size} shorts")
                            allShorts.clear()
                            seenVideoIds.clear()
                            currentPage = 0
                            
                            // Agregar solo videos únicos
                            videos.forEach { video ->
                                if (seenVideoIds.add(video.url)) {
                                    allShorts.add(video)
                                }
                            }
                            
                            _uiState.value = ShortsUiState.Success(allShorts.toList())
                        },
                        onFailure = { exception ->
                            android.util.Log.e("ShortsViewModel", "Error loading shorts", exception)
                            val errorMessage = when {
                                exception.message?.contains("Could not get ytInitialData") == true -> 
                                    "YouTube cambió su estructura. Por favor:\n• Espera 5-10 minutos\n• Cambia a datos móviles\n• Usa una VPN\n\nEsto es temporal y afecta a todas las apps similares."
                                exception.message?.contains("blocked this IP") == true -> 
                                    "Tu IP fue bloqueada temporalmente por YouTube.\n• Espera 15-30 minutos\n• Cambia de red WiFi\n• Usa datos móviles"
                                exception.message?.contains("LOGIN_REQUIRED") == true ->
                                    "YouTube requiere inicio de sesión.\n• Intenta en unos minutos\n• Cambia de red"
                                else -> "Error al cargar shorts: ${exception.message ?: "Desconocido"}\n\nIntenta:\n• Esperar 5-10 minutos\n• Cambiar de red\n• Usar datos móviles"
                            }
                            _uiState.value = ShortsUiState.Error(errorMessage)
                        }
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ShortsViewModel", "Exception loading shorts", e)
                val errorMessage = when {
                    e.message?.contains("Could not get ytInitialData") == true -> 
                        "YouTube cambió su estructura. Por favor:\n• Espera 5-10 minutos\n• Cambia a datos móviles\n• Usa una VPN"
                    e.message?.contains("blocked this IP") == true -> 
                        "Tu IP fue bloqueada temporalmente.\n• Espera 15-30 minutos\n• Cambia de red"
                    e.message?.contains("LOGIN_REQUIRED") == true ->
                        "YouTube requiere inicio de sesión.\n• Intenta en unos minutos"
                    else -> "Error: ${e.message ?: "Desconocido"}\n\n• Espera unos minutos\n• Cambia de red"
                }
                _uiState.value = ShortsUiState.Error(errorMessage)
            }
        }
    }
    
    fun loadMoreShorts() {
        if (isLoadingMore) return
        
        viewModelScope.launch {
            isLoadingMore = true
            try {
                currentPage++
                repository.getShorts(currentPage).collect { result ->
                    result.fold(
                        onSuccess = { videos ->
                            // Filtrar y agregar solo videos únicos
                            val newVideos = videos.filter { video ->
                                seenVideoIds.add(video.url)
                            }
                            
                            if (newVideos.isNotEmpty()) {
                                allShorts.addAll(newVideos)
                                _uiState.value = ShortsUiState.Success(allShorts.toList())
                            }
                        },
                        onFailure = { /* Ignorar errores al cargar más */ }
                    )
                }
            } catch (e: Exception) {
                // Ignorar errores al cargar más
            }
            isLoadingMore = false
        }
    }
    
    fun refreshShorts() {
        // Limpiar todo y cargar desde cero
        allShorts.clear()
        seenVideoIds.clear()
        currentPage = 0
        loadShorts()
    }
    
    fun loadCommentsForShort(videoId: String) {
        val currentState = _uiState.value
        if (currentState !is ShortsUiState.Success) return
        if (currentState.loadingComments.contains(videoId)) return // Ya está cargando
        if (currentState.comments.containsKey(videoId)) return // Ya está cargado
        
        viewModelScope.launch {
            // Marcar como cargando
            _uiState.value = currentState.copy(
                loadingComments = currentState.loadingComments + videoId
            )
            
            try {
                repository.getComments(videoId).collect { result ->
                    result.fold(
                        onSuccess = { comments ->
                            val state = _uiState.value as? ShortsUiState.Success ?: return@collect
                            _uiState.value = state.copy(
                                comments = state.comments + (videoId to comments),
                                loadingComments = state.loadingComments - videoId
                            )
                        },
                        onFailure = {
                            val state = _uiState.value as? ShortsUiState.Success ?: return@collect
                            _uiState.value = state.copy(
                                loadingComments = state.loadingComments - videoId
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                val state = _uiState.value as? ShortsUiState.Success ?: return@launch
                _uiState.value = state.copy(
                    loadingComments = state.loadingComments - videoId
                )
            }
        }
    }
}
