package com.wildwalls.app.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildwalls.app.data.repository.LibraryRepository
import com.wildwalls.app.data.repository.LibraryRepository.DownloadedWallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadsViewModel(private val library: LibraryRepository) : ViewModel() {

    data class UiState(
        val items: List<DownloadedWallpaper> = emptyList(),
        val loading: Boolean = true,
        val message: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            library.observeDownloads().collect { downloads ->
                _state.update { it.copy(items = downloads, loading = false) }
            }
        }
    }

    fun delete(item: DownloadedWallpaper) {
        viewModelScope.launch {
            library.deleteDownload(item.wallpaper.id)
            _state.update { it.copy(message = "Removed ${item.fileName}") }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun totalBytes(): Long = _state.value.items.sumOf { it.sizeBytes }
}
