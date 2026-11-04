package com.wildwalls.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildwalls.app.data.download.WallpaperDownloader
import com.wildwalls.app.data.repository.FeedResult
import com.wildwalls.app.data.repository.LibraryRepository
import com.wildwalls.app.data.repository.WallpaperCache
import com.wildwalls.app.data.repository.WallpaperRepository
import com.wildwalls.app.data.wallpaper.WallpaperSetter
import com.wildwalls.app.data.wallpaper.WallpaperTarget
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: WallpaperRepository,
    private val library: LibraryRepository,
    private val downloader: WallpaperDownloader,
    private val wallpaperSetter: WallpaperSetter,
    private val cache: WallpaperCache
) : ViewModel() {

    sealed interface DownloadState {
        data object Idle : DownloadState
        data object Downloading : DownloadState
        data class Done(val path: String) : DownloadState
        data class Failed(val message: String) : DownloadState
    }

    data class UiState(
        val wallpaper: Wallpaper? = null,
        val loading: Boolean = true,
        val error: String? = null,
        val isFavorite: Boolean = false,
        val download: DownloadState = DownloadState.Idle,
        val applyingTarget: WallpaperTarget? = null,
        val message: String? = null,
        val related: List<Wallpaper> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var currentId: Long = -1

    fun open(wallpaperId: Long) {
        if (currentId == wallpaperId) return
        currentId = wallpaperId

        val cached = cache.get(wallpaperId)
        if (cached != null) {
            _state.update { it.copy(wallpaper = cached, loading = false, error = null) }
            onWallpaperReady(cached)
        } else {
            _state.update { it.copy(loading = true, error = null) }
        }

        viewModelScope.launch {
            if (cached == null) {
                when (val result = repository.byId(wallpaperId)) {
                    is FeedResult.Success -> {
                        val wallpaper = result.items.firstOrNull()
                        if (wallpaper != null) {
                            cache.put(listOf(wallpaper))
                            _state.update { it.copy(wallpaper = wallpaper, loading = false) }
                            onWallpaperReady(wallpaper)
                        } else {
                            _state.update {
                                it.copy(loading = false, error = "This image is not available.")
                            }
                        }
                    }
                    is FeedResult.Failure -> _state.update {
                        it.copy(loading = false, error = result.message)
                    }
                }
            }

            // Local download state, favourite state and related images.
            val row = library.downloadFor(wallpaperId)
            if (row != null) {
                _state.update { it.copy(download = DownloadState.Done(row.localPath)) }
            }
            val wallpaper = _state.value.wallpaper
            if (wallpaper != null) {
                _state.update { it.copy(isFavorite = library.isFavorite(wallpaperId)) }
                loadRelated(wallpaper)
            }
        }
    }

    private fun onWallpaperReady(wallpaper: Wallpaper) {
        viewModelScope.launch { library.recordView(wallpaper) }
    }

    private fun loadRelated(wallpaper: Wallpaper) {
        val category = wallpaper.categories.firstOrNull { it.isNotBlank() }
        val query = if (category != null) {
            "incategory:\"$category\" filetype:bitmap"
        } else {
            "wildlife filetype:bitmap"
        }
        viewModelScope.launch {
            when (val result = repository.search(query, 0, limit = 14, hints = emptyList())) {
                is FeedResult.Success -> {
                    val related = result.items.filter { it.id != wallpaper.id }.take(10)
                    cache.put(related)
                    _state.update { it.copy(related = related) }
                }
                is FeedResult.Failure -> Unit
            }
        }
    }

    fun toggleFavorite() {
        val wallpaper = _state.value.wallpaper ?: return
        viewModelScope.launch {
            val nowFavorite = library.toggleFavorite(wallpaper)
            _state.update {
                it.copy(
                    isFavorite = nowFavorite,
                    message = if (nowFavorite) "Added to favorites" else "Removed from favorites"
                )
            }
        }
    }

    fun download() {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.download is DownloadState.Downloading) return
        _state.update { it.copy(download = DownloadState.Downloading) }
        viewModelScope.launch {
            val result = downloader.download(wallpaper)
            result.fold(
                onSuccess = { file ->
                    library.saveDownload(wallpaper, file)
                    _state.update {
                        it.copy(
                            download = DownloadState.Done(file.absolutePath),
                            message = "Saved for offline use"
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            download = DownloadState.Failed(error.message ?: "Download failed"),
                            message = "Download failed. Check your connection."
                        )
                    }
                }
            )
        }
    }

    fun setAsWallpaper(target: WallpaperTarget) {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.applyingTarget != null) return
        _state.update { it.copy(applyingTarget = target) }
        viewModelScope.launch {
            val localPath = (_state.value.download as? DownloadState.Done)?.path
            val result = wallpaperSetter.setWallpaper(wallpaper, target, localPath)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            applyingTarget = null,
                            message = "Wallpaper set: ${target.label.lowercase()}"
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            applyingTarget = null,
                            message = error.message ?: "Could not set the wallpaper"
                        )
                    }
                }
            )
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun downloadedPath(): String? = (_state.value.download as? DownloadState.Done)?.path
}
