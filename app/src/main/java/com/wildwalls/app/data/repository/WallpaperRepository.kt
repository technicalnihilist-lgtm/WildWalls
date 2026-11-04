package com.wildwalls.app.data.repository

import com.wildwalls.app.data.remote.CommonsMapper
import com.wildwalls.app.data.remote.WikimediaApi
import com.wildwalls.app.domain.model.CategoryCatalog
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Calendar

/**
 * Single source of wallpapers. Every list comes from a live Wikimedia Commons query - there are no
 * bundled sample images and no fabricated results.
 */
class WallpaperRepository(private val api: WikimediaApi) {

    suspend fun search(
        query: String,
        offset: Int = 0,
        limit: Int = PAGE_SIZE,
        hints: List<String> = emptyList(),
        sort: String? = null
    ): FeedResult {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return FeedResult.Success(emptyList(), null, false)
        return execute(trimmed, offset, limit, hints, sort)
    }

    suspend fun categoryFeed(
        categoryId: String,
        offset: Int = 0,
        limit: Int = PAGE_SIZE,
        sort: String? = null,
        live: Boolean = false
    ): FeedResult {
        val category = CategoryCatalog.byId(categoryId)
            ?: return FeedResult.Failure("Unknown category", retryable = false)
        return execute(category.query, offset, limit, category.hints, sort, live)
    }

    /**
     * Newest-first feed: the most recently uploaded photographs across the busiest animal
     * categories, merged and sorted by Commons upload timestamp. This is what makes the home
     * screen "live" - a refresh surfaces files that were not there a moment ago.
     */
    suspend fun freshWallpapers(limit: Int = 12): FeedResult = coroutineScope {
        val results = FRESH_SOURCES.map { source ->
            async {
                // Exact category + newest sort: precise animal matches, freshest first.
                execute(
                    query = "incategory:\"${source.category}\" $FILE_FILTER",
                    offset = 0,
                    limit = 8,
                    hints = source.hints,
                    sort = SORT_NEWEST,
                    live = true
                )
            }
        }.map { it.await() }

        val items = results.filterIsInstance<FeedResult.Success>()
            .flatMap { it.items }
            .distinctBy { it.id }
            .sortedByDescending { it.uploadTimestamp }
            .take(limit)

        val failure = results.filterIsInstance<FeedResult.Failure>().firstOrNull()
        when {
            items.isNotEmpty() -> FeedResult.Success(items, null, false)
            failure != null -> failure
            else -> FeedResult.Success(emptyList(), null, false)
        }
    }

    /** Featured hero carousel, topped up from the quality-image pool when the featured set is small. */
    suspend fun featured(limit: Int = 12): FeedResult {
        val primary = execute(HomeFeed.FEATURED.query, 0, limit, emptyList())
        if (primary is FeedResult.Failure) return primary
        val items = (primary as FeedResult.Success).items
        if (items.size >= 5) return primary.copy(items = items, hasMore = false)
        val topUp = execute(HomeFeed.TRENDING.query, 0, limit, emptyList())
        if (topUp is FeedResult.Success) {
            val merged = (items + topUp.items).distinctBy { it.id }
            return FeedResult.Success(merged, null, false)
        }
        return primary
    }

    /** Freshest uploads across a few large animal categories, merged and sorted by upload time. */
    suspend fun recentlyAdded(limit: Int = 12): FeedResult = coroutineScope {
        val categories = listOf("lions", "tigers", "elephants", "eagles", "owls")
        val results = categories.map { id ->
            async { categoryFeed(id, offset = 0, limit = 8) }
        }.map { it.await() }

        val items = results.filterIsInstance<FeedResult.Success>()
            .flatMap { it.items }
            .distinctBy { it.id }
            .sortedByDescending { it.uploadTimestamp }
            .take(limit)

        val failure = results.filterIsInstance<FeedResult.Failure>().firstOrNull()
        when {
            items.isNotEmpty() -> FeedResult.Success(items, null, false)
            failure != null -> failure
            else -> FeedResult.Success(emptyList(), null, false)
        }
    }

    /**
     * The daily wallpaper: a deterministic pick from the curated pool, stable for the whole day
     * and identical on every device that opens the app on the same date.
     */
    suspend fun dailyWallpaper(): FeedResult {
        val pool = execute(HomeFeed.DAILY.query, 0, 40, emptyList())
        if (pool !is FeedResult.Success) return pool
        if (pool.items.isEmpty()) return pool
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % pool.items.size
        return FeedResult.Success(listOf(pool.items[index]), null, false)
    }

    /** Re-resolves a single wallpaper by its Commons page id. */
    suspend fun byId(pageId: Long): FeedResult = try {
        val response = api.imageById(pageId)
        if (response.error != null) {
            FeedResult.Failure(response.error.info ?: "Image not found", retryable = true)
        } else {
            val items = CommonsMapper.mapPages(response.query?.pages, emptyList())
            if (items.isEmpty()) {
                FeedResult.Failure(
                    "This image is no longer available under a reusable licence.",
                    retryable = false
                )
            } else {
                FeedResult.Success(items, null, false)
            }
        }
    } catch (e: Exception) {
        FeedResult.Failure("Could not load this wallpaper. Check your connection.", true)
    }

    private suspend fun execute(
        query: String,
        offset: Int,
        limit: Int,
        hints: List<String>,
        sort: String? = null,
        live: Boolean = false
    ): FeedResult = try {
        val response = api.searchImages(
            search = query,
            offset = offset.takeIf { it > 0 },
            limit = limit.coerceIn(1, 50),
            sort = sort,
            // The newest-first feed always revalidates with Commons; everything else may use the
            // HTTP cache so scrolling back and forth stays cheap.
            cacheControl = if (live) "no-cache" else null
        )
        if (response.error != null) {
            FeedResult.Failure(
                message = response.error.info ?: "Wikimedia Commons returned an error",
                retryable = true
            )
        } else {
            val items = CommonsMapper.mapPages(response.query?.pages, hints)
            val nextOffset = response.continuation?.offset
            FeedResult.Success(
                items = items,
                nextOffset = nextOffset,
                hasMore = nextOffset != null && nextOffset > offset
            )
        }
    } catch (e: UnknownHostException) {
        FeedResult.Failure("No internet connection. Check your network and try again.", true)
    } catch (e: SocketTimeoutException) {
        FeedResult.Failure("Wikimedia Commons took too long to respond.", true)
    } catch (e: HttpException) {
        val message = when (e.code()) {
            429 -> "Wikimedia is rate limiting requests. Please wait a moment."
            in 500..599 -> "Wikimedia Commons is temporarily unavailable."
            else -> "Request failed (HTTP ${e.code()})."
        }
        FeedResult.Failure(message, retryable = e.code() != 404)
    } catch (e: IOException) {
        FeedResult.Failure("Network error. Check your connection and retry.", true)
    } catch (e: Exception) {
        FeedResult.Failure(e.message ?: "Something went wrong while loading wallpapers.", true)
    }

    companion object {
        const val PAGE_SIZE = 24

        /** Wikimedia CirrusSearch sort: newest uploads first. */
        const val SORT_NEWEST = "create_timestamp_desc"

        private const val FILE_FILTER = "filetype:bitmap"

        /**
         * Sources for the live feed. Exact Commons categories keep the results on-topic even when
         * sorted by upload time, where the broader deep-category search drifts into lookalikes.
         */
        private val FRESH_SOURCES = listOf(
            FreshSource("Panthera leo", listOf("Panthera leo", "lion")),
            FreshSource("Panthera tigris", listOf("Panthera tigris", "tiger")),
            FreshSource("Elephantidae", listOf("Elephantidae", "Elephant", "Loxodonta", "Elephas")),
            FreshSource("Accipitridae", listOf("Accipitridae", "eagle", "Aquila", "Haliaeetus")),
            FreshSource("Spheniscidae", listOf("Spheniscidae", "penguin", "Aptenodytes", "Pygoscelis")),
            FreshSource("Canis lupus", listOf("Canis lupus", "wolf", "wolves"))
        )

        private data class FreshSource(val category: String, val hints: List<String>)
    }
}
