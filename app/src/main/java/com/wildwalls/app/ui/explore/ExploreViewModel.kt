package com.wildwalls.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildwalls.app.data.repository.FeedResult
import com.wildwalls.app.data.repository.WallpaperCache
import com.wildwalls.app.data.repository.WallpaperRepository
import com.wildwalls.app.domain.model.CategoryCatalog
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.wildwalls.app.data.repository.LibraryRepository

class ExploreViewModel(
    private val repository: WallpaperRepository,
    private val library: LibraryRepository,
    private val cache: WallpaperCache
) : ViewModel() {

    data class UiState(
        val query: String = "",
        val activeQuery: String = DEFAULT_LABEL,
        val categoryId: String? = null,
        val items: List<Wallpaper> = emptyList(),
        val loading: Boolean = true,
        val loadingMore: Boolean = false,
        val error: String? = null,
        val hasMore: Boolean = true,
        val suggestions: List<String> = emptyList(),
        val recentSearches: List<String> = emptyList(),
        val searchActive: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var nextOffset: Int? = 0
    private var suggestionJob: Job? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            library.observeSearchHistory().collect { history ->
                _state.update { it.copy(recentSearches = history) }
            }
        }
        loadFirstPage(DEFAULT_QUERY, hints = emptyList(), label = DEFAULT_LABEL)
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, searchActive = true) }
        suggestionJob?.cancel()
        if (query.length < 2) {
            _state.update { it.copy(suggestions = emptyList()) }
            return
        }
        suggestionJob = viewModelScope.launch {
            delay(220)
            val matches = CategoryCatalog.searchVocabulary
                .filter { it.contains(query.trim(), ignoreCase = true) }
                .take(7)
            _state.update { it.copy(suggestions = matches) }
        }
    }

    fun onSearchFocus(active: Boolean) {
        _state.update { it.copy(searchActive = active) }
    }

    fun dismissSuggestions() {
        _state.update { it.copy(searchActive = false, suggestions = emptyList()) }
    }

    fun submitSearch(rawQuery: String) {
        val query = rawQuery.trim()
        if (query.isEmpty()) return
        _state.update {
            it.copy(query = query, suggestions = emptyList(), searchActive = false)
        }
        viewModelScope.launch { library.recordSearch(query) }
        loadFirstPage("$query filetype:bitmap", hints = emptyList(), label = "\"$query\"")
    }

    fun openCategory(categoryId: String) {
        val category = CategoryCatalog.byId(categoryId) ?: return
        _state.update {
            it.copy(
                categoryId = categoryId,
                query = "",
                suggestions = emptyList(),
                searchActive = false
            )
        }
        loadFirstPage(category.query, hints = category.hints, label = category.name)
    }

    fun clearCategory() {
        _state.update { it.copy(categoryId = null) }
        loadFirstPage(DEFAULT_QUERY, hints = emptyList(), label = DEFAULT_LABEL)
    }

    fun retry() {
        val current = _state.value
        val category = CategoryCatalog.byId(current.categoryId)
        if (category != null) {
            loadFirstPage(category.query, category.hints, category.name)
        } else if (current.query.isNotBlank()) {
            submitSearch(current.query)
        } else {
            loadFirstPage(DEFAULT_QUERY, emptyList(), DEFAULT_LABEL)
        }
    }

    fun loadMore() {
        val current = _state.value
        val offset = nextOffset ?: return
        if (current.loading || current.loadingMore || !current.hasMore) return
        val category = CategoryCatalog.byId(current.categoryId)
        val query = category?.query
            ?: current.query.takeIf { it.isNotBlank() }?.let { "$it filetype:bitmap" }
            ?: DEFAULT_QUERY
        val hints = category?.hints ?: emptyList()

        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.search(query, offset, hints = hints)) {
                is FeedResult.Success -> {
                    cache.put(result.items)
                    nextOffset = result.nextOffset
                    _state.update {
                        val merged = (it.items + result.items).distinctBy { item -> item.id }
                        it.copy(
                            items = merged,
                            loadingMore = false,
                            hasMore = result.hasMore,
                            error = null
                        )
                    }
                }
                is FeedResult.Failure -> _state.update {
                    it.copy(loadingMore = false, error = result.message)
                }
            }
        }
    }

    private fun loadFirstPage(query: String, hints: List<String>, label: String) {
        loadJob?.cancel()
        nextOffset = 0
        _state.update {
            it.copy(
                items = emptyList(),
                loading = true,
                error = null,
                hasMore = true,
                activeQuery = label
            )
        }
        loadJob = viewModelScope.launch {
            when (val result = repository.search(query, 0, hints = hints)) {
                is FeedResult.Success -> {
                    cache.put(result.items)
                    nextOffset = result.nextOffset
                    _state.update {
                        it.copy(
                            items = result.items,
                            loading = false,
                            hasMore = result.hasMore,
                            error = null
                        )
                    }
                }
                is FeedResult.Failure -> _state.update {
                    it.copy(loading = false, error = result.message, hasMore = false)
                }
            }
        }
    }

    companion object {
        const val DEFAULT_LABEL = "Wildlife picks"
        const val DEFAULT_QUERY = "deepcat:\"Quality images of animals\" filetype:bitmap"
    }
}
