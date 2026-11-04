package com.wildwalls.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.wildwalls.app.data.repository.FeedResult
import com.wildwalls.app.data.repository.WallpaperRepository
import okhttp3.OkHttpClient
import okhttp3.Request
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
    private val imageLoader: ImageLoader,
    private val repository: WallpaperRepository,
    private val imageClient: OkHttpClient
) : ViewModel() {

    data class UiState(
        val settings: AppSettings = AppSettings(),
        val cacheBytes: Long = 0,
        val message: String? = null,
        val imageTest: String = ""
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

    /**
     * End-to-end check of the image pipeline: asks Commons for one wallpaper, then fetches its
     * thumbnail exactly the way the UI does. The reported status tells us whether a failure is
     * DNS, licensing, rate limiting or a decoding problem.
     */
    fun testImageLoading() {
        _state.update { it.copy(imageTest = "Testing image pipeline…") }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val feed = repository.categoryFeed("lions", offset = 0, limit = 1)
                    val url = (feed as? FeedResult.Success)?.items?.firstOrNull()?.thumbUrl
                        ?: return@runCatching "Commons returned no image URL"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .header("Referer", "https://commons.wikimedia.org/")
                        .build()
                    imageClient.newCall(request).execute().use { response ->
                        val bytes = response.body?.contentLength() ?: -1L
                        val host = url.substringAfter("//").substringBefore("/")
                        "HTTP ${response.code} · $bytes bytes · $host"
                    }
                }.getOrElse { "Failed: ${it.message ?: it::class.java.simpleName}" }
            }
            _state.update { it.copy(imageTest = result) }
        }
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

    private companion object {
        const val USER_AGENT =
            "WildWalls/1.2 (Android wallpaper app; https://commons.wikimedia.org; contact@wildwalls.app)"
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
