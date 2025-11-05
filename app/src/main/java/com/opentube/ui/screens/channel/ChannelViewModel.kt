package com.opentube.ui.screens.channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opentube.data.local.SubscriptionDao
import com.opentube.data.local.SubscriptionEntity
import com.opentube.data.models.Channel
import com.opentube.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChannelUiState {
    object Loading : ChannelUiState
    data class Success(
        val channel: Channel,
        val isSubscribed: Boolean = false
    ) : ChannelUiState
    data class Error(val message: String) : ChannelUiState
}

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val subscriptionDao: SubscriptionDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val channelId: String = checkNotNull(savedStateHandle["channelId"])
    
    private val _uiState = MutableStateFlow<ChannelUiState>(ChannelUiState.Loading)
    val uiState: StateFlow<ChannelUiState> = _uiState.asStateFlow()
    
    init {
        loadChannel()
    }
    
    private fun loadChannel() {
        viewModelScope.launch {
            channelRepository.getChannel(channelId).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { channel ->
                        // Check if subscribed
                        val isSubscribed = subscriptionDao.getSubscription(channelId) != null
                        ChannelUiState.Success(
                            channel = channel,
                            isSubscribed = isSubscribed
                        )
                    },
                    onFailure = { exception ->
                        ChannelUiState.Error(
                            exception.message ?: "Error al cargar el canal"
                        )
                    }
                )
            }
        }
    }
    
    fun toggleSubscription() {
        val currentState = _uiState.value
        if (currentState is ChannelUiState.Success) {
            viewModelScope.launch {
                if (currentState.isSubscribed) {
                    // Unsubscribe
                    subscriptionDao.deleteSubscriptionById(channelId)
                    _uiState.value = currentState.copy(isSubscribed = false)
                } else {
                    // Subscribe
                    subscriptionDao.insertSubscription(
                        SubscriptionEntity(
                            channelId = channelId,
                            channelName = currentState.channel.name,
                            channelAvatar = currentState.channel.avatarUrl
                        )
                    )
                    _uiState.value = currentState.copy(isSubscribed = true)
                }
            }
        }
    }
    
    fun retry() {
        _uiState.value = ChannelUiState.Loading
        loadChannel()
    }
}
