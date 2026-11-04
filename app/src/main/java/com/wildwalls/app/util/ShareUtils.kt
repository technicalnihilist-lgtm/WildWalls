package com.wildwalls.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.wildwalls.app.domain.model.Wallpaper
import java.io.File

/**
 * Sharing always carries the full attribution, whether the image file is attached or only the
 * original Commons page is linked.
 */
object ShareUtils {

    fun shareWallpaper(context: Context, wallpaper: Wallpaper, localPath: String? = null) {
        val attribution = buildString {
            append("\"")
            append(wallpaper.title)
            append("\"\n")
            append(wallpaper.artist)
            append(" · ")
            append(wallpaper.licenseShort)
            append("\n")
            append(wallpaper.pageUrl)
            append("\n\nShared from WildWalls. Photograph via Wikimedia Commons.")
        }

        val file = localPath?.let { File(it) }?.takeIf { it.exists() }
        val intent = if (file != null) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Intent(Intent.ACTION_SEND).apply {
                type = wallpaper.mime.ifBlank { "image/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, wallpaper.title)
        intent.putExtra(Intent.EXTRA_TEXT, attribution)
        context.startActivity(Intent.createChooser(intent, "Share wallpaper"))
    }

    fun openUrl(context: Context, url: String) {
        if (url.isBlank()) return
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        }
    }
}
