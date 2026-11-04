package com.wildwalls.app.ui.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildwalls.app.data.repository.LibraryRepository
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreditsViewModel(private val library: LibraryRepository) : ViewModel() {

    data class UiState(
        val credited: List<Wallpaper> = emptyList(),
        val loading: Boolean = true
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val credited = library.creditedWallpapers()
            _state.update { it.copy(credited = credited, loading = false) }
        }
    }
}
