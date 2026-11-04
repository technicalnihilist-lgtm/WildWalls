package com.wildwalls.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the Commons query contract to every API call: File namespace (6) search, imageinfo with
 * url/size/mime/timestamp, the extmetadata block that carries Artist, LicenseShortName and
 * LicenseUrl, and category names so results can be relevance-filtered.
 */
class CommonsParamsInterceptor(private val thumbWidth: Int) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val existing = original.url.queryParameterNames
        val builder = original.url.newBuilder()

        if ("pageids" !in existing) {
            builder.addQueryParameter("generator", "search")
            builder.addQueryParameter("gsrnamespace", "6")
        }
        if ("action" !in existing) builder.addQueryParameter("action", "query")
        if ("format" !in existing) builder.addQueryParameter("format", "json")
        if ("formatversion" !in existing) builder.addQueryParameter("formatversion", "2")
        if ("prop" !in existing) builder.addQueryParameter("prop", "imageinfo|categories")
        if ("cllimit" !in existing) builder.addQueryParameter("cllimit", "max")
        if ("iiprop" !in existing) {
            builder.addQueryParameter("iiprop", "url|size|mime|timestamp|extmetadata")
        }
        if ("iiurlwidth" !in existing) {
            builder.addQueryParameter("iiurlwidth", thumbWidth.toString())
        }
        if ("iiextmetadatafilter" !in existing) {
            builder.addQueryParameter(
                "iiextmetadatafilter",
                "ImageDescription|Artist|Credit|LicenseShortName|LicenseUrl|UsageTerms|" +
                    "ObjectName|DateTimeOriginal|AttributionRequired|Restrictions"
            )
        }

        val request = original.newBuilder()
            .url(builder.build())
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }

    companion object {
        /**
         * Wikimedia asks every client to identify itself. Replace the contact address with your own
         * before publishing.
         */
        const val USER_AGENT =
            "WildWalls/1.0 (Android wallpaper app; https://commons.wikimedia.org; contact@wildwalls.app)"
    }
}
