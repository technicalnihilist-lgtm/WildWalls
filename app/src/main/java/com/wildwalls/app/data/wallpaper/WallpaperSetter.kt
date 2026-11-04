package com.wildwalls.app.data.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.wildwalls.app.domain.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class WallpaperTarget(val label: String) {
    HOME("Home screen"),
    LOCK("Lock screen"),
    BOTH("Home and lock screen")
}

/**
 * Applies a wallpaper with the platform [WallpaperManager]. The image is centre-cropped to the
 * current screen size first, which is disclosed as a modification in the credits.
 */
class WallpaperSetter(
    private val context: Context,
    private val imageLoader: ImageLoader
) {

    suspend fun setWallpaper(
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        localPath: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val source: Any = localPath?.takeIf { File(it).exists() } ?: wallpaper.imageUrl
            val request = ImageRequest.Builder(context)
                .data(source)
                .allowHardware(false)
                .size(4096)
                .build()
            val result = imageLoader.execute(request)
            if (result !is SuccessResult) {
                return@withContext Result.failure(IllegalStateException("Could not load the image"))
            }
            val loaded = result.drawable.toBitmap()
            val metrics = context.resources.displayMetrics
            val cropped = centerCrop(loaded, metrics.widthPixels, metrics.heightPixels)

            val manager = WallpaperManager.getInstance(context)
            if (!manager.isWallpaperSupported) {
                return@withContext Result.failure(
                    IllegalStateException("This device does not allow changing the wallpaper")
                )
            }
            val flags = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
            manager.setBitmap(cropped, null, true, flags)
            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(IllegalStateException("Wallpaper permission was denied", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Scales to cover the screen, then crops the overflow from the centre. */
    private fun centerCrop(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (source.width == 0 || source.height == 0) return source
        val scale = maxOf(
            targetWidth.toFloat() / source.width,
            targetHeight.toFloat() / source.height
        )
        val scaledWidth = (source.width * scale).toInt().coerceAtLeast(targetWidth)
        val scaledHeight = (source.height * scale).toInt().coerceAtLeast(targetHeight)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val x = ((scaledWidth - targetWidth) / 2).coerceAtLeast(0)
        val y = ((scaledHeight - targetHeight) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(scaled, x, y, targetWidth, targetHeight)
        if (scaled !== cropped && scaled !== source) scaled.recycle()
        if (source !== scaled && source !== cropped) source.recycle()
        return cropped
    }
}
