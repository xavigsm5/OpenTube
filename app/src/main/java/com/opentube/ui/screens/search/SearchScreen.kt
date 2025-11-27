package com.opentube.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opentube.R
import com.opentube.data.models.SearchItem
import com.opentube.ui.components.VideoCard
import com.opentube.data.models.Video

/**
 * Search screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onVideoClick: (String) -> Unit,
    onChannelClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var showSuggestions by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            viewModel.updateSearchQuery(it)
                            showSuggestions = it.isNotEmpty()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.action_search)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.search()
                                showSuggestions = false
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.updateSearchQuery("")
                                    showSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.search()
                            showSuggestions = false
                        },
                        enabled = searchQuery.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                showSuggestions && suggestions.isNotEmpty() -> {
                    // Mostrar sugerencias
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(suggestions) { suggestion ->
                            ListItem(
                                headlineContent = { Text(suggestion) },
                                leadingContent = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                modifier = Modifier.clickable {
                                    viewModel.updateSearchQuery(suggestion)
                                    viewModel.search(suggestion)
                                    showSuggestions = false
                                }
                            )
                        }
                    }
                }
                
                uiState is SearchUiState.Idle && searchHistory.isNotEmpty() -> {
                    // Mostrar historial de búsqueda con tarjetas Material 3
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Historial de búsqueda",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                TextButton(onClick = { viewModel.clearHistory() }) {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = "Limpiar historial",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Limpiar")
                                }
                            }
                        }
                        
                        items(
                            items = searchHistory,
                            key = { it.query }
                        ) { historyItem ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.updateSearchQuery(historyItem.query)
                                    viewModel.search(historyItem.query)
                                    showSuggestions = false
                                }
                            ) {
                                ListItem(
                                    headlineContent = { Text(historyItem.query) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    },
                                    trailingContent = {
                                        IconButton(
                                            onClick = { viewModel.removeFromHistory(historyItem.query) }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                uiState is SearchUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState is SearchUiState.Success -> {
                    val state = uiState as SearchUiState.Success
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = state.results,
                            key = { it.url }
                        ) { item ->
                            when (item.type) {
                                "stream" -> {
                                    val video = Video(
                                        url = item.url,
                                        title = item.title ?: "",
                                        thumbnail = item.thumbnail,
                                        uploaderName = item.uploaderName ?: "",
                                        uploaderUrl = item.uploaderUrl,
                                        uploaderAvatar = item.uploaderAvatar,
                                        uploadedDate = item.uploadedDate,
                                        duration = item.duration ?: 0,
                                        views = item.views ?: 0,
                                        uploaderVerified = item.uploaderVerified ?: false
                                    )
                                    VideoCard(
                                        video = video,
                                        onClick = { onVideoClick(video.videoId) }
                                    )
                                }
                                "channel" -> {
                                    // Channel item (simplified)
                                    ListItem(
                                        headlineContent = { Text(item.name ?: "") },
                                        supportingContent = {
                                            Text("${item.subscribers ?: 0} suscriptores")
                                        },
                                        modifier = Modifier.clickable {
                                            item.url.substringAfterLast("/").let { channelId ->
                                                onChannelClick(channelId)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (state.hasMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                    
                                    // Trigger load more when this item becomes visible
                                    LaunchedEffect(Unit) {
                                        viewModel.loadMore()
                                    }
                                }
                            }
                        }
                    }
                }
                
                uiState is SearchUiState.Error -> {
                    val state = uiState as SearchUiState.Error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                else -> {
                    // Idle state - show prompt
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Busca videos, canales y playlists",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
