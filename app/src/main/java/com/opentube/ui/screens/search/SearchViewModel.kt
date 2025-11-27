package com.opentube.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opentube.data.models.SearchItem
import com.opentube.data.repository.VideoRepository
import com.opentube.util.SearchHistoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.opentube.data.extractor.PagedResult

/**
 * UI State for Search screen
 */
sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(
        val results: List<SearchItem>,
        val hasMore: Boolean
    ) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

/**
 * ViewModel for Search screen
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val searchHistoryManager: SearchHistoryManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Historial de búsqueda
    val searchHistory = searchHistoryManager.historyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    
    private var currentNextPage: String? = null
    private var currentFilter = "all"
    
    init {
        // Auto-suggest as user types
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .collectLatest { query ->
                    loadSuggestions(query)
                }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun search(query: String = searchQuery.value, filter: String = "all") {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            currentFilter = filter
            
            // Agregar al historial
            searchHistoryManager.addSearch(query)
            
            videoRepository.searchPaged(query, null).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { pagedResult ->
                        currentNextPage = pagedResult.nextPageUrl
                        
                        // Convert Video to SearchItem
                        val items = pagedResult.items.map { video ->
                            SearchItem(
                                url = video.url,
                                type = "stream",
                                title = video.title,
                                name = video.title,
                                thumbnail = video.thumbnail,
                                uploaderName = video.uploaderName,
                                uploaderUrl = video.uploaderUrl,
                                uploaderAvatar = video.uploaderAvatar,
                                uploadedDate = video.uploadedDate,
                                duration = video.duration,
                                views = video.views,
                                uploaderVerified = video.uploaderVerified,
                                description = "",
                                subscribers = null,
                                videos = null,
                                verified = video.uploaderVerified
                            )
                        }
                        
                        SearchUiState.Success(
                            results = items,
                            hasMore = pagedResult.nextPageUrl != null
                        )
                    },
                    onFailure = { exception ->
                        SearchUiState.Error(
                            exception.message ?: "Error en la búsqueda"
                        )
                    }
                )
            }
        }
    }
    
    fun loadMore() {
        val nextPage = currentNextPage ?: return
        val currentState = _uiState.value as? SearchUiState.Success ?: return
        
        viewModelScope.launch {
            videoRepository.searchPaged(
                query = searchQuery.value,
                pageUrl = nextPage
            ).collect { result ->
                result.fold(
                    onSuccess = { pagedResult ->
                        currentNextPage = pagedResult.nextPageUrl
                        
                        val newItems: List<SearchItem> = pagedResult.items.map { video ->
                            SearchItem(
                                url = video.url,
                                type = "stream",
                                title = video.title,
                                name = video.title,
                                thumbnail = video.thumbnail,
                                uploaderName = video.uploaderName,
                                uploaderUrl = video.uploaderUrl,
                                uploaderAvatar = video.uploaderAvatar,
                                uploadedDate = video.uploadedDate,
                                duration = video.duration,
                                views = video.views,
                                uploaderVerified = video.uploaderVerified,
                                description = "",
                                subscribers = null,
                                videos = null,
                                verified = video.uploaderVerified
                            )
                        }
                        
                        val updatedResults = currentState.results + newItems
                        
                        _uiState.value = SearchUiState.Success(
                            results = updatedResults,
                            hasMore = pagedResult.nextPageUrl != null
                        ) as SearchUiState
                    },
                    onFailure = {
                        // Keep current state on error
                    }
                )
            }
        }
    }
    
    private fun loadSuggestions(query: String) {
        viewModelScope.launch {
            videoRepository.getSuggestions(query).collect { result ->
                result.onSuccess { suggestions ->
                    _suggestions.value = suggestions
                }
            }
        }
    }
    
    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }
    
    // Funciones para gestionar el historial
    fun removeFromHistory(query: String) {
        viewModelScope.launch {
            searchHistoryManager.removeSearch(query)
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryManager.clearHistory()
        }
    }
}
