package com.wildwalls.app.data.repository

import com.google.gson.Gson
import com.wildwalls.app.data.local.DownloadEntity
import com.wildwalls.app.data.local.FavoriteEntity
import com.wildwalls.app.data.local.RecentViewEntity
import com.wildwalls.app.data.local.SearchHistoryEntity
import com.wildwalls.app.data.local.WildWallsDao
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/** Local, on-device library: favourites, downloads, history and search suggestions. */
class LibraryRepository(private val dao: WildWallsDao, private val gson: Gson) {

    fun observeFavorites(): Flow<List<Wallpaper>> =
        dao.observeFavorites().map { rows -> rows.mapNotNull { decode(it.payload) } }

    fun observeIsFavorite(id: Long): Flow<Boolean> = dao.observeIsFavorite(id)

    suspend fun isFavorite(id: Long): Boolean = dao.favorites().any { it.id == id }

    suspend fun toggleFavorite(wallpaper: Wallpaper): Boolean {
        val existing = dao.favorites().any { it.id == wallpaper.id }
        return if (existing) {
            dao.deleteFavorite(wallpaper.id)
            false
        } else {
            dao.insertFavorite(FavoriteEntity(wallpaper.id, gson.toJson(wallpaper), System.currentTimeMillis()))
            true
        }
    }

    fun observeDownloads(): Flow<List<DownloadedWallpaper>> =
        dao.observeDownloads().map { rows ->
            rows.mapNotNull { row ->
                val wallpaper = decode(row.payload) ?: return@mapNotNull null
                DownloadedWallpaper(wallpaper, row.localPath, row.fileName, row.sizeBytes, row.downloadedAt)
            }
        }

    suspend fun downloadFor(id: Long): DownloadEntity? = dao.download(id)

    suspend fun saveDownload(wallpaper: Wallpaper, file: File) {
        dao.insertDownload(
            DownloadEntity(
                id = wallpaper.id,
                payload = gson.toJson(wallpaper),
                localPath = file.absolutePath,
                fileName = file.name,
                sizeBytes = file.length(),
                downloadedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteDownload(id: Long) {
        val row = dao.download(id) ?: return
        runCatching { File(row.localPath).delete() }
        dao.deleteDownload(id)
    }

    fun observeRecent(limit: Int = 20): Flow<List<Wallpaper>> =
        dao.observeRecent(limit).map { rows -> rows.mapNotNull { decode(it.payload) } }

    suspend fun recordView(wallpaper: Wallpaper) {
        dao.insertRecent(
            RecentViewEntity(wallpaper.id, gson.toJson(wallpaper), System.currentTimeMillis())
        )
        dao.trimRecent(KEEP_RECENT)
    }

    fun observeSearchHistory(limit: Int = 8): Flow<List<String>> =
        dao.observeSearches(limit).map { rows -> rows.map { it.query } }

    suspend fun recordSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        dao.insertSearch(SearchHistoryEntity(trimmed, System.currentTimeMillis()))
    }

    suspend fun clearSearchHistory() = dao.clearSearches()

    suspend fun clearRecent() = dao.clearRecent()

    /** Every image that has a stored attribution record, for the credits screen. */
    suspend fun creditedWallpapers(): List<Wallpaper> {
        val favorites = dao.favorites().mapNotNull { decode(it.payload) }
        val downloads = dao.downloadsAll().mapNotNull { decode(it.payload) }
        return (favorites + downloads).distinctBy { it.id }.sortedBy { it.title }
    }

    private fun decode(payload: String): Wallpaper? =
        runCatching { gson.fromJson(payload, Wallpaper::class.java) }.getOrNull()

    data class DownloadedWallpaper(
        val wallpaper: Wallpaper,
        val localPath: String,
        val fileName: String,
        val sizeBytes: Long,
        val downloadedAt: Long
    )

    companion object {
        private const val KEEP_RECENT = 60
    }
}
