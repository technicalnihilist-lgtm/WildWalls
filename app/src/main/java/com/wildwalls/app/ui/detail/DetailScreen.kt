package com.wildwalls.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wildwalls.app.data.wallpaper.WallpaperTarget
import com.wildwalls.app.domain.model.LicensePolicy
import com.wildwalls.app.ui.components.AttributionRow
import com.wildwalls.app.ui.components.WildIcons
import com.wildwalls.app.ui.components.WildImage
import com.wildwalls.app.ui.components.EmptyState
import com.wildwalls.app.ui.components.ErrorState
import com.wildwalls.app.ui.components.LoadingState
import com.wildwalls.app.ui.components.WideWallpaperCard
import com.wildwalls.app.ui.components.glass
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.GlassBorder
import com.wildwalls.app.ui.theme.OnAccent
import com.wildwalls.app.ui.theme.SurfaceElevated
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary
import com.wildwalls.app.ui.theme.TextTertiary
import com.wildwalls.app.util.ShareUtils
import com.wildwalls.app.util.formatDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    wallpaperId: Long,
    onBack: () -> Unit,
    onOpenWallpaper: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showTargetSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(wallpaperId) { viewModel.open(wallpaperId) }

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
        when {
            state.loading && state.wallpaper == null -> LoadingState(
                label = "Loading wallpaper",
                modifier = Modifier.align(Alignment.Center)
            )

            state.error != null && state.wallpaper == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                ErrorState(message = state.error ?: "", onRetry = { viewModel.open(wallpaperId) })
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBack)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Back", color = Accent, style = MaterialTheme.typography.labelLarge)
                }
            }

            state.wallpaper == null -> EmptyState(
                title = "Wallpaper unavailable",
                message = "This image could not be loaded."
            )

            else -> {
                val wallpaper = state.wallpaper!!
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(
                                    (wallpaper.aspectRatio * 0.9f).coerceIn(0.62f, 1.15f)
                                )
                        ) {
                            WildImage(
                                wallpaper = wallpaper,
                                contentDescription = wallpaper.title,
                                fullResolution = true,
                                decodeWidth = 1440,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color(0x99000000),
                                            0.35f to Color.Transparent,
                                            1f to Color(0xCC000000)
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircleIconButton(
                                    icon = Icons.Outlined.ArrowBack,
                                    description = "Back",
                                    onClick = onBack
                                )
                                Spacer(Modifier.weight(1f))
                                CircleIconButton(
                                    icon = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    description = "Toggle favorite",
                                    tint = if (state.isFavorite) Accent else TextPrimary,
                                    onClick = viewModel::toggleFavorite
                                )
                                Spacer(Modifier.width(10.dp))
                                CircleIconButton(
                                    icon = Icons.Outlined.Share,
                                    description = "Share",
                                    onClick = {
                                        ShareUtils.shareWallpaper(
                                            context,
                                            wallpaper,
                                            viewModel.downloadedPath()
                                        )
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Spacer(Modifier.height(18.dp))
                            Text(
                                text = wallpaper.title,
                                style = MaterialTheme.typography.displayMedium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Accent.copy(alpha = 0.14f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = wallpaper.licenseShort,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Accent
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = formatDimensions(wallpaper.width, wallpaper.height),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            ActionRow(
                                state = state,
                                onDownload = viewModel::download,
                                onSetWallpaper = { showTargetSheet = true },
                                onShare = {
                                    ShareUtils.shareWallpaper(
                                        context,
                                        wallpaper,
                                        viewModel.downloadedPath()
                                    )
                                }
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                                .glass(RoundedCornerShape(22.dp))
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "IMAGE INFORMATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Spacer(Modifier.height(10.dp))
                            AttributionRow(label = "Photographer", value = wallpaper.artist)
                            if (wallpaper.credit.isNotBlank() && wallpaper.credit != wallpaper.artist) {
                                AttributionRow(label = "Credit", value = wallpaper.credit)
                            }
                            AttributionRow(label = "Source", value = "Wikimedia Commons")
                            AttributionRow(
                                label = "Licence",
                                value = wallpaper.licenseShort,
                                onClick = { ShareUtils.openUrl(context, wallpaper.licenseUrl) }
                            )
                            AttributionRow(
                                label = "Licence deed",
                                value = wallpaper.licenseUrl,
                                onClick = { ShareUtils.openUrl(context, wallpaper.licenseUrl) }
                            )
                            AttributionRow(
                                label = "Original page",
                                value = wallpaper.pageUrl,
                                onClick = { ShareUtils.openUrl(context, wallpaper.pageUrl) }
                            )
                            if (wallpaper.dateCreated.isNotBlank()) {
                                AttributionRow(label = "Created", value = wallpaper.dateCreated)
                            }
                            AttributionRow(
                                label = "Modification",
                                value = "None for viewing. Centre-cropped to your screen when applied " +
                                    "as a wallpaper."
                            )
                            if (LicensePolicy.requiresShareAlike(wallpaper.licenseShort)) {
                                AttributionRow(
                                    label = "ShareAlike",
                                    value = "This licence requires derivative works to carry the same licence."
                                )
                            }
                            if (wallpaper.description.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "DESCRIPTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = wallpaper.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            if (wallpaper.categories.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = wallpaper.categories.joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (state.related.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(26.dp))
                            Text(
                                text = "More like this",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.related, key = { it.id }) { related ->
                                    WideWallpaperCard(
                                        wallpaper = related,
                                        onClick = { onOpenWallpaper(related.id) }
                                    )
                                }
                            }
                        }
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

    if (showTargetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTargetSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceElevated
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Set as wallpaper",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "The image is centre-cropped to your screen size.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                Spacer(Modifier.height(12.dp))
                TargetRow(
                    icon = Icons.Outlined.Home,
                    title = "Home screen",
                    onClick = {
                        showTargetSheet = false
                        viewModel.setAsWallpaper(WallpaperTarget.HOME)
                    }
                )
                TargetRow(
                    icon = Icons.Outlined.Lock,
                    title = "Lock screen",
                    onClick = {
                        showTargetSheet = false
                        viewModel.setAsWallpaper(WallpaperTarget.LOCK)
                    }
                )
                TargetRow(
                    icon = WildIcons.Phone,
                    title = "Both screens",
                    onClick = {
                        showTargetSheet = false
                        viewModel.setAsWallpaper(WallpaperTarget.BOTH)
                    }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ActionRow(
    state: DetailViewModel.UiState,
    onDownload: () -> Unit,
    onSetWallpaper: () -> Unit,
    onShare: () -> Unit
) {
    val downloadState = state.download
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .weight(1.2f)
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Accent)
                .clickable(enabled = downloadState !is DetailViewModel.DownloadState.Downloading) {
                    onDownload()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (downloadState) {
                    is DetailViewModel.DownloadState.Downloading -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = OnAccent,
                        strokeWidth = 2.dp
                    )
                    is DetailViewModel.DownloadState.Done -> Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = OnAccent,
                        modifier = Modifier.size(17.dp)
                    )
                    else -> Icon(
                        imageVector = WildIcons.Download,
                        contentDescription = null,
                        tint = OnAccent,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (downloadState) {
                        is DetailViewModel.DownloadState.Downloading -> "Saving"
                        is DetailViewModel.DownloadState.Done -> "Saved"
                        is DetailViewModel.DownloadState.Failed -> "Retry"
                        else -> "Download"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = OnAccent
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1.4f)
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .clickable(enabled = state.applyingTarget == null) { onSetWallpaper() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.applyingTarget != null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Accent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = WildIcons.Wallpaper,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.applyingTarget != null) "Applying" else "Set wallpaper",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .clickable(onClick = onShare),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TargetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}
