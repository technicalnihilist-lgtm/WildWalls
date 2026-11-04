package com.wildwalls.app.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.gson.Gson
import com.wildwalls.app.data.download.WallpaperDownloader
import com.wildwalls.app.data.local.WildWallsDatabase
import com.wildwalls.app.data.prefs.SettingsStore
import com.wildwalls.app.data.remote.NetworkModule
import com.wildwalls.app.data.repository.LibraryRepository
import com.wildwalls.app.data.repository.WallpaperCache
import com.wildwalls.app.data.repository.WallpaperRepository
import com.wildwalls.app.data.wallpaper.WallpaperSetter
import okhttp3.OkHttpClient

/**
 * Small hand-rolled dependency container - no annotation processing needed for wiring.
 */
class AppContainer(private val context: Context) {

    val gson: Gson = Gson()

    private val database by lazy { WildWallsDatabase.get(context) }

    private val apiClient: OkHttpClient by lazy { NetworkModule.apiClient(context, THUMB_WIDTH) }

    val imageClient: OkHttpClient by lazy { NetworkModule.imageClient(context) }

    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .okHttpClient(imageClient)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(220L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }

    val settingsStore by lazy { SettingsStore(context) }

    val wallpaperRepository by lazy { WallpaperRepository(NetworkModule.api(apiClient)) }

    val wallpaperCache = WallpaperCache()

    val libraryRepository by lazy { LibraryRepository(database.dao(), gson) }

    val downloader by lazy { WallpaperDownloader(context, imageClient) }

    val wallpaperSetter by lazy { WallpaperSetter(context, imageLoader) }

    companion object {
        const val THUMB_WIDTH = 800
    }
}
