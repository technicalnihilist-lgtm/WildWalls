package com.wildwalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wildwalls.app.domain.model.Wallpaper
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextTertiary

/** The app's Coil loader, provided once at the root so no screen can fall back to a default client. */
val LocalWildImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("WildWalls image loader was not provided")
}

/**
 * Wikimedia answers requests without a descriptive User-Agent with HTTP 403, so the header is set
 * on every single request as well as on the shared client.
 */
private const val USER_AGENT =
    "WildWalls/1.2 (Android wallpaper app; https://commons.wikimedia.org; contact@wildwalls.app)"
private const val REFERER = "https://commons.wikimedia.org/"

/**
 * Loads a wallpaper image with a fallback chain: thumbnail -> original file -> designed
 * placeholder. A tile therefore never sits blank forever, which is what made failures look like
 * a broken screen.
 */
@Composable
fun WildImage(
    wallpaper: Wallpaper,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fullResolution: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    decodeWidth: Int = 720
) {
    val context = LocalContext.current
    val imageLoader = LocalWildImageLoader.current
    var attempt by remember(wallpaper.id) { mutableIntStateOf(0) }

    val url = when {
        attempt == 0 -> if (fullResolution) wallpaper.imageUrl else wallpaper.thumbUrl
        attempt == 1 -> wallpaper.imageUrl
        else -> null
    }

    if (url == null) {
        ImageUnavailablePlaceholder(title = wallpaper.title, modifier = modifier)
        return
    }

    AsyncImage(
        imageLoader = imageLoader,
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .size(decodeWidth)
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("Referer", REFERER)
            .addHeader("Accept", "image/avif,image/webp,image/*,*/*;q=0.8")
            .listener(onError = { _, _ -> if (attempt < 2) attempt++ })
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
fun ImageUnavailablePlaceholder(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF14141A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            PawMark(size = 20.dp, tint = TextTertiary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Image unavailable",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}
