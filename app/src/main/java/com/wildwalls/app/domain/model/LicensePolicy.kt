package com.wildwalls.app.domain.model

/**
 * WildWalls only shows media that can be reused. This is a whitelist: anything we cannot verify
 * (unknown licence, non-free, fair use, NoDerivatives, NonCommercial) is dropped from results
 * rather than displayed with a vague credit.
 */
object LicensePolicy {

    private val allowedPrefixes = listOf(
        "cc0", "cc by", "cc-by", "public domain", "pd-", "pd ", "no restrictions", "attribution"
    )

    private val blockedTokens = setOf("nc", "nd", "noncommercial", "noderiv", "noderivs")
    private val blockedPhrases = listOf("non-free", "fair use", "all rights reserved", "copyright", "gfdl")

    fun isReusable(licenseShort: String): Boolean {
        val license = licenseShort.trim().lowercase()
        if (license.isEmpty()) return false
        if (blockedPhrases.any { license.contains(it) }) return false
        val tokens = license.split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
        if (tokens.any { it in blockedTokens }) return false
        return allowedPrefixes.any { license.startsWith(it) }
    }

    /** Licence deed link, falling back to the canonical Creative Commons URL when Commons omits it. */
    fun licenseUrl(licenseShort: String, provided: String?): String {
        if (!provided.isNullOrBlank()) return provided.trim()
        val l = licenseShort.trim().lowercase()
        val version = Regex("(\\d+\\.\\d+)").find(l)?.value
        return when {
            l.startsWith("cc0") || l.startsWith("public domain") || l.startsWith("pd") ->
                "https://creativecommons.org/publicdomain/mark/1.0/"
            version == null -> "https://creativecommons.org/licenses/"
            l.contains("by-sa") -> "https://creativecommons.org/licenses/by-sa/$version/"
            l.contains("by") -> "https://creativecommons.org/licenses/by/$version/"
            else -> "https://creativecommons.org/licenses/"
        }
    }

    /** True when the licence obliges the user to credit the author (CC BY, CC BY-SA). */
    fun requiresAttribution(licenseShort: String): Boolean =
        licenseShort.trim().lowercase().startsWith("cc by")

    fun requiresShareAlike(licenseShort: String): Boolean =
        licenseShort.trim().lowercase().contains("sa")
}
