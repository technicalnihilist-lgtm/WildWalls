package com.wildwalls.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.wildwalls.app.data.prefs.AppSettings
import com.wildwalls.app.data.prefs.SettingsStore
import com.wildwalls.app.data.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val library: LibraryRepository,
    private val imageLoader: ImageLoader
) : ViewModel() {

    data class UiState(
        val settings: AppSettings = AppSettings(),
        val cacheBytes: Long = 0,
        val message: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                _state.update { it.copy(settings = settings) }
            }
        }
        refreshCacheSize()
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch { settingsStore.setGridColumns(columns) }
    }

    fun setDataSaver(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setDataSaver(enabled) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            library.clearSearchHistory()
            library.clearRecent()
            _state.update { it.copy(message = "Search history cleared") }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                imageLoader.diskCache?.clear()
                imageLoader.memoryCache?.clear()
            }
            refreshCacheSize()
            _state.update { it.copy(message = "Image cache cleared") }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun refreshCacheSize() {
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                imageLoader.diskCache?.size ?: 0L
            }
            _state.update { it.copy(cacheBytes = bytes) }
        }
    }
}
