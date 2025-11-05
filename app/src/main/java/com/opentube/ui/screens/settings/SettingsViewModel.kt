package com.opentube.ui.screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val theme: ThemeMode = ThemeMode.DARK,
    val defaultQuality: String = "720p",
    val autoplayEnabled: Boolean = true,
    val historyEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dataSaverMode: Boolean = false,
    val autoplayOnMobileData: Boolean = false
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
    YOUTUBE,
    BLUE,
    SKY_BLUE,
    RED
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val AUTOPLAY_ENABLED = booleanPreferencesKey("autoplay_enabled")
        val HISTORY_ENABLED = booleanPreferencesKey("history_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DATA_SAVER_MODE = booleanPreferencesKey("data_saver_mode")
        val AUTOPLAY_ON_MOBILE_DATA = booleanPreferencesKey("autoplay_on_mobile_data")
    }
    
    val settings: StateFlow<AppSettings> = context.dataStore.data
        .map { preferences ->
            AppSettings(
                theme = preferences[PreferencesKeys.THEME]?.let { 
                    ThemeMode.valueOf(it) 
                } ?: ThemeMode.DARK,
                defaultQuality = preferences[PreferencesKeys.DEFAULT_QUALITY] ?: "720p",
                autoplayEnabled = preferences[PreferencesKeys.AUTOPLAY_ENABLED] ?: true,
                historyEnabled = preferences[PreferencesKeys.HISTORY_ENABLED] ?: true,
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                dataSaverMode = preferences[PreferencesKeys.DATA_SAVER_MODE] ?: false,
                autoplayOnMobileData = preferences[PreferencesKeys.AUTOPLAY_ON_MOBILE_DATA] ?: false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
    
    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME] = theme.name
            }
        }
    }
    
    fun setDefaultQuality(quality: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DEFAULT_QUALITY] = quality
            }
        }
    }
    
    fun setAutoplayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.AUTOPLAY_ENABLED] = enabled
            }
        }
    }
    
    fun setHistoryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.HISTORY_ENABLED] = enabled
            }
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
            }
        }
    }
    
    fun setDataSaverMode(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DATA_SAVER_MODE] = enabled
            }
        }
    }
    
    fun setAutoplayOnMobileData(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.AUTOPLAY_ON_MOBILE_DATA] = enabled
            }
        }
    }
}
