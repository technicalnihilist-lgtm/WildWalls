package com.wildwalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wildwalls.app.domain.model.Wallpaper
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.SurfaceSoft
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary

private val cardShape = RoundedCornerShape(20.dp)

/**
 * Grid card. Only the 800px Commons thumbnail is requested here - the original file is loaded
 * exclusively on the detail screen or when downloading.
 */
@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(cardShape)
            .background(SurfaceSoft)
            .clickable(onClick = onClick)
    ) {
        WildImage(
            wallpaper = wallpaper,
            contentDescription = wallpaper.title,
            decodeWidth = 600,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(wallpaper.aspectRatio.coerceIn(0.62f, 1.5f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to Color(0xD9000000)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = wallpaper.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onToggleFavorite != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x66000000))
                    .clickable(onClick = onToggleFavorite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Accent else TextPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/** Large card used by the featured carousel. */
@Composable
fun HeroWallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(26.dp))
            .background(SurfaceSoft)
            .clickable(onClick = onClick)
    ) {
        WildImage(
            wallpaper = wallpaper,
            contentDescription = wallpaper.title,
            decodeWidth = 1080,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x40000000),
                        0.45f to Color.Transparent,
                        1f to Color(0xF2000000)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = "FEATURED WALLPAPER",
                style = MaterialTheme.typography.labelSmall,
                color = Accent
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${wallpaper.artist} · ${wallpaper.licenseShort}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Compact card used in horizontal rows (trending, recently viewed, more like this). */
@Composable
fun WideWallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 150
) {
    Column(
        modifier = modifier
            .width(width.dp)
            .clip(cardShape)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.78f)
                .clip(cardShape)
                .background(SurfaceSoft)
        ) {
            WildImage(
                wallpaper = wallpaper,
                contentDescription = wallpaper.title,
                decodeWidth = 480,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = wallpaper.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = wallpaper.licenseShort,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1
        )
    }
}

/** Square category tile with the category name over a representative photograph. */
@Composable
fun CategoryTile(
    name: String,
    subtitle: String,
    coverUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceSoft)
            .clickable(onClick = onClick)
    ) {
        if (coverUrl != null) {
            CoverImage(url = coverUrl, contentDescription = name)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.3f to Color(0x26000000),
                        1f to Color(0xE6000000)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Small cover photo for a category tile, loaded through the app's own Coil loader. */
@Composable
private fun CoverImage(url: String, contentDescription: String) {
    val context = LocalContext.current
    AsyncImage(
        imageLoader = LocalWildImageLoader.current,
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .size(480)
            .addHeader("User-Agent", "WildWalls/1.2 (Android wallpaper app; contact@wildwalls.app)")
            .addHeader("Referer", "https://commons.wikimedia.org/")
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

/** Attribution strip reused by the detail screen and the credits screen. */
@Composable
fun AttributionRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClick) else Modifier
            )
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(104.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != null) Accent else TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
