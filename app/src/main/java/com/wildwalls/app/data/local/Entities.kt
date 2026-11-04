package com.wildwalls.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Wallpaper snapshots are stored as a JSON payload plus the columns the UI sorts or filters on.
 * That keeps a single schema for favourites, downloads and history while preserving the full
 * attribution record with every saved image.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Long,
    val payload: String,
    val savedAt: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: Long,
    val payload: String,
    val localPath: String,
    val fileName: String,
    val sizeBytes: Long,
    val downloadedAt: Long
)

@Entity(tableName = "recent_views")
data class RecentViewEntity(
    @PrimaryKey val id: Long,
    val payload: String,
    val viewedAt: Long
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val lastUsed: Long
)
