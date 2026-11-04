package com.wildwalls.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WildWallsDao {

    // Favourites ------------------------------------------------------------
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    suspend fun favorites(): List<FavoriteEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    fun observeIsFavorite(id: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: Long)

    // Downloads -------------------------------------------------------------
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun observeDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun download(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    suspend fun downloadsAll(): List<DownloadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: Long)

    // Recently viewed -------------------------------------------------------
    @Query("SELECT * FROM recent_views ORDER BY viewedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentViewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(entity: RecentViewEntity)

    @Query("DELETE FROM recent_views WHERE id NOT IN (SELECT id FROM recent_views ORDER BY viewedAt DESC LIMIT :keep)")
    suspend fun trimRecent(keep: Int)

    @Query("DELETE FROM recent_views")
    suspend fun clearRecent()

    // Search history --------------------------------------------------------
    @Query("SELECT * FROM search_history ORDER BY lastUsed DESC LIMIT :limit")
    fun observeSearches(limit: Int): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearches()
}
