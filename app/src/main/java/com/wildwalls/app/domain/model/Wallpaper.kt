package com.wildwalls.app.domain.model

/**
 * A single wallpaper backed by a real Wikimedia Commons file.
 *
 * Every field that matters for attribution is carried with the image so that credits can be
 * shown on the detail screen, in the credits screen and when sharing - never dropped.
 */
data class Wallpaper(
    val id: Long,
    val title: String,
    val artist: String,
    val credit: String,
    val licenseShort: String,
    val licenseUrl: String,
    val pageUrl: String,
    val imageUrl: String,
    val thumbUrl: String,
    val width: Int,
    val height: Int,
    val mime: String,
    val description: String,
    val dateCreated: String,
    val uploadTimestamp: String,
    val categories: List<String> = emptyList()
) {
    val aspectRatio: Float
        get() = if (width > 0 && height > 0) width.toFloat() / height.toFloat() else 1f

    /** One-line attribution, used when sharing and in compact credit rows. */
    val shortCredit: String
        get() = buildString {
            append(artist.ifBlank { "Unknown contributor" })
            append(" · ")
            append(licenseShort)
            append(" · Wikimedia Commons")
        }
}
