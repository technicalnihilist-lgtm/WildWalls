package com.wildwalls.app.data.download

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Streams the original Commons file to app storage so it stays available offline.
 * Nothing is written to shared storage, so no storage permission is ever requested.
 */
class WallpaperDownloader(
    private val context: Context,
    private val client: OkHttpClient
) {

    suspend fun download(wallpaper: Wallpaper): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dir = picturesDir()
            if (!dir.exists() && !dir.mkdirs()) {
                return@withContext Result.failure(IOException("Cannot create the WildWalls image folder"))
            }
            val target = File(dir, fileName(wallpaper))
            if (target.exists() && target.length() > 0) return@withContext Result.success(target)

            val request = Request.Builder()
                .url(wallpaper.imageUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Download failed (HTTP ${response.code})")
                    )
                }
                val body = response.body ?: return@withContext Result.failure(IOException("Empty response"))
                val temp = File(dir, target.name + ".part")
                body.byteStream().use { input ->
                    temp.outputStream().use { output -> input.copyTo(output, DEFAULT_BUFFER_SIZE) }
                }
                if (target.exists()) target.delete()
                if (!temp.renameTo(target)) {
                    temp.delete()
                    return@withContext Result.failure(IOException("Could not save the image"))
                }
            }
            notifyGallery(target)
            Result.success(target)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun existingFile(wallpaper: Wallpaper): File? {
        val file = File(picturesDir(), fileName(wallpaper))
        return file.takeIf { it.exists() && it.length() > 0 }
    }

    fun picturesDir(): File = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir,
        "WildWalls"
    )

    private fun notifyGallery(file: File) {
        runCatching {
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/*"), null)
        }
    }

    fun fileName(wallpaper: Wallpaper): String {
        val slug = wallpaper.title.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(48)
            .ifBlank { "wallpaper" }
        val extension = when {
            wallpaper.mime.contains("png") -> "png"
            wallpaper.mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        return "wildwalls_${wallpaper.id}_$slug.$extension"
    }

    companion object {
        private const val USER_AGENT =
            "WildWalls/1.0 (Android wallpaper app; contact@wildwalls.app)"
    }
}
