package com.wildwalls.app.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wildwalls.app.data.repository.LibraryRepository.DownloadedWallpaper
import com.wildwalls.app.ui.components.EmptyState
import com.wildwalls.app.ui.components.WildWallsWordmark
import com.wildwalls.app.ui.components.glass
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary
import com.wildwalls.app.ui.theme.TextTertiary
import com.wildwalls.app.util.ShareUtils
import com.wildwalls.app.util.formatBytes
import com.wildwalls.app.util.formatDate
import java.io.File

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    onOpenWallpaper: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                WildWallsWordmark(compact = true)
                Spacer(Modifier.height(14.dp))
                Text(text = "Downloads", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = if (state.items.isEmpty()) "Saved for offline viewing"
                    else "${state.items.size} wallpapers · ${formatBytes(state.items.sumOf { it.sizeBytes })}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (state.items.isEmpty() && !state.loading) {
                EmptyState(
                    title = "No downloads yet",
                    message = "Downloaded wallpapers stay available offline and can be shared or applied anytime."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.items, key = { it.wallpaper.id }) { item ->
                        DownloadRow(
                            item = item,
                            onOpen = { onOpenWallpaper(item.wallpaper.id) },
                            onShare = {
                                ShareUtils.shareWallpaper(context, item.wallpaper, item.localPath)
                            },
                            onDelete = { viewModel.delete(item) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DownloadRow(
    item: DownloadedWallpaper,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpen)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(item.localPath))
                .crossfade(true)
                .build(),
            contentDescription = item.wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 62.dp, height = 82.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.wallpaper.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.wallpaper.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${formatBytes(item.sizeBytes)} · ${formatDate(item.downloadedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onShare),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = TextSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete download",
                tint = Accent,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
