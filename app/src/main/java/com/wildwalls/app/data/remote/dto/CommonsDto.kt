package com.wildwalls.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CommonsResponse(
    @SerializedName("continue") val continuation: CommonsContinue? = null,
    @SerializedName("query") val query: CommonsQuery? = null,
    @SerializedName("error") val error: CommonsError? = null
)

data class CommonsContinue(
    @SerializedName("gsroffset") val offset: Int? = null,
    @SerializedName("continue") val token: String? = null
)

data class CommonsError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("info") val info: String? = null
)

data class CommonsQuery(
    @SerializedName("pages") val pages: List<CommonsPage>? = null
)

data class CommonsPage(
    @SerializedName("pageid") val pageId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("imageinfo") val imageInfo: List<CommonsImageInfo>? = null,
    @SerializedName("categories") val categories: List<CommonsCategory>? = null
)

data class CommonsCategory(
    @SerializedName("title") val title: String? = null
)

data class CommonsImageInfo(
    @SerializedName("url") val url: String? = null,
    @SerializedName("descriptionurl") val descriptionUrl: String? = null,
    @SerializedName("thumburl") val thumbUrl: String? = null,
    @SerializedName("thumbwidth") val thumbWidth: Int? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("extmetadata") val extMetadata: Map<String, CommonsMetaValue>? = null
)

data class CommonsMetaValue(
    @SerializedName("value") val value: String? = null
)
