package com.wildwalls.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.wildwalls.app.data.prefs.SettingsStore
import com.wildwalls.app.data.repository.FeedResult
import com.wildwalls.app.data.repository.WallpaperCache
import com.wildwalls.app.data.repository.WallpaperRepository
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val repository: WallpaperRepository,
    private val settingsStore: SettingsStore,
    private val cache: WallpaperCache,
    private val gson: Gson
) : ViewModel() {

    data class UiState(
        val fresh: List<Wallpaper> = emptyList(),
        val freshLoading: Boolean = true,
        val freshError: String? = null,
        val featured: List<Wallpaper> = emptyList(),
        val featuredLoading: Boolean = true,
        val featuredError: String? = null,
        val trending: List<Wallpaper> = emptyList(),
        val trendingLoading: Boolean = true,
        val trendingError: String? = null,
        val recent: List<Wallpaper> = emptyList(),
        val recentLoading: Boolean = true,
        val recentError: String? = null,
        val daily: Wallpaper? = null,
        val dailyLoading: Boolean = true,
        val dailyError: String? = null,
        val categoryCovers: Map<String, String> = emptyMap()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var lastFreshAt = 0L

    init {
        // Staggered on purpose: firing five heavy Commons queries in the same frame is what made
        // the first seconds of the app stutter on low-end devices.
        viewModelScope.launch {
            loadFresh()
            delay(150)
            loadFeatured()
            delay(150)
            loadTrending()
            delay(150)
            loadRecent()
            delay(150)
            loadDaily()
        }
    }

    fun refreshAll() {
        loadFresh()
        loadFeatured()
        loadTrending()
        loadRecent()
        loadDaily()
    }

    /**
     * Newest uploads first. This is the "live" section: every call re-queries Commons sorted by
     * upload time, so photographs published minutes ago appear at the top of the home screen.
     */
    fun loadFresh() {
        _state.update { it.copy(freshLoading = true, freshError = null) }
        viewModelScope.launch {
            when (val result = repository.freshWallpapers(limit = 12)) {
                is FeedResult.Success -> {
                    cache.put(result.items)
                    lastFreshAt = System.currentTimeMillis()
                    _state.update {
                        it.copy(fresh = result.items, freshLoading = false, freshError = null)
                    }
                }
                is FeedResult.Failure -> _state.update {
                    it.copy(freshLoading = false, freshError = result.message)
                }
            }
        }
    }

    /** Called when the app returns to the foreground so the feed never goes stale. */
    fun refreshIfStale() {
        val age = System.currentTimeMillis() - lastFreshAt
        if (age > REFRESH_INTERVAL_MS || _state.value.freshError != null) loadFresh()
    }

    fun loadFeatured() {
        _state.update { it.copy(featuredLoading = true, featuredError = null) }
        viewModelScope.launch {
            when (val result = repository.featured(limit = 10)) {
                is FeedResult.Success -> {
                    cache.put(result.items)
                    _state.update {
                        it.copy(
                            featured = result.items,
                            featuredLoading = false,
                            categoryCovers = it.categoryCovers + coverMap(result.items)
                        )
                    }
                }
                is FeedResult.Failure -> _state.update {
                    it.copy(featuredLoading = false, featuredError = result.message)
                }
            }
        }
    }

    fun loadTrending() {
        _state.update { it.copy(trendingLoading = true, trendingError = null) }
        viewModelScope.launch {
            when (val result = repository.search(
                query = TRENDING_QUERY,
                limit = 18,
                hints = emptyList()
            )) {
                is FeedResult.Success -> {
                    cache.put(result.items)
                    _state.update {
                        it.copy(trending = result.items, trendingLoading = false)
                    }
                }
                is FeedResult.Failure -> _state.update {
                    it.copy(trendingLoading = false, trendingError = result.message)
                }
            }
        }
    }

    fun loadRecent() {
        _state.update { it.copy(recentLoading = true, recentError = null) }
        viewModelScope.launch {
            when (val result = repository.recentlyAdded(limit = 12)) {
                is FeedResult.Success -> {
                    cache.put(result.items)
                    _state.update {
                        it.copy(
                            recent = result.items,
                            recentLoading = false,
                            categoryCovers = it.categoryCovers + coverMap(result.items)
                        )
                    }
                }
                is FeedResult.Failure -> _state.update {
                    it.copy(recentLoading = false, recentError = result.message)
                }
            }
        }
    }

    /** One wallpaper per day, stable across restarts and offline-friendly via the cached payload. */
    fun loadDaily() {
        _state.update { it.copy(dailyLoading = true, dailyError = null) }
        viewModelScope.launch {
            val today = todayKey()
            val saved = settingsStore.settings.first()
            if (saved.dailyDate == today && saved.dailyPayload.isNotBlank()) {
                val cached = runCatching {
                    gson.fromJson(saved.dailyPayload, Wallpaper::class.java)
                }.getOrNull()
                if (cached != null) {
                    cache.put(listOf(cached))
                    _state.update { it.copy(daily = cached, dailyLoading = false) }
                    return@launch
                }
            }
            when (val result = repository.dailyWallpaper()) {
                is FeedResult.Success -> {
                    val wallpaper = result.items.firstOrNull()
                    if (wallpaper != null) {
                        cache.put(listOf(wallpaper))
                        settingsStore.setDaily(today, gson.toJson(wallpaper))
                    }
                    _state.update { it.copy(daily = wallpaper, dailyLoading = false) }
                }
                is FeedResult.Failure -> _state.update {
                    it.copy(dailyLoading = false, dailyError = result.message)
                }
            }
        }
    }

    private fun coverMap(items: List<Wallpaper>): Map<String, String> = emptyMap()

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private companion object {
        const val REFRESH_INTERVAL_MS = 60_000L
        const val TRENDING_QUERY = "deepcat:\"Quality images of animals\" filetype:bitmap"
    }
}
