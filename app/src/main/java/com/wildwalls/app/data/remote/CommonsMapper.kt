package com.wildwalls.app.data.remote

import android.text.Html
import com.wildwalls.app.data.remote.dto.CommonsImageInfo
import com.wildwalls.app.data.remote.dto.CommonsPage
import com.wildwalls.app.domain.model.LicensePolicy
import com.wildwalls.app.domain.model.Wallpaper

/**
 * Turns raw Commons pages into [Wallpaper]s.
 *
 * Anything that fails a check is dropped instead of being shown with a guess:
 *  - only bitmap photographs (jpeg / png / webp)
 *  - minimum resolution so a wallpaper actually fills a phone screen
 *  - a verifiable, reusable licence (see [LicensePolicy])
 *  - for category feeds, the file must really be filed under the animal
 *  - drawings, statues, coats of arms and similar non-photographs are skipped
 */
object CommonsMapper {

    private const val MIN_WIDTH = 1000
    private const val MIN_HEIGHT = 1000
    private const val MAX_DESCRIPTION = 420

    private val tagRegex = Regex("<[^>]*>")
    private val spaceRegex = Regex("[ \\t\\r\\n]+")
    private val allowedMimes = setOf("image/jpeg", "image/png", "image/webp")

    /**
     * Commons categories mix photographs with artwork, idols, museum specimens and diagrams.
     * A wallpaper app should only surface photographs of living animals, so anything filed under
     * these themes is skipped.
     */
    private val nonPhotographic = listOf(
        "statue", "sculpture", "statuette", "figurine", "coats of arms", "coat of arms",
        "painting", "drawing", "sketch", "engraving", "lithograph", "woodcut", "etching",
        "illustration", "cartoon", "comics", "mosaic", "relief", "carving", "monument",
        "idol", "puja", "deity", "mythology", "heraldry", "in art", "art of", "temple",
        "shrine", "taxidermy", "museum specimen", "specimens", "skull", "skeleton", "fossil",
        "map", "diagram", "logo", "signage", "banknote", "coin", "postage stamp", "stamp",
        "manuscript", "illuminated"
    )

    fun mapPages(pages: List<CommonsPage>?, hints: List<String>): List<Wallpaper> {
        if (pages.isNullOrEmpty()) return emptyList()
        val seen = HashSet<Long>()
        val out = ArrayList<Wallpaper>(pages.size)
        for (page in pages) {
            val wallpaper = mapPage(page, hints) ?: continue
            if (seen.add(wallpaper.id)) out.add(wallpaper)
        }
        return out
    }

    private fun mapPage(page: CommonsPage, hints: List<String>): Wallpaper? {
        val id = page.pageId ?: return null
        val info = page.imageInfo?.firstOrNull() ?: return null
        val title = page.title ?: return null
        if (!isUsable(info)) return null

        val meta = info.extMetadata.orEmpty()
        val licenseShort = meta["LicenseShortName"]?.value?.let(::plainText).orEmpty()
        if (!LicensePolicy.isReusable(licenseShort)) return null

        val categories = page.categories.orEmpty().mapNotNull { it.title }
        if (hints.isNotEmpty() && !matchesHints(title, categories, hints)) return null
        if (isNonPhotographic(title, categories)) return null

        val imageUrl = normalizeUrl(info.url ?: return null)
        val pageUrl = info.descriptionUrl ?: return null
        val thumb = info.thumbUrl?.takeIf { it.isNotBlank() }?.let(::normalizeUrl) ?: imageUrl

        val artist = plainText(meta["Artist"]?.value)
            .ifBlank { plainText(meta["Credit"]?.value) }
            .ifBlank { "Wikimedia Commons contributor" }

        val description = plainText(meta["ImageDescription"]?.value).take(MAX_DESCRIPTION)
        val objectName = plainText(meta["ObjectName"]?.value)

        return Wallpaper(
            id = id,
            title = cleanTitle(title, objectName),
            artist = artist,
            credit = plainText(meta["Credit"]?.value),
            licenseShort = licenseShort,
            licenseUrl = LicensePolicy.licenseUrl(licenseShort, meta["LicenseUrl"]?.value),
            pageUrl = pageUrl,
            imageUrl = imageUrl,
            thumbUrl = thumb,
            width = info.width ?: 0,
            height = info.height ?: 0,
            mime = info.mime.orEmpty(),
            description = description,
            dateCreated = plainText(meta["DateTimeOriginal"]?.value),
            uploadTimestamp = info.timestamp.orEmpty(),
            categories = categories.map { it.removePrefix("Category:") }.take(12)
        )
    }

    /**
     * Commons returns media URLs on `thumb.wikimedia.org` with utm tracking parameters.
     * The canonical `upload.wikimedia.org` host resolves far more reliably on mobile networks,
     * and the tracking parameters only fragment the image cache, so both are normalised away.
     */
    private fun normalizeUrl(raw: String): String =
        raw.substringBefore('?')
            .replace("https://thumb.wikimedia.org/", "https://upload.wikimedia.org/")

    private fun isUsable(info: CommonsImageInfo): Boolean {
        val mime = info.mime?.lowercase() ?: return false
        if (mime !in allowedMimes) return false
        val w = info.width ?: 0
        val h = info.height ?: 0
        return w >= MIN_WIDTH && h >= MIN_HEIGHT && info.url != null && info.descriptionUrl != null
    }

    private fun matchesHints(title: String, categories: List<String>, hints: List<String>): Boolean {
        val haystack = (categories.joinToString(" ") + " " + title).lowercase()
        return hints.any { haystack.contains(it.lowercase()) }
    }

    private fun isNonPhotographic(title: String, categories: List<String>): Boolean {
        val haystack = (categories.joinToString(" ") + " " + title).lowercase()
        return nonPhotographic.any { haystack.contains(it) }
    }

    private fun cleanTitle(title: String, objectName: String): String {
        if (objectName.isNotBlank() && objectName.length in 3..90) return objectName
        return title.removePrefix("File:")
            .substringBeforeLast('.')
            .replace('_', ' ')
            .trim()
    }

    /** Strips the HTML that Commons stores in Artist / ImageDescription / Credit fields. */
    fun plainText(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val stripped = Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString()
        return spaceRegex.replace(stripIfAny(stripped), " ").trim()
    }

    private fun stripIfAny(value: String): String =
        if (value.contains('<')) tagRegex.replace(value, " ") else value
}
