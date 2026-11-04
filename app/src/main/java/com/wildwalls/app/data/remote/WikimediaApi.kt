package com.wildwalls.app.data.remote

import com.wildwalls.app.data.remote.dto.CommonsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * The official MediaWiki API of Wikimedia Commons.
 * Fixed parameters (action=query, prop=imageinfo|categories, iiprop, extmetadata filter) are added
 * by [CommonsParamsInterceptor] so every call returns attribution and licence metadata.
 */
interface WikimediaApi {

    @GET("w/api.php")
    suspend fun searchImages(
        @Query("gsrsearch") search: String,
        @Query("gsroffset") offset: Int?,
        @Query("gsrlimit") limit: Int,
        /** "create_timestamp_desc" returns the newest uploads first. */
        @Query("gsrsort") sort: String? = null,
        /** "no-cache" forces revalidation with Commons so the feed is always live. */
        @Header("Cache-Control") cacheControl: String? = null
    ): CommonsResponse

    /** Used when a wallpaper has to be re-resolved by its Commons page id (deep link, process death). */
    @GET("w/api.php")
    suspend fun imageById(@Query("pageids") pageId: Long): CommonsResponse
}
