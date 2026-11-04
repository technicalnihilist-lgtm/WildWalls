package com.wildwalls.app.data.repository

import com.wildwalls.app.domain.model.Wallpaper

/** Result of one page of Commons results. */
sealed interface FeedResult {
    data class Success(
        val items: List<Wallpaper>,
        val nextOffset: Int?,
        val hasMore: Boolean
    ) : FeedResult

    data class Failure(val message: String, val retryable: Boolean) : FeedResult
}

/** A named, pre-configured feed shown on the home screen. */
enum class HomeFeed(val label: String, val query: String) {
    FEATURED("Featured", "incategory:\"Featured pictures of animals\" filetype:bitmap"),
    TRENDING("Trending", "deepcat:\"Quality images of animals\" filetype:bitmap"),
    DAILY("Daily wildlife", "deepcat:\"Quality images of animals\" filetype:bitmap")
}
