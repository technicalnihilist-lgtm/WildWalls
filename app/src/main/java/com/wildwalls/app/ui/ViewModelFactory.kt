package com.wildwalls.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wildwalls.app.di.AppContainer
import com.wildwalls.app.ui.credits.CreditsViewModel
import com.wildwalls.app.ui.detail.DetailViewModel
import com.wildwalls.app.ui.downloads.DownloadsViewModel
import com.wildwalls.app.ui.explore.ExploreViewModel
import com.wildwalls.app.ui.favorites.FavoritesViewModel
import com.wildwalls.app.ui.home.HomeViewModel
import com.wildwalls.app.ui.settings.SettingsViewModel

class WildViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
            container.wallpaperRepository,
            container.settingsStore,
            container.wallpaperCache,
            container.gson
        ) as T

        modelClass.isAssignableFrom(ExploreViewModel::class.java) -> ExploreViewModel(
            container.wallpaperRepository,
            container.libraryRepository,
            container.wallpaperCache
        ) as T

        modelClass.isAssignableFrom(DetailViewModel::class.java) -> DetailViewModel(
            container.wallpaperRepository,
            container.libraryRepository,
            container.downloader,
            container.wallpaperSetter,
            container.wallpaperCache
        ) as T

        modelClass.isAssignableFrom(FavoritesViewModel::class.java) -> FavoritesViewModel(
            container.libraryRepository
        ) as T

        modelClass.isAssignableFrom(DownloadsViewModel::class.java) -> DownloadsViewModel(
            container.libraryRepository
        ) as T

        modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
            container.settingsStore,
            container.libraryRepository,
            container.imageLoader
        ) as T

        modelClass.isAssignableFrom(CreditsViewModel::class.java) -> CreditsViewModel(
            container.libraryRepository
        ) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
