package com.wildwalls.app.data.repository

import com.wildwalls.app.domain.model.Wallpaper

/** Keeps the last N wallpapers in memory so the detail screen opens instantly. */
class WallpaperCache(private val capacity: Int = 300) {

    private val entries = object : LinkedHashMap<Long, Wallpaper>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Wallpaper>?): Boolean =
            size > capacity
    }

    @Synchronized
    fun put(wallpapers: List<Wallpaper>) {
        wallpapers.forEach { entries[it.id] = it }
    }

    @Synchronized
    fun get(id: Long): Wallpaper? = entries[id]
}
